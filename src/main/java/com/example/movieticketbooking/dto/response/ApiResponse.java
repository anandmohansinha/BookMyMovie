package com.example.movieticketbooking.dto.response;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
        OffsetDateTime timestamp,
        int status,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(OffsetDateTime.now(), status, message, data);
    }

    public static <T> ApiResponse<T> failure(int status, String message, T data) {
        return new ApiResponse<>(OffsetDateTime.now(), status, message, data);
    }
}
