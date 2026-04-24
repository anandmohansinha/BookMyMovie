package com.example.movieticketbooking.dto.response;

import com.example.movieticketbooking.enums.SeatCategory;

public record LockedSeatResponse(
        Long inventoryId,
        Long seatId,
        String seatLabel,
        String rowLabel,
        Integer seatNumber,
        SeatCategory seatCategory
) {
}
