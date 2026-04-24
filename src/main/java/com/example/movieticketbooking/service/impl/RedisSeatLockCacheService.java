package com.example.movieticketbooking.service.impl;

import com.example.movieticketbooking.config.IntegrationProperties;
import com.example.movieticketbooking.service.SeatLockCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(value = "app.integration.redis-lock-cache-enabled", havingValue = "true", matchIfMissing = true)
public class RedisSeatLockCacheService implements SeatLockCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisSeatLockCacheService.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final IntegrationProperties integrationProperties;

    public RedisSeatLockCacheService(StringRedisTemplate stringRedisTemplate, IntegrationProperties integrationProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.integrationProperties = integrationProperties;
    }

    @Override
    public void cacheLock(String lockReference, Long showId, List<Long> seatIds, LocalDateTime expiresAt, String lockedBy) {
        Duration ttl = Duration.between(LocalDateTime.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }

        try {
            stringRedisTemplate.opsForValue().set(lockKey(lockReference), lockedBy, ttl);
            for (Long seatId : seatIds) {
                stringRedisTemplate.opsForValue().set(seatKey(showId, seatId), lockReference, ttl);
            }
            log.info("Cached seat lock in Redis for lockReference={} seatCount={}", lockReference, seatIds.size());
        } catch (DataAccessException exception) {
            log.warn("Redis lock cache unavailable for lockReference={}", lockReference, exception);
        }
    }

    @Override
    public void evictLock(String lockReference, Long showId, List<Long> seatIds) {
        try {
            stringRedisTemplate.delete(lockKey(lockReference));
            for (Long seatId : seatIds) {
                stringRedisTemplate.delete(seatKey(showId, seatId));
            }
        } catch (DataAccessException exception) {
            log.warn("Redis lock cache eviction failed for lockReference={}", lockReference, exception);
        }
    }

    private String lockKey(String lockReference) {
        return integrationProperties.getRedisKeyPrefix() + ":lock:" + lockReference;
    }

    private String seatKey(Long showId, Long seatId) {
        return integrationProperties.getRedisKeyPrefix() + ":show:" + showId + ":seat:" + seatId;
    }
}
