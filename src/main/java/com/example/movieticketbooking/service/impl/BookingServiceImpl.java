package com.example.movieticketbooking.service.impl;

import com.example.movieticketbooking.config.BookingProperties;
import com.example.movieticketbooking.dto.request.CreateBookingRequest;
import com.example.movieticketbooking.dto.request.CustomerRequest;
import com.example.movieticketbooking.dto.request.LockSeatsRequest;
import com.example.movieticketbooking.dto.response.BookingResponse;
import com.example.movieticketbooking.dto.response.LockSeatsResponse;
import com.example.movieticketbooking.dto.response.LockedSeatResponse;
import com.example.movieticketbooking.dto.response.PricingSummaryResponse;
import com.example.movieticketbooking.entity.Booking;
import com.example.movieticketbooking.entity.BookingSeat;
import com.example.movieticketbooking.entity.Customer;
import com.example.movieticketbooking.entity.Seat;
import com.example.movieticketbooking.entity.Show;
import com.example.movieticketbooking.entity.ShowSeatInventory;
import com.example.movieticketbooking.enums.BookingStatus;
import com.example.movieticketbooking.enums.ShowSeatStatus;
import com.example.movieticketbooking.exception.BusinessValidationException;
import com.example.movieticketbooking.exception.ResourceNotFoundException;
import com.example.movieticketbooking.exception.SeatUnavailableException;
import com.example.movieticketbooking.mapper.BookingMapper;
import com.example.movieticketbooking.repository.BookingRepository;
import com.example.movieticketbooking.repository.CustomerRepository;
import com.example.movieticketbooking.repository.ShowRepository;
import com.example.movieticketbooking.repository.ShowSeatInventoryRepository;
import com.example.movieticketbooking.service.BookingEventPublisher;
import com.example.movieticketbooking.service.BookingService;
import com.example.movieticketbooking.service.PricingService;
import com.example.movieticketbooking.service.SeatLockCacheService;
import com.example.movieticketbooking.util.ReferenceGenerator;
import com.example.movieticketbooking.util.SeatCategoryUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final ShowRepository showRepository;
    private final ShowSeatInventoryRepository showSeatInventoryRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final PricingService pricingService;
    private final BookingMapper bookingMapper;
    private final BookingProperties bookingProperties;
    private final SeatLockCacheService seatLockCacheService;
    private final BookingEventPublisher bookingEventPublisher;
    private final MeterRegistry meterRegistry;
    private final ObservationRegistry observationRegistry;

    public BookingServiceImpl(
            ShowRepository showRepository,
            ShowSeatInventoryRepository showSeatInventoryRepository,
            BookingRepository bookingRepository,
            CustomerRepository customerRepository,
            PricingService pricingService,
            BookingMapper bookingMapper,
            BookingProperties bookingProperties,
            SeatLockCacheService seatLockCacheService,
            BookingEventPublisher bookingEventPublisher,
            MeterRegistry meterRegistry,
            ObservationRegistry observationRegistry
    ) {
        this.showRepository = showRepository;
        this.showSeatInventoryRepository = showSeatInventoryRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.pricingService = pricingService;
        this.bookingMapper = bookingMapper;
        this.bookingProperties = bookingProperties;
        this.seatLockCacheService = seatLockCacheService;
        this.bookingEventPublisher = bookingEventPublisher;
        this.meterRegistry = meterRegistry;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public LockSeatsResponse lockSeats(LockSeatsRequest request) {
        Timer.Sample timerSample = Timer.start(meterRegistry);
        meterRegistry.counter("bookmymovie.booking.lock.attempts").increment();
        Observation observation = Observation.start("bookmymovie.booking.lock", observationRegistry);
        observation.lowCardinalityKeyValue("operation", "lock-seats");
        observation.lowCardinalityKeyValue("show.id", String.valueOf(request.showId()));
        try (Observation.Scope ignored = observation.openScope()) {
            Show show = showRepository.findActiveById(request.showId())
                    .orElseThrow(() -> new ResourceNotFoundException("Show not found or inactive with id " + request.showId()));
            validateShowBookable(show);

            List<Long> requestedSeatIds = resolveRequestedSeatIds(request);
            if (requestedSeatIds.isEmpty()) {
                throw new BusinessValidationException("At least one seat must be selected");
            }

            LocalDateTime now = LocalDateTime.now();
            showSeatInventoryRepository.releaseExpiredLocksForShow(
                    show.getId(),
                    ShowSeatStatus.LOCKED,
                    ShowSeatStatus.AVAILABLE,
                    now,
                    now
            );

            String lockReference = ReferenceGenerator.lockReference();
            LocalDateTime expiresAt = now.plusMinutes(bookingProperties.getLockDurationMinutes());
            String lockedBy = resolveLockedBy(request);

            /*
             * Double-booking prevention strategy:
             * Seat rows are updated from AVAILABLE -> LOCKED in one database transaction.
             * The bulk update succeeds only for seats that are still AVAILABLE.
             * If the updated row count is less than the requested seat count, we fail the request.
             * This avoids partial locking and ensures only one concurrent request can win for the same seat.
             */
            int updatedCount = showSeatInventoryRepository.lockSeats(
                    show.getId(),
                    requestedSeatIds,
                    ShowSeatStatus.AVAILABLE,
                    ShowSeatStatus.LOCKED,
                    lockedBy,
                    lockReference,
                    expiresAt,
                    now
            );
            if (updatedCount != requestedSeatIds.size()) {
                throw new SeatUnavailableException("One or more selected seats are no longer available");
            }

            seatLockCacheService.cacheLock(lockReference, show.getId(), requestedSeatIds, expiresAt, lockedBy);
            List<ShowSeatInventory> lockedInventories = showSeatInventoryRepository.findByLockReferenceWithDetails(lockReference);
            PricingSummaryResponse pricingSummary = pricingService.calculatePricing(show, lockedInventories.size());

            meterRegistry.counter("bookmymovie.booking.lock.success").increment();
            log.info("Locked {} seats for showId={} with lockReference={}", lockedInventories.size(), show.getId(), lockReference);
            return new LockSeatsResponse(
                    lockReference,
                    show.getId(),
                    lockedInventories.stream()
                            .map(inventory -> new LockedSeatResponse(
                                    inventory.getId(),
                                    inventory.getSeat().getId(),
                                    seatLabel(inventory.getSeat()),
                                    inventory.getSeat().getRowLabel(),
                                    inventory.getSeat().getSeatNumber(),
                                    SeatCategoryUtil.fromSeatType(inventory.getSeat().getSeatType())
                            ))
                            .toList(),
                    expiresAt,
                    pricingSummary
            );
        } catch (RuntimeException exception) {
            meterRegistry.counter("bookmymovie.booking.lock.failure").increment();
            observation.error(exception);
            throw exception;
        } finally {
            observation.stop();
            timerSample.stop(meterRegistry.timer("bookmymovie.booking.lock.duration"));
        }
    }

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {
        Timer.Sample timerSample = Timer.start(meterRegistry);
        meterRegistry.counter("bookmymovie.booking.create.attempts").increment();
        Observation observation = Observation.start("bookmymovie.booking.create", observationRegistry);
        observation.lowCardinalityKeyValue("operation", "create-booking");
        try (Observation.Scope ignored = observation.openScope()) {
            Booking existing = bookingRepository.findByLockReference(request.lockReference().trim()).orElse(null);
            if (existing != null) {
                return bookingMapper.toResponse(bookingRepository.findDetailedById(existing.getId()).orElse(existing));
            }

            List<ShowSeatInventory> lockedInventories = showSeatInventoryRepository.findByLockReferenceWithDetails(request.lockReference().trim());
            if (lockedInventories.isEmpty()) {
                throw new ResourceNotFoundException("No locked seats found for lock reference " + request.lockReference());
            }

            validateLockedInventories(lockedInventories, request.lockReference().trim());
            LocalDateTime expiresAt = lockedInventories.get(0).getLockExpiryTime();
            if (expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
                throw new BusinessValidationException("Seat lock has expired");
            }

            Show show = lockedInventories.get(0).getShow();
            validateShowBookable(show);

            List<Long> seatIds = lockedInventories.stream()
                    .map(inventory -> inventory.getSeat().getId())
                    .toList();
            int bookedCount = showSeatInventoryRepository.markSeatsBooked(
                    show.getId(),
                    seatIds,
                    request.lockReference().trim(),
                    ShowSeatStatus.LOCKED,
                    ShowSeatStatus.BOOKED,
                    LocalDateTime.now()
            );
            if (bookedCount != seatIds.size()) {
                throw new SeatUnavailableException("Unable to complete booking because one or more seats changed state");
            }

            Customer customer = resolveCustomer(request.customerId(), request.customer());
            PricingSummaryResponse pricing = pricingService.calculatePricing(show, lockedInventories.size());

            Booking booking = new Booking();
            booking.setBookingReference(ReferenceGenerator.bookingReference());
            booking.setLockReference(request.lockReference().trim());
            booking.setCustomer(customer);
            booking.setShow(show);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setTotalAmount(pricing.baseAmount());
            booking.setConvenienceFee(pricing.convenienceFee());
            booking.setFinalAmount(pricing.finalAmount());

            for (ShowSeatInventory inventory : lockedInventories) {
                BookingSeat bookingSeat = new BookingSeat();
                bookingSeat.setSeat(inventory.getSeat());
                bookingSeat.setPrice(show.getTicketPrice());
                bookingSeat.setSeatCategory(SeatCategoryUtil.fromSeatType(inventory.getSeat().getSeatType()));
                booking.addBookingSeat(bookingSeat);
            }

            Booking savedBooking = bookingRepository.save(booking);
            seatLockCacheService.evictLock(request.lockReference().trim(), show.getId(), seatIds);
            bookingEventPublisher.publishBookingCreated(savedBooking);
            meterRegistry.counter("bookmymovie.booking.create.success").increment();
            log.info("Created confirmed booking id={} reference={} for lockReference={}", savedBooking.getId(), savedBooking.getBookingReference(), savedBooking.getLockReference());
            return bookingMapper.toResponse(bookingRepository.findDetailedById(savedBooking.getId()).orElse(savedBooking));
        } catch (RuntimeException exception) {
            meterRegistry.counter("bookmymovie.booking.create.failure").increment();
            observation.error(exception);
            throw exception;
        } finally {
            observation.stop();
            timerSample.stop(meterRegistry.timer("bookmymovie.booking.create.duration"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId) {
        Booking booking = bookingRepository.findDetailedById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));
        return bookingMapper.toResponse(booking);
    }

    @Override
    public int expireStaleLocks() {
        LocalDateTime now = LocalDateTime.now();
        List<String> expiredReferences = showSeatInventoryRepository.findExpiredLockReferences(ShowSeatStatus.LOCKED, now);
        if (expiredReferences.isEmpty()) {
            return 0;
        }

        Map<String, List<ShowSeatInventory>> inventoriesByReference = new LinkedHashMap<>();
        for (String expiredReference : expiredReferences) {
            List<ShowSeatInventory> inventories = showSeatInventoryRepository.findByLockReferenceWithDetails(expiredReference);
            if (!inventories.isEmpty()) {
                inventoriesByReference.put(expiredReference, inventories);
            }
        }

        int released = showSeatInventoryRepository.releaseExpiredLocksByReferences(
                expiredReferences,
                ShowSeatStatus.LOCKED,
                ShowSeatStatus.AVAILABLE,
                now
        );
        for (Map.Entry<String, List<ShowSeatInventory>> entry : inventoriesByReference.entrySet()) {
            List<ShowSeatInventory> inventories = entry.getValue();
            seatLockCacheService.evictLock(
                    entry.getKey(),
                    inventories.get(0).getShow().getId(),
                    inventories.stream().map(inventory -> inventory.getSeat().getId()).toList()
            );
        }
        log.info("Released {} expired seat locks for references={}", released, expiredReferences);
        return released;
    }

    private List<Long> resolveRequestedSeatIds(LockSeatsRequest request) {
        boolean hasSeatIds = request.seatIds() != null && !request.seatIds().isEmpty();
        boolean hasSeatNumbers = request.seatNumbers() != null && !request.seatNumbers().isEmpty();
        if (hasSeatIds == hasSeatNumbers) {
            throw new BusinessValidationException("Provide either seatIds or seatNumbers");
        }

        if (hasSeatIds) {
            return request.seatIds().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
        }

        List<ShowSeatInventory> layout = showSeatInventoryRepository.findSeatLayoutByShowId(request.showId());
        if (layout.isEmpty()) {
            throw new ResourceNotFoundException("Show not found with id " + request.showId());
        }

        Map<String, Long> seatLabelToId = new LinkedHashMap<>();
        for (ShowSeatInventory inventory : layout) {
            seatLabelToId.put(seatLabel(inventory.getSeat()).toUpperCase(), inventory.getSeat().getId());
        }

        Set<Long> seatIds = new LinkedHashSet<>();
        for (String seatNumber : request.seatNumbers()) {
            Long seatId = seatLabelToId.get(seatNumber.trim().toUpperCase());
            if (seatId == null) {
                throw new ResourceNotFoundException("Seat " + seatNumber + " not found for show " + request.showId());
            }
            seatIds.add(seatId);
        }
        return new ArrayList<>(seatIds);
    }

    private String resolveLockedBy(LockSeatsRequest request) {
        if (request.customerId() != null) {
            return "CUSTOMER-" + request.customerId();
        }
        if (request.customer() != null && request.customer().email() != null && !request.customer().email().isBlank()) {
            return request.customer().email().trim().toLowerCase();
        }
        throw new BusinessValidationException("Customer id or customer details are required to lock seats");
    }

    private Customer resolveCustomer(Long customerId, CustomerRequest customerRequest) {
        if (customerId != null) {
            return customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + customerId));
        }
        if (customerRequest == null) {
            throw new BusinessValidationException("Customer details are required");
        }

        Customer customer = customerRepository.findByEmailIgnoreCase(customerRequest.email().trim())
                .orElseGet(Customer::new);
        customer.setName(customerRequest.name().trim());
        customer.setEmail(customerRequest.email().trim().toLowerCase());
        customer.setPhone(customerRequest.phone().trim());
        return customerRepository.save(customer);
    }

    private void validateLockedInventories(List<ShowSeatInventory> lockedInventories, String lockReference) {
        boolean invalid = lockedInventories.stream().anyMatch(inventory ->
                inventory.getStatus() != ShowSeatStatus.LOCKED || !lockReference.equals(inventory.getLockReference()));
        if (invalid) {
            throw new BusinessValidationException("Seat lock is no longer valid");
        }
    }

    private void validateShowBookable(Show show) {
        if (!Boolean.TRUE.equals(show.getActive())) {
            throw new BusinessValidationException("Show is not active for booking");
        }
        LocalDateTime showStart = LocalDateTime.of(show.getShowDate(), show.getStartTime());
        if (!showStart.isAfter(LocalDateTime.now())) {
            throw new BusinessValidationException("Booking is not allowed for past shows");
        }
    }

    private String seatLabel(Seat seat) {
        return seat.getRowLabel() + seat.getSeatNumber();
    }
}
