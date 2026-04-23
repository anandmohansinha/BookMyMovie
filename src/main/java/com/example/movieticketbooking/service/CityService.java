package com.example.movieticketbooking.service;

import com.example.movieticketbooking.dto.city.CityCreateRequest;
import com.example.movieticketbooking.dto.city.CityResponse;

import java.util.List;

public interface CityService {

    CityResponse createCity(CityCreateRequest request);

    List<CityResponse> listCities();
}
