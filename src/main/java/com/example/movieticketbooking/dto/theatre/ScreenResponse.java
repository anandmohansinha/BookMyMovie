package com.example.movieticketbooking.dto.theatre;

import java.util.List;

public record ScreenResponse(
        Long id,
        String name,
        Integer totalSeats,
        List<SeatResponse> seats
) {
}
