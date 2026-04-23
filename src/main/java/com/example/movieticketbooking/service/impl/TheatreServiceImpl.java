package com.example.movieticketbooking.service.impl;

import com.example.movieticketbooking.dto.city.CityResponse;
import com.example.movieticketbooking.dto.theatre.ScreenRequest;
import com.example.movieticketbooking.dto.theatre.ScreenResponse;
import com.example.movieticketbooking.dto.theatre.SeatRequest;
import com.example.movieticketbooking.dto.theatre.SeatResponse;
import com.example.movieticketbooking.dto.theatre.TheatreCreateRequest;
import com.example.movieticketbooking.dto.theatre.TheatreResponse;
import com.example.movieticketbooking.entity.City;
import com.example.movieticketbooking.entity.Screen;
import com.example.movieticketbooking.entity.Seat;
import com.example.movieticketbooking.entity.Theatre;
import com.example.movieticketbooking.exception.BusinessValidationException;
import com.example.movieticketbooking.exception.ResourceNotFoundException;
import com.example.movieticketbooking.repository.CityRepository;
import com.example.movieticketbooking.repository.TheatreRepository;
import com.example.movieticketbooking.service.TheatreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TheatreServiceImpl implements TheatreService {

    private static final Logger log = LoggerFactory.getLogger(TheatreServiceImpl.class);

    private final TheatreRepository theatreRepository;
    private final CityRepository cityRepository;

    public TheatreServiceImpl(TheatreRepository theatreRepository, CityRepository cityRepository) {
        this.theatreRepository = theatreRepository;
        this.cityRepository = cityRepository;
    }

    @Override
    public TheatreResponse createTheatre(TheatreCreateRequest request) {
        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id " + request.cityId()));

        if (theatreRepository.existsByNameIgnoreCaseAndCityId(request.name().trim(), request.cityId())) {
            throw new BusinessValidationException("Theatre already exists in the selected city");
        }

        validateUniqueScreenNames(request.screens());

        Theatre theatre = new Theatre();
        theatre.setName(request.name().trim());
        theatre.setAddress(request.address().trim());
        theatre.setCity(city);
        theatre.setActive(Boolean.TRUE);

        for (ScreenRequest screenRequest : request.screens()) {
            validateUniqueSeats(screenRequest);
            Screen screen = new Screen();
            screen.setName(screenRequest.name().trim());

            for (SeatRequest seatRequest : screenRequest.seats()) {
                Seat seat = new Seat();
                seat.setRowLabel(seatRequest.rowLabel().trim());
                seat.setSeatNumber(seatRequest.seatNumber());
                seat.setSeatType(seatRequest.seatType());
                seat.setActive(Boolean.TRUE);
                screen.addSeat(seat);
            }
            theatre.addScreen(screen);
        }

        Theatre savedTheatre = theatreRepository.save(theatre);
        log.info("Created theatre with id={} name={} cityId={}", savedTheatre.getId(), savedTheatre.getName(), city.getId());
        return mapToResponse(savedTheatre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TheatreResponse> listTheatres(Long cityId) {
        List<Theatre> theatres = cityId == null
                ? theatreRepository.findAllWithDetails()
                : theatreRepository.findAllWithDetailsByCityId(cityId);

        return theatres.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateUniqueScreenNames(List<ScreenRequest> screenRequests) {
        Set<String> seen = new HashSet<>();
        for (ScreenRequest screenRequest : screenRequests) {
            String normalized = screenRequest.name().trim().toLowerCase();
            if (!seen.add(normalized)) {
                throw new BusinessValidationException("Duplicate screen name found in theatre request: " + screenRequest.name());
            }
        }
    }

    private void validateUniqueSeats(ScreenRequest screenRequest) {
        Set<String> seen = new HashSet<>();
        for (SeatRequest seatRequest : screenRequest.seats()) {
            String key = seatRequest.rowLabel().trim().toUpperCase() + "-" + seatRequest.seatNumber();
            if (!seen.add(key)) {
                throw new BusinessValidationException("Duplicate seat found in screen " + screenRequest.name() + ": " + key);
            }
        }
    }

    private TheatreResponse mapToResponse(Theatre theatre) {
        City city = theatre.getCity();
        CityResponse cityResponse = new CityResponse(city.getId(), city.getName(), city.getState(), city.getCountry());

        List<ScreenResponse> screenResponses = theatre.getScreens().stream()
                .map(screen -> new ScreenResponse(
                        screen.getId(),
                        screen.getName(),
                        screen.getSeats().size(),
                        screen.getSeats().stream()
                                .map(seat -> new SeatResponse(
                                        seat.getId(),
                                        seat.getRowLabel(),
                                        seat.getSeatNumber(),
                                        seat.getSeatType(),
                                        seat.getActive()
                                ))
                                .toList()
                ))
                .toList();

        return new TheatreResponse(
                theatre.getId(),
                theatre.getName(),
                theatre.getAddress(),
                theatre.getActive(),
                cityResponse,
                screenResponses
        );
    }
}
