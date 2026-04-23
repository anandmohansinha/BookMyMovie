package com.example.movieticketbooking.dto.city;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CityCreateRequest(
        @NotBlank(message = "City name is required")
        @Size(max = 100, message = "City name must be at most 100 characters")
        String name,

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State must be at most 100 characters")
        String state,

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must be at most 100 characters")
        String country
) {
}
