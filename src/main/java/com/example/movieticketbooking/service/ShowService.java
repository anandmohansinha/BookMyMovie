package com.example.movieticketbooking.service;

import com.example.movieticketbooking.dto.show.ShowCreateRequest;
import com.example.movieticketbooking.dto.show.ShowResponse;

public interface ShowService {

    ShowResponse createShow(ShowCreateRequest request);
}
