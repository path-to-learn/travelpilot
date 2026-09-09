package com.example.travelpilot.provider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.example.travelpilot.domain.CabinClass;
import com.example.travelpilot.domain.FlightOption;
import com.example.travelpilot.domain.FlightSearchCriteria;

/**
 * Deterministic in-memory flight provider used for local development and tests.
 */
@Component
public class MockFlightProvider implements FlightSearchPort {

    private static final List<FlightOption> INVENTORY = List.of(
            new FlightOption(
                    "FL-001",
                    "Demo Air",
                    "DA204",
                    "SFO",
                    "TYO",
                    CabinClass.ECONOMY,
                    LocalDateTime.of(2026, 10, 10, 9, 30),
                    LocalDateTime.of(2026, 10, 11, 15, 0),
                    new BigDecimal("1240.00"),
                    "USD",
                    1,
                    true),
            new FlightOption(
                    "FL-002",
                    "CloudJet",
                    "CJ088",
                    "SFO",
                    "TYO",
                    CabinClass.ECONOMY,
                    LocalDateTime.of(2026, 10, 10, 13, 15),
                    LocalDateTime.of(2026, 10, 11, 18, 45),
                    new BigDecimal("1485.00"),
                    "USD",
                    1,
                    false),
            new FlightOption(
                    "FL-003",
                    "Pacific Express",
                    "PX710",
                    "SFO",
                    "TYO",
                    CabinClass.BUSINESS,
                    LocalDateTime.of(2026, 10, 10, 11, 0),
                    LocalDateTime.of(2026, 10, 11, 14, 50),
                    new BigDecimal("3890.00"),
                    "USD",
                    0,
                    true),
            new FlightOption(
                    "FL-004",
                    "Demo Air",
                    "DA310",
                    "LAX",
                    "TYO",
                    CabinClass.ECONOMY,
                    LocalDateTime.of(2026, 10, 10, 10, 0),
                    LocalDateTime.of(2026, 10, 11, 16, 30),
                    new BigDecimal("1090.00"),
                    "USD",
                    1,
                    true));

    @Override
    public List<FlightOption> search(FlightSearchCriteria criteria) {
        validate(criteria);

        return INVENTORY.stream()
                .filter(flight -> matchesLocation(flight.origin(), criteria.origin()))
                .filter(flight -> matchesLocation(flight.destination(), criteria.destination()))
                .filter(flight -> flight.departureTime().toLocalDate().equals(criteria.departureDate()))
                .filter(flight -> flight.cabinClass() == criteria.cabinClass())
                .filter(flight -> criteria.maxPrice() == null
                        || flight.totalPrice().compareTo(criteria.maxPrice()) <= 0)
                .toList();
    }

    private static void validate(FlightSearchCriteria criteria) {
        Objects.requireNonNull(criteria, "criteria must not be null");
        requireText(criteria.origin(), "origin");
        requireText(criteria.destination(), "destination");
        Objects.requireNonNull(criteria.departureDate(), "departureDate must not be null");
        Objects.requireNonNull(criteria.cabinClass(), "cabinClass must not be null");

        if (criteria.returnDate() != null && criteria.returnDate().isBefore(criteria.departureDate())) {
            throw new IllegalArgumentException("returnDate must not be before departureDate");
        }
        if (criteria.passengers() < 1) {
            throw new IllegalArgumentException("passengers must be at least 1");
        }
        if (criteria.maxPrice() != null && criteria.maxPrice().signum() < 0) {
            throw new IllegalArgumentException("maxPrice must not be negative");
        }
    }

    private static boolean matchesLocation(String actual, String requested) {
        String normalized = requested.trim().toUpperCase(Locale.ROOT);
        return actual.equalsIgnoreCase(normalized)
                || (actual.equals("SFO") && normalized.equals("SAN FRANCISCO"))
                || (actual.equals("TYO") && normalized.equals("TOKYO"));
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
