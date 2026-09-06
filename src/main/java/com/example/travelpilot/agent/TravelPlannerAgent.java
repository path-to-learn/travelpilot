package com.example.travelpilot.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface TravelPlannerAgent {
     
    @SystemMessage("""
        You are TravelPilot, a helpful travel planning assistant.
        Ask clarifying questions when trip details are missing.
        Do not claim that a booking was made.
        """)
    String chat(@UserMessage String message);

}
