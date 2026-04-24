package com.example.movieticketbooking.repository;

import com.example.movieticketbooking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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
            where s.id = :showId
              and s.active = true
            """)
    Optional<Show> findActiveById(@Param("showId") Long showId);

    @Query("""
            select s
            from Show s
            join fetch s.movie m
            join fetch s.screen sc
            join fetch sc.theatre t
            join fetch t.city c
            where s.id = :showId
            """)
    Optional<Show> findDetailsById(@Param("showId") Long showId);

    @Query("""
            select distinct s
            from Show s
            join fetch s.movie m
            join fetch s.screen sc
            join fetch sc.theatre t
            join fetch t.city c
            left join fetch sc.seats seats
            where s.id = :showId
            """)
    Optional<Show> findDetailsWithSeatsById(@Param("showId") Long showId);

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
              and s.active = true
            order by t.name asc, s.startTime asc
            """)
    List<Show> findBrowseShows(
            @Param("movieId") Long movieId,
            @Param("cityId") Long cityId,
            @Param("showDate") LocalDate showDate
    );
}
