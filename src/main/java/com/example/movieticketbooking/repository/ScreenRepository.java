package com.example.movieticketbooking.repository;

import com.example.movieticketbooking.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    @Query("""
            select s
            from Screen s
            join fetch s.theatre t
            join fetch t.city
            where s.id = :screenId
            """)
    Optional<Screen> findWithTheatreById(@Param("screenId") Long screenId);
}
