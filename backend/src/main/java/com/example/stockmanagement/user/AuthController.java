package com.example.stockmanagement.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String USER_ID_ATTRIBUTE = "userId";

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken());
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already registered.");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That email is already registered.");
        }

        AppUser user = userRepository.save(new AppUser(
                request.username().trim(),
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.password())));
        startSession(servletRequest, user);
        return UserResponse.from(user);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(account -> passwordEncoder.matches(request.password(), account.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
        startSession(servletRequest, user);
        return UserResponse.from(user);
    }

    @GetMapping("/me")
    public UserResponse me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(USER_ID_ATTRIBUTE) instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in.");
        }

        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in."));
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private void startSession(HttpServletRequest request, AppUser user) {
        request.getSession(true);
        request.changeSessionId();
        request.getSession().setAttribute(USER_ID_ATTRIBUTE, user.getId());
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 40) String username,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record UserResponse(Long id, String username, String email) {
        static UserResponse from(AppUser user) {
            return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
        }
    }
}
