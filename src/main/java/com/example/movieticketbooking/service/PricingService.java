package com.example.movieticketbooking.service;

import com.example.movieticketbooking.dto.response.PricingSummaryResponse;
import com.example.movieticketbooking.entity.Show;

public interface PricingService {

    PricingSummaryResponse calculatePricing(Show show, int seatCount);
}
