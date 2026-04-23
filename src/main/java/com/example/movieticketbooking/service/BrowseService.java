package com.example.movieticketbooking.service;

import com.example.movieticketbooking.dto.browse.BrowseShowResponse;

import java.time.LocalDate;

public interface BrowseService {

    BrowseShowResponse browseShows(Long movieId, Long cityId, LocalDate showDate);
}
