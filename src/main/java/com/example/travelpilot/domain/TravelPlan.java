package com.example.travelpilot.domain;

import java.util.List;

public record TravelPlan(
        PlanStatus status,
        String summary,
        String clarificationQuestion,
        List<FlightOption> recommendedFlights,
        List<HotelOption> recommendedHotels,
        List<String> assumptions,
        List<String> warnings,
        boolean bookingRequired) {
}
