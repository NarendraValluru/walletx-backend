package com.example.walletxbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for user login.
 */
public record LoginRequest(
        @NotBlank @Size(min = 3, max = 50) String usernameOrEmail,
        @NotBlank @Size(min = 6, max = 100) String password) {
}
