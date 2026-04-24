package com.example.movieticketbooking.dto.response;

public record CustomerResponse(
        Long id,
        String name,
        String email,
        String phone
) {
}
