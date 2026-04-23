package com.example.movieticketbooking.dto.show;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowCreateRequest(
        @NotNull(message = "Movie id is required")
        Long movieId,

        @NotNull(message = "Screen id is required")
        Long screenId,

        @NotNull(message = "Show date is required")
        LocalDate showDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @NotNull(message = "Ticket price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Ticket price must be greater than 0")
        BigDecimal ticketPrice
) {
}
