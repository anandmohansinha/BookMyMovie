package com.example.movieticketbooking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookMyMovieOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("BookMyMovie API")
                        .description("Phase 1 APIs for managing movie master data and browsing shows by city, movie, and date.")
                        .version("v1")
                        .contact(new Contact()
                                .name("BookMyMovie")
                                .email("support@bookmymovie.local"))
                        .license(new License()
                                .name("Internal Demo Use")));
    }
}
