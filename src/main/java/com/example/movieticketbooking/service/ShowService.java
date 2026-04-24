package com.example.movieticketbooking.service;

import com.example.movieticketbooking.dto.show.ShowCreateRequest;
import com.example.movieticketbooking.dto.response.ShowSeatLayoutResponse;
import com.example.movieticketbooking.dto.show.ShowResponse;

import java.time.LocalDate;
import java.util.List;

public interface ShowService {

    ShowResponse createShow(ShowCreateRequest request);

    ShowResponse getShowById(Long showId);

    List<ShowResponse> listShows(Long movieId, Long cityId, LocalDate showDate);

    ShowSeatLayoutResponse getShowSeats(Long showId);
}
