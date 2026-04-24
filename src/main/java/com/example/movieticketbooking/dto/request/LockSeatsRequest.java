package com.example.movieticketbooking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record LockSeatsRequest(
        @NotNull(message = "Show id is required")
        Long showId,

        List<Long> seatIds,

        @Size(max = 50, message = "You can lock at most 50 seats in one request")
        List<String> seatNumbers,

        Long customerId,

        @Valid
        CustomerRequest customer
) {
}
