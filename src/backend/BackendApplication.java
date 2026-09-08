package backend;

import backend.service.BidderService;
import backend.service.DocumentService;
import backend.service.TenderBidService;
import backend.service.TenderRequirementService;
import backend.service.TenderService;
import backend.storage.BidderStorage;
import backend.storage.DocumentStorage;
import backend.storage.TenderBidStorage;
import backend.storage.TenderRequirementStorage;
import backend.storage.TenderStorage;
import backend.security.AppUser;
import backend.security.Role;
import backend.security.UserStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {

        SpringApplication.run(BackendApplication.class, args);
    }


    @Bean
    public TenderStorage tenderStorage() {

        return new TenderStorage();
    }


    @Bean
    public BidderStorage bidderStorage() {

        return new BidderStorage();
    }


    @Bean
    public DocumentStorage documentStorage() {

        return new DocumentStorage();
    }


    @Bean
    public TenderBidStorage tenderBidStorage() {

        return new TenderBidStorage();
    }


    @Bean
    public TenderRequirementStorage tenderRequirementStorage() {

        return new TenderRequirementStorage();
    }


    @Bean
    public TenderService tenderService(TenderStorage tenderStorage) {

        return new TenderService(tenderStorage);
    }


    @Bean
    public BidderService bidderService(BidderStorage bidderStorage) {

        return new BidderService(bidderStorage);
    }


    @Bean
    public TenderBidService tenderBidService(
            TenderBidStorage tenderBidStorage,
            TenderService tenderService,
            BidderService bidderService
    ) {

        return new TenderBidService(
                tenderBidStorage,
                tenderService,
                bidderService
        );
    }


    @Bean
    public DocumentService documentService(
            DocumentStorage documentStorage,
            TenderBidService tenderBidService
    ) {

        return new DocumentService(
                documentStorage,
                tenderBidService
        );
    }


    @Bean
    public TenderRequirementService tenderRequirementService(
            TenderRequirementStorage tenderRequirementStorage,
            TenderService tenderService
    ) {

        return new TenderRequirementService(
                tenderRequirementStorage,
                tenderService
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