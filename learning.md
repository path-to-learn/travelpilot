# TravelPilot learning path

This file is the working study plan for the TravelPilot project. Use it with the source code and Git history to decide the next learning milestone.

## Current position

- Days 1–2: Spring Boot, REST, Java 21, provider ports, and PostgreSQL Docker Compose configuration are implemented. Application-image packaging and health checks remain to be added.
- Day 3: AI Service and structured `TravelPlan` response are implemented in the current milestone.
- Day 4: flight and hotel `@Tool` methods with deterministic mock providers are implemented and verified through the UI.
- Next milestone: Days 5–6 — RAG/vector search.
- Remaining verification: run the Maven test suite locally after dependencies are available.

## Suggested 15-day learning path

| Days | Topic | Deliverable |
|---|---|---|
| 1–2 | Spring Boot, Docker, configuration | Running API with health checks and provider abstraction |
| 3 | LangChain4j fundamentals | Chat model, AI Service, prompts, structured output |
| 4 | Tools/function calling | `@Tool` methods for flight and hotel search |
| 5–6 | RAG/vector search | Policy and destination knowledge base with citations |
| 7 | Memory | Chat memory, user preferences, and task state |
| 8 | MCP | Build an MCP server for travel tools and consume it from the app |
| 9–10 | LangGraph4j | Stateful workflow with retries, branching, and bounded loops |
| 11 | Multi-agent design | Search agent, policy agent, itinerary agent, critic |
| 12 | gRPC | Separate mock availability service exposed over gRPC |
| 13 | Testing and evaluation | JUnit tests, golden dataset, tool-call tests, LLM-as-judge |
| 14 | Observability and security | OpenTelemetry traces, cost/latency metrics, prompt-injection tests |
| 15 | Demo and interview preparation | Architecture diagram, ADRs, README, Docker demo |

## Milestone rule

For each milestone, add the implementation, at least one focused test, and a short README or ADR explaining the design decision. Do not move to the next topic until the current feature can be demonstrated through the API or UI.

## End-to-end startup and request sequence

The application has two distinct lifecycles:

1. **Startup:** Spring creates and wires the reusable objects. This is where the
   `ChatModel`, tools, providers, LangChain4j proxy, service, and controller are
   initialized. Startup does not normally send a user prompt to Ollama.
2. **Request handling:** Each `POST /api/v1/chat` request travels through the
   controller, service, LangChain4j proxy, model, and—when requested by the
   model—travel search tools. The proxy then converts the final model output
   into `TravelPlan`.

The diagrams use green for startup and blue for request-time execution.

### Startup: dependency initialization and wiring

```mermaid
sequenceDiagram
    autonumber
    actor JVM
    participant Boot as SpringApplication
    participant Context as ApplicationContext
    participant AutoConfig as LangChain4j/OpenAI auto-configuration
    participant Ollama as Ollama server
    participant Providers as Mock flight/hotel providers
    participant Tools as TravelSearchTools
    participant AgentConfig as TravelPilotAiConfiguration
    participant Agent as TravelPlannerAgent proxy
    participant Assistant as LangChainTravelAssistant
    participant Controller as ChatController
    participant Tomcat as Embedded web server

    rect rgb(232, 245, 233)
        JVM->>Boot: main() / SpringApplication.run()
        Boot->>Context: Create environment and ApplicationContext
        Context->>Context: Scan @Configuration, @Component, @Service, @RestController
        Context->>AutoConfig: Apply LangChain4j starter auto-configuration
        AutoConfig->>AutoConfig: Create ChatModel bean
        Note right of AutoConfig: Configured as OpenAI-compatible client\nwith base URL http://localhost:11434/v1
        Context->>Providers: Create provider beans
        Context->>Tools: Create TravelSearchTools
        Tools->>Providers: Inject FlightSearchPort and HotelSearchPort
        Context->>AgentConfig: Resolve @Bean dependencies
        AgentConfig->>Agent: AiServices.builder(...).build()
        Note right of Agent: Proxy contains ChatModel, tools, prompts,\nand maxToolCallingRoundTrips(2)
        Context->>Assistant: Inject TravelPlannerAgent proxy
        Context->>Controller: Inject TravelAssistant
        Context->>Tomcat: Register POST /api/v1/chat
        Tomcat-->>Boot: Application ready
    end

    Note over Ollama,Tomcat: No user chat request is sent during normal bean creation.\nOllama is contacted when the proxy handles a chat call.
```

### Request: from the UI to the final `TravelPlan`

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant UI as Browser UI
    participant Controller as ChatController
    participant Assistant as LangChainTravelAssistant
    participant Proxy as TravelPlannerAgent proxy
    participant Listener as ChatModelListener
    participant Model as ChatModel
    participant Ollama as Ollama /v1/chat/completions
    participant Tools as TravelSearchTools
    participant Provider as Flight/Hotel provider

    rect rgb(227, 242, 253)
        User->>UI: Enter travel request
        UI->>Controller: POST /api/v1/chat (ChatRequest)
        Controller->>Assistant: travelAssistant.chat(request)
        Assistant->>Proxy: agent.chat(request.message())
        Proxy->>Proxy: Combine @SystemMessage + user message
        Proxy->>Proxy: Add TravelPlan structured-output schema
        Proxy->>Proxy: Add searchFlights/searchHotels tool definitions
        Proxy->>Model: chat(ChatRequest)
        Model->>Listener: onRequest(ChatModelRequestContext)
        Model->>Ollama: HTTP chat completion request
        Ollama-->>Model: Assistant response
        Model->>Listener: onResponse(ChatModelResponseContext)
        Model-->>Proxy: Return ChatResponse

        alt Model requests a travel tool
            Proxy->>Tools: Invoke selected @Tool method
            Tools->>Provider: Search using domain criteria
            Provider-->>Tools: Flight/hotel options
            Tools-->>Proxy: Tool result
            Proxy->>Model: chat(updated conversation + tool result)
            Model->>Listener: onRequest(ChatModelRequestContext)
            Model->>Ollama: Second chat completion request
            Ollama-->>Model: Final structured answer
            Model->>Listener: onResponse(ChatModelResponseContext)
            Model-->>Proxy: Return final ChatResponse
            Note over Proxy,Ollama: Tool loop is bounded by\nmaxToolCallingRoundTrips(2)
        else Model can answer without a tool
            Note over Proxy,Ollama: Proxy uses the first model response\nif no tool call is required.
        end

        Proxy->>Proxy: Parse structured output into TravelPlan
        Proxy-->>Assistant: Return TravelPlan
        Assistant->>Assistant: Wrap with conversationId
        Assistant-->>Controller: Return ChatResponse
        Controller-->>UI: HTTP 200 JSON response
        UI-->>User: Render plan, options, assumptions, warnings
    end
```

The request sequence is the runtime view of the detailed explanation above
`agent.chat(...)` in
[`LangChainTravelAssistant.java`](src/main/java/com/example/travelpilot/application/LangChainTravelAssistant.java).

## Next milestone: RAG/vector search

Build a policy assistant for baggage, cancellation, hotel cancellation, and visa/destination guidance. The response must cite the retrieved source documents and explicitly say when the knowledge base does not contain an answer.
