package org.example.examinationapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.examinationapp.dto.AuthResponse;
import org.example.examinationapp.dto.LoginRequest;
import org.example.examinationapp.dto.RegisterRequest;
import org.example.examinationapp.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;

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
    /*public ResponseEntity<Void> confirm(@RequestParam("token") String token) {
        // 1. Mark the user as enabled in the database
        authService.confirmToken(token);

        // 2. The URL of your React frontend Login page with a success flag
        String reactLoginUrl = "http://localhost:5173/login?verified=true";

        // 3. Send a 302 Redirect back to the browser
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(reactLoginUrl))
                .build();
    }*/

    public ModelAndView confirm(@RequestParam("token") String token) {
        ModelAndView mav = new ModelAndView("confirm-result"); // This tells Spring to look for 'confirm-result.html'

        try {
            String message = authService.confirmToken(token);
            mav.addObject("success", true);
            mav.addObject("message", "Thank you! " + message + ". You can now securely log in to your account.");
        } catch (RuntimeException e) {
            // If the token is expired or fake, we show the error nicely on the same page
            mav.addObject("success", false);
            mav.addObject("message", e.getMessage());
        }

        return mav;
    }
}