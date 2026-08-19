package com.example.walletxbackend.controller;

import com.example.walletxbackend.dto.request.LoginRequest;
import com.example.walletxbackend.dto.request.SignupRequest;
import com.example.walletxbackend.dto.response.AuthResponse;
import com.example.walletxbackend.service.UserService;
import com.example.walletxbackend.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints: signup, login, logout.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse authResponse = userService.register(request);
        // Return a simple success message including the token
        return ResponseEntity.ok("Signup successful: " + authResponse.token());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = userService.login(request);
        // Return a simple success message including the token
        return ResponseEntity.ok("Login successful: " + authResponse.token());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Authorization token is required");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        if (username != null) {
            userService.logout(username);
        }
        return ResponseEntity.ok("Logout successful");
    }
}
