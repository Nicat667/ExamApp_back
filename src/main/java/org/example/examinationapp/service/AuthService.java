package org.example.examinationapp.service;

import lombok.RequiredArgsConstructor;
import org.example.examinationapp.dto.AuthResponse;
import org.example.examinationapp.dto.LoginRequest;
import org.example.examinationapp.dto.RegisterRequest;
import org.example.examinationapp.entity.ConfirmationToken;
import org.example.examinationapp.entity.User;
import org.example.examinationapp.enums.Role;
import org.example.examinationapp.repository.TokenRepository;
import org.example.examinationapp.repository.UserRepository;
import org.example.examinationapp.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already taken");
        }

        var user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(List.of(Role.Student)); // Ensure Role Enum is imported
        user.setEnabled(false);

        var savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setToken(token);
        confirmationToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        confirmationToken.setConfirmedAt(null);
        confirmationToken.setUser(savedUser);

        tokenRepository.save(confirmationToken);

        String link = "http://localhost:8080/api/auth/confirm?token=" + token;

        // Pass the raw data to EmailService, and let Thymeleaf handle the HTML!
        emailService.sendEmail(
                request.getEmail(),
                "Confirm your ExamPortal Account",
                user.getFullName(), // Passes the user's name to the template
                link                // Passes the link to the template
        );

        return AuthResponse.builder()
                .message("Registration successful. Check your email to confirm.")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()

                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .message("Login successful")
                .fullName(user.getFullName())
                .role(user.getRoles().getFirst().name())
                .build();
    }

    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            return "Email already confirmed";
        }

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        confirmationToken.setConfirmedAt(LocalDateTime.now());
        tokenRepository.save(confirmationToken);

        User user = confirmationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        return "Email confirmed successfully";
    }
}