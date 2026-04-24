package com.example.movieticketbooking.service.impl;

import com.example.movieticketbooking.dto.show.ShowCreateRequest;
import com.example.movieticketbooking.dto.response.ShowSeatLayoutResponse;
import com.example.movieticketbooking.dto.show.ShowResponse;
import com.example.movieticketbooking.entity.Movie;
import com.example.movieticketbooking.entity.Screen;
import com.example.movieticketbooking.entity.Show;
import com.example.movieticketbooking.entity.ShowSeatInventory;
import com.example.movieticketbooking.enums.MovieStatus;
import com.example.movieticketbooking.enums.ShowSeatStatus;
import com.example.movieticketbooking.exception.BusinessValidationException;
import com.example.movieticketbooking.exception.ResourceNotFoundException;
import com.example.movieticketbooking.mapper.ShowMapper;
import com.example.movieticketbooking.repository.CityRepository;
import com.example.movieticketbooking.repository.MovieRepository;
import com.example.movieticketbooking.repository.ScreenRepository;
import com.example.movieticketbooking.repository.ShowRepository;
import com.example.movieticketbooking.repository.ShowSeatInventoryRepository;
import com.example.movieticketbooking.service.ShowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ShowServiceImpl implements ShowService {

    private static final Logger log = LoggerFactory.getLogger(ShowServiceImpl.class);

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final ShowSeatInventoryRepository showSeatInventoryRepository;
    private final CityRepository cityRepository;
    private final ShowMapper showMapper;

    public ShowServiceImpl(
            ShowRepository showRepository,
            MovieRepository movieRepository,
            ScreenRepository screenRepository,
            ShowSeatInventoryRepository showSeatInventoryRepository,
            CityRepository cityRepository,
            ShowMapper showMapper
    ) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
        this.showSeatInventoryRepository = showSeatInventoryRepository;
        this.cityRepository = cityRepository;
        this.showMapper = showMapper;
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
        show.setActive(Boolean.TRUE);

        Show savedShow = showRepository.save(show);
        List<ShowSeatInventory> inventories = screen.getSeats().stream()
                .map(seat -> {
                    ShowSeatInventory inventory = new ShowSeatInventory();
                    inventory.setShow(savedShow);
                    inventory.setSeat(seat);
                    inventory.setStatus(ShowSeatStatus.AVAILABLE);
                    return inventory;
                })
                .toList();
        showSeatInventoryRepository.saveAll(inventories);
        log.info(
                "Created show with id={} movieId={} screenId={} date={} start={} and inventoryCount={}",
                savedShow.getId(),
                movie.getId(),
                screen.getId(),
                savedShow.getShowDate(),
                savedShow.getStartTime(),
                inventories.size()
        );
        return showMapper.toShowResponse(savedShow);
    }

    @Override
    @Transactional(readOnly = true)
    public ShowResponse getShowById(Long showId) {
        Show show = showRepository.findDetailsById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id " + showId));
        return showMapper.toShowResponse(show);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowResponse> listShows(Long movieId, Long cityId, LocalDate showDate) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id " + movieId);
        }
        if (!cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException("City not found with id " + cityId);
        }
        List<Show> shows = showRepository.findBrowseShows(movieId, cityId, showDate);
        return shows.stream()
                .map(showMapper::toShowResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ShowSeatLayoutResponse getShowSeats(Long showId) {
        List<ShowSeatInventory> inventories = showSeatInventoryRepository.findSeatLayoutByShowId(showId);
        if (inventories.isEmpty()) {
            throw new ResourceNotFoundException("Show not found with id " + showId);
        }
        return showMapper.toSeatLayoutResponse(inventories.get(0).getShow(), inventories);
    }
}
