package com.example.travelpilot.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FlightOption(
        String id,
        String airline,
        String flightNumber,
        String origin,
        String destination,
        CabinClass cabinClass,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal totalPrice,
        String currency,
        int stops,
        boolean refundable) {
}
