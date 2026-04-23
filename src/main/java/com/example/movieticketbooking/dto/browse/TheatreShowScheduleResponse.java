package com.example.movieticketbooking.dto.browse;

import java.util.List;

public record TheatreShowScheduleResponse(
        Long theatreId,
        String theatreName,
        String theatreAddress,
        List<ShowTimingResponse> showTimings
) {
}
