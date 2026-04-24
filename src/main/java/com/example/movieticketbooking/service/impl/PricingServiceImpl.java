package com.example.movieticketbooking.service.impl;

import com.example.movieticketbooking.config.BookingProperties;
import com.example.movieticketbooking.dto.response.PricingSummaryResponse;
import com.example.movieticketbooking.entity.Show;
import com.example.movieticketbooking.service.PricingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingServiceImpl implements PricingService {

    private final BookingProperties bookingProperties;

    public PricingServiceImpl(BookingProperties bookingProperties) {
        this.bookingProperties = bookingProperties;
    }

    @Override
    public PricingSummaryResponse calculatePricing(Show show, int seatCount) {
        BigDecimal baseAmount = show.getTicketPrice().multiply(BigDecimal.valueOf(seatCount));
        BigDecimal convenienceFee = bookingProperties.getConvenienceFeePerSeat().multiply(BigDecimal.valueOf(seatCount));
        BigDecimal finalAmount = baseAmount.add(convenienceFee);
        return new PricingSummaryResponse(seatCount, baseAmount, convenienceFee, finalAmount);
    }
}
