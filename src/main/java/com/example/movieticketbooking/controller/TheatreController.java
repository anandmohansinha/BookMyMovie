package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.dto.theatre.TheatreCreateRequest;
import com.example.movieticketbooking.dto.theatre.TheatreResponse;
import com.example.movieticketbooking.service.TheatreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
@Tag(name = "Theatres", description = "Create and list theatres with screens and seats")
public class TheatreController {

    private final TheatreService theatreService;

    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TheatreResponse createTheatre(@Valid @RequestBody TheatreCreateRequest request) {
        return theatreService.createTheatre(request);
    }

    @GetMapping
    public List<TheatreResponse> listTheatres(@RequestParam(required = false) Long cityId) {
        return theatreService.listTheatres(cityId);
    }
}
