package backend.controller;

import backend.dto.request.LoginRequest;
import backend.dto.response.ApiResponse;
import backend.dto.response.LoginResponse;
import backend.security.AppUser;
import backend.security.JwtService;
import backend.security.UserStorage;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserStorage userStorage;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public AuthController(
            UserStorage userStorage,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {

        this.userStorage = userStorage;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AppUser user = userStorage.findByUsername(request.getUsername());

        if (user == null || !passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
                        throw new BadCredentialsException("Invalid username or password");
        }

        LoginResponse loginResponse = new LoginResponse(
                jwtService.generateToken(user),
                user.getUsername(),
                user.getRole()
        );

        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                loginResponse,
                "Login successful"
        );

        return ResponseEntity.ok(apiResponse);
    }
}