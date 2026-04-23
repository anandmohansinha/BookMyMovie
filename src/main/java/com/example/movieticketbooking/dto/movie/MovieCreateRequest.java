package com.example.movieticketbooking.dto.movie;

import com.example.movieticketbooking.enums.MovieStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MovieCreateRequest(
        @NotBlank(message = "Movie title is required")
        @Size(max = 150, message = "Movie title must be at most 150 characters")
        String title,

        @NotBlank(message = "Language is required")
        @Size(max = 50, message = "Language must be at most 50 characters")
        String language,

        @NotBlank(message = "Genre is required")
        @Size(max = 50, message = "Genre must be at most 50 characters")
        String genre,

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be at least 1 minute")
        Integer durationMinutes,

        @Size(max = 20, message = "Certification must be at most 20 characters")
        String certification,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        LocalDate releaseDate,

        @NotNull(message = "Movie status is required")
        MovieStatus status
) {
}
