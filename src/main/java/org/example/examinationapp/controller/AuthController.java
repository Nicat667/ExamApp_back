package org.example.examinationapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.examinationapp.dto.*;
import org.example.examinationapp.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//@CrossOrigin
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<AuthResponse> resendOtp(@RequestBody ResendOtp request) {
        return ResponseEntity.ok(authService.resendOtp(request));
    }
}