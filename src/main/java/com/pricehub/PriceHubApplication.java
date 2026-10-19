package com.pricehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the PriceHub application.
 * Serves static files from src/main/resources/static and provides REST endpoints.
 */
@SpringBootApplication
public class PriceHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(PriceHubApplication.class, args);
    }
}