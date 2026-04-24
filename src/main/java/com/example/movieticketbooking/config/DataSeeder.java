package com.example.movieticketbooking.config;

import com.example.movieticketbooking.entity.City;
import com.example.movieticketbooking.entity.Movie;
import com.example.movieticketbooking.entity.Screen;
import com.example.movieticketbooking.entity.Seat;
import com.example.movieticketbooking.entity.Show;
import com.example.movieticketbooking.entity.ShowSeatInventory;
import com.example.movieticketbooking.entity.Theatre;
import com.example.movieticketbooking.enums.MovieStatus;
import com.example.movieticketbooking.enums.SeatType;
import com.example.movieticketbooking.enums.ShowSeatStatus;
import com.example.movieticketbooking.repository.CityRepository;
import com.example.movieticketbooking.repository.MovieRepository;
import com.example.movieticketbooking.repository.ShowRepository;
import com.example.movieticketbooking.repository.ShowSeatInventoryRepository;
import com.example.movieticketbooking.repository.TheatreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    @ConditionalOnProperty(value = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
    ApplicationRunner seedData(
            CityRepository cityRepository,
            TheatreRepository theatreRepository,
            MovieRepository movieRepository,
            ShowRepository showRepository,
            ShowSeatInventoryRepository showSeatInventoryRepository
    ) {
        return args -> {
            if (cityRepository.count() > 0 || movieRepository.count() > 0 || showRepository.count() > 0) {
                log.info("Skipping sample seed data because records already exist");
                return;
            }

            City bengaluru = createCity("Bengaluru", "Karnataka", "India");
            City hyderabad = createCity("Hyderabad", "Telangana", "India");
            cityRepository.save(bengaluru);
            cityRepository.save(hyderabad);

            Theatre orion = createTheatre(bengaluru, "Orion Multiplex", "Rajajinagar, Bengaluru", "Screen 1", "Screen 2");
            Theatre forum = createTheatre(bengaluru, "Forum Cinemas", "Koramangala, Bengaluru", "Audi 1");
            Theatre inox = createTheatre(hyderabad, "INOX GVK One", "Banjara Hills, Hyderabad", "Screen Gold");
            theatreRepository.save(orion);
            theatreRepository.save(forum);
            theatreRepository.save(inox);

            Movie activeMovie = createMovie(
                    "Skyline Mission",
                    "English",
                    "Action",
                    145,
                    "UA",
                    "An elite crew races against time to stop a global catastrophe.",
                    LocalDate.now().minusWeeks(1),
                    MovieStatus.ACTIVE
            );
            Movie familyMovie = createMovie(
                    "Laugh Lines",
                    "Hindi",
                    "Comedy",
                    125,
                    "U",
                    "A chaotic family reunion turns into a heartfelt road trip.",
                    LocalDate.now().minusDays(10),
                    MovieStatus.ACTIVE
            );
            Movie inactiveMovie = createMovie(
                    "Archive Dreams",
                    "Tamil",
                    "Drama",
                    110,
                    "UA",
                    "A restorer uncovers the untold history behind a lost film reel.",
                    LocalDate.now().minusMonths(1),
                    MovieStatus.INACTIVE
            );
            movieRepository.save(activeMovie);
            movieRepository.save(familyMovie);
            movieRepository.save(inactiveMovie);

            LocalDate tomorrow = LocalDate.now().plusDays(1);
            LocalDate dayAfterTomorrow = LocalDate.now().plusDays(2);

            Screen orionScreen1 = getScreenByName(orion, "Screen 1");
            Screen orionScreen2 = getScreenByName(orion, "Screen 2");
            Screen forumScreen1 = getScreenByName(forum, "Audi 1");
            Screen inoxScreen = getScreenByName(inox, "Screen Gold");

            createShowWithInventory(showRepository, showSeatInventoryRepository, activeMovie, orionScreen1, tomorrow, "10:00", "12:25", "250.00");
            createShowWithInventory(showRepository, showSeatInventoryRepository, activeMovie, orionScreen2, tomorrow, "13:15", "15:40", "280.00");
            createShowWithInventory(showRepository, showSeatInventoryRepository, activeMovie, forumScreen1, tomorrow, "18:30", "20:55", "300.00");
            createShowWithInventory(showRepository, showSeatInventoryRepository, familyMovie, inoxScreen, dayAfterTomorrow, "11:00", "13:05", "220.00");

            log.info("Sample seed data created successfully");
        };
    }

    private City createCity(String name, String state, String country) {
        City city = new City();
        city.setName(name);
        city.setState(state);
        city.setCountry(country);
        return city;
    }

    private Theatre createTheatre(City city, String name, String address, String... screenNames) {
        Theatre theatre = new Theatre();
        theatre.setCity(city);
        theatre.setName(name);
        theatre.setAddress(address);
        theatre.setActive(Boolean.TRUE);

        for (String screenName : screenNames) {
            Screen screen = new Screen();
            screen.setName(screenName);
            addSeats(screen, "A", 1, 6, SeatType.REGULAR);
            addSeats(screen, "B", 1, 6, SeatType.PREMIUM);
            addSeats(screen, "C", 1, 4, SeatType.RECLINER);
            theatre.addScreen(screen);
        }
        return theatre;
    }

    private void addSeats(Screen screen, String rowLabel, int start, int end, SeatType seatType) {
        for (int seatNo = start; seatNo <= end; seatNo++) {
            Seat seat = new Seat();
            seat.setRowLabel(rowLabel);
            seat.setSeatNumber(seatNo);
            seat.setSeatType(seatType);
            seat.setActive(Boolean.TRUE);
            screen.addSeat(seat);
        }
    }

    private Movie createMovie(
            String title,
            String language,
            String genre,
            int durationMinutes,
            String certification,
            String description,
            LocalDate releaseDate,
            MovieStatus status
    ) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setLanguage(language);
        movie.setGenre(genre);
        movie.setDurationMinutes(durationMinutes);
        movie.setCertification(certification);
        movie.setDescription(description);
        movie.setReleaseDate(releaseDate);
        movie.setStatus(status);
        return movie;
    }

    private Screen getScreenByName(Theatre theatre, String screenName) {
        return theatre.getScreens().stream()
                .filter(screen -> screen.getName().equals(screenName))
                .findFirst()
                .orElseThrow();
    }

    private Show createShow(Movie movie, Screen screen, LocalDate date, String start, String end, String price) {
        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(date);
        show.setStartTime(LocalTime.parse(start));
        show.setEndTime(LocalTime.parse(end));
        show.setTicketPrice(new BigDecimal(price));
        show.setActive(Boolean.TRUE);
        return show;
    }

    private void createShowWithInventory(
            ShowRepository showRepository,
            ShowSeatInventoryRepository showSeatInventoryRepository,
            Movie movie,
            Screen screen,
            LocalDate date,
            String start,
            String end,
            String price
    ) {
        Show show = showRepository.save(createShow(movie, screen, date, start, end, price));
        showSeatInventoryRepository.saveAll(screen.getSeats().stream()
                .map(seat -> {
                    ShowSeatInventory inventory = new ShowSeatInventory();
                    inventory.setShow(show);
                    inventory.setSeat(seat);
                    inventory.setStatus(ShowSeatStatus.AVAILABLE);
                    return inventory;
                })
                .toList());
    }
}
