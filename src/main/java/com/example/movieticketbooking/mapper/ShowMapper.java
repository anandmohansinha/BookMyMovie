package com.example.movieticketbooking.mapper;

import com.example.movieticketbooking.dto.response.SeatAvailabilityResponse;
import com.example.movieticketbooking.dto.response.SeatRowResponse;
import com.example.movieticketbooking.dto.response.ShowSeatLayoutResponse;
import com.example.movieticketbooking.dto.response.ShowSummaryResponse;
import com.example.movieticketbooking.dto.show.ShowResponse;
import com.example.movieticketbooking.entity.Show;
import com.example.movieticketbooking.entity.ShowSeatInventory;
import com.example.movieticketbooking.util.SeatCategoryUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ShowMapper {

    public ShowResponse toShowResponse(Show show) {
        return new ShowResponse(
                show.getId(),
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                show.getScreen().getTheatre().getId(),
                show.getScreen().getTheatre().getName(),
                show.getScreen().getId(),
                show.getScreen().getName(),
                show.getShowDate(),
                show.getStartTime(),
                show.getEndTime(),
                show.getTicketPrice()
        );
    }

    public ShowSummaryResponse toShowSummary(Show show) {
        return new ShowSummaryResponse(
                show.getId(),
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                show.getScreen().getTheatre().getId(),
                show.getScreen().getTheatre().getName(),
                show.getScreen().getId(),
                show.getScreen().getName(),
                show.getShowDate(),
                show.getStartTime(),
                show.getEndTime(),
                show.getTicketPrice()
        );
    }

    public ShowSeatLayoutResponse toSeatLayoutResponse(Show show, List<ShowSeatInventory> inventories) {
        Map<String, List<SeatAvailabilityResponse>> grouped = inventories.stream()
                .collect(Collectors.groupingBy(
                        inventory -> inventory.getSeat().getRowLabel(),
                        Collectors.mapping(this::toSeatAvailability, Collectors.toList())
                ));

        List<SeatRowResponse> rows = grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new SeatRowResponse(entry.getKey(), entry.getValue()))
                .toList();

        return new ShowSeatLayoutResponse(toShowSummary(show), rows);
    }

    private SeatAvailabilityResponse toSeatAvailability(ShowSeatInventory inventory) {
        String seatLabel = inventory.getSeat().getRowLabel() + inventory.getSeat().getSeatNumber();
        return new SeatAvailabilityResponse(
                inventory.getId(),
                inventory.getSeat().getId(),
                seatLabel,
                inventory.getSeat().getSeatNumber(),
                SeatCategoryUtil.fromSeatType(inventory.getSeat().getSeatType()),
                inventory.getStatus()
        );
    }
}
