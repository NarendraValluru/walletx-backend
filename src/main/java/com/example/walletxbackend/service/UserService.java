package com.example.walletxbackend.service;

import com.example.walletxbackend.dto.request.LoginRequest;
import com.example.walletxbackend.dto.request.SignupRequest;
import com.example.walletxbackend.dto.response.AuthResponse;

/**
 * Service handling user registration, authentication and logout.
 */
public interface UserService {
    AuthResponse register(SignupRequest request);
    AuthResponse login(LoginRequest request);
    void logout(String username);
}
