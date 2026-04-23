package com.example.movieticketbooking.service.impl;

import com.example.movieticketbooking.dto.show.ShowCreateRequest;
import com.example.movieticketbooking.dto.show.ShowResponse;
import com.example.movieticketbooking.entity.Movie;
import com.example.movieticketbooking.entity.Screen;
import com.example.movieticketbooking.entity.Show;
import com.example.movieticketbooking.enums.MovieStatus;
import com.example.movieticketbooking.exception.BusinessValidationException;
import com.example.movieticketbooking.exception.ResourceNotFoundException;
import com.example.movieticketbooking.repository.MovieRepository;
import com.example.movieticketbooking.repository.ScreenRepository;
import com.example.movieticketbooking.repository.ShowRepository;
import com.example.movieticketbooking.service.ShowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShowServiceImpl implements ShowService {

    private static final Logger log = LoggerFactory.getLogger(ShowServiceImpl.class);

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    public ShowServiceImpl(ShowRepository showRepository, MovieRepository movieRepository, ScreenRepository screenRepository) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
    }

    @Override
    public ShowResponse createShow(ShowCreateRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BusinessValidationException("Show start time must be before end time");
        }

        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id " + request.movieId()));
        if (movie.getStatus() != MovieStatus.ACTIVE) {
            throw new BusinessValidationException("Only active movies can be scheduled");
        }

        Screen screen = screenRepository.findWithTheatreById(request.screenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id " + request.screenId()));

        boolean overlapping = showRepository.existsByScreen_IdAndShowDateAndStartTimeLessThanAndEndTimeGreaterThan(
                request.screenId(),
                request.showDate(),
                request.endTime(),
                request.startTime()
        );
        if (overlapping) {
            throw new BusinessValidationException("Show timing overlaps with an existing show on the same screen");
        }

        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(request.showDate());
        show.setStartTime(request.startTime());
        show.setEndTime(request.endTime());
        show.setTicketPrice(request.ticketPrice());

        Show savedShow = showRepository.save(show);
        log.info(
                "Created show with id={} movieId={} screenId={} date={} start={}",
                savedShow.getId(),
                movie.getId(),
                screen.getId(),
                savedShow.getShowDate(),
                savedShow.getStartTime()
        );
        return mapToResponse(savedShow);
    }

    private ShowResponse mapToResponse(Show show) {
        return new ShowResponse(
                show.getId(),
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                show.getScreen().getTheatre().getId(),
                show.getScreen().getTheatre().getName(),
                show.getScreen().getId(),
                show.getScreen().getName(),
                show.getShowDate(),
                show.getStartTime(),
                show.getEndTime(),
                show.getTicketPrice()
        );
    }
}
