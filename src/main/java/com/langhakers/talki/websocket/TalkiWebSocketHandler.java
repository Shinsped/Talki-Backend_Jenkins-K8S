package com.langhakers.talki.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langhakers.talki.dto.*;
import com.langhakers.talki.service.ChatService;
import com.langhakers.talki.service.TALKiSimpleService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TalkiWebSocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionParticipants = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatService chatService;
    private final TALKiSimpleService talkiService;

    @Autowired
    public TalkiWebSocketHandler(ChatService chatService, TALKiSimpleService talkiService) {
        this.chatService = chatService;
        this.talkiService = talkiService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        System.out.println("TALKi WebSocket connection established: " + sessionId);
        
        // Send connection acknowledgment
        TALKiMessageDTO ackMessage = new TALKiMessageDTO("CONNECTION_ESTABLISHED", 
            Map.of("session_id", sessionId, "status", "connected"));
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ackMessage)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String sessionId = session.getId();
        
        try {
            TALKiMessageDTO talkiMessage = objectMapper.readValue(payload, TALKiMessageDTO.class);
            String messageType = talkiMessage.getType();
            
            TALKiResponseDTO response = null;
            
            switch (messageType) {
                case "JOIN_SESSION":
                    TALKiJoinSessionDTO joinRequest = objectMapper.convertValue(talkiMessage.getData(), TALKiJoinSessionDTO.class);
                    String participantId = joinRequest.getParticipantId();
                    sessionParticipants.put(sessionId, participantId);
                    talkiService.registerSession(participantId, session);
                    response = talkiService.handleJoinSession(joinRequest);
                    break;
                    
                case "CREATE_BRANCH":
                    TALKiCreateBranchDTO branchRequest = objectMapper.convertValue(talkiMessage.getData(), TALKiCreateBranchDTO.class);
                    response = talkiService.handleCreateBranch(branchRequest);
                    break;
                    
                case "CHARACTER_MESSAGE":
                    TALKiCharacterMessageDTO messageRequest = objectMapper.convertValue(talkiMessage.getData(), TALKiCharacterMessageDTO.class);
                    response = talkiService.handleCharacterMessage(messageRequest);
                    break;
                    
                case "CONFIGURE_TTS":
                    TALKiTTSConfigDTO ttsRequest = objectMapper.convertValue(talkiMessage.getData(), TALKiTTSConfigDTO.class);
                    response = talkiService.handleTTSConfiguration(ttsRequest);
                    break;
                    
                default:
                    // Fallback to legacy message handling
                    MessageDTO legacyMsg = objectMapper.readValue(payload, MessageDTO.class);
                    String roomId = getRoomIdFromSession(session);
                    legacyMsg.setTimestamp(LocalDateTime.now());
                    legacyMsg.setRoomId(roomId);
                    chatService.saveMessage(legacyMsg);
                    sendMessageToRoom(roomId, objectMapper.writeValueAsString(legacyMsg));
                    return;
            }
            
            // Send response back to client for TALKi messages
            if (response != null) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
            
        } catch (Exception e) {
            System.err.println("Error handling text message: " + e.getMessage());
            TALKiResponseDTO errorResponse = TALKiResponseDTO.error("MESSAGE_PROCESSING_ERROR", e.getMessage());
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
            } catch (Exception sendError) {
                System.err.println("Failed to send error response: " + sendError.getMessage());
            }
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        // DB 서버 역할이므로 바이너리 메시지 처리는 간단히 로깅만 하거나 무시
        System.out.println("Received binary message from " + session.getId() + ": " + message.getPayloadLength() + " bytes");
        // 필요한 경우, 바이너리 데이터를 저장하는 로직 추가
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("WebSocket connection closed: " + session.getId() + " with status: " + status.getCode());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error for session " + session.getId() + ": " + exception.getMessage());
    }

    // 특정 방에 메시지 전송
    private void sendMessageToRoom(String roomId, String jsonMessage) throws Exception {
        for (WebSocketSession s : sessions.values()) {
            String sessionRoomId = getRoomIdFromSession(s);
            if (s.isOpen() && sessionRoomId != null && sessionRoomId.equals(roomId)) {
                s.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }

    // 세션 URL에서 roomId 추출 (예: /ws/{roomId})
    private String getRoomIdFromSession(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        if (parts.length > 2) {
            return parts[2]; // 예: /ws/room123 -> room123
        }
        return "default"; // roomId가 없는 경우 기본값
    }
}

