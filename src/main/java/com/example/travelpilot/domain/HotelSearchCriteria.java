package com.example.travelpilot.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HotelSearchCriteria(
        String city,
        LocalDate checkIn,
        LocalDate checkOut,
        int guests,
        BigDecimal maxNightlyRate,
        String area) {
}
