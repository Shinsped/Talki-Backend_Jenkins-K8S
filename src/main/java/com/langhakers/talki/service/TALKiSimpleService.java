package com.langhakers.talki.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langhakers.talki.dto.*;
import com.langhakers.talki.entity.ChatMessage;
import com.langhakers.talki.entity.Speaker;
import com.langhakers.talki.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TALKiSimpleService {
    
    private static final Logger logger = LoggerFactory.getLogger(TALKiSimpleService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatMessageRepository chatMessageRepository;
    
    // Active WebSocket sessions mapped by participant ID
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    // Session participants mapped by session ID
    private final Map<String, Set<String>> sessionParticipants = new ConcurrentHashMap<>();
    // Session data storage
    private final Map<String, TALKiSessionData> sessions = new ConcurrentHashMap<>();
    // Branch data storage
    private final Map<String, TALKiBranchData> branches = new ConcurrentHashMap<>();
    // Message storage
    private final Map<String, List<TALKiMessageData>> branchMessages = new ConcurrentHashMap<>();
    
    @Autowired
    public TALKiSimpleService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }
    
    public void registerSession(String participantId, WebSocketSession session) {
        activeSessions.put(participantId, session);
    }
    
    public void unregisterSession(String participantId) {
        activeSessions.remove(participantId);
        // Remove from all session participants
        sessionParticipants.values().forEach(participants -> participants.remove(participantId));
    }
    
    public TALKiResponseDTO handleJoinSession(TALKiJoinSessionDTO joinRequest) {
        String sessionId = joinRequest.getSessionId();
        String participantId = joinRequest.getParticipantId();
        
        // Find or create session
        TALKiSessionData session = sessions.computeIfAbsent(sessionId, k -> {
            TALKiSessionData newSession = new TALKiSessionData();
            newSession.sessionId = sessionId;
            newSession.sessionName = "TALKi Session " + sessionId;
            newSession.createdAt = LocalDateTime.now();
            newSession.isActive = true;
            newSession.participants = new ArrayList<>();
            return newSession;
        });
        
        // Add participant to session
        TALKiParticipantData participant = new TALKiParticipantData();
        participant.participantId = participantId;
        participant.participantName = joinRequest.getParticipantName();
        participant.participantType = joinRequest.getParticipantType();
        participant.joinedAt = LocalDateTime.now();
        participant.isActive = true;
        
        session.participants.add(participant);
        
        // Add to session participants tracking
        sessionParticipants.computeIfAbsent(sessionId, k -> new HashSet<>()).add(participantId);
        
        // Notify other participants
        broadcastToSession(sessionId, createParticipantJoinedMessage(participant), participantId);
        
        return TALKiResponseDTO.success("SESSION_JOINED", createSessionInfoDTO(session));
    }
    
    public TALKiResponseDTO handleCreateBranch(TALKiCreateBranchDTO branchRequest) {
        String sessionId = branchRequest.getSessionId();
        String branchId = UUID.randomUUID().toString();
        
        TALKiSessionData session = sessions.get(sessionId);
        if (session == null) {
            return TALKiResponseDTO.error("SESSION_NOT_FOUND", "Session not found");
        }
        
        TALKiBranchData branch = new TALKiBranchData();
        branch.branchId = branchId;
        branch.sessionId = sessionId;
        branch.branchName = branchRequest.getBranchName();
        branch.description = branchRequest.getDescription();
        branch.branchType = branchRequest.getBranchType();
        branch.createdBy = branchRequest.getCreatedBy();
        branch.createdAt = LocalDateTime.now();
        branch.isActive = true;
        branch.parentBranchId = branchRequest.getParentBranchId();
        
        branches.put(branchId, branch);
        branchMessages.put(branchId, new ArrayList<>());
        
        // Notify session participants
        broadcastToSession(sessionId, createBranchCreatedMessage(branch), null);
        
        return TALKiResponseDTO.success("BRANCH_CREATED", createBranchInfoDTO(branch));
    }
    
    @Transactional
    public TALKiResponseDTO handleCharacterMessage(TALKiCharacterMessageDTO messageRequest) {
        try {
            String sessionId = messageRequest.getSessionId();
            String branchId = messageRequest.getBranchId();
            String characterId = messageRequest.getCharacterId();
            String content = messageRequest.getMessage();
            String messageType = messageRequest.getMessageType();
            
            logger.info("📝 메시지 처리 시작: sessionId={}, branchId={}, characterId={}, messageType={}", 
                       sessionId, branchId, characterId, messageType);
            
            // Null 체크 및 기본값 설정
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = "default_session";
            }
            if (branchId == null || branchId.trim().isEmpty()) {
                branchId = "main_branch";
            }
            if (characterId == null || characterId.trim().isEmpty()) {
                logger.error("❌ characterId가 비어있음");
                return TALKiResponseDTO.error("INVALID_CHARACTER_ID", "Character ID is required");
            }
            if (content == null || content.trim().isEmpty()) {
                logger.error("❌ 메시지 내용이 비어있음");
                return TALKiResponseDTO.error("INVALID_MESSAGE", "Message content is required");
            }
            
            // 브랜치가 메모리에 없으면 생성
            if (!branches.containsKey(branchId)) {
                logger.info("🔧 브랜치가 없어서 자동 생성: {}", branchId);
                TALKiBranchData branch = new TALKiBranchData();
                branch.branchId = branchId;
                branch.sessionId = sessionId;
                branch.branchName = "Auto-created branch";
                branch.description = "Automatically created for TALKi integration";
                branch.branchType = "MAIN";
                branch.createdBy = "system";
                branch.createdAt = LocalDateTime.now();
                branch.isActive = true;
                branches.put(branchId, branch);
                branchMessages.put(branchId, new ArrayList<>());
            }
            
            // 💾 실제 데이터베이스에 저장
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setRoomId(branchId);  // branchId를 roomId로 사용
            chatMessage.setSenderId(characterId);
            chatMessage.setSenderName(getSenderName(characterId));
            chatMessage.setContent(content);
            
            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
            logger.info("✅ DB에 메시지 저장 성공: id={}, roomId={}, senderId={}, content length={}", 
                       savedMessage.getId(), savedMessage.getRoomId(), savedMessage.getSenderId(), content.length());
            
            // 메모리에도 저장 (기존 로직 유지)
            TALKiMessageData message = new TALKiMessageData();
            message.messageId = savedMessage.getId().toString();
            message.branchId = branchId;
            message.characterId = characterId;
            message.content = content;
            message.messageType = messageType;
            message.emotion = messageRequest.getEmotion();
            message.animation = messageRequest.getAnimation();
            message.timestamp = savedMessage.getTimestamp();
            
            List<TALKiMessageData> messages = branchMessages.get(branchId);
            message.sequenceNumber = messages.size() + 1;
            messages.add(message);
            
            // Broadcast message to session participants
            TALKiMessageDTO broadcastMessage = new TALKiMessageDTO();
            broadcastMessage.setType("CHARACTER_MESSAGE");
            broadcastMessage.setData(createUtteranceInfoDTO(message));
            broadcastMessage.setTimestamp(System.currentTimeMillis());
            
            broadcastToSession(sessionId, broadcastMessage, null);
            
            return TALKiResponseDTO.success("MESSAGE_SENT", createUtteranceInfoDTO(message));
        } catch (Exception e) {
            logger.error("💥 메시지 처리 중 오류 발생", e);
            return TALKiResponseDTO.error("MESSAGE_SAVE_FAILED", "Failed to save message: " + e.getMessage());
        }
    }
    
    public TALKiResponseDTO handleTTSConfiguration(TALKiTTSConfigDTO ttsRequest) {
        String sessionId = ttsRequest.getSessionId();
        
        TALKiSessionData session = sessions.get(sessionId);
        if (session == null) {
            return TALKiResponseDTO.error("SESSION_NOT_FOUND", "Session not found");
        }
        
        TALKiTTSConfigInfoDTO configInfo = new TALKiTTSConfigInfoDTO();
        configInfo.setConfigId(UUID.randomUUID().toString());
        configInfo.setCharacterId(ttsRequest.getCharacterId());
        configInfo.setProvider(ttsRequest.getProvider());
        configInfo.setVoiceId(ttsRequest.getVoiceId());
        configInfo.setLanguageCode(ttsRequest.getLanguageCode());
        configInfo.setSpeechRate(ttsRequest.getSpeechRate());
        configInfo.setPitch(ttsRequest.getPitch());
        configInfo.setVolume(ttsRequest.getVolume());
        
        return TALKiResponseDTO.success("TTS_CONFIGURED", configInfo);
    }
    
    private String getSenderName(String characterId) {
        if (characterId == null) {
            return "Unknown";
        }
        
        switch (characterId.toLowerCase()) {
            case "user":
                return "TALKi User";
            case "assistant":
                return "AI Assistant";
            case "ai1":
                return "Mia";
            case "ai2":
                return "Leo";
            default:
                return characterId; // 원래 ID를 이름으로 사용
        }
    }
    
    private void broadcastToSession(String sessionId, TALKiMessageDTO message, String excludeParticipantId) {
        Set<String> participants = sessionParticipants.get(sessionId);
        if (participants != null) {
            for (String participantId : participants) {
                if (!participantId.equals(excludeParticipantId)) {
                    WebSocketSession session = activeSessions.get(participantId);
                    if (session != null && session.isOpen()) {
                        try {
                            String messageJson = objectMapper.writeValueAsString(message);
                            session.sendMessage(new TextMessage(messageJson));
                        } catch (Exception e) {
                            System.err.println("Failed to send message to participant " + participantId + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
    
    private TALKiMessageDTO createParticipantJoinedMessage(TALKiParticipantData participant) {
        TALKiMessageDTO message = new TALKiMessageDTO();
        message.setType("PARTICIPANT_JOINED");
        message.setData(createParticipantInfoDTO(participant));
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    private TALKiMessageDTO createBranchCreatedMessage(TALKiBranchData branch) {
        TALKiMessageDTO message = new TALKiMessageDTO();
        message.setType("BRANCH_CREATED");
        message.setData(createBranchInfoDTO(branch));
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    private TALKiSessionInfoDTO createSessionInfoDTO(TALKiSessionData session) {
        TALKiSessionInfoDTO dto = new TALKiSessionInfoDTO();
        dto.setSessionId(session.sessionId);
        dto.setSessionName(session.sessionName);
        dto.setSessionType("MULTI_PARTY");
        dto.setCreatedAt(session.createdAt.toString());
        dto.setIsActive(session.isActive);
        dto.setMaxParticipants(10);
        return dto;
    }
    
    private TALKiParticipantInfoDTO createParticipantInfoDTO(TALKiParticipantData participant) {
        TALKiParticipantInfoDTO dto = new TALKiParticipantInfoDTO();
        dto.setParticipantId(participant.participantId);
        dto.setParticipantName(participant.participantName);
        dto.setParticipantType(participant.participantType);
        dto.setJoinedAt(participant.joinedAt.toString());
        dto.setIsActive(participant.isActive);
        return dto;
    }
    
    private TALKiBranchInfoDTO createBranchInfoDTO(TALKiBranchData branch) {
        TALKiBranchInfoDTO dto = new TALKiBranchInfoDTO();
        dto.setBranchId(branch.branchId);
        dto.setBranchName(branch.branchName);
        dto.setDescription(branch.description);
        dto.setBranchType(branch.branchType);
        dto.setCreatedBy(branch.createdBy);
        dto.setCreatedAt(branch.createdAt.toString());
        dto.setIsActive(branch.isActive);
        dto.setParentBranchId(branch.parentBranchId);
        return dto;
    }
    
    private TALKiUtteranceInfoDTO createUtteranceInfoDTO(TALKiMessageData message) {
        TALKiUtteranceInfoDTO dto = new TALKiUtteranceInfoDTO();
        dto.setUtteranceId(message.messageId);
        dto.setBranchId(message.branchId);
        dto.setSpeakerId(message.characterId);
        dto.setContent(message.content);
        dto.setUtteranceType(message.messageType != null ? message.messageType : "CHARACTER_SPEECH");
        dto.setTimestamp(message.timestamp.toString());
        dto.setSequenceNumber(message.sequenceNumber);
        return dto;
    }
    
    // Simple data classes for in-memory storage
    private static class TALKiSessionData {
        String sessionId;
        String sessionName;
        LocalDateTime createdAt;
        Boolean isActive;
        List<TALKiParticipantData> participants;
    }
    
    private static class TALKiParticipantData {
        String participantId;
        String participantName;
        String participantType;
        LocalDateTime joinedAt;
        Boolean isActive;
    }
    
    private static class TALKiBranchData {
        String branchId;
        String sessionId;
        String branchName;
        String description;
        String branchType;
        String createdBy;
        LocalDateTime createdAt;
        Boolean isActive;
        String parentBranchId;
    }
    
    private static class TALKiMessageData {
        String messageId;
        String branchId;
        String characterId;
        String content;
        String messageType;
        String emotion;
        String animation;
        LocalDateTime timestamp;
        Integer sequenceNumber;
    }
}