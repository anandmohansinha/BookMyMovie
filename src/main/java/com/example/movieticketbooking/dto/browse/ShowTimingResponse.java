package com.example.movieticketbooking.dto.browse;

import java.math.BigDecimal;
import java.time.LocalTime;

public record ShowTimingResponse(
        Long showId,
        Long screenId,
        String screenName,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal ticketPrice
) {
}
