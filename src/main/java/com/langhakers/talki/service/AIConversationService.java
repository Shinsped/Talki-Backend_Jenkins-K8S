package com.langhakers.talki.service;

import com.langhakers.talki.dto.AIConversationDTO;
import com.langhakers.talki.entity.AIConversation;
import com.langhakers.talki.repository.AIConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIConversationService {

    private final AIConversationRepository aiConversationRepository;

    @Transactional
    public AIConversation saveConversation(AIConversationDTO dto) {
        log.info("AI 대화 데이터 저장 시작: type={}, content={}, role={}", 
                 dto.getType(), dto.getContent(), dto.getRole());

        AIConversation conversation = new AIConversation();
        conversation.setSessionId(dto.getSessionId());
        conversation.setUserId(dto.getUserId());
        conversation.setMessageType(dto.getType());
        conversation.setContent(dto.getContent());
        conversation.setRole(dto.getRole());
        conversation.setClientTimestamp(dto.getClientTimestamp());
        conversation.setServerTimestamp(dto.getServerTimestamp());
        conversation.setMetadata(dto.getMetadata());

        AIConversation saved = aiConversationRepository.save(conversation);
        log.info("AI 대화 데이터 저장 완료: id={}", saved.getId());
        
        return saved;
    }

    @Transactional(readOnly = true)
    public List<AIConversation> getConversationsBySession(String sessionId) {
        log.info("세션별 대화 조회: sessionId={}", sessionId);
        return aiConversationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional(readOnly = true)
    public List<AIConversation> getConversationsByUser(String userId) {
        log.info("사용자별 대화 조회: userId={}", userId);
        return aiConversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<AIConversation> getConversationsBySessionAndUser(String sessionId, String userId) {
        log.info("세션 및 사용자별 대화 조회: sessionId={}, userId={}", sessionId, userId);
        return aiConversationRepository.findBySessionIdAndUserIdOrderByCreatedAtAsc(sessionId, userId);
    }

    @Transactional(readOnly = true)
    public List<AIConversation> getConversationsByTimeRange(String sessionId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("시간 범위별 대화 조회: sessionId={}, startTime={}, endTime={}", sessionId, startTime, endTime);
        return aiConversationRepository.findBySessionIdAndTimeRange(sessionId, startTime, endTime);
    }

    @Transactional(readOnly = true)
    public List<AIConversation> getFinalMessagesOnly() {
        List<String> finalMessageTypes = List.of("final_user_request", "final_assistant_answer");
        log.info("최종 메시지만 조회: messageTypes={}", finalMessageTypes);
        return aiConversationRepository.findByMessageTypeIn(finalMessageTypes);
    }

    @Transactional(readOnly = true)
    public long getConversationCountBySession(String sessionId) {
        log.info("세션별 대화 개수 조회: sessionId={}", sessionId);
        return aiConversationRepository.countBySessionId(sessionId);
    }

    @Transactional
    public void deleteConversationsBySession(String sessionId) {
        log.info("세션별 대화 삭제: sessionId={}", sessionId);
        aiConversationRepository.deleteBySessionId(sessionId);
    }
}

