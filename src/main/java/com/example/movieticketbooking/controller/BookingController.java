package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.dto.request.CreateBookingRequest;
import com.example.movieticketbooking.dto.request.LockSeatsRequest;
import com.example.movieticketbooking.dto.response.ApiResponse;
import com.example.movieticketbooking.dto.response.BookingResponse;
import com.example.movieticketbooking.dto.response.LockSeatsResponse;
import com.example.movieticketbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/lock-seats")
    public ResponseEntity<ApiResponse<LockSeatsResponse>> lockSeats(@Valid @RequestBody LockSeatsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Seats locked successfully",
                bookingService.lockSeats(request)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Booking created successfully",
                bookingService.createBooking(request)
        ));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Booking fetched successfully",
                bookingService.getBooking(bookingId)
        ));
    }
}
