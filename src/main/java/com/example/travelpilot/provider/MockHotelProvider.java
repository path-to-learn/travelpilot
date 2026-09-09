package com.example.travelpilot.provider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.example.travelpilot.domain.HotelOption;
import com.example.travelpilot.domain.HotelSearchCriteria;

/**
 * Deterministic in-memory hotel provider used for local development and tests.
 */
@Component
public class MockHotelProvider implements HotelSearchPort {

    private static final List<HotelInventoryItem> INVENTORY = List.of(
            new HotelInventoryItem(
                    "HT-001",
                    "Shibuya Central Hotel",
                    "Tokyo",
                    "Shibuya",
                    new BigDecimal("210.00"),
                    "USD",
                    new BigDecimal("4.5"),
                    true),
            new HotelInventoryItem(
                    "HT-002",
                    "Shibuya Garden Stay",
                    "Tokyo",
                    "Shibuya",
                    new BigDecimal("245.00"),
                    "USD",
                    new BigDecimal("4.2"),
                    true),
            new HotelInventoryItem(
                    "HT-003",
                    "Shinjuku Metro Hotel",
                    "Tokyo",
                    "Shinjuku",
                    new BigDecimal("180.00"),
                    "USD",
                    new BigDecimal("4.1"),
                    false),
            new HotelInventoryItem(
                    "HT-004",
                    "Shibuya Premium Suites",
                    "Tokyo",
                    "Shibuya",
                    new BigDecimal("320.00"),
                    "USD",
                    new BigDecimal("4.8"),
                    true));

    @Override
    public List<HotelOption> search(HotelSearchCriteria criteria) {
        validate(criteria);

        long nights = ChronoUnit.DAYS.between(criteria.checkIn(), criteria.checkOut());

        return INVENTORY.stream()
                .filter(hotel -> matches(hotel.city(), criteria.city()))
                .filter(hotel -> criteria.area() == null || criteria.area().isBlank()
                        || matches(hotel.area(), criteria.area()))
                .filter(hotel -> criteria.maxNightlyRate() == null
                        || hotel.nightlyRate().compareTo(criteria.maxNightlyRate()) <= 0)
                .map(hotel -> toOption(hotel, criteria.checkIn(), criteria.checkOut(), nights))
                .toList();
    }

    private static HotelOption toOption(
            HotelInventoryItem hotel,
            LocalDate checkIn,
            LocalDate checkOut,
            long nights) {
        return new HotelOption(
                hotel.id(),
                hotel.name(),
                hotel.city(),
                hotel.area(),
                hotel.nightlyRate(),
                hotel.currency(),
                hotel.nightlyRate().multiply(BigDecimal.valueOf(nights)),
                checkIn,
                checkOut,
                hotel.rating(),
                hotel.refundable());
    }

    private static void validate(HotelSearchCriteria criteria) {
        Objects.requireNonNull(criteria, "criteria must not be null");
        requireText(criteria.city(), "city");
        Objects.requireNonNull(criteria.checkIn(), "checkIn must not be null");
        Objects.requireNonNull(criteria.checkOut(), "checkOut must not be null");

        if (!criteria.checkOut().isAfter(criteria.checkIn())) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
        if (criteria.guests() < 1) {
            throw new IllegalArgumentException("guests must be at least 1");
        }
        if (criteria.maxNightlyRate() != null && criteria.maxNightlyRate().signum() < 0) {
            throw new IllegalArgumentException("maxNightlyRate must not be negative");
        }
    }

    private static boolean matches(String actual, String requested) {
        return actual.toLowerCase(Locale.ROOT).equals(requested.trim().toLowerCase(Locale.ROOT));
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private record HotelInventoryItem(
            String id,
            String name,
            String city,
            String area,
            BigDecimal nightlyRate,
            String currency,
            BigDecimal rating,
            boolean refundable) {
    }
}
