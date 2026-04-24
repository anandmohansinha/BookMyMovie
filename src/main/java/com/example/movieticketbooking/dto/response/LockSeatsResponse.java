package com.example.movieticketbooking.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record LockSeatsResponse(
        String lockReference,
        Long showId,
        List<LockedSeatResponse> lockedSeats,
        LocalDateTime expiresAt,
        PricingSummaryResponse pricingSummary
) {
}
