package com.example.travelpilot.application;

import org.springframework.stereotype.Service;

import com.example.travelpilot.agent.TravelPlannerAgent;
import com.example.travelpilot.api.ChatRequest;
import com.example.travelpilot.api.ChatResponse;

@Service
public class LangChainTravelAssistant implements TravelAssistant {

    private final TravelPlannerAgent agent;

    public LangChainTravelAssistant(TravelPlannerAgent agent) {
        this.agent = agent;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        // Implement the chat logic here using LangChain4j or any other logic
        String responseMessage = agent.chat(request.message());
        return new ChatResponse(request.conversationId(), responseMessage);
    }

}
