package com.example.movieticketbooking.service.impl;

import com.example.movieticketbooking.service.SeatLockCacheService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(value = "app.integration.redis-lock-cache-enabled", havingValue = "false")
public class NoOpSeatLockCacheService implements SeatLockCacheService {

    @Override
    public void cacheLock(String lockReference, Long showId, List<Long> seatIds, LocalDateTime expiresAt, String lockedBy) {
        // Redis caching is intentionally disabled in this environment.
    }

    @Override
    public void evictLock(String lockReference, Long showId, List<Long> seatIds) {
        // Redis caching is intentionally disabled in this environment.
    }
}
