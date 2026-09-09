package com.example.travelpilot.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HotelOption(
        String id,
        String name,
        String city,
        String area,
        BigDecimal nightlyRate,
        String currency,
        BigDecimal totalPrice,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal rating,
        boolean refundable) {
}
