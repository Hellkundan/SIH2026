package com.dixy.backend.service;

import com.dixy.backend.dto.AuthResponse;
import com.dixy.backend.dto.LoginRequest;
import com.dixy.backend.dto.RegisterRequest;
import com.dixy.backend.dto.UserProfileResponse;
import com.dixy.backend.entity.Role;
import com.dixy.backend.entity.User;
import com.dixy.backend.exception.DuplicateResourceException;
import com.dixy.backend.exception.ResourceNotFoundException;
import com.dixy.backend.repository.UserRepository;
import com.dixy.backend.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication and User Account Management Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final com.dixy.backend.audit.AuditService auditService;

    /**
     * Authenticate user with credentials and issue JWT
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for identifier: {}", request.getUsernameOrEmail());

        // Authenticate credentials using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();

        // Reset failed login attempts on successful login
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        String token = jwtUtils.generateToken(user);

        log.info("User '{}' successfully authenticated with role '{}'", user.getUsername(), user.getRole());

        auditService.recordEvent(
                user.getUsername(),
                user.getId(),
                com.dixy.backend.audit.AuditAction.LOGIN_SUCCESS,
                "User",
                String.valueOf(user.getId()),
                null,
                com.dixy.backend.audit.AuditStatus.SUCCESS,
                "User successfully logged in",
                null
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtUtils.getExpirationMs())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .organization(user.getOrganization())
                .role(user.getRole())
                .build();
    }

    /**
     * Register new user and issue JWT
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with username '{}' and role '{}'", request.getUsername(), request.getRole());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        // Security check: Self-registration as ROLE_ADMIN or ROLE_AUDITOR is restricted
        Role assignedRole = request.getRole();
        if (assignedRole == Role.ROLE_ADMIN || assignedRole == Role.ROLE_AUDITOR) {
            log.warn("Unauthorized attempt to self-register as administrative role: {}", assignedRole);
            throw new IllegalArgumentException("Self-registration as " + assignedRole + " is forbidden. Contact administrator.");
        }

        User newUser = User.builder()
                .username(request.getUsername().trim().toLowerCase())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .organization(request.getOrganization() != null ? request.getOrganization().trim() : null)
                .role(assignedRole)
                .enabled(true)
                .accountNonLocked(true)
                .failedLoginAttempts(0)
                .build();

        User savedUser = userRepository.save(newUser);
        String token = jwtUtils.generateToken(savedUser);

        log.info("Successfully registered user '{}' with ID {}", savedUser.getUsername(), savedUser.getId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtUtils.getExpirationMs())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .organization(savedUser.getOrganization())
                .role(savedUser.getRole())
                .build();
    }

    /**
     * Get profile of the currently authenticated user
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No active session or credentials found");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return UserProfileResponse.fromUser(user);
    }
}
