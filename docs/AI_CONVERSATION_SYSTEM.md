# AI Multi-party Conversation Session Branching and TTS Streaming Routing System

## Overview

This system provides a comprehensive backend infrastructure for managing AI-powered multi-party conversations with advanced session branching capabilities and Text-to-Speech (TTS) streaming routing configuration. The system is built on Spring Boot with JPA/Hibernate for database persistence.

## Core Components

### 1. Conversation Sessions
- **Purpose**: Manage multi-party conversation sessions with participant tracking
- **Key Features**:
  - Session lifecycle management (create, active, paused, ended, archived)
  - Participant capacity control
  - Real-time participant tracking
  - Session metadata and preferences

### 2. Session Branching
- **Purpose**: Enable conversation flow branching for topic divergence and parallel discussions
- **Key Features**:
  - Hierarchical branch structure (parent-child relationships)
  - Multiple branch types (main, topic split, private chat, AI-generated, user-created, parallel)
  - Branch status management (active, paused, merged, archived, deleted)
  - Utterance sequencing within branches

### 3. TTS Streaming Routing
- **Purpose**: Configure and manage Text-to-Speech streaming for different participants
- **Key Features**:
  - Multi-provider support (Google Cloud, AWS Polly, Azure, OpenAI, ElevenLabs, Custom, Local)
  - Per-participant voice configuration
  - Audio format and quality settings
  - Real-time streaming session management
  - Priority-based routing

## Database Schema

### Core Entities

#### ConversationSession
```sql
CREATE TABLE conversation_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status ENUM('ACTIVE', 'PAUSED', 'ENDED', 'ARCHIVED') NOT NULL,
    max_participants INT NOT NULL,
    current_participants INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP
);
```

#### SessionBranch
```sql
CREATE TABLE session_branch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id VARCHAR(255) NOT NULL UNIQUE,
    session_id BIGINT NOT NULL,
    parent_branch_id BIGINT,
    branch_name VARCHAR(255) NOT NULL,
    branch_description TEXT,
    branch_type ENUM('MAIN', 'TOPIC_SPLIT', 'PRIVATE_CHAT', 'AI_GENERATED', 'USER_CREATED', 'PARALLEL') NOT NULL,
    branch_status ENUM('ACTIVE', 'PAUSED', 'MERGED', 'ARCHIVED', 'DELETED') NOT NULL,
    sequence_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    merged_at TIMESTAMP,
    created_by VARCHAR(255),
    FOREIGN KEY (session_id) REFERENCES conversation_session(id),
    FOREIGN KEY (parent_branch_id) REFERENCES session_branch(id)
);
```

#### SessionParticipant
```sql
CREATE TABLE session_participant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    participant_id VARCHAR(255) NOT NULL,
    participant_name VARCHAR(255) NOT NULL,
    participant_type ENUM('HUMAN', 'AI_AGENT', 'BOT', 'SYSTEM') NOT NULL,
    role ENUM('HOST', 'PARTICIPANT', 'OBSERVER', 'ADMIN', 'GUEST') NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    left_at TIMESTAMP,
    is_active BOOLEAN NOT NULL,
    connection_id VARCHAR(255),
    preferences TEXT,
    FOREIGN KEY (session_id) REFERENCES conversation_session(id)
);
```

#### TTSRoutingConfig
```sql
CREATE TABLE tts_routing_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    routing_id VARCHAR(255) NOT NULL,
    participant_id VARCHAR(255) NOT NULL,
    provider ENUM('GOOGLE_CLOUD', 'AWS_POLLY', 'AZURE_COGNITIVE', 'OPENAI', 'ELEVENLABS', 'CUSTOM', 'LOCAL') NOT NULL,
    voice_id VARCHAR(255) NOT NULL,
    language VARCHAR(10) NOT NULL,
    speed FLOAT NOT NULL DEFAULT 1.0,
    pitch FLOAT NOT NULL DEFAULT 1.0,
    volume FLOAT NOT NULL DEFAULT 1.0,
    audio_format ENUM('MP3', 'WAV', 'OGG', 'FLAC', 'AAC', 'WEBM', 'PCM') NOT NULL,
    sample_rate INT NOT NULL,
    streaming_endpoint VARCHAR(500) NOT NULL,
    routing_rules TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    priority INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES conversation_session(id)
);
```

