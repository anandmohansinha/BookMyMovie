package com.example.movieticketbooking.dto.show;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowResponse(
        Long id,
        Long movieId,
        String movieTitle,
        Long theatreId,
        String theatreName,
        Long screenId,
        String screenName,
        LocalDate showDate,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal ticketPrice
) {
}
