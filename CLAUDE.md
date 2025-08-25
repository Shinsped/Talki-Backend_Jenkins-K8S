# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Test
```bash
# Build application (compile and generate protobuf classes)
./gradlew build

# Build without running tests
./gradlew build -x test

# Run tests only
./gradlew test

# Run application locally
./gradlew bootRun

# Check code quality
./gradlew check
```

### Docker Development
```bash
# Full development environment setup
./scripts/dev-setup.sh

# Start infrastructure services only
docker-compose up -d mysql kafka zookeeper

# Start all services
docker-compose up -d

# Test environment
docker-compose -f docker-compose.test.yml up -d

# View logs
docker-compose logs -f talki-app
```

### Database Operations
```bash
# Access MySQL database
docker-compose exec mysql mysql -u talki1234 -p123456781! talki_db

# Run database migrations (handled automatically via JPA ddl-auto: update)
```

## Architecture Overview

### Core System Design
This is a **Spring Boot 3.5** application implementing an **AI multi-party conversation system** with advanced session branching and TTS routing capabilities. The system integrates with the TALKi project for AI-to-AI conversations.

### Key Components

#### 1. Multi-Party Conversation Management
- **ConversationSession**: Manages conversation sessions with participant tracking
- **SessionBranch**: Enables conversation flow branching (topic splits, parallel discussions)
- **SessionParticipant**: Tracks human/AI participants with roles and states

#### 2. AI Conversation System
- **AIConversation**: Handles AI agent interactions and message routing
- **ChatMessage/Utterance**: Manages conversational content with metadata
- **BranchUtterance**: Links utterances to specific conversation branches

#### 3. TTS Streaming & Routing
- **TTSRoutingConfig**: Per-participant voice configuration (ElevenLabs, OpenAI, etc.)
- **TTSStreamingSession**: Real-time TTS streaming session management
- **AudioData**: Audio content storage and metadata

#### 4. Communication Layer
- **gRPC Services**: High-performance communication (ai.proto, audio.proto, chat.proto, utterance.proto)
- **WebSocket**: Real-time bidirectional communication (`/ws/chat`, `/ws/audio`)
- **REST API**: Standard HTTP endpoints for session management

### Database Schema
- **MySQL 8.0** with JPA/Hibernate
- Automatic schema updates via `hibernate.ddl-auto: update`
- Key tables: `conversation_session`, `session_branch`, `session_participant`, `tts_routing_config`

### External Integrations
- **TALKi Project**: Integration with existing AI-to-AI conversation system
- **Multiple TTS Providers**: ElevenLabs, OpenAI, AWS Polly, Google Cloud, Azure
- **Kafka**: Message queue for audio/chat events (optional, configured with dummy servers)

## Key API Endpoints

### Session Management
- `POST /api/sessions` - Create conversation session
- `GET /api/sessions/{sessionId}` - Get session details
- `POST /api/sessions/{sessionId}/participants` - Add participant

### Conversation Branching
- `POST /api/branches` - Create conversation branch
- `GET /api/branches/session/{sessionId}` - Get session branches
- `POST /api/branches/{branchId}/merge` - Merge branch

### TTS Configuration
- `POST /api/tts/routing-config` - Configure TTS for participant
- `POST /api/tts/streaming` - Start TTS streaming session

### Real-time Communication
- `WebSocket: /ws/chat` - Real-time chat
- `WebSocket: /ws/audio` - Real-time audio streaming

## Development Patterns

### Entity Design
- All entities use **JPA/Hibernate** with standard annotations
- **UUID-based IDs** for external references (sessionId, branchId, etc.)
- **Enum types** for status fields (SessionStatus, BranchType, TTSProvider, etc.)
- **LocalDateTime** for timestamps with proper timezone handling

### Service Layer
- **@Transactional** methods for data consistency
- **Repository pattern** with Spring Data JPA
- **Service composition** for complex operations (session creation auto-creates main branch)

### API Design
- **REST controllers** with `@RequestMapping("/api/...")`
- **CORS enabled** with `@CrossOrigin(origins = "*")`
- **ResponseEntity** return types for proper HTTP status codes
- **Map<String, Object>** for flexible request bodies

### Configuration Management
- **Profile-based configuration** (local, docker, test)
- **Environment variable overrides** for deployment flexibility
- **gRPC server** on port 9090 with reflection enabled

## Testing

### Running Tests
```bash
# Unit tests
./gradlew test

# Integration tests (if available)
./gradlew integrationTest
```

### Test Environment
- Uses `application-test.yml` configuration
- Docker Compose test environment: `docker-compose.test.yml`
- Test database connection handled automatically

## Deployment

### Local Development
1. Run `./scripts/dev-setup.sh` for full setup
2. Ensure MySQL and Kafka are running
3. Use `./gradlew bootRun` or Docker Compose

### Docker Deployment
- **Multi-stage Dockerfile** with builder pattern
- **Docker Compose** for orchestration
- **Health checks** via Spring Boot Actuator
- **Nginx reverse proxy** configuration included

### Kubernetes
- **Kustomize-based** deployment in `k8s/` directory
- **Environment-specific overlays** (development, staging, production)
- **HPA, Ingress, ConfigMaps** properly configured

## Monitoring

### Health Checks
- Spring Boot Actuator: `http://localhost:8080/api/actuator/health`
- Database connectivity checks included
- Custom health indicators for external services

### Logging
- **SLF4J with Logback** (Spring Boot default)
- **Structured logging** in JSON format for production
- Container logs accessible via `docker-compose logs`

## Important Notes

### gRPC Integration
- **Protobuf definitions** in `src/main/proto/`
- **Generated classes** in `build/generated/source/proto/`
- gRPC services implement both streaming and unary methods

### Security Considerations
- **Database credentials** should be externalized in production
- **API keys** for TTS providers must be secured
- **CORS configuration** should be restricted for production

### Performance
- **Connection pooling** configured for database
- **JPA query optimization** with proper indexing
- **Async processing** for TTS streaming operations