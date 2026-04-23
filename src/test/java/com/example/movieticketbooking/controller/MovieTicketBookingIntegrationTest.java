package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.entity.City;
import com.example.movieticketbooking.entity.Movie;
import com.example.movieticketbooking.entity.Screen;
import com.example.movieticketbooking.entity.Seat;
import com.example.movieticketbooking.entity.Show;
import com.example.movieticketbooking.entity.Theatre;
import com.example.movieticketbooking.enums.MovieStatus;
import com.example.movieticketbooking.enums.SeatType;
import com.example.movieticketbooking.repository.CityRepository;
import com.example.movieticketbooking.repository.MovieRepository;
import com.example.movieticketbooking.repository.ShowRepository;
import com.example.movieticketbooking.repository.TheatreRepository;
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
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @BeforeEach
    void setUp() {
        showRepository.deleteAll();
        movieRepository.deleteAll();
        theatreRepository.deleteAll();
        cityRepository.deleteAll();
    }

    @Test
    void shouldCreateAndListCities() throws Exception {
        String payload = """
                {
                  "name": "Pune",
                  "state": "Maharashtra",
                  "country": "India"
                }
                """;

        mockMvc.perform(post("/api/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pune"));

        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pune"))
                .andExpect(jsonPath("$[0].state").value("Maharashtra"));
    }

    @Test
    void shouldRejectShowCreationForInactiveMovie() throws Exception {
        City city = cityRepository.save(createCity("Chennai", "Tamil Nadu"));
        Theatre theatre = theatreRepository.save(createTheatre(city, "Marina Cinemas", "Screen 1"));
        Screen screen = theatre.getScreens().iterator().next();

        Movie movie = new Movie();
        movie.setTitle("Silent Frames");
        movie.setLanguage("Tamil");
        movie.setGenre("Drama");
        movie.setDurationMinutes(120);
        movie.setStatus(MovieStatus.INACTIVE);
        movie = movieRepository.save(movie);

        String payload = objectMapper.writeValueAsString(new ShowRequestPayload(
                movie.getId(),
                screen.getId(),
                LocalDate.of(2026, 4, 24),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                new BigDecimal("180.00")
        ));

        mockMvc.perform(post("/api/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only active movies can be scheduled"));
    }

    @Test
    void shouldRejectOverlappingShowsOnSameScreenAndDate() throws Exception {
        City city = cityRepository.save(createCity("Delhi", "Delhi"));
        Theatre theatre = theatreRepository.save(createTheatre(city, "Capital Screens", "Hall 1"));
        Screen screen = theatre.getScreens().iterator().next();

        Movie movie = createMovie("Metro Chase", MovieStatus.ACTIVE);
        movie = movieRepository.save(movie);

        Show existingShow = new Show();
        existingShow.setMovie(movie);
        existingShow.setScreen(screen);
        existingShow.setShowDate(LocalDate.of(2026, 4, 25));
        existingShow.setStartTime(LocalTime.of(9, 0));
        existingShow.setEndTime(LocalTime.of(11, 30));
        existingShow.setTicketPrice(new BigDecimal("210.00"));
        showRepository.save(existingShow);

        String payload = objectMapper.writeValueAsString(new ShowRequestPayload(
                movie.getId(),
                screen.getId(),
                LocalDate.of(2026, 4, 25),
                LocalTime.of(11, 0),
                LocalTime.of(13, 15),
                new BigDecimal("220.00")
        ));

        mockMvc.perform(post("/api/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Show timing overlaps with an existing show on the same screen"));
    }

    @Test
    void shouldBrowseTheatresAndShowTimingsForMovieCityAndDate() throws Exception {
        City city = cityRepository.save(createCity("Mumbai", "Maharashtra"));
        Theatre firstTheatre = theatreRepository.save(createTheatre(city, "Galaxy Multiplex", "Audi 1"));
        Theatre secondTheatre = theatreRepository.save(createTheatre(city, "Harbor Screens", "Audi 2"));

        Movie movie = createMovie("Neon Run", MovieStatus.ACTIVE);
        movie = movieRepository.save(movie);

        showRepository.save(createShow(movie, firstTheatre.getScreens().iterator().next(), LocalDate.of(2026, 4, 26), "10:00", "12:00", "250.00"));
        showRepository.save(createShow(movie, secondTheatre.getScreens().iterator().next(), LocalDate.of(2026, 4, 26), "14:30", "16:30", "275.00"));

        mockMvc.perform(get("/api/browse/shows")
                        .param("movieId", movie.getId().toString())
                        .param("cityId", city.getId().toString())
                        .param("showDate", "2026-04-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cityId").value(city.getId()))
                .andExpect(jsonPath("$.movieId").value(movie.getId()))
                .andExpect(jsonPath("$.theatres.length()").value(2))
                .andExpect(jsonPath("$.theatres[0].showTimings[0].startTime").value("10:00:00"))
                .andExpect(jsonPath("$.theatres[1].showTimings[0].startTime").value("14:30:00"));
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
        return show;
    }

    private record ShowRequestPayload(
            Long movieId,
            Long screenId,
            LocalDate showDate,
            LocalTime startTime,
            LocalTime endTime,
            BigDecimal ticketPrice
    ) {
    }
}
