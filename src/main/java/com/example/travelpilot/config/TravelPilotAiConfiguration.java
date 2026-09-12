package com.example.travelpilot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.travelpilot.agent.TravelPlannerAgent;
import com.example.travelpilot.tools.TravelSearchTools;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.service.AiServices;

@Configuration
public class TravelPilotAiConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TravelPilotAiConfiguration.class);

    /**
     * Builds the LangChain4j-generated implementation of TravelPlannerAgent.
     *
     * <p>The returned object is still a runtime proxy. Explicit construction
     * lets us see and control the model, tools, and maximum tool-calling loop
     * directly instead of relying on {@code @AiService} auto-configuration.
     */
    @Bean
    TravelPlannerAgent travelPlannerAgent(
            ChatModel chatModel,
            TravelSearchTools travelSearchTools) {

        return AiServices.builder(TravelPlannerAgent.class)
                .chatModel(chatModel)
                .tools(travelSearchTools)
                .maxToolCallingRoundTrips(2)
                .build();
    }

    /**
     * Logs the LangChain4j request sent to the model, the response received,
     * and any error. The request includes messages and available tools.
     *
     * <p>Keep this logger limited to local development because prompts,
     * user messages, and tool arguments may contain sensitive information.
     */
    @Bean
    ChatModelListener llmLogger() {
        return new ChatModelListener() {
            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                log.info("LLM request provider={} request={}",
                        requestContext.modelProvider(),
                        requestContext.chatRequest());
            }

            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                log.info("LLM response provider={} response={}",
                        responseContext.modelProvider(),
                        responseContext.chatResponse());
            }

            @Override
            public void onError(ChatModelErrorContext errorContext) {
                log.error("LLM error provider={} request={}",
                        errorContext.modelProvider(),
                        errorContext.chatRequest(),
                        errorContext.error());
            }
        };
    }
}
