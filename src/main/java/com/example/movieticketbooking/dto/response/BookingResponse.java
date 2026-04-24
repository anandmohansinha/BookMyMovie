package com.example.movieticketbooking.dto.response;

import com.example.movieticketbooking.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long bookingId,
        String bookingReference,
        String lockReference,
        BookingStatus status,
        CustomerResponse customer,
        ShowSummaryResponse show,
        List<BookingSeatResponse> seats,
        BigDecimal totalAmount,
        BigDecimal convenienceFee,
        BigDecimal finalAmount,
        LocalDateTime createdAt
) {
}
