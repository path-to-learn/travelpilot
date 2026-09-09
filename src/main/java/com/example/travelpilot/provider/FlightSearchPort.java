package com.example.travelpilot.provider;

import java.util.List;

import com.example.travelpilot.domain.FlightOption;
import com.example.travelpilot.domain.FlightSearchCriteria;

/**
 * Application-facing capability for searching flights.
 *
 * <p>The agent-facing tool will depend on this interface rather than on a
 * concrete airline API client. This is a port in a ports-and-adapters design.
 */
public interface FlightSearchPort {

    List<FlightOption> search(FlightSearchCriteria criteria);
}
