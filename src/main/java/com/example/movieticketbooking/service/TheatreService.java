package com.example.movieticketbooking.service;

import com.example.movieticketbooking.dto.theatre.TheatreCreateRequest;
import com.example.movieticketbooking.dto.theatre.TheatreResponse;

import java.util.List;

public interface TheatreService {

    TheatreResponse createTheatre(TheatreCreateRequest request);

    List<TheatreResponse> listTheatres(Long cityId);
}
