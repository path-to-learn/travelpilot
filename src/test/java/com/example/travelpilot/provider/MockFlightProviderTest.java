package com.example.travelpilot.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.example.travelpilot.domain.CabinClass;
import com.example.travelpilot.domain.FlightOption;
import com.example.travelpilot.domain.FlightSearchCriteria;

class MockFlightProviderTest {

    private final MockFlightProvider provider = new MockFlightProvider();

    @Test
    void returnsMatchingFlightsWithinBudget() {
        FlightSearchCriteria criteria = new FlightSearchCriteria(
                "SFO",
                "TYO",
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 20),
                1,
                CabinClass.ECONOMY,
                new BigDecimal("1500"));

        assertThat(provider.search(criteria))
                .extracting(FlightOption::id)
                .containsExactly("FL-001", "FL-002");
    }

    @Test
    void returnsNoFlightsWhenBudgetIsTooLow() {
        FlightSearchCriteria criteria = new FlightSearchCriteria(
                "SFO",
                "TYO",
                LocalDate.of(2026, 10, 10),
                null,
                1,
                CabinClass.ECONOMY,
                new BigDecimal("1000"));

        assertThat(provider.search(criteria)).isEmpty();
    }
}
