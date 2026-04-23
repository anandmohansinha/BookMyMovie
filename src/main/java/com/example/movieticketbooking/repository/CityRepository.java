package com.example.movieticketbooking.repository;

import com.example.movieticketbooking.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {

    boolean existsByNameIgnoreCaseAndStateIgnoreCase(String name, String state);

    List<City> findAllByOrderByNameAsc();
}
