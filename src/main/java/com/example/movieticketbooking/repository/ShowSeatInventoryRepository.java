package com.example.movieticketbooking.repository;

import com.example.movieticketbooking.entity.ShowSeatInventory;
import com.example.movieticketbooking.enums.ShowSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowSeatInventoryRepository extends JpaRepository<ShowSeatInventory, Long> {

    @Query("""
            select i
            from ShowSeatInventory i
            join fetch i.show s
            join fetch s.movie
            join fetch s.screen sc
            join fetch sc.theatre t
            join fetch t.city
            join fetch i.seat seat
            where s.id = :showId
            order by seat.rowLabel asc, seat.seatNumber asc
            """)
    List<ShowSeatInventory> findSeatLayoutByShowId(@Param("showId") Long showId);

    @Query("""
            select i
            from ShowSeatInventory i
            join fetch i.show s
            join fetch s.movie
            join fetch s.screen sc
            join fetch sc.theatre t
            join fetch t.city
            join fetch i.seat seat
            where i.lockReference = :lockReference
            order by seat.rowLabel asc, seat.seatNumber asc
            """)
    List<ShowSeatInventory> findByLockReferenceWithDetails(@Param("lockReference") String lockReference);

    @Query("""
            select i
            from ShowSeatInventory i
            join fetch i.show s
            join fetch s.movie
            join fetch s.screen sc
            join fetch sc.theatre t
            join fetch t.city
            join fetch i.seat seat
            where s.id = :showId
              and seat.id in :seatIds
            order by seat.rowLabel asc, seat.seatNumber asc
            """)
    List<ShowSeatInventory> findByShowIdAndSeatIds(@Param("showId") Long showId, @Param("seatIds") List<Long> seatIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ShowSeatInventory i
            set i.status = :lockedStatus,
                i.lockedBy = :lockedBy,
                i.lockReference = :lockReference,
                i.lockExpiryTime = :expiresAt,
                i.updatedAt = :updatedAt
            where i.show.id = :showId
              and i.seat.id in :seatIds
              and i.status = :availableStatus
            """)
    int lockSeats(
            @Param("showId") Long showId,
            @Param("seatIds") List<Long> seatIds,
            @Param("availableStatus") ShowSeatStatus availableStatus,
            @Param("lockedStatus") ShowSeatStatus lockedStatus,
            @Param("lockedBy") String lockedBy,
            @Param("lockReference") String lockReference,
            @Param("expiresAt") LocalDateTime expiresAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ShowSeatInventory i
            set i.status = :availableStatus,
                i.lockedBy = null,
                i.lockReference = null,
                i.lockExpiryTime = null,
                i.updatedAt = :updatedAt
            where i.show.id = :showId
              and i.status = :lockedStatus
              and i.lockExpiryTime < :now
            """)
    int releaseExpiredLocksForShow(
            @Param("showId") Long showId,
            @Param("lockedStatus") ShowSeatStatus lockedStatus,
            @Param("availableStatus") ShowSeatStatus availableStatus,
            @Param("now") LocalDateTime now,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ShowSeatInventory i
            set i.status = :bookedStatus,
                i.lockedBy = null,
                i.lockReference = null,
                i.lockExpiryTime = null,
                i.updatedAt = :updatedAt
            where i.show.id = :showId
              and i.seat.id in :seatIds
              and i.status = :lockedStatus
              and i.lockReference = :lockReference
            """)
    int markSeatsBooked(
            @Param("showId") Long showId,
            @Param("seatIds") List<Long> seatIds,
            @Param("lockReference") String lockReference,
            @Param("lockedStatus") ShowSeatStatus lockedStatus,
            @Param("bookedStatus") ShowSeatStatus bookedStatus,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ShowSeatInventory i
            set i.status = :availableStatus,
                i.lockedBy = null,
                i.lockReference = null,
                i.lockExpiryTime = null,
                i.updatedAt = :updatedAt
            where i.show.id = :showId
              and i.seat.id in :seatIds
              and i.status = :lockedStatus
              and i.lockReference = :lockReference
            """)
    int releaseLockedSeats(
            @Param("showId") Long showId,
            @Param("seatIds") List<Long> seatIds,
            @Param("lockReference") String lockReference,
            @Param("lockedStatus") ShowSeatStatus lockedStatus,
            @Param("availableStatus") ShowSeatStatus availableStatus,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ShowSeatInventory i
            set i.status = :availableStatus,
                i.lockedBy = null,
                i.lockReference = null,
                i.lockExpiryTime = null,
                i.updatedAt = :updatedAt
            where i.show.id = :showId
              and i.seat.id in :seatIds
              and i.status = :bookedStatus
            """)
    int releaseBookedSeats(
            @Param("showId") Long showId,
            @Param("seatIds") List<Long> seatIds,
            @Param("bookedStatus") ShowSeatStatus bookedStatus,
            @Param("availableStatus") ShowSeatStatus availableStatus,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Query("""
            select distinct i.lockReference
            from ShowSeatInventory i
            where i.status = :lockedStatus
              and i.lockExpiryTime < :now
              and i.lockReference is not null
            """)
    List<String> findExpiredLockReferences(
            @Param("lockedStatus") ShowSeatStatus lockedStatus,
            @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ShowSeatInventory i
            set i.status = :availableStatus,
                i.lockedBy = null,
                i.lockReference = null,
                i.lockExpiryTime = null,
                i.updatedAt = :updatedAt
            where i.lockReference in :lockReferences
              and i.status = :lockedStatus
            """)
    int releaseExpiredLocksByReferences(
            @Param("lockReferences") List<String> lockReferences,
            @Param("lockedStatus") ShowSeatStatus lockedStatus,
            @Param("availableStatus") ShowSeatStatus availableStatus,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
