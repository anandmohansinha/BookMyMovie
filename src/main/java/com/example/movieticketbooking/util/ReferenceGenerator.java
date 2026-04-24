package com.example.movieticketbooking.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class ReferenceGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private ReferenceGenerator() {
    }

    public static String bookingReference() {
        return "BKG-" + FORMATTER.format(LocalDateTime.now()) + "-" + shortUuid();
    }

    public static String paymentReference() {
        return "PAY-" + FORMATTER.format(LocalDateTime.now()) + "-" + shortUuid();
    }

    public static String lockReference() {
        return "LCK-" + FORMATTER.format(LocalDateTime.now()) + "-" + shortUuid();
    }

    private static String shortUuid() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
