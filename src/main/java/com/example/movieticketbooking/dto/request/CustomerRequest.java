package com.example.movieticketbooking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "Customer name is required")
        @Size(max = 150, message = "Customer name must be at most 150 characters")
        String name,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Customer email must be valid")
        @Size(max = 150, message = "Customer email must be at most 150 characters")
        String email,

        @NotBlank(message = "Customer phone is required")
        @Size(max = 20, message = "Customer phone must be at most 20 characters")
        String phone
) {
}
