package com.example.movieticketbooking.service.impl;

import com.example.movieticketbooking.dto.movie.MovieCreateRequest;
import com.example.movieticketbooking.dto.movie.MovieResponse;
import com.example.movieticketbooking.entity.Movie;
import com.example.movieticketbooking.repository.MovieRepository;
import com.example.movieticketbooking.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MovieServiceImpl implements MovieService {

    private static final Logger log = LoggerFactory.getLogger(MovieServiceImpl.class);

    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public MovieResponse createMovie(MovieCreateRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.title().trim());
        movie.setLanguage(request.language().trim());
        movie.setGenre(request.genre().trim());
        movie.setDurationMinutes(request.durationMinutes());
        movie.setCertification(request.certification());
        movie.setDescription(request.description());
        movie.setReleaseDate(request.releaseDate());
        movie.setStatus(request.status());

        Movie savedMovie = movieRepository.save(movie);
        log.info("Created movie with id={} title={} status={}", savedMovie.getId(), savedMovie.getTitle(), savedMovie.getStatus());
        return mapToResponse(savedMovie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> listMovies() {
        return movieRepository.findAllByOrderByTitleAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private MovieResponse mapToResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getLanguage(),
                movie.getGenre(),
                movie.getDurationMinutes(),
                movie.getCertification(),
                movie.getDescription(),
                movie.getReleaseDate(),
                movie.getStatus()
        );
    }
}
