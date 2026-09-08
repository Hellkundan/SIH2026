package com.dixy.backend.audit;

import com.dixy.backend.entity.Role;
import com.dixy.backend.entity.User;
import com.dixy.backend.repository.UserRepository;
import com.dixy.backend.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit & Integration test suite for the DIXY Audit Subsystem (Step 5).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuditSubsystemTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private String adminToken;
    private String auditorToken;
    private String bidderToken;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(User.builder()
                .username("audit_admin")
                .email("admin@audit.gov.in")
                .password(passwordEncoder.encode("Admin@123"))
                .fullName("Audit Admin")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .accountNonLocked(true)
                .build());

        User auditor = userRepository.save(User.builder()
                .username("audit_auditor")
                .email("auditor@audit.gov.in")
                .password(passwordEncoder.encode("Auditor@123"))
                .fullName("Audit Auditor")
                .role(Role.ROLE_AUDITOR)
                .enabled(true)
                .accountNonLocked(true)
                .build());

        User bidder = userRepository.save(User.builder()
                .username("audit_bidder")
                .email("bidder@audit.com")
                .password(passwordEncoder.encode("Bidder@123"))
                .fullName("Audit Bidder")
                .role(Role.ROLE_BIDDER)
                .enabled(true)
                .accountNonLocked(true)
                .build());

        adminToken = jwtUtils.generateToken(admin);
        auditorToken = jwtUtils.generateToken(auditor);
        bidderToken = jwtUtils.generateToken(bidder);
    }

    @Test
    @DisplayName("AuditLog persistence: saving audit event creates entity with timestamps and valid fields")
    void auditLogPersistence_CreatesValidRecord() {
        AuditLog saved = auditService.recordEvent(
                "officer_raj",
                10L,
                AuditAction.CREATE_TENDER,
                "Tender",
                "TND-2026-001",
                "192.168.1.100",
                AuditStatus.SUCCESS,
                "Created new procurement tender",
                Map.of("tenderTitle", "Solar Inverter Supply")
        );

        assertNotNull(saved.getId());
        assertEquals("officer_raj", saved.getActor());
        assertEquals(AuditAction.CREATE_TENDER, saved.getAction());
        assertEquals(AuditStatus.SUCCESS, saved.getStatus());
        assertEquals("Tender", saved.getEntityType());
        assertEquals("TND-2026-001", saved.getEntityId());
        assertNotNull(saved.getTimestamp());
        assertTrue(saved.getMetadata().contains("Solar Inverter Supply"));
    }

    @Test
    @DisplayName("Security: sensitive credentials are stripped from audit metadata")
    void auditLog_SanitizesSensitiveCredentials() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("password", "SecretPass123!");
        metadata.put("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        metadata.put("secret", "super-secret-key");
        metadata.put("tenderId", "TND-99");

        AuditLog saved = auditService.recordEvent(
                "test_user",
                null,
                AuditAction.ADMIN_ACTION,
                "Config",
                "1",
                "127.0.0.1",
                AuditStatus.SUCCESS,
                "Updated config",
                metadata
        );

        assertFalse(saved.getMetadata().contains("SecretPass123!"));
        assertFalse(saved.getMetadata().contains("super-secret-key"));
        assertFalse(saved.getMetadata().contains("eyJhbGci"));
        assertTrue(saved.getMetadata().contains("TND-99"));
    }

    @Test
    @DisplayName("Pagination & entity queries: query audit logs by entity and actor")
    void queryAuditLogs_ByEntityAndActor() {
        auditService.recordEvent("bidder1", 1L, AuditAction.UPLOAD_DOCUMENT, "Document", "DOC-101", "10.0.0.1", AuditStatus.SUCCESS, "Upload doc", null);
        auditService.recordEvent("bidder1", 1L, AuditAction.RUN_VERIFICATION, "Document", "DOC-101", "10.0.0.1", AuditStatus.SUCCESS, "Verification", null);
        auditService.recordEvent("bidder2", 2L, AuditAction.UPLOAD_DOCUMENT, "Document", "DOC-102", "10.0.0.2", AuditStatus.SUCCESS, "Upload doc", null);

        Page<AuditLog> entityLogs = auditService.getAuditLogsByEntity("Document", "DOC-101", 0, 10);
        assertEquals(2, entityLogs.getTotalElements());

        Page<AuditLog> actorLogs = auditService.getAuditLogsByActor("bidder1", 0, 10);
        assertEquals(2, actorLogs.getTotalElements());
    }

    @Test
    @DisplayName("Audit API Authorization: ADMIN and AUDITOR can access /api/v1/audit, BIDDER is forbidden (403)")
    void auditApi_RbacAuthorization() throws Exception {
        auditService.recordEvent("admin", 1L, AuditAction.ADMIN_ACTION, "System", "1", "127.0.0.1", AuditStatus.SUCCESS, "Admin check", null);

        // Admin can access
        mockMvc.perform(get("/api/v1/audit")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        // Auditor can access
        mockMvc.perform(get("/api/v1/audit")
                        .header("Authorization", "Bearer " + auditorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Bidder is forbidden (403)
        mockMvc.perform(get("/api/v1/audit")
                        .header("Authorization", "Bearer " + bidderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Audit API: fetch single audit log by ID and entity endpoint")
    void auditApi_QueryByIdAndEntity() throws Exception {
        AuditLog entry = auditService.recordEvent("officer1", 2L, AuditAction.CREATE_TENDER, "Tender", "TND-888", "127.0.0.1", AuditStatus.SUCCESS, "Created tender", null);

        mockMvc.perform(get("/api/v1/audit/" + entry.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(entry.getId()))
                .andExpect(jsonPath("$.data.action").value("CREATE_TENDER"));

        mockMvc.perform(get("/api/v1/audit/entity/Tender/TND-888")
                        .header("Authorization", "Bearer " + auditorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].entityId").value("TND-888"));
    }
}
