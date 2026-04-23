package com.example.movieticketbooking.service.impl;

import com.example.movieticketbooking.dto.browse.BrowseShowResponse;
import com.example.movieticketbooking.dto.browse.ShowTimingResponse;
import com.example.movieticketbooking.dto.browse.TheatreShowScheduleResponse;
import com.example.movieticketbooking.entity.Show;
import com.example.movieticketbooking.exception.ResourceNotFoundException;
import com.example.movieticketbooking.repository.CityRepository;
import com.example.movieticketbooking.repository.MovieRepository;
import com.example.movieticketbooking.repository.ShowRepository;
import com.example.movieticketbooking.service.BrowseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class BrowseServiceImpl implements BrowseService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final CityRepository cityRepository;

    public BrowseServiceImpl(ShowRepository showRepository, MovieRepository movieRepository, CityRepository cityRepository) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.cityRepository = cityRepository;
    }

    @Override
    public BrowseShowResponse browseShows(Long movieId, Long cityId, LocalDate showDate) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id " + movieId);
        }
        if (!cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException("City not found with id " + cityId);
        }

        List<Show> shows = showRepository.findBrowseShows(movieId, cityId, showDate);
        Map<Long, TheatreAccumulator> grouped = new LinkedHashMap<>();

        for (Show show : shows) {
            Long theatreId = show.getScreen().getTheatre().getId();
            TheatreAccumulator accumulator = grouped.computeIfAbsent(
                    theatreId,
                    key -> new TheatreAccumulator(
                            theatreId,
                            show.getScreen().getTheatre().getName(),
                            show.getScreen().getTheatre().getAddress()
                    )
            );
            accumulator.showTimings().add(new ShowTimingResponse(
                    show.getId(),
                    show.getScreen().getId(),
                    show.getScreen().getName(),
                    show.getStartTime(),
                    show.getEndTime(),
                    show.getTicketPrice()
            ));
        }

        List<TheatreShowScheduleResponse> theatres = grouped.values().stream()
                .map(accumulator -> new TheatreShowScheduleResponse(
                        accumulator.theatreId(),
                        accumulator.theatreName(),
                        accumulator.theatreAddress(),
                        accumulator.showTimings()
                ))
                .toList();

        return new BrowseShowResponse(cityId, movieId, showDate, theatres);
    }

    private record TheatreAccumulator(
            Long theatreId,
            String theatreName,
            String theatreAddress,
            List<ShowTimingResponse> showTimings
    ) {
        private TheatreAccumulator(Long theatreId, String theatreName, String theatreAddress) {
            this(theatreId, theatreName, theatreAddress, new ArrayList<>());
        }
    }
}
