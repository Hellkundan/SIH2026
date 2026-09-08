package com.dixy.backend.dto;

import com.dixy.backend.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response with JWT token and user profile")
public class AuthResponse {

    @Schema(description = "JWT Access Token")
    private String token;

    @Builder.Default
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Token expiration duration in milliseconds", example = "86400000")
    private Long expiresInMs;

    @Schema(description = "Username of authenticated user", example = "admin")
    private String username;

    @Schema(description = "Email of authenticated user", example = "admin@dixy.gov.in")
    private String email;

    @Schema(description = "Full name of authenticated user", example = "System Administrator")
    private String fullName;

    @Schema(description = "Organization of authenticated user", example = "DIXY Platform Admin")
    private String organization;

    @Schema(description = "Assigned security role", example = "ROLE_ADMIN")
    private Role role;
}
