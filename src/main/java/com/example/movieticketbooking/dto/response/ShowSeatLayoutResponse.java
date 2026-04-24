package com.example.movieticketbooking.dto.response;

import java.util.List;

public record ShowSeatLayoutResponse(
        ShowSummaryResponse show,
        List<SeatRowResponse> rows
) {
}
