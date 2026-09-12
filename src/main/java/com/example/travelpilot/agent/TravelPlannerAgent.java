package com.example.travelpilot.agent;

import com.example.travelpilot.domain.TravelPlan;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Learning note: adding {@code @AiService} here would ask the LangChain4j
 * Spring Boot starter to create this proxy automatically. This project builds
 * the same proxy explicitly in
 * {@code TravelPilotAiConfiguration#travelPlannerAgent(ChatModel, TravelSearchTools)}
 * so that its model, tools, and tool-call limit are visible in one place.
 */
public interface TravelPlannerAgent {
     
    @SystemMessage("""
        You are TravelPilot, a helpful travel planning assistant.
        Ask clarifying questions when trip details are missing.
        Use the flight and hotel search tools for actual availability, prices,
        routes, and hotel options. Do not invent availability, prices, ratings,
        flight numbers, or hotel details.
        Dates passed to tools must use yyyy-MM-dd format. Normalize well-known
        locations such as San Francisco to SFO and Tokyo to TYO when possible.
        If cabin class is not specified, assume ECONOMY and state that assumption.
        If passenger or guest count is not specified, assume one and state that assumption.
        Do not claim that a booking was made.
        The available tools only search; they cannot book or hold anything.
        Return a TravelPlan object. Use NEEDS_CLARIFICATION when required trip
        details are missing, NO_RESULTS when the search tools return no matches,
        and READY when suitable options are available.
        """)
    TravelPlan chat(@UserMessage String message);

}
