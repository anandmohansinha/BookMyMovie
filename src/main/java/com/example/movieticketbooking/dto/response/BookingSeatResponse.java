package com.example.movieticketbooking.dto.response;

import com.example.movieticketbooking.enums.SeatCategory;

import java.math.BigDecimal;

public record BookingSeatResponse(
        Long seatId,
        String seatLabel,
        SeatCategory seatCategory,
        BigDecimal price
) {
}
