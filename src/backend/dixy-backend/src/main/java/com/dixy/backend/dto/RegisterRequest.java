package com.dixy.backend.dto;

import com.dixy.backend.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration payload")
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Unique username", example = "vendor_raj")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Schema(description = "User email address", example = "raj@vendor.in")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @jakarta.validation.constraints.Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    @Schema(description = "Account password (min 8 chars, uppercase, lowercase, digit, special char)", example = "Vendor@123")
    @ToString.Exclude
    private String password;

    @NotBlank(message = "Full name is required")
    @Schema(description = "User full name", example = "Rajesh Patel")
    private String fullName;

    @Schema(description = "Organization or Company name", example = "Patel Infotech Solutions")
    private String organization;

    @NotNull(message = "Role is required")
    @Schema(description = "User role in the DIXY platform", example = "ROLE_BIDDER")
    private Role role;
}
