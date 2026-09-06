package com.example.travelpilot.api;

public record ChatRequest(
        String conversationId,
        String message) {
}
