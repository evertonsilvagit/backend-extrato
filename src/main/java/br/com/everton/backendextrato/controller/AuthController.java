package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.application.auth.port.in.GetProfileUseCase;
import br.com.everton.backendextrato.application.auth.port.in.LoginUseCase;
import br.com.everton.backendextrato.application.auth.port.in.RegisterUseCase;
import br.com.everton.backendextrato.application.auth.port.in.UpdateProfileUseCase;
import br.com.everton.backendextrato.application.auth.usecase.command.AuthCommand;
import br.com.everton.backendextrato.application.auth.usecase.command.UpdateProfileCommand;
import br.com.everton.backendextrato.application.auth.usecase.result.AuthSession;
import br.com.everton.backendextrato.domain.auth.UserProfile;
import br.com.everton.backendextrato.dto.AuthRequest;
import br.com.everton.backendextrato.dto.AuthResponse;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.dto.ProfileResponse;
import br.com.everton.backendextrato.dto.ProfileUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public AuthController(
            RegisterUseCase registerUseCase,
            LoginUseCase loginUseCase,
            GetProfileUseCase getProfileUseCase,
            UpdateProfileUseCase updateProfileUseCase,
            AuthenticatedUserResolver authenticatedUserResolver
    ) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.getProfileUseCase = getProfileUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    toAuthResponse(registerUseCase.execute(toAuthCommand(request)))
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(toAuthResponse(loginUseCase.execute(toAuthCommand(request))));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest httpServletRequest) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            return ResponseEntity.ok(toProfileResponse(getProfileUseCase.execute(user.email())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            return ResponseEntity.ok(
                    toAuthResponse(updateProfileUseCase.execute(user.email(), toUpdateProfileCommand(request)))
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    private AuthCommand toAuthCommand(AuthRequest request) {
        return new AuthCommand(
                request == null ? null : request.email(),
                request == null ? null : request.password(),
                request == null ? null : request.name(),
                request == null ? null : request.photo()
        );
    }

    private UpdateProfileCommand toUpdateProfileCommand(ProfileUpdateRequest request) {
        return new UpdateProfileCommand(
                request == null ? null : request.name(),
                request == null ? null : request.photo()
        );
    }

    private AuthResponse toAuthResponse(AuthSession session) {
        return new AuthResponse(session.token(), session.email(), session.name(), session.photo());
    }

    private ProfileResponse toProfileResponse(UserProfile userProfile) {
        return new ProfileResponse(userProfile.email(), userProfile.displayName(), userProfile.profileImageUrl());
    }
}
