package com.example.movieticketbooking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record CreateBookingRequest(
        @NotBlank(message = "Lock reference is required")
        String lockReference,

        Long customerId,

        @Valid
        CustomerRequest customer
) {
}
