package com.dixy.backend;

import com.dixy.backend.dto.LoginRequest;
import com.dixy.backend.dto.RegisterRequest;
import com.dixy.backend.entity.Role;
import com.dixy.backend.entity.User;
import com.dixy.backend.repository.UserRepository;
import com.dixy.backend.security.JwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DIXY Authentication & JWT Integration Tests
 * ───────────────────────────────────────────
 * Tests the complete authentication flow:
 * - Registration
 * - Login (positive & negative)
 * - JWT issuance & verification
 * - Protected endpoint access
 * - Role-Based Access Control (RBAC)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DixyAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private User adminUser;
    private User bidderUser;
    private String adminToken;
    private String bidderToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        adminUser = userRepository.save(User.builder()
                .username("test_admin")
                .email("admin@test.gov.in")
                .password(passwordEncoder.encode("Admin@123"))
                .fullName("Test Admin")
                .organization("Test Admin Cell")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .accountNonLocked(true)
                .build());

        bidderUser = userRepository.save(User.builder()
                .username("test_bidder")
                .email("bidder@test.com")
                .password(passwordEncoder.encode("Bidder@123"))
                .fullName("Test Bidder")
                .organization("Test Bidder Corp")
                .role(Role.ROLE_BIDDER)
                .enabled(true)
                .accountNonLocked(true)
                .build());

        adminToken = jwtUtils.generateToken(adminUser);
        bidderToken = jwtUtils.generateToken(bidderUser);
    }

    @Test
    @DisplayName("Login with valid credentials returns JWT token and 200 OK")
    void login_Success() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("test_admin")
                .password("Admin@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.username").value("test_admin"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Login with invalid password returns 401 Unauthorized with standardized error format")
    void login_InvalidPassword_Returns401() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("test_admin")
                .password("WrongPassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("Register new Bidder user creates account and returns 201 Created")
    void register_Success() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("new_vendor")
                .email("vendor@enterprise.in")
                .password("SecurePass123!")
                .fullName("Vikrant Enterprise")
                .organization("Vikrant Tech Ltd")
                .role(Role.ROLE_BIDDER)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("new_vendor"))
                .andExpect(jsonPath("$.data.role").value("ROLE_BIDDER"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("Register with existing username returns 409 Conflict with standardized error format")
    void register_DuplicateUsername_Returns409() throws Exception {
        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .username("test_admin")
                .email("different_email@test.com")
                .password("Password@123")
                .fullName("Duplicate User")
                .role(Role.ROLE_BIDDER)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("Get /api/v1/auth/me with valid Bearer token returns profile")
    void getMe_WithValidToken_ReturnsProfile() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("test_admin"))
                .andExpect(jsonPath("$.data.email").value("admin@test.gov.in"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Get /api/v1/auth/me without token returns 401 Unauthorized with standardized error format")
    void getMe_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/me"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("RBAC: Admin token can access /api/v1/admin/dashboard")
    void rbac_AdminCanAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access").value("FULL_SYSTEM_ADMIN"));
    }

    @Test
    @DisplayName("RBAC: Bidder token cannot access /api/v1/admin/dashboard (403 Forbidden with standardized error format)")
    void rbac_BidderForbiddenFromAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + bidderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.path").value("/api/v1/admin/dashboard"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("RBAC: Bidder token can access /api/v1/bidder/my-status")
    void rbac_BidderCanAccessBidderEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/bidder/my-status")
                        .header("Authorization", "Bearer " + bidderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bidder").value("test_bidder"));
    }

    @Test
    @DisplayName("Validation failure returns 400 Bad Request with standardized error structure")
    void register_ValidationFailure_Returns400() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("")
                .email("not-an-email")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @DisplayName("Non-existent public route returns 404 Not Found with standardized error structure")
    void nonExistentRoute_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/public/non-existent-path"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/public/non-existent-path"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Password Validation Tests (Step 3)
    // ─────────────────────────────────────────────────────────────────────────

    private RegisterRequest createValidRegisterRequest(String password) {
        return RegisterRequest.builder()
                .username("pwd_test_user")
                .email("pwd_test@domain.in")
                .password(password)
                .fullName("Password Test User")
                .organization("Security Corp")
                .role(Role.ROLE_BIDDER)
                .build();
    }

    @Test
    @DisplayName("Password validation: null password fails with 400 Bad Request")
    void register_NullPassword_FailsWith400() throws Exception {
        RegisterRequest request = createValidRegisterRequest(null);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"))
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("Password validation: blank password fails with 400 Bad Request")
    void register_BlankPassword_FailsWith400() throws Exception {
        RegisterRequest request = createValidRegisterRequest("   ");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("Password validation: fewer than 8 characters fails with 400 Bad Request")
    void register_ShortPassword_FailsWith400() throws Exception {
        RegisterRequest request = createValidRegisterRequest("Ab1@xyz"); // 7 chars

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("Password validation: all lowercase fails with 400 Bad Request")
    void register_AllLowercasePassword_FailsWith400() throws Exception {
        RegisterRequest request = createValidRegisterRequest("password123!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("Password validation: all uppercase fails with 400 Bad Request")
    void register_AllUppercasePassword_FailsWith400() throws Exception {
        RegisterRequest request = createValidRegisterRequest("PASSWORD123!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("Password validation: letters + numbers with no special character fails with 400 Bad Request")
    void register_NoSpecialCharPassword_FailsWith400() throws Exception {
        RegisterRequest request = createValidRegisterRequest("SecurePass123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("Password validation: letters + special character with no digit fails with 400 Bad Request")
    void register_NoDigitPassword_FailsWith400() throws Exception {
        RegisterRequest request = createValidRegisterRequest("SecurePass!@#");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("Password validation: uppercase + lowercase + digit + special character succeeds")
    void register_StrongPassword_Succeeds() throws Exception {
        RegisterRequest request = createValidRegisterRequest("Secure@Pass123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("Password security: toString excludes password from DTOs and User entity")
    void passwordSecurity_ToStringExcludesPassword() {
        LoginRequest login = LoginRequest.builder()
                .usernameOrEmail("admin")
                .password("SuperSecret@123")
                .build();
        org.junit.jupiter.api.Assertions.assertFalse(login.toString().contains("SuperSecret@123"));

        RegisterRequest register = RegisterRequest.builder()
                .username("test")
                .password("SuperSecret@123")
                .build();
        org.junit.jupiter.api.Assertions.assertFalse(register.toString().contains("SuperSecret@123"));

        User user = User.builder()
                .username("test")
                .password("SuperSecret@123")
                .build();
        org.junit.jupiter.api.Assertions.assertFalse(user.toString().contains("SuperSecret@123"));
    }
}
