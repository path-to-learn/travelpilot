package com.example.travelpilot.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FlightSearchCriteria(
        String origin,
        String destination,
        LocalDate departureDate,
        LocalDate returnDate,
        int passengers,
        CabinClass cabinClass,
        BigDecimal maxPrice) {
}
