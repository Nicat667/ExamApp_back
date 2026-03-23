package org.example.examinationapp.service;

import lombok.RequiredArgsConstructor;
import org.example.examinationapp.dto.*;
import org.example.examinationapp.entity.Otp;
import org.example.examinationapp.entity.User;
import org.example.examinationapp.entity.Role;
import org.example.examinationapp.repository.OtpRepository;
import org.example.examinationapp.repository.UserRepository;
import org.example.examinationapp.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 3;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already taken");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(List.of(request.getRole()))
                .enabled(false)
                .build();

        User savedUser = userRepository.save(user);

        // Delete any previous OTPs for this user before creating a new one
        otpRepository.deleteAllByUserEmail(savedUser.getEmail());

        String code = generateOtpCode();

        Otp otp = Otp.builder()
                .code(code)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .used(false)
                .user(savedUser)
                .build();

        otpRepository.save(otp);

        emailService.sendOtpEmail(savedUser.getEmail(), savedUser.getFullName(), code);

        return AuthResponse.builder()
                .message("Registration successful. A verification code has been sent to your email.")
                .build();
    }

    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        Otp otp = otpRepository
                .findTopByUserEmailAndCodeAndUsedFalseOrderByIssuedAtDesc(
                        request.getEmail(), request.getCode()
                )
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP code"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP code has expired. Please register again to get a new code.");
        }

        // Mark OTP as used
        otp.setUsed(true);
        otpRepository.save(otp);

        // Enable the user
        User user = otp.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        return AuthResponse.builder()
                .message("Email verified successfully. You can now log in.")
                .build();
    }

    @Transactional
    public AuthResponse resendOtp(ResendOtp request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow( () -> new RuntimeException("User not found!"));

        if (!user.isEnabled()) {
            otpRepository.deleteAllByUserEmail(user.getEmail());

            String code = generateOtpCode();

            Otp otp = Otp.builder()
                    .code(code)
                    .issuedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                    .used(false)
                    .user(user)
                    .build();

            otpRepository.save(otp);

            emailService.sendOtpEmail(user.getEmail(), user.getFullName(), code);
        }
        else{
            throw new RuntimeException("User is already enabled");
        }

        return AuthResponse.builder()
                .message("A new verification code has been sent to your email.")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .message("Login successful")
                .fullName(user.getFullName())
                .role(user.getRoles().getFirst().name())
                .build();
    }

    private String generateOtpCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // always 6 digits
        return String.valueOf(code);
    }
}