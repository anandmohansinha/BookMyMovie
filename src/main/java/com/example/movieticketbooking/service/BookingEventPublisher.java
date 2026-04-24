package com.example.movieticketbooking.service;

import com.example.movieticketbooking.entity.Booking;

public interface BookingEventPublisher {

    void publishBookingCreated(Booking booking);
}
