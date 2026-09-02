package com.example.travelpilot.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travelpilot.application.TravelAssistant;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final TravelAssistant travelAssistant;

    public ChatController(TravelAssistant travelAssistant) {
        this.travelAssistant = travelAssistant;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return travelAssistant.chat(request);
    }
}
