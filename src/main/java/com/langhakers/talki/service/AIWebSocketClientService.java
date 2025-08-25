package com.langhakers.talki.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langhakers.talki.dto.AIConversationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIWebSocketClientService {

    private final AIConversationService aiConversationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${ai.websocket.url:ws://221.163.19.142:58029/ws}")
    private String aiWebSocketUrl;
    
    @Value("${ai.websocket.auto-connect:false}")
    private boolean autoConnect;
    
    @Value("${ai.websocket.reconnect-interval:30}")
    private int reconnectInterval;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean isConnecting = false;
    private boolean shouldReconnect = true;

    // AI 서버에서 보내는 메시지 중 저장할 타입들
    private final Set<String> SAVE_MESSAGE_TYPES = Set.of(
        "final_user_request",
        "final_assistant_answer",
        "ai_conversation_paused_status"
    );

    @PostConstruct
    public void init() {
        if (autoConnect) {
            log.info("AI 웹소켓 자동 연결 시작: {}", aiWebSocketUrl);
            // 실제 웹소켓 연결은 필요시 구현
        }
    }

    @PreDestroy
    public void destroy() {
        shouldReconnect = false;
        scheduler.shutdown();
    }

    /**
     * AI 서버에서 받은 메시지를 처리하는 메서드
     * 실제 웹소켓 연결 대신 HTTP API로 데이터를 받을 수도 있음
     */
    public void handleAIMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String messageType = jsonNode.path("type").asText();

            // 저장할 메시지 타입인지 확인
            if (SAVE_MESSAGE_TYPES.contains(messageType)) {
                AIConversationDTO dto = new AIConversationDTO();
                dto.setType(messageType);
                dto.setContent(jsonNode.path("content").asText());
                dto.setRole(jsonNode.path("role").asText());
                
                // 세션 ID와 사용자 ID는 기본값 설정 (실제 구현에서는 적절히 설정)
                dto.setSessionId("default-session");
                dto.setUserId("ai-user");
                dto.setServerTimestamp(System.currentTimeMillis());

                // 추가 메타데이터가 있다면 저장
                if (jsonNode.has("is_paused")) {
                    dto.setMetadata(String.format("{\"is_paused\": %s}", jsonNode.path("is_paused").asBoolean()));
                }

                // 데이터베이스에 저장
                aiConversationService.saveConversation(dto);
                log.info("AI 메시지 저장 완료: type={}, content={}", messageType, dto.getContent());
            } else {
                log.debug("저장하지 않는 메시지 타입: {}", messageType);
            }

        } catch (Exception e) {
            log.error("AI 메시지 처리 중 오류: {}", message, e);
        }
    }

    public String getConnectionStatus() {
        return "HTTP API 모드";
    }

    /**
     * AI 서버에서 직접 호출할 수 있는 메서드
     * AI 서버에서 HTTP POST로 이 백엔드의 /api/ai-conversation/save 엔드포인트를 호출하면 됨
     */
    public void processAIData(AIConversationDTO dto) {
        try {
            aiConversationService.saveConversation(dto);
            log.info("AI 데이터 처리 완료: type={}, content={}", dto.getType(), dto.getContent());
        } catch (Exception e) {
            log.error("AI 데이터 처리 중 오류", e);
        }
    }
}

