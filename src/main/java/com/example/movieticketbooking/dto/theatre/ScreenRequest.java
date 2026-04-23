package com.example.movieticketbooking.dto.theatre;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ScreenRequest(
        @NotBlank(message = "Screen name is required")
        @Size(max = 100, message = "Screen name must be at most 100 characters")
        String name,

        @NotEmpty(message = "At least one seat is required for a screen")
        List<@Valid SeatRequest> seats
) {
}
