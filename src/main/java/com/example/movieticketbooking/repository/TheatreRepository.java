package com.example.movieticketbooking.repository;

import com.example.movieticketbooking.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    boolean existsByNameIgnoreCaseAndCityId(String name, Long cityId);

    @Query("""
            select distinct t
            from Theatre t
            join fetch t.city c
            left join fetch t.screens s
            left join fetch s.seats
            order by t.name asc
            """)
    List<Theatre> findAllWithDetails();

    @Query("""
            select distinct t
            from Theatre t
            join fetch t.city c
            left join fetch t.screens s
            left join fetch s.seats
            where c.id = :cityId
            order by t.name asc
            """)
    List<Theatre> findAllWithDetailsByCityId(@Param("cityId") Long cityId);
}
