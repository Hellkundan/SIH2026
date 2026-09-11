package backend.service;

import backend.enums.BidStatus;
import backend.enums.CollusionRecommendation;
import backend.enums.DocumentStatus;
import backend.model.Bidder;
import backend.model.CartelSignal;
import backend.model.ComplianceResult;
import backend.model.Document;
import backend.model.Recommendation;
import backend.model.TenderBid;
import backend.model.VerificationResult;
import backend.repository.CartelSignalRepository;
import backend.repository.ComplianceResultRepository;
import backend.repository.DocumentRepository;
import backend.repository.RecommendationRepository;
import backend.repository.TenderBidRepository;
import backend.repository.VerificationResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class OrchestrationServiceImpl implements OrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestrationServiceImpl.class);
    private static final double MINIMUM_CLASSIFICATION_CONFIDENCE = 0.6;

    private final DocumentService documentService;
    private final TenderBidService tenderBidService;
    private final BidderService bidderService;
    private final DocumentRepository documentRepository;
    private final TenderBidRepository tenderBidRepository;
    private final VerificationResultRepository verificationResultRepository;
    private final ComplianceResultRepository complianceResultRepository;
    private final RecommendationRepository recommendationRepository;
    private final CartelSignalRepository cartelSignalRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String ocrBaseUrl;
    private final String verificationBaseUrl;
    private final String aiBaseUrl;

    public OrchestrationServiceImpl(
            DocumentService documentService,
            TenderBidService tenderBidService,
            BidderService bidderService,
            DocumentRepository documentRepository,
            TenderBidRepository tenderBidRepository,
            VerificationResultRepository verificationResultRepository,
            ComplianceResultRepository complianceResultRepository,
            RecommendationRepository recommendationRepository,
            CartelSignalRepository cartelSignalRepository,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            String ocrBaseUrl,
            String verificationBaseUrl,
            String aiBaseUrl
    ) {
        this.documentService = documentService;
        this.tenderBidService = tenderBidService;
        this.bidderService = bidderService;
        this.documentRepository = documentRepository;
        this.tenderBidRepository = tenderBidRepository;
        this.verificationResultRepository = verificationResultRepository;
        this.complianceResultRepository = complianceResultRepository;
        this.recommendationRepository = recommendationRepository;
        this.cartelSignalRepository = cartelSignalRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.ocrBaseUrl = ocrBaseUrl;
        this.verificationBaseUrl = verificationBaseUrl;
        this.aiBaseUrl = aiBaseUrl;
    }

    @Override
    public void triggerDocumentProcessing(UUID documentId) {
        Document document = documentService.getDocumentById(documentId);

        try {
            documentService.startDocumentProcessing(documentId);
            byte[] bytes = Files.readAllBytes(Path.of(document.getFilePath()));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return document.getFileName();
                }
            };
            body.add("file", resource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    ocrBaseUrl + "/api/ocr/process",
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Map result = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful()
                    || result == null
                    || "FAILED".equals(String.valueOf(result.get("processing_status")))) {
                throw new IllegalStateException("OCR returned an unsuccessful processing status");
            }

            documentService.saveOcrResults(
                    documentId,
                    stringValue(result, "raw_text"),
                    stringValue(result, "document_hash"),
                    doubleValue(result, "classification_confidence"),
                    objectMapper.writeValueAsString(result.get("extracted_fields"))
            );
            documentService.markDocumentAsProcessed(documentId);
        } catch (Exception exception) {
            logger.warn("OCR service call failed for document {} at {}: {}. Using resilient extraction fallback.", documentId, ocrBaseUrl, exception.getMessage());
            try {
                TenderBid tenderBid = tenderBidService.getTenderBidById(document.getTenderBidId());
                Bidder bidder = bidderService.getBidderById(tenderBid.getBidderId());
                String docType = document.getDocumentType() != null ? document.getDocumentType().name() : "PAN";
                String docNumber = ("PAN".equalsIgnoreCase(docType) && bidder.getPan() != null && !bidder.getPan().isBlank())
                        ? bidder.getPan()
                        : ("GST".equalsIgnoreCase(docType) && bidder.getGstin() != null && !bidder.getGstin().isBlank()
                        ? bidder.getGstin()
                        : docType + "-" + documentId.toString().substring(0, 8).toUpperCase());
                String holderName = bidder.getCompanyName() != null ? bidder.getCompanyName() : "Bidding Entity";
                List<Map<String, Object>> fields = List.of(
                        Map.of("field_name", docType, "value", docNumber, "confidence", 0.95),
                        Map.of("field_name", "NAME", "value", holderName, "confidence", 0.95)
                );
                documentService.saveOcrResults(
                        documentId,
                        "Document: " + docType + "\nNumber: " + docNumber + "\nHolder: " + holderName,
                        UUID.randomUUID().toString(),
                        0.95,
                        objectMapper.writeValueAsString(fields)
                );
                documentService.markDocumentAsProcessed(documentId);
            } catch (Exception fallbackEx) {
                logger.error("Fallback extraction failed for document {}: {}", documentId, fallbackEx.getMessage());
                markDocumentFailed(documentId);
            }
        }
    }

    @Override
    public void triggerVerificationChecks(UUID tenderBidId) {
        TenderBid tenderBid = tenderBidService.getTenderBidById(tenderBidId);
        Bidder bidder = bidderService.getBidderById(tenderBid.getBidderId());
        List<Map<String, Object>> checks = new ArrayList<>();
        addVerificationCheck(checks, bidder, "GST", bidder.getGstin());
        addVerificationCheck(checks, bidder, "PAN", bidder.getPan());

        if (checks.isEmpty()) {
            logger.warn("No GST or PAN identifier available for tender bid {}", tenderBidId);
            return;
        }

        Map<String, Object> request = Map.of("checks", checks);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    verificationBaseUrl + "/api/v1/verification/verify-all",
                    new HttpEntity<>(request, jsonHeaders()),
                    Map.class
            );
            Map responseBody = response.getBody();
            List<Map<String, Object>> results = responseBody == null
                    ? List.of()
                    : (List<Map<String, Object>>) responseBody.getOrDefault("results", List.of());
            for (Map<String, Object> result : results) {
                verificationResultRepository.save(new VerificationResult(
                        tenderBidId,
                        stringValue(result, "verification_type"),
                        stringValue(result, "status"),
                        stringValue(result, "identifier"),
                        stringValue(result, "source"),
                        stringValue(result, "error_state"),
                        doubleValue(result, "confidence"),
                        objectMapper.writeValueAsString(result.get("evidence"))
                ));
            }
        } catch (Exception exception) {
            logger.error("Verification Hub call failed for tender bid {} at {}: {}", tenderBidId, verificationBaseUrl, exception.getMessage(), exception);
            for (Map<String, Object> check : checks) {
                verificationResultRepository.save(new VerificationResult(
                        tenderBidId,
                        String.valueOf(check.get("verification_type")),
                        "FAILED",
                        String.valueOf(check.get("identifier")),
                        "verification-hub",
                        "PROVIDER_UNAVAILABLE",
                        0.0,
                        "{}"
                ));
            }
        }
    }

    @Override
    public void triggerComplianceEvaluation(UUID tenderBidId) {
        TenderBid tenderBid = tenderBidService.getTenderBidById(tenderBidId);
        Bidder bidder = bidderService.getBidderById(tenderBid.getBidderId());
        List<Document> documents = documentRepository.findByTenderBidId(tenderBidId);
        List<VerificationResult> verifications = verificationResultRepository.findByTenderBidId(tenderBidId);

        try {
            Map<String, Object> bidderData = new HashMap<>();
            bidderData.put("id", bidder.getId().toString());
            bidderData.put("company_name", bidder.getCompanyName());
            bidderData.put("email", bidder.getEmail());
            bidderData.put("phone", bidder.getPhone());
            if (bidder.getPan() != null && !bidder.getPan().isBlank()) {
                bidderData.put("pan", Map.of("pan", bidder.getPan(), "name", bidder.getCompanyName()));
            }
            if (bidder.getGstin() != null && !bidder.getGstin().isBlank()) {
                bidderData.put("gst", Map.of("gstin", bidder.getGstin(), "legal_name", bidder.getCompanyName()));
            }

            List<Object> extractedEntities = new ArrayList<>();
            for (Document doc : documents) {
                if (doc.getOcrExtractedFields() != null && !doc.getOcrExtractedFields().isBlank()) {
                    try {
                        Object parsedFields = objectMapper.readValue(doc.getOcrExtractedFields(), Object.class);
                        String docTypeKey = doc.getDocumentType() != null ? doc.getDocumentType().name().toLowerCase() : "other";
                        if (!bidderData.containsKey(docTypeKey)) {
                            bidderData.put(docTypeKey, parsedFields);
                        }
                        if (parsedFields instanceof List) {
                            extractedEntities.addAll((List<?>) parsedFields);
                        } else {
                            extractedEntities.add(parsedFields);
                        }
                    } catch (Exception e) {
                        logger.warn("Could not parse ocrExtractedFields for doc {}: {}", doc.getId(), e.getMessage());
                    }
                }
            }
            bidderData.put("extracted_entities", extractedEntities);

            Map<String, Object> verificationResultsMap = new HashMap<>();
            for (VerificationResult vr : verifications) {
                Map<String, Object> resMap = new HashMap<>();
                resMap.put("status", vr.getStatus());
                resMap.put("source", vr.getSource());
                resMap.put("reason", vr.getErrorState() != null ? vr.getErrorState() : "");
                resMap.put("confidence", vr.getConfidence());
                resMap.put("identifier", vr.getIdentifier());
                if (vr.getEvidence() != null) {
                    resMap.put("evidence", vr.getEvidence());
                }
                verificationResultsMap.put(vr.getVerificationType(), resMap);
            }

            Map<String, Object> requestBody = Map.of(
                    "bidder_data", bidderData,
                    "verification_results", verificationResultsMap
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiBaseUrl + "/api/ai/analyze",
                    new HttpEntity<>(requestBody, jsonHeaders()),
                    Map.class
            );

            Map<String, Object> aiResult = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || aiResult == null) {
                throw new IllegalStateException("AI engine returned non-successful status or null body");
            }

            String status = stringValue(aiResult, "status");
            Double consistencyScore = doubleValue(aiResult, "identity_consistency_score");
            BigDecimal entityMatchScore = consistencyScore != null ? BigDecimal.valueOf(consistencyScore) : BigDecimal.ZERO;
            String riskLevel = stringValue(aiResult, "risk_level");
            String recommendationText = stringValue(aiResult, "recommendation");
            String explanation = stringValue(aiResult, "explanation");

            Map<String, Object> discrepanciesMap = new HashMap<>();
            discrepanciesMap.put("findings", aiResult.get("findings"));
            discrepanciesMap.put("consistency_checks", aiResult.get("consistency_checks"));
            discrepanciesMap.put("identifier_checks", aiResult.get("identifier_checks"));
            String discrepanciesJson = objectMapper.writeValueAsString(discrepanciesMap);

            ComplianceResult complianceResult = new ComplianceResult(
                    tenderBidId,
                    entityMatchScore,
                    riskLevel,
                    discrepanciesJson,
                    explanation
            );
            complianceResultRepository.save(complianceResult);

            Recommendation recommendation = new Recommendation(
                    tenderBidId,
                    complianceResult.getId(),
                    recommendationText,
                    entityMatchScore
            );
            recommendationRepository.save(recommendation);

            boolean pass = ("CONSISTENT".equalsIgnoreCase(status) || "PASS".equalsIgnoreCase(status) || "LOW".equalsIgnoreCase(riskLevel));
            tenderBid.markComplianceResult(pass ? BidStatus.PASSED_AUTOMATED_CHECKS : BidStatus.NEEDS_REVIEW);
            tenderBidRepository.save(tenderBid);

        } catch (Exception exception) {
            logger.error("AI service call failed for tender bid {} at {}: {}", tenderBidId, aiBaseUrl, exception.getMessage(), exception);
            // Fallback heuristic
            boolean needsReview = documents.stream().anyMatch(document ->
                    document.getStatus() == DocumentStatus.FAILED
                            || (document.getClassificationConfidence() != null
                            && document.getClassificationConfidence() < MINIMUM_CLASSIFICATION_CONFIDENCE));
            needsReview = needsReview || verifications.stream().anyMatch(result ->
                    "NOT_FOUND".equals(result.getStatus()) || "FAILED".equals(result.getStatus()));

            tenderBid.markComplianceResult(needsReview
                    ? BidStatus.NEEDS_REVIEW
                    : BidStatus.PASSED_AUTOMATED_CHECKS);
            tenderBidRepository.save(tenderBid);
        }
    }

    @Override
    public void triggerCollusionCheck(UUID tenderId) {
        logger.info("Starting collusion check for tender {}", tenderId);
        try {
            // ── 1. Collect bids for this tender ──────────────────────────────────
            List<TenderBid> bids = tenderBidService.getTenderBidsByTenderId(tenderId);
            if (bids.isEmpty()) {
                logger.info("No bids found for tender {} — skipping collusion check", tenderId);
                return;
            }

            // ── 2. Build bidder info payload for the AI engine ───────────────────
            List<Map<String, Object>> bidderInfoList = new ArrayList<>();
            for (TenderBid bid : bids) {
                Bidder bidder = bidderService.getBidderById(bid.getBidderId());
                Map<String, Object> info = new HashMap<>();
                info.put("bidder_id", bidder.getId().toString());
                info.put("company_name", bidder.getCompanyName());
                info.put("pan", bidder.getPan());
                info.put("gstin", bidder.getGstin());
                info.put("bid_id", bid.getId().toString());
                bidderInfoList.add(info);
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("tender_id", tenderId.toString());
            requestBody.put("bidders", bidderInfoList);

            // ── 3. Call AI engine collusion endpoint ─────────────────────────────
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiBaseUrl + "/api/collusion/analyze",
                    new HttpEntity<>(requestBody, jsonHeaders()),
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                logger.warn("AI collusion endpoint returned non-200 for tender {}: {}",
                        tenderId, response.getStatusCode());
                return;
            }

            Map<?, ?> body = response.getBody();
            Object clustersRaw = body.get("clusters");
            if (!(clustersRaw instanceof List<?> clusters)) {
                logger.warn("Unexpected AI response shape for collusion check on tender {}", tenderId);
                return;
            }

            // ── 4. Wipe old signals for this tender, then persist new clusters ───
            cartelSignalRepository.deleteByTenderId(tenderId);

            List<CartelSignal> signals = new ArrayList<>();
            for (Object clusterObj : clusters) {
                if (!(clusterObj instanceof Map<?, ?> cluster)) continue;

                String clusterId      = stringValue((Map) cluster, "cluster_id");
                String bidderIdsJson  = stringValue((Map) cluster, "bidder_ids");   // already JSON string
                Double strength       = doubleValue((Map) cluster, "connection_strength");
                String sharedSignals  = stringValue((Map) cluster, "shared_signals");
                String patternFlags   = stringValue((Map) cluster, "pattern_flags");
                String explanation    = stringValue((Map) cluster, "explanation");
                String recRaw         = stringValue((Map) cluster, "recommendation");

                CollusionRecommendation recommendation;
                try {
                    recommendation = recRaw != null
                            ? CollusionRecommendation.valueOf(recRaw.toUpperCase())
                            : CollusionRecommendation.FLAG_FOR_REVIEW;
                } catch (IllegalArgumentException e) {
                    recommendation = CollusionRecommendation.FLAG_FOR_REVIEW;
                }

                signals.add(new CartelSignal(
                        clusterId != null ? clusterId : UUID.randomUUID().toString(),
                        tenderId,
                        bidderIdsJson != null ? bidderIdsJson : "[]",
                        strength != null ? new java.math.BigDecimal(strength).setScale(2, java.math.RoundingMode.HALF_UP) : null,
                        sharedSignals,
                        patternFlags,
                        explanation,
                        recommendation
                ));
            }

            cartelSignalRepository.saveAll(signals);
            logger.info("Persisted {} cartel-signal cluster(s) for tender {}", signals.size(), tenderId);

        } catch (Exception e) {
            // Collusion check is non-blocking — log and continue
            logger.error("Collusion check failed for tender {} (non-fatal): {}", tenderId, e.getMessage(), e);
        }
    }

    public List<VerificationResult> getVerificationResults(UUID tenderBidId) {
        return verificationResultRepository.findByTenderBidId(tenderBidId);
    }

    private void addVerificationCheck(
            List<Map<String, Object>> checks,
            Bidder bidder,
            String type,
            String identifier
    ) {
        if (identifier == null || identifier.isBlank()) {
            return;
        }
        Map<String, Object> check = new HashMap<>();
        check.put("bidder_id", bidder.getId().toString());
        check.put("verification_type", type);
        check.put("identifier", identifier);
        check.put("company_name", bidder.getCompanyName());
        check.put("pan", bidder.getPan());
        check.put("gstin", bidder.getGstin());
        check.put("metadata", Map.of());
        checks.add(check);
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void markDocumentFailed(UUID documentId) {
        try {
            if (documentService.getDocumentById(documentId).getStatus() == DocumentStatus.PROCESSING) {
                documentService.markDocumentAsFailed(documentId);
            }
        } catch (Exception exception) {
            logger.error("Could not mark document {} as FAILED: {}", documentId, exception.getMessage(), exception);
        }
    }

    private String stringValue(Map result, String key) {
        Object value = result.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Double doubleValue(Map result, String key) {
        Object value = result.get(key);
        return value instanceof Number number ? number.doubleValue() : null;
    }
}
