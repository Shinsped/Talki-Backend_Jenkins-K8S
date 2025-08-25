package com.langhakers.talki.controller;

import com.langhakers.talki.dto.AIConversationDTO;
import com.langhakers.talki.entity.AIConversation;
import com.langhakers.talki.service.AIConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai-conversation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // AI 서버에서의 요청을 허용
public class AIConversationController {

    private final AIConversationService aiConversationService;

    /**
     * AI 서버에서 대화 데이터를 저장하는 엔드포인트
     * AI 서버에서 final_user_request, final_assistant_answer 등의 데이터를 전송할 때 사용
     */
    @PostMapping("/save")
    public ResponseEntity<?> saveConversation(@RequestBody AIConversationDTO dto) {
        try {
            log.info("AI 대화 데이터 저장 요청 수신: {}", dto);
            
            // 필수 필드 검증
            if (dto.getType() == null || dto.getContent() == null) {
                log.warn("필수 필드 누락: type={}, content={}", dto.getType(), dto.getContent());
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "type과 content는 필수 필드입니다."));
            }

            AIConversation saved = aiConversationService.saveConversation(dto);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "대화 데이터가 성공적으로 저장되었습니다.",
                "id", saved.getId(),
                "timestamp", saved.getCreatedAt()
            ));
            
        } catch (Exception e) {
            log.error("AI 대화 데이터 저장 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 세션별 대화 내역 조회
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AIConversation>> getConversationsBySession(@PathVariable String sessionId) {
        try {
            List<AIConversation> conversations = aiConversationService.getConversationsBySession(sessionId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("세션별 대화 조회 중 오류 발생: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자별 대화 내역 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AIConversation>> getConversationsByUser(@PathVariable String userId) {
        try {
            List<AIConversation> conversations = aiConversationService.getConversationsByUser(userId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("사용자별 대화 조회 중 오류 발생: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 세션 및 사용자별 대화 내역 조회
     */
    @GetMapping("/session/{sessionId}/user/{userId}")
    public ResponseEntity<List<AIConversation>> getConversationsBySessionAndUser(
            @PathVariable String sessionId, 
            @PathVariable String userId) {
        try {
            List<AIConversation> conversations = aiConversationService.getConversationsBySessionAndUser(sessionId, userId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("세션 및 사용자별 대화 조회 중 오류 발생: sessionId={}, userId={}", sessionId, userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 최종 메시지만 조회 (final_user_request, final_assistant_answer)
     */
    @GetMapping("/final-messages")
    public ResponseEntity<List<AIConversation>> getFinalMessagesOnly() {
        try {
            List<AIConversation> conversations = aiConversationService.getFinalMessagesOnly();
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("최종 메시지 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 세션별 대화 개수 조회
     */
    @GetMapping("/session/{sessionId}/count")
    public ResponseEntity<Map<String, Object>> getConversationCountBySession(@PathVariable String sessionId) {
        try {
            long count = aiConversationService.getConversationCountBySession(sessionId);
            return ResponseEntity.ok(Map.of("sessionId", sessionId, "count", count));
        } catch (Exception e) {
            log.error("세션별 대화 개수 조회 중 오류 발생: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 세션별 대화 삭제
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteConversationsBySession(@PathVariable String sessionId) {
        try {
            aiConversationService.deleteConversationsBySession(sessionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "세션의 모든 대화가 삭제되었습니다.",
                "sessionId", sessionId
            ));
        } catch (Exception e) {
            log.error("세션별 대화 삭제 중 오류 발생: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * AI 서버 연결 테스트용 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "message", "AI Conversation API가 정상적으로 작동 중입니다.",
            "timestamp", LocalDateTime.now()
        ));
    }
}

