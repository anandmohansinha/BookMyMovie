package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.dto.browse.BrowseShowResponse;
import com.example.movieticketbooking.service.BrowseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/browse")
public class BrowseController {

    private final BrowseService browseService;

    public BrowseController(BrowseService browseService) {
        this.browseService = browseService;
    }

    @GetMapping("/shows")
    public BrowseShowResponse browseShows(
            @RequestParam Long movieId,
            @RequestParam Long cityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate
    ) {
        return browseService.browseShows(movieId, cityId, showDate);
    }
}