#### TTSStreamingSession
```sql
CREATE TABLE tts_streaming_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    routing_config_id BIGINT NOT NULL,
    streaming_session_id VARCHAR(255) NOT NULL UNIQUE,
    utterance_text TEXT NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'CANCELLED', 'TIMEOUT') NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message TEXT,
    audio_data_id BIGINT,
    streaming_url VARCHAR(500),
    chunk_count INT DEFAULT 0,
    total_bytes BIGINT DEFAULT 0,
    duration_ms BIGINT,
    FOREIGN KEY (routing_config_id) REFERENCES tts_routing_config(id)
);
```

## API Endpoints

### Conversation Session Management

#### Create Session
```http
POST /api/sessions
Content-Type: application/json

{
    "title": "AI Strategy Discussion",
    "description": "Multi-party discussion on AI implementation",
    "maxParticipants": 10
}
```

#### Get Session
```http
GET /api/sessions/{sessionId}
```

#### Add Participant
```http
POST /api/sessions/{sessionId}/participants
Content-Type: application/json

{
    "participantId": "user123",
    "participantName": "John Doe",
    "type": "HUMAN",
    "role": "PARTICIPANT"
}
```

#### End Session
```http
POST /api/sessions/{sessionId}/end
```

### Session Branch Management

#### Create Branch
```http
POST /api/branches
Content-Type: application/json

{
    "sessionId": "session-uuid",
    "branchName": "Technical Discussion",
    "description": "Deep dive into technical implementation",
    "branchType": "TOPIC_SPLIT",
    "parentBranchId": "parent-branch-uuid",
    "createdBy": "user123"
}
```

#### Get Session Branches
```http
GET /api/branches/session/{sessionId}
```

#### Merge Branch
```http
POST /api/branches/{branchId}/merge
Content-Type: application/json

{
    "mergedBy": "user123"
}
```

### TTS Routing Configuration

#### Create Routing Config
```http
POST /api/tts/routing-config
Content-Type: application/json

{
    "sessionId": "session-uuid",
    "participantId": "user123",
    "provider": "OPENAI",
    "voiceId": "alloy",
    "language": "en-US",
    "streamingEndpoint": "https://api.openai.com/v1/audio/speech"
}
```

#### Start TTS Streaming
```http
POST /api/tts/streaming
Content-Type: application/json

{
    "routingId": "routing-uuid",
    "utteranceText": "Hello, this is a test message for TTS streaming."
}
```

#### Update Streaming Progress
```http
PUT /api/tts/streaming/{streamingSessionId}/progress
Content-Type: application/json

{
    "chunkCount": 5,
    "totalBytes": 12345,
    "durationMs": 3000
}
```

## Configuration Options

### TTS Provider Configuration

#### OpenAI Configuration
```yaml
tts:
  providers:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com/v1
      voices: [alloy, echo, fable, onyx, nova, shimmer]
      formats: [mp3, opus, aac, flac]
      speeds: [0.25, 4.0]
```

#### Google Cloud Configuration
```yaml
tts:
  providers:
    google-cloud:
      credentials-path: ${GOOGLE_CLOUD_CREDENTIALS}
      project-id: ${GOOGLE_CLOUD_PROJECT}
      voices: [en-US-Wavenet-A, en-US-Wavenet-B, en-US-Wavenet-C]
      formats: [LINEAR16, MP3, OGG_OPUS]
```

