# TALKi Integration Guide: Multi-party AI Conversation Session Branching & TTS Routing

## Overview

This guide provides instructions for integrating the advanced multi-party conversation session branching and TTS streaming routing configuration system with the existing TALKi project (https://github.com/ReLuee/TALKi.git).

## TALKi Project Analysis

### Current TALKi Features
- **AI-to-AI Conversations**: Two AI agents can converse with interruption capabilities
- **Multiple TTS Engines**: ElevenLabs, Coqui, Orpheus, Kokoro
- **Multiple LLM Engines**: OpenAI, Ollama, LMStudio
- **Real-time Communication**: WebSocket-based real-time interaction
- **GPU Acceleration**: NVIDIA GPU support for performance
- **Docker Support**: Containerized deployment

### Integration Benefits
Our system will enhance TALKi with:
- **Multi-party Sessions**: Support for more than 2 participants (humans + AI agents)
- **Conversation Branching**: Topic-based conversation splitting and parallel discussions
- **Advanced TTS Routing**: Per-participant voice configuration and streaming management
- **Session Management**: Persistent session state and participant tracking
- **Scalable Architecture**: Database-backed session persistence

## Integration Architecture

### Hybrid Architecture Approach
```
┌─────────────────────────────────────────────────────────────┐
│                    TALKi Frontend                           │
│                 (WebSocket Client)                          │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────┐
│                WebSocket Gateway                            │
│            (Real-time Communication)                        │
└─────────────┬───────────────────────┬───────────────────────┘
              │                       │
┌─────────────┴─────────────┐ ┌───────┴───────────────────────┐
│     TALKi Core Engine     │ │   Session Management API      │
│   (AI-to-AI, TTS, LLM)   │ │  (Spring Boot Backend)        │
│      Python-based         │ │                               │
└───────────────────────────┘ └───────────────┬───────────────┘
                                              │
                                    ┌─────────┴─────────┐
                                    │   MySQL Database  │
                                    │  (Session State)  │
                                    └───────────────────┘
```

## Integration Steps

### Step 1: Database Integration

#### 1.1 Add Database Configuration to TALKi
Create `database/schema.sql` in TALKi project:

```sql
-- Add our conversation management tables to TALKi database
CREATE DATABASE IF NOT EXISTS talki_enhanced;
USE talki_enhanced;

-- Import all our entity schemas
SOURCE conversation_session.sql;
SOURCE session_branch.sql;
SOURCE session_participant.sql;
SOURCE tts_routing_config.sql;
SOURCE tts_streaming_session.sql;
SOURCE branch_utterance.sql;
```

#### 1.2 Update TALKi Docker Compose
Add MySQL service to TALKi's `docker-compose.yml`:

```yaml
version: '3.8'
services:
  # Existing TALKi services...
  
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: talki_root_password
      MYSQL_DATABASE: talki_enhanced
      MYSQL_USER: talki_user
      MYSQL_PASSWORD: talki_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    networks:
      - talki_network

  session_manager:
    build: ./session-manager
    environment:
      - DATABASE_URL=jdbc:mysql://mysql:3306/talki_enhanced
      - DATABASE_USER=talki_user
      - DATABASE_PASSWORD=talki_password
    depends_on:
      - mysql
    ports:
      - "8080:8080"
    networks:
      - talki_network

volumes:
  mysql_data:

networks:
  talki_network:
    driver: bridge
```

### Step 2: Session Manager Service Integration

#### 2.1 Create Session Manager Microservice
Create `session-manager/` directory in TALKi project with our Spring Boot application:

```
talki-project/
├── session-manager/
│   ├── Dockerfile
│   ├── src/main/java/com/talki/session/
│   │   ├── entity/          # Our entity classes
│   │   ├── repository/      # Our repository interfaces
│   │   ├── service/         # Our service classes
│   │   ├── controller/      # Our REST controllers
│   │   └── SessionManagerApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
```

#### 2.2 Session Manager Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/session-manager-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 2.3 Enhanced Application Configuration
```yaml
# session-manager/src/main/resources/application.yml
server:
  port: 8080

spring:
  datasource:
    url: ${DATABASE_URL:jdbc:mysql://localhost:3306/talki_enhanced}
    username: ${DATABASE_USER:talki_user}
    password: ${DATABASE_PASSWORD:talki_password}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

# TALKi Integration Settings
talki:
  integration:
    websocket-endpoint: ws://localhost:8765
    tts-engines:
      elevenlabs:
        api-key: ${ELEVENLABS_API_KEY}
      coqui:
        endpoint: http://localhost:5002
      orpheus:
        endpoint: http://localhost:5003
      kokoro:
        endpoint: http://localhost:5004
    llm-engines:
      openai:
        api-key: ${OPENAI_API_KEY}
      ollama:
        endpoint: http://localhost:11434
      lmstudio:
        endpoint: http://localhost:1234
```

### Step 3: WebSocket Integration Bridge

#### 3.1 Create WebSocket Bridge Service
Create `websocket-bridge/talki_bridge.py`:

```python
import asyncio
import websockets
import json
import requests
from typing import Dict, Set
import logging

class TALKiBridge:
    def __init__(self):
        self.session_manager_url = "http://session_manager:8080/api"
        self.active_connections: Dict[str, websockets.WebSocketServerProtocol] = {}
        self.session_participants: Dict[str, Set[str]] = {}
        
    async def handle_client(self, websocket, path):
        """Handle WebSocket connections from TALKi frontend"""
        client_id = None
        try:
            async for message in websocket:
                data = json.loads(message)
                await self.process_message(websocket, data)
        except websockets.exceptions.ConnectionClosed:
            if client_id:
                await self.handle_disconnect(client_id)
        except Exception as e:
            logging.error(f"Error handling client: {e}")
    
    async def process_message(self, websocket, data):
        """Process messages from TALKi clients"""
        message_type = data.get('type')
        
        if message_type == 'join_session':
            await self.handle_join_session(websocket, data)
        elif message_type == 'create_branch':
            await self.handle_create_branch(websocket, data)
        elif message_type == 'switch_branch':
            await self.handle_switch_branch(websocket, data)
        elif message_type == 'configure_tts':
            await self.handle_configure_tts(websocket, data)
        elif message_type == 'ai_message':
            await self.handle_ai_message(websocket, data)
    
    async def handle_join_session(self, websocket, data):
        """Handle participant joining a session"""
        session_id = data.get('session_id')
        participant_id = data.get('participant_id')
        participant_name = data.get('participant_name')
        participant_type = data.get('participant_type', 'HUMAN')
        
        # Register with session manager
        response = requests.post(f"{self.session_manager_url}/sessions/{session_id}/participants", 
                               json={
                                   'participantId': participant_id,
                                   'participantName': participant_name,
                                   'type': participant_type,
                                   'role': 'PARTICIPANT'
                               })
        
        if response.status_code == 200:
            self.active_connections[participant_id] = websocket
            if session_id not in self.session_participants:
                self.session_participants[session_id] = set()
            self.session_participants[session_id].add(participant_id)
            
            await websocket.send(json.dumps({
                'type': 'session_joined',
                'session_id': session_id,
                'participant_id': participant_id
            }))
    
    async def handle_create_branch(self, websocket, data):
        """Handle conversation branch creation"""
        session_id = data.get('session_id')
        branch_name = data.get('branch_name')
        branch_type = data.get('branch_type', 'TOPIC_SPLIT')
        created_by = data.get('created_by')
        
        response = requests.post(f"{self.session_manager_url}/branches", 
                               json={
                                   'sessionId': session_id,
                                   'branchName': branch_name,
                                   'branchType': branch_type,
                                   'createdBy': created_by
                               })
        
        if response.status_code == 200:
            branch_data = response.json()
            # Notify all session participants
            await self.broadcast_to_session(session_id, {
                'type': 'branch_created',
                'branch': branch_data
            })
    
    async def handle_configure_tts(self, websocket, data):
        """Handle TTS configuration for participants"""
        session_id = data.get('session_id')
        participant_id = data.get('participant_id')
        tts_config = data.get('tts_config')
        
        # Map TALKi TTS engines to our system
        provider_mapping = {
            'elevenlabs': 'ELEVENLABS',
            'coqui': 'CUSTOM',
            'orpheus': 'CUSTOM',
            'kokoro': 'CUSTOM'
        }
        
        provider = provider_mapping.get(tts_config.get('engine'), 'CUSTOM')
        
        response = requests.post(f"{self.session_manager_url}/tts/routing-config",
                               json={
                                   'sessionId': session_id,
                                   'participantId': participant_id,
                                   'provider': provider,
                                   'voiceId': tts_config.get('voice_id'),
                                   'language': tts_config.get('language', 'en-US'),
                                   'streamingEndpoint': tts_config.get('endpoint')
                               })
        
        if response.status_code == 200:
            await websocket.send(json.dumps({
                'type': 'tts_configured',
                'participant_id': participant_id,
                'config': response.json()
            }))
    
    async def broadcast_to_session(self, session_id: str, message: dict):
        """Broadcast message to all participants in a session"""
        if session_id in self.session_participants:
            for participant_id in self.session_participants[session_id]:
                if participant_id in self.active_connections:
                    try:
                        await self.active_connections[participant_id].send(
                            json.dumps(message)
                        )
                    except websockets.exceptions.ConnectionClosed:
                        # Clean up closed connections
                        del self.active_connections[participant_id]
                        self.session_participants[session_id].discard(participant_id)

# Start WebSocket bridge server
if __name__ == "__main__":
    bridge = TALKiBridge()
    start_server = websockets.serve(bridge.handle_client, "0.0.0.0", 8765)
    
    logging.info("TALKi WebSocket Bridge started on port 8765")
    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()
```

### Step 4: Frontend Integration

#### 4.1 Enhanced TALKi Frontend JavaScript
Add to TALKi's frontend JavaScript:

```javascript
class TALKiSessionManager {
    constructor(websocketUrl = 'ws://localhost:8765') {
        this.ws = new WebSocket(websocketUrl);
        this.currentSession = null;
        this.currentBranch = null;
        this.participantId = this.generateParticipantId();
        
        this.setupWebSocketHandlers();
    }
    
    setupWebSocketHandlers() {
        this.ws.onopen = () => {
            console.log('Connected to TALKi Session Manager');
        };
        
        this.ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
            this.handleMessage(data);
        };
        
        this.ws.onclose = () => {
            console.log('Disconnected from TALKi Session Manager');
            // Implement reconnection logic
        };
    }
    
    // Create new multi-party session
    async createSession(title, maxParticipants = 10) {
        const response = await fetch('/api/sessions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                title: title,
                description: 'TALKi Multi-party AI Conversation',
                maxParticipants: maxParticipants
            })
        });
        
        const session = await response.json();
        this.currentSession = session;
        
        // Join the session
        this.joinSession(session.sessionId);
        return session;
    }
    
    // Join existing session
    joinSession(sessionId, participantName = 'User') {
        this.ws.send(JSON.stringify({
            type: 'join_session',
            session_id: sessionId,
            participant_id: this.participantId,
            participant_name: participantName,
            participant_type: 'HUMAN'
        }));
    }
    
    // Create conversation branch
    createBranch(branchName, branchType = 'TOPIC_SPLIT') {
        if (!this.currentSession) {
            throw new Error('No active session');
        }
        
        this.ws.send(JSON.stringify({
            type: 'create_branch',
            session_id: this.currentSession.sessionId,
            branch_name: branchName,
            branch_type: branchType,
            created_by: this.participantId
        }));
    }
    
    // Configure TTS for AI participants
    configureTTS(participantId, ttsEngine, voiceId, language = 'en-US') {
        this.ws.send(JSON.stringify({
            type: 'configure_tts',
            session_id: this.currentSession.sessionId,
            participant_id: participantId,
            tts_config: {
                engine: ttsEngine,
                voice_id: voiceId,
                language: language,
                endpoint: this.getTTSEndpoint(ttsEngine)
            }
        }));
    }
    
    getTTSEndpoint(engine) {
        const endpoints = {
            'elevenlabs': 'https://api.elevenlabs.io/v1/text-to-speech',
            'coqui': 'http://localhost:5002/tts',
            'orpheus': 'http://localhost:5003/tts',
            'kokoro': 'http://localhost:5004/tts'
        };
        return endpoints[engine] || 'http://localhost:5000/tts';
    }
    
    handleMessage(data) {
        switch (data.type) {
            case 'session_joined':
                console.log('Joined session:', data.session_id);
                this.onSessionJoined(data);
                break;
            case 'branch_created':
                console.log('New branch created:', data.branch);
                this.onBranchCreated(data.branch);
                break;
            case 'participant_joined':
                console.log('Participant joined:', data.participant);
                this.onParticipantJoined(data.participant);
                break;
            case 'ai_message':
                this.onAIMessage(data);
                break;
        }
    }
    
    // Event handlers (to be implemented by TALKi frontend)
    onSessionJoined(data) {
        // Update UI to show session info
    }
    
    onBranchCreated(branch) {
        // Update UI to show new branch option
    }
    
    onParticipantJoined(participant) {
        // Update participant list in UI
    }
    
    onAIMessage(data) {
        // Handle AI message with TTS routing
    }
    
    generateParticipantId() {
        return 'participant_' + Math.random().toString(36).substr(2, 9);
    }
}

// Initialize TALKi Session Manager
const talkiSessionManager = new TALKiSessionManager();
```

### Step 5: AI Agent Integration

#### 5.1 Enhanced AI Agent Configuration
Create `ai-agents/enhanced_agent.py`:

```python
import asyncio
import json
import requests
from typing import Dict, List
import openai
from elevenlabs import generate, set_api_key

class EnhancedTALKiAgent:
    def __init__(self, agent_id: str, session_manager_url: str):
        self.agent_id = agent_id
        self.session_manager_url = session_manager_url
        self.current_session = None
        self.current_branch = None
        self.tts_config = None
        
    async def join_session(self, session_id: str, agent_name: str):
        """Join a conversation session as an AI agent"""
        response = requests.post(
            f"{self.session_manager_url}/sessions/{session_id}/participants",
            json={
                'participantId': self.agent_id,
                'participantName': agent_name,
                'type': 'AI_AGENT',
                'role': 'PARTICIPANT'
            }
        )
        
        if response.status_code == 200:
            self.current_session = session_id
            return True
        return False
    
    async def configure_tts(self, provider: str, voice_id: str, language: str = 'en-US'):
        """Configure TTS settings for this AI agent"""
        response = requests.post(
            f"{self.session_manager_url}/tts/routing-config",
            json={
                'sessionId': self.current_session,
                'participantId': self.agent_id,
                'provider': provider,
                'voiceId': voice_id,
                'language': language,
                'streamingEndpoint': self.get_tts_endpoint(provider)
            }
        )
        
        if response.status_code == 200:
            self.tts_config = response.json()
            return True
        return False
    
    async def generate_response(self, conversation_context: List[Dict], branch_context: str = None):
        """Generate AI response considering branch context"""
        # Enhanced prompt with branch awareness
        system_prompt = f"""
        You are an AI participant in a multi-party conversation.
        Current conversation branch: {branch_context or 'main'}
        
        Consider the conversation flow and branch context when responding.
        Keep responses natural and contextually appropriate.
        """
        
        # Use OpenAI or other LLM
        response = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": system_prompt},
                *conversation_context
            ]
        )
        
        return response.choices[0].message.content
    
    async def speak_with_tts(self, text: str):
        """Convert text to speech using configured TTS"""
        if not self.tts_config:
            return None
            
        # Start TTS streaming session
        response = requests.post(
            f"{self.session_manager_url}/tts/streaming",
            json={
                'routingId': self.tts_config['routingId'],
                'utteranceText': text
            }
        )
        
        if response.status_code == 200:
            streaming_session = response.json()
            return streaming_session['streamingSessionId']
        return None
    
    def get_tts_endpoint(self, provider: str):
        endpoints = {
            'ELEVENLABS': 'https://api.elevenlabs.io/v1/text-to-speech',
            'OPENAI': 'https://api.openai.com/v1/audio/speech',
            'CUSTOM': 'http://localhost:5002/tts'
        }
        return endpoints.get(provider, 'http://localhost:5000/tts')
```

### Step 6: Deployment Configuration

#### 6.1 Enhanced Docker Compose for TALKi
Update TALKi's `docker-compose.yml`:

```yaml
version: '3.8'
services:
  # Existing TALKi services...
  
  # Database
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: talki_root_password
      MYSQL_DATABASE: talki_enhanced
      MYSQL_USER: talki_user
      MYSQL_PASSWORD: talki_password
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    networks:
      - talki_network

  # Session Manager (Spring Boot)
  session-manager:
    build: ./session-manager
    environment:
      - DATABASE_URL=jdbc:mysql://mysql:3306/talki_enhanced
      - DATABASE_USER=talki_user
      - DATABASE_PASSWORD=talki_password
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - ELEVENLABS_API_KEY=${ELEVENLABS_API_KEY}
    depends_on:
      - mysql
    ports:
      - "8080:8080"
    networks:
      - talki_network

  # WebSocket Bridge
  websocket-bridge:
    build: ./websocket-bridge
    environment:
      - SESSION_MANAGER_URL=http://session-manager:8080
    depends_on:
      - session-manager
    ports:
      - "8765:8765"
    networks:
      - talki_network

  # Enhanced AI Agents
  ai-agent-1:
    build: ./ai-agents
    environment:
      - AGENT_ID=ai_agent_1
      - AGENT_NAME=TALKi Assistant Alpha
      - SESSION_MANAGER_URL=http://session-manager:8080
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - ELEVENLABS_API_KEY=${ELEVENLABS_API_KEY}
    depends_on:
      - session-manager
      - websocket-bridge
    networks:
      - talki_network

  ai-agent-2:
    build: ./ai-agents
    environment:
      - AGENT_ID=ai_agent_2
      - AGENT_NAME=TALKi Assistant Beta
      - SESSION_MANAGER_URL=http://session-manager:8080
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - ELEVENLABS_API_KEY=${ELEVENLABS_API_KEY}
    depends_on:
      - session-manager
      - websocket-bridge
    networks:
      - talki_network

volumes:
  mysql_data:

networks:
  talki_network:
    driver: bridge
```

## Usage Examples

### Example 1: Multi-party AI Discussion
```javascript
// Create a new session with multiple participants
const session = await talkiSessionManager.createSession("AI Ethics Discussion", 6);

// Configure different AI agents with different voices
talkiSessionManager.configureTTS('ai_agent_1', 'elevenlabs', 'voice_1', 'en-US');
talkiSessionManager.configureTTS('ai_agent_2', 'coqui', 'voice_2', 'en-US');

// Create topic-specific branches
talkiSessionManager.createBranch("Privacy Concerns", "TOPIC_SPLIT");
talkiSessionManager.createBranch("Technical Implementation", "TOPIC_SPLIT");
```

### Example 2: AI-to-AI with Human Moderation
```python
# AI agents join session
agent1 = EnhancedTALKiAgent('ai_agent_1', 'http://session-manager:8080')
agent2 = EnhancedTALKiAgent('ai_agent_2', 'http://session-manager:8080')

await agent1.join_session(session_id, "TALKi Assistant Alpha")
await agent2.join_session(session_id, "TALKi Assistant Beta")

# Configure different TTS for each agent
await agent1.configure_tts('ELEVENLABS', 'alloy', 'en-US')
await agent2.configure_tts('OPENAI', 'nova', 'en-US')
```

## Benefits of Integration

1. **Enhanced Scalability**: Support for unlimited participants vs. original 2-AI limit
2. **Conversation Management**: Persistent session state and branching capabilities
3. **Advanced TTS Routing**: Per-participant voice configuration and streaming
4. **Database Persistence**: Conversation history and session management
5. **API Integration**: RESTful APIs for external system integration
6. **Monitoring & Analytics**: Session metrics and performance tracking

## Migration Path

1. **Phase 1**: Deploy enhanced system alongside existing TALKi
2. **Phase 2**: Migrate existing conversations to new session management
3. **Phase 3**: Enhance frontend with branching UI components
4. **Phase 4**: Full integration and legacy system retirement

This integration maintains TALKi's core strengths while adding enterprise-grade conversation management capabilities.
