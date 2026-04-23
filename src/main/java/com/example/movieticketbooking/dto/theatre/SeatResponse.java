package com.example.movieticketbooking.dto.theatre;

import com.example.movieticketbooking.enums.SeatType;

public record SeatResponse(
        Long id,
        String rowLabel,
        Integer seatNumber,
        SeatType seatType,
        Boolean active
) {
}
