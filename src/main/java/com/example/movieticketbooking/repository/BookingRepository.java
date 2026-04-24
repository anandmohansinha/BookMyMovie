package com.example.movieticketbooking.repository;

import com.example.movieticketbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByLockReference(String lockReference);

    @Query("""
            select distinct b
            from Booking b
            join fetch b.customer
            join fetch b.show s
            join fetch s.movie
            join fetch s.screen sc
            join fetch sc.theatre t
            join fetch t.city
            left join fetch b.bookingSeats bs
            left join fetch bs.seat
            where b.id = :bookingId
            """)
    Optional<Booking> findDetailedById(@Param("bookingId") Long bookingId);
}
