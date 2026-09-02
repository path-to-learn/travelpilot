package com.example.travelpilot.application;

import com.example.travelpilot.api.ChatRequest;
import com.example.travelpilot.api.ChatResponse;

public interface TravelAssistant {
        ChatResponse chat(ChatRequest request);
}
