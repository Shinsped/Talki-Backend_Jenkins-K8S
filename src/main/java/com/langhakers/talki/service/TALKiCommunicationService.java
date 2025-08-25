package com.langhakers.talki.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langhakers.talki.dto.*;
import com.langhakers.talki.entity.*;
import com.langhakers.talki.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TALKiCommunicationService {
    
    private final ConversationSessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final SessionBranchRepository branchRepository;
    private final BranchUtteranceRepository utteranceRepository;
    private final TTSRoutingConfigRepository ttsConfigRepository;
    private final ObjectMapper objectMapper;
    
    // Active WebSocket sessions mapped by participant ID
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    // Session participants mapped by session ID
    private final Map<String, Set<String>> sessionParticipants = new ConcurrentHashMap<>();
    
    @Autowired
    public TALKiCommunicationService(
            ConversationSessionRepository sessionRepository,
            SessionParticipantRepository participantRepository,
            SessionBranchRepository branchRepository,
            BranchUtteranceRepository utteranceRepository,
            TTSRoutingConfigRepository ttsConfigRepository,
            ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.participantRepository = participantRepository;
        this.branchRepository = branchRepository;
        this.utteranceRepository = utteranceRepository;
        this.ttsConfigRepository = ttsConfigRepository;
        this.objectMapper = objectMapper;
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
        ConversationSession session = sessionRepository.findBySessionId(sessionId)
                .orElseGet(() -> createNewSession(sessionId));
        
        // Create participant
        SessionParticipant participant = new SessionParticipant();
        participant.setParticipantId(participantId);
        participant.setSession(session);
        participant.setParticipantName(joinRequest.getParticipantName());
        participant.setParticipantType(ParticipantType.valueOf(joinRequest.getParticipantType()));
        participant.setJoinedAt(LocalDateTime.now());
        participant.setIsActive(true);
        
        participantRepository.save(participant);
        
        // Add to session participants tracking
        sessionParticipants.computeIfAbsent(sessionId, k -> new HashSet<>()).add(participantId);
        
        // Notify other participants
        broadcastToSession(sessionId, createParticipantJoinedMessage(participant), participantId);
        
        return TALKiResponseDTO.success("SESSION_JOINED", createSessionInfoDTO(session));
    }
    
    public TALKiResponseDTO handleCreateBranch(TALKiCreateBranchDTO branchRequest) {
        String sessionId = branchRequest.getSessionId();
        
        ConversationSession session = sessionRepository.findBySessionId(sessionId)
                .orElse(null);
        
        if (session == null) {
            return TALKiResponseDTO.error("SESSION_NOT_FOUND", "Session not found");
        }
        
        SessionBranch branch = new SessionBranch();
        branch.setSession(session);
        branch.setBranchName(branchRequest.getBranchName());
        branch.setBranchDescription(branchRequest.getDescription());
        branch.setBranchType(BranchType.valueOf(branchRequest.getBranchType()));
        branch.setCreatedBy(branchRequest.getCreatedBy());
        branch.setCreatedAt(LocalDateTime.now());
        branch.setIsActive(true);
        
        if (branchRequest.getParentBranchId() != null) {
            SessionBranch parentBranch = branchRepository.findByBranchId(branchRequest.getParentBranchId())
                    .orElse(null);
            branch.setParentBranch(parentBranch);
        }
        
        branchRepository.save(branch);
        
        // Notify session participants
        broadcastToSession(sessionId, createBranchCreatedMessage(branch), null);
        
        return TALKiResponseDTO.success("BRANCH_CREATED", createBranchInfoDTO(branch));
    }
    
    public TALKiResponseDTO handleCharacterMessage(TALKiCharacterMessageDTO messageRequest) {
        String sessionId = messageRequest.getSessionId();
        String branchId = messageRequest.getBranchId();
        
        SessionBranch branch = branchRepository.findByBranchId(branchId).orElse(null);
        if (branch == null) {
            return TALKiResponseDTO.error("BRANCH_NOT_FOUND", "Branch not found");
        }
        
        BranchUtterance utterance = new BranchUtterance();
        utterance.setBranch(branch);
        utterance.setSpeakerId(messageRequest.getCharacterId());
        utterance.setContent(messageRequest.getMessage());
        utterance.setUtteranceType(UtteranceType.CHARACTER_SPEECH);
        utterance.setTimestamp(LocalDateTime.now());
        utterance.setSequenceNumber(getNextSequenceNumber(branchId));
        
        utteranceRepository.save(utterance);
        
        // Broadcast message to session participants
        TALKiMessageDTO broadcastMessage = new TALKiMessageDTO();
        broadcastMessage.setType("CHARACTER_MESSAGE");
        broadcastMessage.setData(createUtteranceInfoDTO(utterance));
        broadcastMessage.setTimestamp(System.currentTimeMillis());
        
        broadcastToSession(sessionId, broadcastMessage, null);
        
        return TALKiResponseDTO.success("MESSAGE_SENT", createUtteranceInfoDTO(utterance));
    }
    
    public TALKiResponseDTO handleTTSConfiguration(TALKiTTSConfigDTO ttsRequest) {
        String sessionId = ttsRequest.getSessionId();
        
        ConversationSession session = sessionRepository.findBySessionId(sessionId).orElse(null);
        if (session == null) {
            return TALKiResponseDTO.error("SESSION_NOT_FOUND", "Session not found");
        }
        
        TTSRoutingConfig ttsConfig = new TTSRoutingConfig();
        ttsConfig.setSession(session);
        ttsConfig.setRoutingId("tts_" + sessionId + "_" + ttsRequest.getCharacterId());
        ttsConfig.setParticipantId(ttsRequest.getCharacterId());
        ttsConfig.setProvider(TTSProvider.valueOf(ttsRequest.getProvider()));
        ttsConfig.setVoiceId(ttsRequest.getVoiceId());
        ttsConfig.setLanguage(ttsRequest.getLanguageCode());
        ttsConfig.setSpeed(ttsRequest.getSpeechRate());
        ttsConfig.setPitch(ttsRequest.getPitch());
        ttsConfig.setVolume(ttsRequest.getVolume());
        ttsConfig.setAudioFormat(AudioFormat.MP3); // Default to MP3
        ttsConfig.setSampleRate(22050); // Default sample rate
        ttsConfig.setStreamingEndpoint("/api/tts/stream/" + ttsRequest.getCharacterId());
        ttsConfig.setCreatedAt(LocalDateTime.now());
        ttsConfig.setIsActive(true);
        
        ttsConfigRepository.save(ttsConfig);
        
        return TALKiResponseDTO.success("TTS_CONFIGURED", createTTSConfigInfoDTO(ttsConfig));
    }
    
    private ConversationSession createNewSession(String sessionId) {
        ConversationSession session = new ConversationSession();
        session.setSessionId(sessionId);
        session.setTitle("TALKi Session " + sessionId);
        session.setStatus(SessionStatus.ACTIVE);
        session.setMaxParticipants(10);
        
        return sessionRepository.save(session);
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
                            // Log error but continue
                            System.err.println("Failed to send message to participant " + participantId + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
    
    private TALKiMessageDTO createParticipantJoinedMessage(SessionParticipant participant) {
        TALKiMessageDTO message = new TALKiMessageDTO();
        message.setType("PARTICIPANT_JOINED");
        message.setData(createParticipantInfoDTO(participant));
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    private TALKiMessageDTO createBranchCreatedMessage(SessionBranch branch) {
        TALKiMessageDTO message = new TALKiMessageDTO();
        message.setType("BRANCH_CREATED");
        message.setData(createBranchInfoDTO(branch));
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    private TALKiSessionInfoDTO createSessionInfoDTO(ConversationSession session) {
        TALKiSessionInfoDTO dto = new TALKiSessionInfoDTO();
        dto.setSessionId(session.getSessionId());
        dto.setSessionName(session.getSessionName());
        dto.setSessionType(session.getSessionType().toString());
        dto.setCreatedAt(session.getCreatedAt().toString());
        dto.setIsActive(session.getIsActive());
        dto.setMaxParticipants(session.getMaxParticipants());
        return dto;
    }
    
    private TALKiParticipantInfoDTO createParticipantInfoDTO(SessionParticipant participant) {
        TALKiParticipantInfoDTO dto = new TALKiParticipantInfoDTO();
        dto.setParticipantId(participant.getParticipantId());
        dto.setParticipantName(participant.getParticipantName());
        dto.setParticipantType(participant.getParticipantType().toString());
        dto.setJoinedAt(participant.getJoinedAt().toString());
        dto.setIsActive(participant.getIsActive());
        return dto;
    }
    
    private TALKiBranchInfoDTO createBranchInfoDTO(SessionBranch branch) {
        TALKiBranchInfoDTO dto = new TALKiBranchInfoDTO();
        dto.setBranchId(branch.getBranchId());
        dto.setBranchName(branch.getBranchName());
        dto.setDescription(branch.getBranchDescription());
        dto.setBranchType(branch.getBranchType().toString());
        dto.setCreatedBy(branch.getCreatedBy());
        dto.setCreatedAt(branch.getCreatedAt().toString());
        dto.setIsActive(branch.getIsActive());
        if (branch.getParentBranch() != null) {
            dto.setParentBranchId(branch.getParentBranch().getBranchId());
        }
        return dto;
    }
    
    private TALKiUtteranceInfoDTO createUtteranceInfoDTO(BranchUtterance utterance) {
        TALKiUtteranceInfoDTO dto = new TALKiUtteranceInfoDTO();
        dto.setUtteranceId(utterance.getUtteranceId());
        dto.setBranchId(utterance.getBranch().getBranchId());
        dto.setSpeakerId(utterance.getSpeakerId());
        dto.setContent(utterance.getContent());
        dto.setUtteranceType(utterance.getUtteranceType().toString());
        dto.setTimestamp(utterance.getTimestamp().toString());
        dto.setSequenceNumber(utterance.getSequenceNumber());
        return dto;
    }
    
    private TALKiTTSConfigInfoDTO createTTSConfigInfoDTO(TTSRoutingConfig config) {
        TALKiTTSConfigInfoDTO dto = new TALKiTTSConfigInfoDTO();
        dto.setConfigId(config.getRoutingId());
        dto.setCharacterId(config.getParticipantId());
        dto.setProvider(config.getProvider().toString());
        dto.setVoiceId(config.getVoiceId());
        dto.setLanguageCode(config.getLanguage());
        dto.setSpeechRate(config.getSpeed());
        dto.setPitch(config.getPitch());
        dto.setVolume(config.getVolume());
        return dto;
    }
    
    private Integer getNextSequenceNumber(String branchId) {
        return utteranceRepository.findMaxSequenceNumberByBranchId(branchId)
                .map(max -> max + 1)
                .orElse(1);
    }
}
