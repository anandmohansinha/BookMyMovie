package com.example.movieticketbooking.repository;

import com.example.movieticketbooking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    boolean existsByScreen_IdAndShowDateAndStartTimeLessThanAndEndTimeGreaterThan(
            Long screenId,
            LocalDate showDate,
            LocalTime endTime,
            LocalTime startTime
    );

    @Query("""
            select s
            from Show s
            join fetch s.movie m
            join fetch s.screen sc
            join fetch sc.theatre t
            join fetch t.city c
            where m.id = :movieId
              and c.id = :cityId
              and s.showDate = :showDate
            order by t.name asc, s.startTime asc
            """)
    List<Show> findBrowseShows(
            @Param("movieId") Long movieId,
            @Param("cityId") Long cityId,
            @Param("showDate") LocalDate showDate
    );
}
