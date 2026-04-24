package com.example.movieticketbooking.dto.response;

import com.example.movieticketbooking.enums.SeatCategory;
import com.example.movieticketbooking.enums.ShowSeatStatus;

public record SeatAvailabilityResponse(
        Long inventoryId,
        Long seatId,
        String seatLabel,
        Integer seatNumber,
        SeatCategory seatCategory,
        ShowSeatStatus status
) {
}
