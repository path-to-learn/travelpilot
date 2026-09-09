package com.example.travelpilot.tools;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Component;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import com.example.travelpilot.domain.CabinClass;
import com.example.travelpilot.domain.FlightOption;
import com.example.travelpilot.domain.FlightSearchCriteria;
import com.example.travelpilot.domain.HotelOption;
import com.example.travelpilot.domain.HotelSearchCriteria;
import com.example.travelpilot.provider.FlightSearchPort;
import com.example.travelpilot.provider.HotelSearchPort;

/**
 * AI-facing travel capabilities.
 *
 * <p>These methods translate tool-call arguments into domain criteria and
 * delegate the actual search to provider ports. They do not contain provider
 * or booking logic.
 */
@Component
public class TravelSearchTools {

    private final FlightSearchPort flightSearchPort;
    private final HotelSearchPort hotelSearchPort;

    public TravelSearchTools(
            FlightSearchPort flightSearchPort,
            HotelSearchPort hotelSearchPort) {
        this.flightSearchPort = flightSearchPort;
        this.hotelSearchPort = hotelSearchPort;
    }

    @Tool(
            name = "searchFlights",
            value = "Searches available flights. Use this for availability, route, date, cabin, and price requests. "
                    + "Dates must use ISO-8601 format: yyyy-MM-dd. This tool does not book flights.")
    public List<FlightOption> searchFlights(
            @P("Origin airport code, for example SFO") String origin,
            @P("Destination airport code, for example TYO") String destination,
            @P("Departure date in yyyy-MM-dd format") String departureDate,
            @P("Return date in yyyy-MM-dd format; may be omitted for one-way travel") String returnDate,
            @P("Number of passengers") int passengers,
            @P("Cabin class: ECONOMY, PREMIUM_ECONOMY, BUSINESS, or FIRST") CabinClass cabinClass,
            @P("Maximum total price in the requested currency; may be omitted") BigDecimal maxPrice) {

        FlightSearchCriteria criteria = new FlightSearchCriteria(
                origin,
                destination,
                parseRequiredDate(departureDate, "departureDate"),
                parseOptionalDate(returnDate, "returnDate"),
                passengers,
                cabinClass,
                maxPrice);

        return flightSearchPort.search(criteria);
    }

    @Tool(
            name = "searchHotels",
            value = "Searches available hotels. Use this for hotel availability, area, dates, guests, and nightly budget. "
                    + "Dates must use ISO-8601 format: yyyy-MM-dd. This tool does not book hotels.")
    public List<HotelOption> searchHotels(
            @P("Destination city, for example Tokyo") String city,
            @P("Check-in date in yyyy-MM-dd format") String checkIn,
            @P("Check-out date in yyyy-MM-dd format") String checkOut,
            @P("Number of guests") int guests,
            @P("Maximum nightly rate in the requested currency; may be omitted") BigDecimal maxNightlyRate,
            @P("Preferred area, for example Shibuya; may be omitted") String area) {

        HotelSearchCriteria criteria = new HotelSearchCriteria(
                city,
                parseRequiredDate(checkIn, "checkIn"),
                parseRequiredDate(checkOut, "checkOut"),
                guests,
                maxNightlyRate,
                area);

        return hotelSearchPort.search(criteria);
    }

    private static LocalDate parseRequiredDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    fieldName + " must use yyyy-MM-dd format", exception);
        }
    }

    private static LocalDate parseOptionalDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseRequiredDate(value, fieldName);
    }
}
