package backend.service;

import backend.enums.BidStatus;
import backend.enums.DocumentStatus;
import backend.model.Bidder;
import backend.model.Document;
import backend.model.TenderBid;
import backend.model.VerificationResult;
import backend.repository.DocumentRepository;
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
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String ocrBaseUrl;
    private final String verificationBaseUrl;

    public OrchestrationServiceImpl(
            DocumentService documentService,
            TenderBidService tenderBidService,
            BidderService bidderService,
            DocumentRepository documentRepository,
            TenderBidRepository tenderBidRepository,
            VerificationResultRepository verificationResultRepository,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            String ocrBaseUrl,
            String verificationBaseUrl
    ) {
        this.documentService = documentService;
        this.tenderBidService = tenderBidService;
        this.bidderService = bidderService;
        this.documentRepository = documentRepository;
        this.tenderBidRepository = tenderBidRepository;
        this.verificationResultRepository = verificationResultRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.ocrBaseUrl = ocrBaseUrl;
        this.verificationBaseUrl = verificationBaseUrl;
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
            logger.error("OCR call failed for document {} at {}: {}", documentId, ocrBaseUrl, exception.getMessage(), exception);
            markDocumentFailed(documentId);
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
        List<Document> documents = documentRepository.findByTenderBidId(tenderBidId);
        List<VerificationResult> verifications = verificationResultRepository.findByTenderBidId(tenderBidId);

        // Placeholder rules for the future AI/ML compliance scoring service.
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
