package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.dto.movie.MovieCreateRequest;
import com.example.movieticketbooking.dto.movie.MovieResponse;
import com.example.movieticketbooking.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@Tag(name = "Movies", description = "Create and list movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse createMovie(@Valid @RequestBody MovieCreateRequest request) {
        return movieService.createMovie(request);
    }

    @GetMapping
    public List<MovieResponse> listMovies() {
        return movieService.listMovies();
    }
}
