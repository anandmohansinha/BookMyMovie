package com.example.movieticketbooking.util;

import com.example.movieticketbooking.enums.SeatCategory;
import com.example.movieticketbooking.enums.SeatType;

public final class SeatCategoryUtil {

    private SeatCategoryUtil() {
    }

    public static SeatCategory fromSeatType(SeatType seatType) {
        return SeatCategory.valueOf(seatType.name());
    }
}
