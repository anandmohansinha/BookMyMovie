package com.example.movieticketbooking.service.impl;

import com.example.movieticketbooking.dto.city.CityCreateRequest;
import com.example.movieticketbooking.dto.city.CityResponse;
import com.example.movieticketbooking.entity.City;
import com.example.movieticketbooking.exception.BusinessValidationException;
import com.example.movieticketbooking.repository.CityRepository;
import com.example.movieticketbooking.service.CityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CityServiceImpl implements CityService {

    private static final Logger log = LoggerFactory.getLogger(CityServiceImpl.class);

    private final CityRepository cityRepository;

    public CityServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public CityResponse createCity(CityCreateRequest request) {
        if (cityRepository.existsByNameIgnoreCaseAndStateIgnoreCase(request.name().trim(), request.state().trim())) {
            throw new BusinessValidationException("City already exists for the given state");
        }

        City city = new City();
        city.setName(request.name().trim());
        city.setState(request.state().trim());
        city.setCountry(request.country().trim());

        City savedCity = cityRepository.save(city);
        log.info("Created city with id={} name={}", savedCity.getId(), savedCity.getName());
        return mapToResponse(savedCity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityResponse> listCities() {
        return cityRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CityResponse mapToResponse(City city) {
        return new CityResponse(city.getId(), city.getName(), city.getState(), city.getCountry());
    }
}
