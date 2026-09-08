package com.dixy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login credentials request payload")
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Schema(description = "Registered username or email address", example = "admin")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Schema(description = "Account password", example = "Admin@123")
    @ToString.Exclude
    private String password;
}
