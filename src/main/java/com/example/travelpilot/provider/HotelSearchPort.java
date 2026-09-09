package com.example.travelpilot.provider;

import java.util.List;

import com.example.travelpilot.domain.HotelOption;
import com.example.travelpilot.domain.HotelSearchCriteria;

/**
 * Application-facing capability for searching hotels.
 */
public interface HotelSearchPort {

    List<HotelOption> search(HotelSearchCriteria criteria);
}
