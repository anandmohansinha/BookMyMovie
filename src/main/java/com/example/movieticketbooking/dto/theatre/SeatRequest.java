package com.example.movieticketbooking.dto.theatre;

import com.example.movieticketbooking.enums.SeatType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SeatRequest(
        @NotBlank(message = "Seat row label is required")
        @Size(max = 10, message = "Seat row label must be at most 10 characters")
        String rowLabel,

        @NotNull(message = "Seat number is required")
        @Min(value = 1, message = "Seat number must be at least 1")
        Integer seatNumber,

        @NotNull(message = "Seat type is required")
        SeatType seatType
) {
}
