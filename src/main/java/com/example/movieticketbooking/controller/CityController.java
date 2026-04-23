package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.dto.city.CityCreateRequest;
import com.example.movieticketbooking.dto.city.CityResponse;
import com.example.movieticketbooking.service.CityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CityResponse createCity(@Valid @RequestBody CityCreateRequest request) {
        return cityService.createCity(request);
    }

    @GetMapping
    public List<CityResponse> listCities() {
        return cityService.listCities();
    }
}
