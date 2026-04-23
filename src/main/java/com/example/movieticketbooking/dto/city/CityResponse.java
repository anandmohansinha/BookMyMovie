package com.example.movieticketbooking.dto.city;

public record CityResponse(
        Long id,
        String name,
        String state,
        String country
) {
}
