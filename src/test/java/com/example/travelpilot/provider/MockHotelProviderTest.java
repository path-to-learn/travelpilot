package com.example.travelpilot.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.example.travelpilot.domain.HotelOption;
import com.example.travelpilot.domain.HotelSearchCriteria;

class MockHotelProviderTest {

    private final MockHotelProvider provider = new MockHotelProvider();

    @Test
    void calculatesHotelTotalForMatchingAreaAndDates() {
        HotelSearchCriteria criteria = new HotelSearchCriteria(
                "Tokyo",
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 13),
                1,
                new BigDecimal("250"),
                "Shibuya");

        assertThat(provider.search(criteria))
                .extracting(HotelOption::id)
                .containsExactly("HT-001", "HT-002");
        assertThat(provider.search(criteria).getFirst().totalPrice())
                .isEqualByComparingTo("630.00");
    }

    @Test
    void rejectsCheckoutBeforeCheckin() {
        HotelSearchCriteria criteria = new HotelSearchCriteria(
                "Tokyo",
                LocalDate.of(2026, 10, 13),
                LocalDate.of(2026, 10, 10),
                1,
                null,
                null);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> provider.search(criteria))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("checkOut must be after checkIn");
    }
}
