package com.example.movieticketbooking.service;

import com.example.movieticketbooking.dto.request.CreateBookingRequest;
import com.example.movieticketbooking.dto.request.LockSeatsRequest;
import com.example.movieticketbooking.dto.response.BookingResponse;
import com.example.movieticketbooking.dto.response.LockSeatsResponse;

public interface BookingService {

    LockSeatsResponse lockSeats(LockSeatsRequest request);

    BookingResponse createBooking(CreateBookingRequest request);

    BookingResponse getBooking(Long bookingId);

    int expireStaleLocks();
}
