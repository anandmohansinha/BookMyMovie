package com.example.movieticketbooking.service;

import com.example.movieticketbooking.dto.movie.MovieCreateRequest;
import com.example.movieticketbooking.dto.movie.MovieResponse;

import java.util.List;

public interface MovieService {

    MovieResponse createMovie(MovieCreateRequest request);

    List<MovieResponse> listMovies();
}