#### AWS Polly Configuration
```yaml
tts:
  providers:
    aws-polly:
      access-key: ${AWS_ACCESS_KEY}
      secret-key: ${AWS_SECRET_KEY}
      region: ${AWS_REGION}
      voices: [Joanna, Matthew, Amy, Brian]
      formats: [mp3, ogg_vorbis, pcm]
```

### Session Configuration

#### Default Session Settings
```yaml
conversation:
  session:
    default-max-participants: 10
    auto-create-main-branch: true
    branch-depth-limit: 5
    participant-timeout-minutes: 30
    session-timeout-hours: 24
```

#### Branch Configuration
```yaml
conversation:
  branching:
    max-branches-per-session: 20
    auto-merge-inactive-branches: true
    branch-inactivity-timeout-minutes: 60
    preserve-merged-branches: true
```

### Streaming Configuration

#### Audio Quality Settings
```yaml
tts:
  streaming:
    default-sample-rate: 44100
    default-format: MP3
    chunk-size-bytes: 4096
    buffer-size-ms: 500
    timeout-seconds: 30
```

#### Performance Settings
```yaml
tts:
  performance:
    max-concurrent-streams: 50
    queue-size: 1000
    retry-attempts: 3
    circuit-breaker-threshold: 10
```

## Usage Examples

### Creating a Multi-party AI Discussion

1. **Create Session**
```java
ConversationSession session = sessionService.createSession(
    "AI Ethics Discussion", 
    "Multi-stakeholder discussion on AI ethics", 
    8
);
```

2. **Add Participants**
```java
sessionService.addParticipant(session.getSessionId(), "human1", "Alice", 
    ParticipantType.HUMAN, ParticipantRole.HOST);
sessionService.addParticipant(session.getSessionId(), "ai1", "AI Assistant", 
    ParticipantType.AI_AGENT, ParticipantRole.PARTICIPANT);
```

3. **Create Topic Branch**
```java
SessionBranch ethicsBranch = branchService.createBranch(
    session.getSessionId(), 
    "Privacy Concerns", 
    "Discussion on privacy implications",
    BranchType.TOPIC_SPLIT, 
    mainBranchId, 
    "human1"
);
```

4. **Configure TTS for AI Participant**
```java
TTSRoutingConfig config = ttsRoutingService.createRoutingConfig(
    session.getSessionId(),
    "ai1",
    TTSProvider.OPENAI,
    "alloy",
    "en-US",
    "https://api.openai.com/v1/audio/speech"
);
```

### Monitoring and Management

#### Check Active Sessions
```java
List<ConversationSession> activeSessions = sessionService.getActiveSessions();
```

#### Monitor TTS Streaming
```java
List<TTSStreamingSession> activeStreams = ttsRoutingService.getActiveStreamingSessions();
List<TTSStreamingSession> stuckStreams = ttsRoutingService.getStuckStreamingSessions(30);
```

## Best Practices

1. **Session Management**
   - Always set appropriate participant limits
   - Monitor session duration and implement timeouts
   - Clean up ended sessions periodically

2. **Branch Management**
   - Limit branch depth to prevent complexity
   - Merge inactive branches to maintain performance
   - Use descriptive branch names and descriptions

3. **TTS Configuration**
   - Test voice configurations before production use
   - Implement fallback providers for reliability
   - Monitor streaming performance and adjust settings

4. **Performance Optimization**
   - Use database indexing on frequently queried fields
   - Implement caching for session and routing configurations
   - Monitor and tune connection pool settings

## Security Considerations

1. **Authentication & Authorization**
   - Implement proper participant authentication
   - Use role-based access control for session management
   - Secure TTS provider API keys

2. **Data Protection**
   - Encrypt sensitive conversation data
   - Implement data retention policies
   - Secure audio streaming endpoints

3. **Rate Limiting**
   - Implement rate limits for API endpoints
   - Control TTS streaming requests per participant
   - Monitor for abuse patterns
