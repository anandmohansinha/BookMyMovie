package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.dto.request.CreateBookingRequest;
import com.example.movieticketbooking.dto.request.CustomerRequest;
import com.example.movieticketbooking.dto.request.LockSeatsRequest;
import com.example.movieticketbooking.dto.response.LockSeatsResponse;
import com.example.movieticketbooking.dto.show.ShowCreateRequest;
import com.example.movieticketbooking.dto.show.ShowResponse;
import com.example.movieticketbooking.entity.City;
import com.example.movieticketbooking.entity.Movie;
import com.example.movieticketbooking.entity.Screen;
import com.example.movieticketbooking.entity.Seat;
import com.example.movieticketbooking.entity.Show;
import com.example.movieticketbooking.entity.ShowSeatInventory;
import com.example.movieticketbooking.entity.Theatre;
import com.example.movieticketbooking.enums.BookingStatus;
import com.example.movieticketbooking.enums.MovieStatus;
import com.example.movieticketbooking.enums.SeatType;
import com.example.movieticketbooking.enums.ShowSeatStatus;
import com.example.movieticketbooking.exception.SeatUnavailableException;
import com.example.movieticketbooking.repository.BookingRepository;
import com.example.movieticketbooking.repository.CityRepository;
import com.example.movieticketbooking.repository.CustomerRepository;
import com.example.movieticketbooking.repository.MovieRepository;
import com.example.movieticketbooking.repository.ShowRepository;
import com.example.movieticketbooking.repository.ShowSeatInventoryRepository;
import com.example.movieticketbooking.repository.TheatreRepository;
import com.example.movieticketbooking.service.BookingService;
import com.example.movieticketbooking.service.ShowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovieTicketBookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatInventoryRepository showSeatInventoryRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ShowService showService;

    @Autowired
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        showSeatInventoryRepository.deleteAll();
        showRepository.deleteAll();
        movieRepository.deleteAll();
        theatreRepository.deleteAll();
        cityRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void shouldExposeOpenApiDocumentationForRetainedScenarios() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("BookMyMovie API"))
                .andExpect(jsonPath("$.paths['/api/browse/shows']").exists())
                .andExpect(jsonPath("$.paths['/api/bookings/lock-seats']").exists())
                .andExpect(jsonPath("$.paths['/api/bookings']").exists())
                .andExpect(jsonPath("$.paths['/api/shows/{showId}/seats']").exists());
    }

    @Test
    void shouldExposePrometheusMetricsEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/plain")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("jvm_memory_used_bytes")));
    }

    @Test
    void shouldEchoCorrelationIdHeader() throws Exception {
        mockMvc.perform(get("/api/cities").header("X-Correlation-Id", "corr-test-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "corr-test-123"));
    }

    @Test
    void shouldBrowseTheatresAndShowTimingsForMovieCityAndDate() throws Exception {
        City city = cityRepository.save(createCity("Mumbai", "Maharashtra"));
        Theatre firstTheatre = theatreRepository.save(createTheatre(city, "Galaxy Multiplex", "Audi 1"));
        Theatre secondTheatre = theatreRepository.save(createTheatre(city, "Harbor Screens", "Audi 2"));
        Movie movie = movieRepository.save(createMovie("Neon Run", MovieStatus.ACTIVE));

        showRepository.save(createShow(movie, firstTheatre.getScreens().iterator().next(), LocalDate.now().plusDays(2), "10:00", "12:00", "250.00"));
        showRepository.save(createShow(movie, secondTheatre.getScreens().iterator().next(), LocalDate.now().plusDays(2), "14:30", "16:30", "275.00"));

        mockMvc.perform(get("/api/browse/shows")
                        .param("movieId", movie.getId().toString())
                        .param("cityId", city.getId().toString())
                        .param("showDate", LocalDate.now().plusDays(2).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theatres.length()").value(2))
                .andExpect(jsonPath("$.theatres[0].theatreName").value("Galaxy Multiplex"))
                .andExpect(jsonPath("$.theatres[1].theatreName").value("Harbor Screens"));
    }

    @Test
    void shouldViewSeatLayoutAndAvailabilityForShow() throws Exception {
        Show show = createActiveShowWithInventory("Bengaluru", "Orion", "Screen 1", "Skyline Mission");

        mockMvc.perform(get("/api/shows/{showId}/seats", show.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.show.showId").value(show.getId()))
                .andExpect(jsonPath("$.data.rows.length()").value(2))
                .andExpect(jsonPath("$.data.rows[0].rowLabel").value("A"))
                .andExpect(jsonPath("$.data.rows[0].seats[0].status").value("AVAILABLE"));
    }

    @Test
    void shouldLockAvailableSeatsSuccessfully() throws Exception {
        Show show = createActiveShowWithInventory("Hyderabad", "Zen Screens", "Screen 1", "Pulse Mission");
        List<ShowSeatInventory> inventories = showSeatInventoryRepository.findSeatLayoutByShowId(show.getId());

        String payload = objectMapper.writeValueAsString(new LockSeatsRequest(
                show.getId(),
                List.of(inventories.get(0).getSeat().getId(), inventories.get(1).getSeat().getId()),
                null,
                null,
                new CustomerRequest("Anand", "anand1@example.com", "9999999999")
        ));

        mockMvc.perform(post("/api/bookings/lock-seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lockReference").exists())
                .andExpect(jsonPath("$.data.lockedSeats.length()").value(2))
                .andExpect(jsonPath("$.data.pricingSummary.seatCount").value(2));
    }

    @Test
    void shouldFailWhenOneOfRequestedSeatsIsAlreadyLocked() {
        Show show = createActiveShowWithInventory("Pune", "Metro Dome", "Screen 1", "Blue Orbit");
        List<ShowSeatInventory> inventories = showSeatInventoryRepository.findSeatLayoutByShowId(show.getId());
        bookingService.lockSeats(new LockSeatsRequest(
                show.getId(),
                List.of(inventories.get(0).getSeat().getId()),
                null,
                null,
                new CustomerRequest("Customer 1", "c1@example.com", "9000000001")
        ));

        org.junit.jupiter.api.Assertions.assertThrows(SeatUnavailableException.class, () ->
                bookingService.lockSeats(new LockSeatsRequest(
                        show.getId(),
                        List.of(inventories.get(0).getSeat().getId(), inventories.get(1).getSeat().getId()),
                        null,
                        null,
                        new CustomerRequest("Customer 2", "c2@example.com", "9000000002")
                )));
    }

    @Test
    void shouldCreateConfirmedBookingFromValidLockReference() throws Exception {
        Show show = createActiveShowWithInventory("Kolkata", "Metro Gold", "Screen Gold", "Rooftop Echo");
        LockSeatsResponse lockResponse = bookingService.lockSeats(new LockSeatsRequest(
                show.getId(),
                null,
                List.of("A1", "A2"),
                null,
                new CustomerRequest("Rahul", "rahul@example.com", "9000000003")
        ));

        String payload = objectMapper.writeValueAsString(new CreateBookingRequest(
                lockResponse.lockReference(),
                null,
                new CustomerRequest("Rahul", "rahul@example.com", "9000000003")
        ));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bookingReference").exists())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.seats.length()").value(2));

        List<ShowSeatInventory> inventories = showSeatInventoryRepository.findSeatLayoutByShowId(show.getId());
        assertThat(inventories.stream()
                .filter(inventory -> List.of("A1", "A2").contains(inventory.getSeat().getRowLabel() + inventory.getSeat().getSeatNumber()))
                .allMatch(inventory -> inventory.getStatus() == ShowSeatStatus.BOOKED))
                .isTrue();
    }

    @Test
    void shouldFailBookingCreationForExpiredLock() {
        Show show = createActiveShowWithInventory("Jaipur", "Cine Aura", "Screen 1", "Desert Drive");
        LockSeatsResponse lockResponse = bookingService.lockSeats(new LockSeatsRequest(
                show.getId(),
                null,
                List.of("A1"),
                null,
                new CustomerRequest("Meera", "meera@example.com", "9000000004")
        ));

        List<ShowSeatInventory> lockedSeats = showSeatInventoryRepository.findByLockReferenceWithDetails(lockResponse.lockReference());
        lockedSeats.forEach(inventory -> inventory.setLockExpiryTime(LocalDateTime.now().minusMinutes(1)));
        showSeatInventoryRepository.saveAll(lockedSeats);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                bookingService.createBooking(new CreateBookingRequest(
                        lockResponse.lockReference(),
                        null,
                        new CustomerRequest("Meera", "meera@example.com", "9000000004")
                )));
    }

    @Test
    void shouldExpireSeatLocksDuringCleanup() {
        Show show = createActiveShowWithInventory("Lucknow", "Royal Cinema", "Screen 1", "Monsoon Edge");
        LockSeatsResponse lockResponse = bookingService.lockSeats(new LockSeatsRequest(
                show.getId(),
                null,
                List.of("A1", "A2"),
                null,
                new CustomerRequest("Arjun", "arjun@example.com", "9000000010")
        ));

        List<ShowSeatInventory> lockedSeats = showSeatInventoryRepository.findByLockReferenceWithDetails(lockResponse.lockReference());
        lockedSeats.forEach(inventory -> inventory.setLockExpiryTime(LocalDateTime.now().minusMinutes(10)));
        showSeatInventoryRepository.saveAll(lockedSeats);

        int released = bookingService.expireStaleLocks();
        assertThat(released).isEqualTo(2);

        List<ShowSeatInventory> refreshed = showSeatInventoryRepository.findSeatLayoutByShowId(show.getId());
        assertThat(refreshed.stream()
                .filter(inventory -> List.of("A1", "A2").contains(inventory.getSeat().getRowLabel() + inventory.getSeat().getSeatNumber()))
                .allMatch(inventory -> inventory.getStatus() == ShowSeatStatus.AVAILABLE))
                .isTrue();
    }

    @Test
    void shouldAllowOnlyOneConcurrentLockForSameSeat() throws Exception {
        Show show = createActiveShowWithInventory("Ahmedabad", "Velocity", "Screen 1", "Crimson Route");
        Long seatId = showSeatInventoryRepository.findSeatLayoutByShowId(show.getId()).get(0).getSeat().getId();
        CountDownLatch startGate = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<Boolean> taskOne = concurrentLockTask(show.getId(), seatId, "c11@example.com", startGate);
        Callable<Boolean> taskTwo = concurrentLockTask(show.getId(), seatId, "c12@example.com", startGate);

        Future<Boolean> futureOne = executorService.submit(taskOne);
        Future<Boolean> futureTwo = executorService.submit(taskTwo);
        startGate.countDown();

        boolean resultOne = futureOne.get();
        boolean resultTwo = futureTwo.get();
        executorService.shutdownNow();

        assertThat(List.of(resultOne, resultTwo)).containsExactlyInAnyOrder(true, false);
    }

    @Test
    void shouldFetchBookingDetailsAfterTicketBooking() throws Exception {
        Show show = createActiveShowWithInventory("Chandigarh", "Sunbeam", "Screen 1", "Urban Signal");
        LockSeatsResponse lockResponse = bookingService.lockSeats(new LockSeatsRequest(
                show.getId(),
                null,
                List.of("A1", "A2"),
                null,
                new CustomerRequest("Nisha", "nisha@example.com", "9000000005")
        ));

        Long bookingId = bookingService.createBooking(new CreateBookingRequest(
                lockResponse.lockReference(),
                null,
                new CustomerRequest("Nisha", "nisha@example.com", "9000000005")
        )).bookingId();

        mockMvc.perform(get("/api/bookings/{bookingId}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.show.showId").value(show.getId()))
                .andExpect(jsonPath("$.data.seats.length()").value(2));
    }

    private Callable<Boolean> concurrentLockTask(Long showId, Long seatId, String email, CountDownLatch startGate) {
        return () -> {
            startGate.await();
            try {
                bookingService.lockSeats(new LockSeatsRequest(
                        showId,
                        List.of(seatId),
                        null,
                        null,
                        new CustomerRequest("Concurrent", email, "9888888888")
                ));
                return true;
            } catch (SeatUnavailableException ex) {
                return false;
            }
        };
    }

    private Show createActiveShowWithInventory(String cityName, String theatreName, String screenName, String movieTitle) {
        City city = cityRepository.save(createCity(cityName, "State"));
        Theatre theatre = theatreRepository.save(createTheatre(city, theatreName, screenName));
        Movie movie = movieRepository.save(createMovie(movieTitle, MovieStatus.ACTIVE));
        ShowResponse response = showService.createShow(new ShowCreateRequest(
                movie.getId(),
                theatre.getScreens().iterator().next().getId(),
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0),
                LocalTime.of(12, 30),
                new BigDecimal("250.00")
        ));
        return showRepository.findDetailsById(response.id()).orElseThrow();
    }

    private City createCity(String name, String state) {
        City city = new City();
        city.setName(name);
        city.setState(state);
        city.setCountry("India");
        return city;
    }

    private Theatre createTheatre(City city, String name, String screenName) {
        Theatre theatre = new Theatre();
        theatre.setCity(city);
        theatre.setName(name);
        theatre.setAddress(name + " Address");
        theatre.setActive(Boolean.TRUE);

        Screen screen = new Screen();
        screen.setName(screenName);
        addSeat(screen, "A", 1, SeatType.REGULAR);
        addSeat(screen, "A", 2, SeatType.REGULAR);
        addSeat(screen, "B", 1, SeatType.PREMIUM);
        theatre.addScreen(screen);
        return theatre;
    }

    private void addSeat(Screen screen, String rowLabel, int seatNumber, SeatType seatType) {
        Seat seat = new Seat();
        seat.setRowLabel(rowLabel);
        seat.setSeatNumber(seatNumber);
        seat.setSeatType(seatType);
        seat.setActive(Boolean.TRUE);
        screen.addSeat(seat);
    }

    private Movie createMovie(String title, MovieStatus status) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setLanguage("English");
        movie.setGenre("Action");
        movie.setDurationMinutes(130);
        movie.setStatus(status);
        return movie;
    }

    private Show createShow(Movie movie, Screen screen, LocalDate showDate, String start, String end, String price) {
        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(showDate);
        show.setStartTime(LocalTime.parse(start));
        show.setEndTime(LocalTime.parse(end));
        show.setTicketPrice(new BigDecimal(price));
        show.setActive(Boolean.TRUE);
        return show;
    }
}
