package backend;

import backend.service.BidderService;
import backend.service.DocumentService;
import backend.service.TenderBidService;
import backend.service.TenderRequirementService;
import backend.service.TenderService;
import backend.service.OrchestrationService;
import backend.service.OrchestrationServiceImpl;
import backend.security.AppUser;
import backend.security.Role;
import backend.security.UserStorage;
import backend.repository.BidderRepository;
import backend.repository.ComplianceResultRepository;
import backend.repository.DocumentRepository;
import backend.repository.RecommendationRepository;
import backend.repository.TenderBidRepository;
import backend.repository.TenderRequirementRepository;
import backend.repository.TenderRepository;
import backend.repository.VerificationResultRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {

        SpringApplication.run(BackendApplication.class, args);
    }


    @Bean
    public TenderService tenderService(TenderRepository tenderRepository) {

        return new TenderService(tenderRepository);
    }


    @Bean
    public BidderService bidderService(BidderRepository bidderRepository) {

        return new BidderService(bidderRepository);
    }


    @Bean
    public TenderBidService tenderBidService(
            TenderBidRepository tenderBidRepository,
            TenderService tenderService,
            BidderService bidderService
    ) {

        return new TenderBidService(
                tenderBidRepository,
                tenderService,
                bidderService
        );
    }


    @Bean
    public DocumentService documentService(
            DocumentRepository documentRepository,
            TenderBidService tenderBidService
    ) {

        return new DocumentService(
                documentRepository,
                tenderBidService
        );
    }


    @Bean
    public TenderRequirementService tenderRequirementService(
            TenderRequirementRepository tenderRequirementRepository,
            TenderService tenderService
    ) {

        return new TenderRequirementService(
                tenderRequirementRepository,
                tenderService
        );
    }


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public OrchestrationService orchestrationService(
            DocumentService documentService,
            TenderBidService tenderBidService,
            BidderService bidderService,
            DocumentRepository documentRepository,
            TenderBidRepository tenderBidRepository,
            VerificationResultRepository verificationResultRepository,
            ComplianceResultRepository complianceResultRepository,
            RecommendationRepository recommendationRepository,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.ocr.base-url:http://localhost:8001}") String ocrBaseUrl,
            @Value("${app.verification.base-url:http://localhost:8000}") String verificationBaseUrl,
            @Value("${app.ai.base-url:http://localhost:8002}") String aiBaseUrl
    ) {
        return new OrchestrationServiceImpl(
                documentService,
                tenderBidService,
                bidderService,
                documentRepository,
                tenderBidRepository,
                verificationResultRepository,
                complianceResultRepository,
                recommendationRepository,
                restTemplate,
                objectMapper,
                ocrBaseUrl,
                verificationBaseUrl,
                aiBaseUrl
        );
    }


    @Bean
    public UserStorage userStorage(PasswordEncoder passwordEncoder) {

        UserStorage userStorage = new UserStorage();

        userStorage.saveUser(new AppUser(
            "procurement",
            passwordEncoder.encode("procurement123"),
            Role.PROCUREMENT_OFFICER
        ));
        userStorage.saveUser(new AppUser(
            "admin",
            passwordEncoder.encode("admin123"),
            Role.ADMIN
        ));
        userStorage.saveUser(new AppUser(
            "bidder",
            passwordEncoder.encode("bidder123"),
            Role.BIDDER
        ));

        return userStorage;
    }
}