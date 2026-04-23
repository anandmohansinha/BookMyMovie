package com.example.movieticketbooking.dto.theatre;

import com.example.movieticketbooking.dto.city.CityResponse;

import java.util.List;

public record TheatreResponse(
        Long id,
        String name,
        String address,
        Boolean active,
        CityResponse city,
        List<ScreenResponse> screens
) {
}
