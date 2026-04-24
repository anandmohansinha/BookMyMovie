package com.example.movieticketbooking.service;

import java.time.LocalDateTime;
import java.util.List;

public interface SeatLockCacheService {

    void cacheLock(String lockReference, Long showId, List<Long> seatIds, LocalDateTime expiresAt, String lockedBy);

    void evictLock(String lockReference, Long showId, List<Long> seatIds);
}
