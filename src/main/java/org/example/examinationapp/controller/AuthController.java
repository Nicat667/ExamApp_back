package org.example.examinationapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.examinationapp.dto.AuthResponse;
import org.example.examinationapp.dto.LoginRequest;
import org.example.examinationapp.dto.RegisterRequest;
import org.example.examinationapp.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin // Enables React Frontend to talk to this backend
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

    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.confirmToken(token));
    }
}