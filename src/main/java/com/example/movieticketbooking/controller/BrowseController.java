package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.dto.browse.BrowseShowResponse;
import com.example.movieticketbooking.service.BrowseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/browse")
@Tag(name = "Browse", description = "Browse theatres and show timings")
public class BrowseController {

    private final BrowseService browseService;

    public BrowseController(BrowseService browseService) {
        this.browseService = browseService;
    }

    @GetMapping("/shows")
    @Operation(summary = "Browse theatres and show timings by movie, city, and date")
    public BrowseShowResponse browseShows(
            @RequestParam Long movieId,
            @RequestParam Long cityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate
    ) {
        return browseService.browseShows(movieId, cityId, showDate);
    }
}
