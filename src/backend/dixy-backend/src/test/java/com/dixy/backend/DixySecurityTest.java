package com.dixy.backend;

import com.dixy.backend.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DIXY Security Integration Tests — Phase 3
 * ──────────────────────────────────────────
 * These tests verify the security rules WITHOUT needing Postman.
 * They simulate HTTP requests and check the responses automatically.
 *
 * @SpringBootTest — starts the full Spring context (including Security)
 * @AutoConfigureMockMvc — gives us MockMvc to simulate HTTP calls
 * @ActiveProfiles("test") — uses application-test.yml (H2, not PostgreSQL)
 *
 * Run with: mvn test
 *
 * WHAT WE TEST:
 *   Phase 3 Security Scenarios:
 *   1. Public endpoint returns 200 (no auth needed)
 *   2. Protected endpoint returns 401 when no token sent
 *   3. Admin endpoint returns 401 when no token sent
 *   4. Bidder endpoint returns 401 when no token sent
 *   (JWT-authenticated tests come in Phase 5)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DixySecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // ─────────────────────────────────────────────────────────────────────────
    // Public Endpoint Tests
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("Health check should return 200 without authentication")
        void healthCheck_ShouldReturn200_WithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/public/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("DIXY Backend"));
        }

        @Test
        @DisplayName("Health check response should include timestamp and requestId")
        void healthCheck_ShouldInclude_TimestampAndRequestId() throws Exception {
            mockMvc.perform(get("/api/v1/public/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Protected Endpoint Tests — No Auth
    //
    // NOTE (Phase 3): Without a JWT filter configured, Spring Security's default
    // behavior is to redirect unauthenticated requests (302 to /login).
    // In Phase 5, we add the JWT filter + stateless session → this changes to 401.
    // For now we verify the endpoints are NOT publicly accessible (not 200/204).
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Protected Endpoints Without Authentication")
    class ProtectedEndpointsNoAuth {

        @Test
        @DisplayName("Profile endpoint /api/v1/auth/me returns 401 Unauthorized without token")
        void meEndpoint_ShouldReturn401_WithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/me"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        @DisplayName("Admin endpoint returns 401 Unauthorized without token")
        void adminEndpoint_ShouldReturn401_WithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/admin/dashboard"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        @DisplayName("Tender endpoint returns 401 Unauthorized without token")
        void tenderEndpoint_ShouldReturn401_WithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/tender/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/tender/list"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        @DisplayName("Bidder endpoint returns 401 Unauthorized without token")
        void bidderEndpoint_ShouldReturn401_WithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/bidder/my-status"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/bidder/my-status"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").isNotEmpty());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORS and Security Headers Tests (Step 4)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CORS and Security Headers")
    class CorsAndSecurityHeaders {

        @Test
        @DisplayName("CORS: Allowed frontend origin receives Access-Control-Allow-Origin header")
        void cors_AllowedOrigin_ReturnsCorsHeaders() throws Exception {
            mockMvc.perform(get("/api/v1/public/health")
                            .header("Origin", "http://localhost:3000"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }

        @Test
        @DisplayName("CORS: Disallowed origin does not receive Access-Control-Allow-Origin header")
        void cors_DisallowedOrigin_DoesNotReturnAllowOrigin() throws Exception {
            mockMvc.perform(get("/api/v1/public/health")
                            .header("Origin", "http://malicious-site.com"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CORS: Preflight OPTIONS request for allowed origin returns 200 and allowed methods")
        void cors_PreflightOptions_ReturnsAllowedMethodsAndHeaders() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/v1/auth/login")
                            .header("Origin", "http://localhost:5173")
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                    .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("POST")))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }

        @Test
        @DisplayName("Security Headers: Content-Type sniffing protection and Frame options are enforced")
        void securityHeaders_EnforcesContentTypeAndFrameOptions() throws Exception {
            mockMvc.perform(get("/api/v1/public/health"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                    .andExpect(header().string("X-Frame-Options", "DENY"));
        }
    }
}
