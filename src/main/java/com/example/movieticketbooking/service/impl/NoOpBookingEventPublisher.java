package com.example.movieticketbooking.service.impl;

import com.example.movieticketbooking.entity.Booking;
import com.example.movieticketbooking.service.BookingEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "app.integration.kafka-booking-events-enabled", havingValue = "false")
public class NoOpBookingEventPublisher implements BookingEventPublisher {

    @Override
    public void publishBookingCreated(Booking booking) {
        // Kafka publishing is intentionally disabled in this environment.
    }
}
