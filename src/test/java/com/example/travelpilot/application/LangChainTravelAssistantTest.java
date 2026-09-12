package com.example.travelpilot.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.travelpilot.agent.TravelPlannerAgent;
import com.example.travelpilot.api.ChatRequest;
import com.example.travelpilot.api.ChatResponse;
import com.example.travelpilot.domain.PlanStatus;
import com.example.travelpilot.domain.TravelPlan;

class LangChainTravelAssistantTest {

    @Test
    void wrapsStructuredPlanWithConversationId() {
        TravelPlan expectedPlan = new TravelPlan(
                PlanStatus.NEEDS_CLARIFICATION,
                "I need more information.",
                "What dates would you like to travel?",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false);

        TravelPlannerAgent agent = message -> expectedPlan;
        LangChainTravelAssistant assistant = new LangChainTravelAssistant(agent);

        ChatResponse response = assistant.chat(
                new ChatRequest("conversation-1", "Plan a trip to Tokyo"));

        assertThat(response.conversationId()).isEqualTo("conversation-1");
        assertThat(response.travelPlan()).isSameAs(expectedPlan);
    }
}
