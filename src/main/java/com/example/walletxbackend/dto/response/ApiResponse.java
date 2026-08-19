package com.example.walletxbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Standard API response wrapper used across the WalletX backend.
 *
 * <p>Fields:
 * <ul>
 *   <li><strong>status</strong> – HTTP status code (e.g., 200, 400).</li>
 *   <li><strong>message</strong> – Human‑readable description of the outcome.</li>
 *   <li><strong>data</strong> – Payload of the response; null for error cases.</li>
 *   <li><strong>timestamp</strong> – Time the response was created (UTC).</li>
 * </ul>
 * </p>
 *
 * <p>Factory methods are provided for common success and error responses.
 * This class is a {@code record} (Java 21) which works nicely with Jackson.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(int status, String message, T data, LocalDateTime timestamp) {

    /**
     * Create a successful response (HTTP 200) with the given payload.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data, LocalDateTime.now());
    }

    /**
     * Create an error response with a custom status and message.
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null, LocalDateTime.now());
    }
}
