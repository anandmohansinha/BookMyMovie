package com.example.movieticketbooking.dto.response;

import java.math.BigDecimal;

public record PricingSummaryResponse(
        Integer seatCount,
        BigDecimal baseAmount,
        BigDecimal convenienceFee,
        BigDecimal finalAmount
) {
}
