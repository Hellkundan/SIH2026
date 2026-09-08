package com.dixy.backend.config;

import com.dixy.backend.entity.Role;
import com.dixy.backend.entity.User;
import com.dixy.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds initial demo accounts on application startup if database is empty.
 * Runs on standard and development profiles (excluded on test profile if desired, or provides default demo accounts).
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("DIXY User Database is empty. Seeding initial demo accounts for SIH 2026...");

            User admin = User.builder()
                    .username("admin")
                    .email("admin@dixy.gov.in")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("System Administrator")
                    .organization("DIXY Central Administration")
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .accountNonLocked(true)
                    .build();

            User officer = User.builder()
                    .username("officer")
                    .email("officer@gem.gov.in")
                    .password(passwordEncoder.encode("Officer@123"))
                    .fullName("Dr. Rajesh Kumar")
                    .organization("GeM Procurement Division")
                    .role(Role.ROLE_PROCUREMENT_OFFICER)
                    .enabled(true)
                    .accountNonLocked(true)
                    .build();

            User bidder = User.builder()
                    .username("bidder")
                    .email("bidder@bharattech.in")
                    .password(passwordEncoder.encode("Bidder@123"))
                    .fullName("Vikram Sharma")
                    .organization("Bharat Tech Solutions Pvt Ltd")
                    .role(Role.ROLE_BIDDER)
                    .enabled(true)
                    .accountNonLocked(true)
                    .build();

            User auditor = User.builder()
                    .username("auditor")
                    .email("auditor@vigilance.gov.in")
                    .password(passwordEncoder.encode("Auditor@123"))
                    .fullName("Ananya Verma")
                    .organization("Central Vigilance Audit Cell")
                    .role(Role.ROLE_AUDITOR)
                    .enabled(true)
                    .accountNonLocked(true)
                    .build();

            userRepository.saveAll(List.of(admin, officer, bidder, auditor));
            log.info("✅ Successfully seeded 4 demo accounts: admin, officer, bidder, auditor");
        } else {
            log.info("User database already initialized with {} users.", userRepository.count());
        }
    }
}
