package com.example.travelpilot.application;

import org.springframework.stereotype.Service;

import com.example.travelpilot.agent.TravelPlannerAgent;
import com.example.travelpilot.api.ChatRequest;
import com.example.travelpilot.api.ChatResponse;
import com.example.travelpilot.domain.TravelPlan;

@Service
public class LangChainTravelAssistant implements TravelAssistant {

    private final TravelPlannerAgent agent;

    /**
     * Creates a new instance of LangChainTravelAssistant with the specified TravelPlannerAgent.
     * agent is a LangChain4j-generated proxy for TravelPlannerAgent,
     * created by @AiService.
     * @param agent
     */
    public LangChainTravelAssistant(TravelPlannerAgent agent) {
        this.agent = agent;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        /*
         * Important learning point: most of the wiring needed by this line
         * happened once during Spring application startup, not here.
         *
         * During startup, Spring creates the ChatModel (configured to call
         * Ollama through its OpenAI-compatible API), creates TravelSearchTools,
         * and calls TravelPilotAiConfiguration#travelPlannerAgent(...). That
         * @Bean method uses AiServices.builder(...) to create a LangChain4j
         * runtime proxy implementing TravelPlannerAgent. The proxy retains the
         * model, the @Tool methods, the system prompt metadata, and the
         * maxToolCallingRoundTrips(2) setting. Spring then injects that already
         * constructed proxy into this service.
         *
         * Therefore, this is not a normal call to an application-written
         * TravelPlannerAgent implementation. It is the entry point into the
         * LangChain4j proxy. The proxy generally performs the following work:
         *
         * 1. It combines the @SystemMessage on TravelPlannerAgent with the
         *    user's message. (A conversationId is carried in our API response,
         *    but it is not automatically chat memory unless memory is added.)
         * 2. It converts the TravelPlan return type into structured-output
         *    instructions/schema so the model knows the expected response shape.
         * 3. It exposes the TravelSearchTools @Tool methods to the model as
         *    tool/function definitions, including their names, parameters, and
         *    descriptions.
         * 4. It calls ChatModel, which sends the assembled request to Ollama.
         * 5. If the model requests a tool, LangChain4j invokes the matching
         *    Java method, places the tool result into the conversation, and
         *    calls the model again. This loop is bounded by the configured
         *    maximum of two tool-calling round trips.
         * 6. When the model returns a final answer, LangChain4j parses the
         *    structured response into the TravelPlan record. A parsing or model
         *    error is propagated back through this service to the API layer.
         *
         * The result assigned below is therefore the completed result of the
         * proxy's model/tool/structured-output workflow. The next line wraps
         * it with the conversation id for the REST response.
         *
         * Learning reference: [end-to-end startup and request sequence]
         * (../../../../learning.md#end-to-end-startup-and-request-sequence)
         */
        TravelPlan travelPlan = agent.chat(request.message());
        return new ChatResponse(request.conversationId(), travelPlan);
    }

}
