package com.example.movieticketbooking.dto.movie;

import com.example.movieticketbooking.enums.MovieStatus;

import java.time.LocalDate;

public record MovieResponse(
        Long id,
        String title,
        String language,
        String genre,
        Integer durationMinutes,
        String certification,
        String description,
        LocalDate releaseDate,
        MovieStatus status
) {
}
