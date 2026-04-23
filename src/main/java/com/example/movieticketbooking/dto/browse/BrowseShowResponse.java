package com.example.movieticketbooking.dto.browse;

import java.time.LocalDate;
import java.util.List;

public record BrowseShowResponse(
        Long cityId,
        Long movieId,
        LocalDate showDate,
        List<TheatreShowScheduleResponse> theatres
) {
}
