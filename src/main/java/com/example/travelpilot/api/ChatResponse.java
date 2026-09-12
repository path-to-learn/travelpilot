package com.example.travelpilot.api;

import com.example.travelpilot.domain.TravelPlan;

public record ChatResponse(String conversationId, TravelPlan travelPlan) {
}
