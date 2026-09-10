package backend;

import backend.enums.BidStatus;
import backend.model.Bidder;
import backend.model.ComplianceResult;
import backend.model.Recommendation;
import backend.model.Tender;
import backend.model.TenderBid;
import backend.repository.BidderRepository;
import backend.repository.ComplianceResultRepository;
import backend.repository.RecommendationRepository;
import backend.repository.TenderBidRepository;
import backend.repository.TenderRepository;
import backend.service.OrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "app.jwt.secret=ZGl4eS1zaWgtMjAyNi1qd3Qtc2VjcmV0LWtleS1mb3ItZGV2",
    "app.ocr.base-url=http://localhost:8001",
    "app.verification.base-url=http://localhost:8000",
    "app.ai.base-url=http://localhost:8002",
    "app.storage.documents-dir=./data/documents"
})
public class OrchestrationServiceComplianceTest {

    @Autowired
    private OrchestrationService orchestrationService;

    @Autowired
    private TenderRepository tenderRepository;

    @Autowired
    private BidderRepository bidderRepository;

    @Autowired
    private TenderBidRepository tenderBidRepository;

    @Autowired
    private ComplianceResultRepository complianceResultRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void testTriggerComplianceEvaluation_Success() {
        Tender tender = tenderRepository.save(new Tender("Test Tender", "Description"));
        Bidder bidder = bidderRepository.save(new Bidder("Acme Corp", "acme@example.com", "+1234567890"));
        bidder.setIdentifiers("ABCDE1234F", "27ABCDE1234F1Z5");
        bidderRepository.save(bidder);

        TenderBid bid = tenderBidRepository.save(new TenderBid(tender.getId(), bidder.getId()));

        String mockAiResponse = """
            {
              "status": "CONSISTENT",
              "identity_consistency_score": 98.5,
              "evidence_coverage": 1.0,
              "available_documents": ["PAN", "GST"],
              "missing_documents": [],
              "risk_points": 0,
              "risk_level": "LOW",
              "consistency_checks": [],
              "identifier_checks": [],
              "findings": [],
              "recommendation": "IDENTITY CONSISTENT",
              "explanation": "No significant identity conflicts were detected."
            }
            """;

        mockServer.expect(requestTo("http://localhost:8002/api/ai/analyze"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(mockAiResponse, MediaType.APPLICATION_JSON));

        orchestrationService.triggerComplianceEvaluation(bid.getId());

        mockServer.verify();

        List<ComplianceResult> compResults = complianceResultRepository.findByTenderBidId(bid.getId());
        assertEquals(1, compResults.size());
        ComplianceResult comp = compResults.get(0);
        assertEquals("LOW", comp.getSeverity());
        assertEquals("No significant identity conflicts were detected.", comp.getExplanation());

        List<Recommendation> recResults = recommendationRepository.findByTenderBidId(bid.getId());
        assertEquals(1, recResults.size());
        Recommendation rec = recResults.get(0);
        assertEquals("IDENTITY CONSISTENT", rec.getAiRecommendation());
        assertEquals("PENDING", rec.getOfficerDecision());

        TenderBid updatedBid = tenderBidRepository.findById(bid.getId()).orElseThrow();
        assertEquals(BidStatus.PASSED_AUTOMATED_CHECKS, updatedBid.getStatus());
    }
}
