package com.example.movieticketbooking.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingCreatedEvent(
        Long bookingId,
        String bookingReference,
        Long customerId,
        String customerEmail,
        Long showId,
        String movieTitle,
        String theatreName,
        String screenName,
        List<String> seatLabels,
        BigDecimal finalAmount,
        LocalDateTime bookedAt
) {
}
