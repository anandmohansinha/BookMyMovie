package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.dto.response.ApiResponse;
import com.example.movieticketbooking.dto.response.ShowSeatLayoutResponse;
import com.example.movieticketbooking.dto.show.ShowCreateRequest;
import com.example.movieticketbooking.dto.show.ShowResponse;
import com.example.movieticketbooking.service.ShowService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@Tag(name = "Shows", description = "Create shows for active movies")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShowResponse>> createShow(@Valid @RequestBody ShowCreateRequest request) {
        ShowResponse response = showService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Show created successfully", response));
    }

    @GetMapping("/{showId}")
    public ResponseEntity<ApiResponse<ShowResponse>> getShowById(@PathVariable Long showId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Show fetched successfully", showService.getShowById(showId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShowResponse>>> listShows(
            @RequestParam Long movieId,
            @RequestParam Long cityId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Shows fetched successfully",
                showService.listShows(movieId, cityId, showDate)
        ));
    }

    @GetMapping("/{showId}/seats")
    public ResponseEntity<ApiResponse<ShowSeatLayoutResponse>> getShowSeats(@PathVariable Long showId) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Show seat layout fetched successfully",
                showService.getShowSeats(showId)
        ));
    }
}
