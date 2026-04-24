package com.example.movieticketbooking.dto.response;

import java.util.List;

public record SeatRowResponse(
        String rowLabel,
        List<SeatAvailabilityResponse> seats
) {
}
