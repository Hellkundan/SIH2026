package com.dixy.backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtils security and secret validation.
 * Verifies strict 256-bit (32-byte) key requirements, fail-fast behavior,
 * and token generation/validation lifecycle.
 */
@DisplayName("JwtUtils Secret Hardening & Token Operations")
class JwtUtilsTest {

    private static final long DEFAULT_EXPIRATION = 86400000L; // 24 hours
    private static final String VALID_32_BYTE_SECRET = "12345678901234567890123456789012"; // exactly 32 bytes
    private static final String VALID_LONG_SECRET = "dixy-dev-secret-key-change-this-in-production-minimum-256-bits-long";

    @Nested
    @DisplayName("JWT Secret Validation & Fail-Fast Checks")
    class SecretValidationTests {

        @Test
        @DisplayName("1. Missing (null) secret throws IllegalStateException")
        void missingSecret_ThrowsException() {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> new JwtUtils(null, DEFAULT_EXPIRATION));
            assertTrue(ex.getMessage().contains("JWT_SECRET must be provided"));
        }

        @Test
        @DisplayName("2. Blank (whitespace) secret throws IllegalStateException")
        void blankSecret_ThrowsException() {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> new JwtUtils("   \t\n  ", DEFAULT_EXPIRATION));
            assertTrue(ex.getMessage().contains("JWT_SECRET must be provided"));
        }

        @Test
        @DisplayName("3. Secret shorter than 32 bytes throws IllegalStateException without leaking secret")
        void secretShorterThan32Bytes_ThrowsException() {
            String shortSecret = "too-short-key";
            assertEquals(13, shortSecret.getBytes(StandardCharsets.UTF_8).length);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> new JwtUtils(shortSecret, DEFAULT_EXPIRATION));

            assertTrue(ex.getMessage().contains("at least 32 bytes"));
            assertFalse(ex.getMessage().contains(shortSecret), "Secret value must never leak into exception message");
        }

        @Test
        @DisplayName("4. Exactly 32-byte secret initializes successfully")
        void secretExactly32Bytes_Succeeds() {
            assertEquals(32, VALID_32_BYTE_SECRET.getBytes(StandardCharsets.UTF_8).length);
            assertDoesNotThrow(() -> new JwtUtils(VALID_32_BYTE_SECRET, DEFAULT_EXPIRATION));
        }

        @Test
        @DisplayName("5. Longer valid secret (> 32 bytes) initializes successfully")
        void longerValidSecret_Succeeds() {
            assertTrue(VALID_LONG_SECRET.getBytes(StandardCharsets.UTF_8).length > 32);
            assertDoesNotThrow(() -> new JwtUtils(VALID_LONG_SECRET, DEFAULT_EXPIRATION));
        }

        @Test
        @DisplayName("Secret length validation strictly evaluates UTF-8 BYTES, not char count")
        void utf8ByteLengthValidation() {
            // 31 ASCII characters = 31 bytes -> must fail (< 32 bytes)
            String thirtyOneBytes = "1234567890123456789012345678901";
            assertEquals(31, thirtyOneBytes.getBytes(StandardCharsets.UTF_8).length);
            assertThrows(IllegalStateException.class, () -> new JwtUtils(thirtyOneBytes, DEFAULT_EXPIRATION));

            // Multi-byte string: 16 characters of 2-byte characters = 32 bytes -> must succeed
            String multiByteSecret = "éééééééééééééééé"; // 16 chars * 2 bytes = 32 bytes
            assertEquals(16, multiByteSecret.length());
            assertEquals(32, multiByteSecret.getBytes(StandardCharsets.UTF_8).length);
            assertDoesNotThrow(() -> new JwtUtils(multiByteSecret, DEFAULT_EXPIRATION));
        }
    }

    @Nested
    @DisplayName("Token Generation & Validation Lifecycle")
    class TokenLifecycleTests {

        private final JwtUtils jwtUtils = new JwtUtils(VALID_LONG_SECRET, DEFAULT_EXPIRATION);

        private UserDetails createTestUser(String username, String role) {
            return new User(username, "password123", List.of(new SimpleGrantedAuthority(role)));
        }

        @Test
        @DisplayName("6. JWT generation with valid secret creates standard 3-part token")
        void jwtGeneration_CreatesValidToken() {
            UserDetails user = createTestUser("officer_sharma", "ROLE_PROCUREMENT_OFFICER");
            String token = jwtUtils.generateToken(user);

            assertNotNull(token);
            assertFalse(token.isBlank());
            // Standard JWT structure: header.payload.signature
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length, "JWT must consist of exactly 3 dot-separated parts");

            // Verify extracted subject and roles
            assertEquals("officer_sharma", jwtUtils.extractUsername(token));
            assertEquals("ROLE_PROCUREMENT_OFFICER", jwtUtils.extractRoles(token));
        }

        @Test
        @DisplayName("7. JWT validation validates authentic token and rejects tampered token")
        void jwtValidation_ValidatesAndRejectsTampered() {
            UserDetails user = createTestUser("bidder_corp", "ROLE_BIDDER");
            String token = jwtUtils.generateToken(user);

            // Valid token
            assertTrue(jwtUtils.validateToken(token));
            assertTrue(jwtUtils.validateToken(token, user));

            // Tampered signature
            String tamperedToken = token.substring(0, token.length() - 5) + "abcde";
            assertFalse(jwtUtils.validateToken(tamperedToken));

            // Invalid format
            assertFalse(jwtUtils.validateToken("not-a-real-token"));
            assertFalse(jwtUtils.validateToken(""));
        }

        @Test
        @DisplayName("Expired token is rejected")
        void expiredToken_IsRejected() {
            // Instantiate with 1ms expiration
            JwtUtils fastExpiringJwt = new JwtUtils(VALID_LONG_SECRET, 1L);
            UserDetails user = createTestUser("expired_user", "ROLE_BIDDER");
            String token = fastExpiringJwt.generateToken(user);

            // Wait 10ms to ensure expiration
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}

            assertFalse(fastExpiringJwt.validateToken(token));
            assertTrue(fastExpiringJwt.isTokenExpired(token));
        }
    }
}
