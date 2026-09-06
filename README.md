# TravelPilot

TravelPilot is a Java and Spring Boot travel-planning assistant. It currently provides a simple chat UI backed by a LangChain4j AI Service. The application uses Ollama by default, so local development does not require an OpenAI API key or paid LLM usage.

This project is being built as a learning project for:

- Spring Boot and REST APIs
- LangChain4j AI Services and prompt design
- Agentic workflows, tools, MCP, RAG, vector search, and memory
- LangGraph4j workflow orchestration
- Multi-agent system design
- AI testing and evaluation
- Docker, PostgreSQL, gRPC, observability, and MLOps concepts

## Current status

The current vertical slice contains:

- A Spring Boot REST API at `POST /api/v1/chat`
- A browser-based chat interface served by Spring Boot
- A LangChain4j `@AiService` named `TravelPlannerAgent`
- Ollama configured through the `ollama` Spring profile
- An optional OpenAI profile for later provider comparison
- A PostgreSQL Docker Compose definition prepared for future memory and vector-search work

The application does not yet search live flights or hotels, persist conversations, or make bookings. Those capabilities will be added incrementally through typed tools and provider adapters.

## Prerequisites

Install the following before running the project:

- Java 21
- Git
- Ollama
- Docker Desktop, only if you want to start the optional PostgreSQL container

Verify Java and Maven:

```bash
java -version
./mvnw -version
```

The project targets Java 21. Make sure Maven is also using a JDK 21 installation; checking only `java -version` may not be enough because Maven can use a different `JAVA_HOME`.

## Get the project

```bash
git clone https://github.com/path-to-learn/travelpilot.git
cd travelpilot
```

## Set up Ollama

Install Ollama from [ollama.com](https://ollama.com/) or with Homebrew:

```bash
brew install ollama
```

Start the Ollama server in a separate terminal:

```bash
ollama serve
```

If the Ollama desktop application is already running, its server may already be active and this command is not required.

Download the model used by the application:

```bash
ollama pull gemma3
```

You can verify the model and test it directly:

```bash
ollama list
ollama run gemma3
```

## Start the application

Ollama is the default Spring profile, so this is sufficient:

```bash
./mvnw spring-boot:run
```

You can also activate the profile explicitly:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=ollama
```

Open the UI at [http://localhost:8080](http://localhost:8080).

The backend uses the local Ollama OpenAI-compatible endpoint:

```text
http://localhost:11434/v1
```

## Optional PostgreSQL container

PostgreSQL is included for upcoming conversation-memory and vector-search milestones. It is not required by the current chat flow.

Start only the database service with:

```bash
docker compose up -d postgres
```

The local database connection is:

```text
Database: travelpilot
User: travelpilot
Password: travelpilot_dev
Host: localhost
Port: 5432
```

These values are development-only examples and must not be used for a production deployment.

Stop the database container with:

```bash
docker compose stop postgres
```

## API usage

The chat endpoint accepts a conversation ID and message:

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H 'Content-Type: application/json' \
  -d '{"conversationId":"demo-1","message":"Plan a three-day trip to Tokyo"}'
```

Example response:

```json
{
  "conversationId": "demo-1",
  "chatResponse": "..."
}
```

## Spring profiles and LLM providers

Common application settings are in `application.properties`. Provider-specific settings are kept in profile files:

- `application-ollama.properties` — local Ollama; active by default
- `application-openai.properties` — optional OpenAI configuration

To use OpenAI instead of Ollama:

```bash
export OPENAI_API_KEY="your-api-key"
./mvnw spring-boot:run -Dspring-boot.run.profiles=openai
```

Do not commit API keys to Git. Use environment variables or a local secrets manager.

## Troubleshooting

### `connection refused` for Ollama

Start Ollama and confirm that the model exists:

```bash
ollama serve
ollama list
```

### `model not found`

Download the configured model:

```bash
ollama pull gemma3
```

### `release version 21 not supported`

Maven is using an older JDK. Compare both outputs:

```bash
java -version
./mvnw -version
```

Configure `JAVA_HOME` so both commands use Java 21.

### PostgreSQL port already in use

Another local PostgreSQL service may already be using port `5432`. Stop that service or change the port mapping in `compose.yaml`.

## Development direction

The intended architecture is:

```text
ChatController
      |
TravelAssistant
      |
Agentic workflow / LangGraph4j
      |---- Travel tools and typed service ports
      |---- Conversation memory
      |---- RAG and vector search
      |---- MCP tools
      |
Ollama or another configurable ChatModel provider
```

Live flight and hotel integrations will be introduced behind interfaces such as `FlightSearchPort` and `HotelSearchPort`. This keeps the agent independent of any particular external provider and allows mock data to be used for tests and local development.

