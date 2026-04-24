package com.example.movieticketbooking.service.impl;

import com.example.movieticketbooking.config.IntegrationProperties;
import com.example.movieticketbooking.entity.Booking;
import com.example.movieticketbooking.event.BookingCreatedEvent;
import com.example.movieticketbooking.service.BookingEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "app.integration.kafka-booking-events-enabled", havingValue = "true", matchIfMissing = true)
public class KafkaBookingEventPublisher implements BookingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaBookingEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final IntegrationProperties integrationProperties;

    public KafkaBookingEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            IntegrationProperties integrationProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.integrationProperties = integrationProperties;
    }

    @Override
    public void publishBookingCreated(Booking booking) {
        BookingCreatedEvent event = new BookingCreatedEvent(
                booking.getId(),
                booking.getBookingReference(),
                booking.getCustomer().getId(),
                booking.getCustomer().getEmail(),
                booking.getShow().getId(),
                booking.getShow().getMovie().getTitle(),
                booking.getShow().getScreen().getTheatre().getName(),
                booking.getShow().getScreen().getName(),
                booking.getBookingSeats().stream()
                        .map(seat -> seat.getSeat().getRowLabel() + seat.getSeat().getSeatNumber())
                        .toList(),
                booking.getFinalAmount(),
                booking.getCreatedAt()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(integrationProperties.getBookingCreatedTopic(), booking.getBookingReference(), payload)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            log.warn("Failed to publish booking-created event for bookingReference={}", booking.getBookingReference(), throwable);
                        } else {
                            log.info("Published booking-created event for bookingReference={} topic={}",
                                    booking.getBookingReference(), integrationProperties.getBookingCreatedTopic());
                        }
                    });
        } catch (JsonProcessingException exception) {
            log.warn("Unable to serialize booking-created event for bookingReference={}", booking.getBookingReference(), exception);
        }
    }
}
