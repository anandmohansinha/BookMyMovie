package com.example.movieticketbooking.mapper;

import com.example.movieticketbooking.dto.response.BookingResponse;
import com.example.movieticketbooking.dto.response.BookingSeatResponse;
import com.example.movieticketbooking.dto.response.CustomerResponse;
import com.example.movieticketbooking.entity.Booking;
import com.example.movieticketbooking.util.SeatCategoryUtil;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class BookingMapper {

    private final ShowMapper showMapper;

    public BookingMapper(ShowMapper showMapper) {
        this.showMapper = showMapper;
    }

    public BookingResponse toResponse(Booking booking) {
        CustomerResponse customer = new CustomerResponse(
                booking.getCustomer().getId(),
                booking.getCustomer().getName(),
                booking.getCustomer().getEmail(),
                booking.getCustomer().getPhone()
        );

        return new BookingResponse(
                booking.getId(),
                booking.getBookingReference(),
                booking.getLockReference(),
                booking.getStatus(),
                customer,
                showMapper.toShowSummary(booking.getShow()),
                booking.getBookingSeats().stream()
                        .sorted(Comparator.comparing(bs -> bs.getSeat().getRowLabel() + bs.getSeat().getSeatNumber()))
                        .map(seat -> new BookingSeatResponse(
                                seat.getSeat().getId(),
                                seat.getSeat().getRowLabel() + seat.getSeat().getSeatNumber(),
                                seat.getSeatCategory(),
                                seat.getPrice()
                        ))
                        .toList(),
                booking.getTotalAmount(),
                booking.getConvenienceFee(),
                booking.getFinalAmount(),
                booking.getCreatedAt()
        );
    }
}
