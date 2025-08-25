package com.langhakers.talki.service;

import com.langhakers.talki.entity.*;
import com.langhakers.talki.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TTSRoutingService {
    
    @Autowired
    private TTSRoutingConfigRepository routingConfigRepository;
    
    @Autowired
    private TTSStreamingSessionRepository streamingSessionRepository;
    
    @Autowired
    private ConversationSessionRepository sessionRepository;
    
    public TTSRoutingConfig createRoutingConfig(String sessionId, String participantId, TTSProvider provider,
                                              String voiceId, String language, String streamingEndpoint) {
        ConversationSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        
        // 제공업체별 설정 유효성 검증
        validateProviderConfig(provider, voiceId, language);
        
        String routingId = UUID.randomUUID().toString();
        TTSRoutingConfig config = new TTSRoutingConfig();
        config.setSession(session);
        config.setRoutingId(routingId);
        config.setParticipantId(participantId);
        config.setProvider(provider);
        config.setVoiceId(voiceId);
        config.setLanguage(language);
        config.setStreamingEndpoint(streamingEndpoint);
        config.setAudioFormat(AudioFormat.WAV);
        config.setSampleRate(44100);
        
        return routingConfigRepository.save(config);
    }
    
    public Optional<TTSRoutingConfig> getRoutingConfig(String routingId) {
        return routingConfigRepository.findByRoutingId(routingId);
    }
    
    public List<TTSRoutingConfig> getActiveConfigsForSession(String sessionId) {
        ConversationSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        return routingConfigRepository.findActiveConfigsBySession(session);
    }
    
    public List<TTSRoutingConfig> getActiveConfigsForParticipant(String sessionId, String participantId) {
        ConversationSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        return routingConfigRepository.findActiveConfigsBySessionAndParticipant(session, participantId);
    }
    
    public TTSRoutingConfig updateRoutingConfig(String routingId, Float speed, Float pitch, Float volume, 
                                              AudioFormat audioFormat, Integer sampleRate) {
        TTSRoutingConfig config = routingConfigRepository.findByRoutingId(routingId)
            .orElseThrow(() -> new RuntimeException("Routing config not found: " + routingId));
        
        if (speed != null) config.setSpeed(speed);
        if (pitch != null) config.setPitch(pitch);
        if (volume != null) config.setVolume(volume);
        if (audioFormat != null) config.setAudioFormat(audioFormat);
        if (sampleRate != null) config.setSampleRate(sampleRate);
        
        config.setUpdatedAt(LocalDateTime.now());
        return routingConfigRepository.save(config);
    }
    
    public TTSStreamingSession startStreaming(String routingId, String utteranceText) {
        TTSRoutingConfig config = routingConfigRepository.findByRoutingId(routingId)
            .orElseThrow(() -> new RuntimeException("Routing config not found: " + routingId));
        
        String streamingSessionId = UUID.randomUUID().toString();
        TTSStreamingSession streamingSession = new TTSStreamingSession(config, streamingSessionId, utteranceText);
        streamingSession.setStatus(StreamingStatus.PENDING);
        
        return streamingSessionRepository.save(streamingSession);
    }
    
    public TTSStreamingSession updateStreamingStatus(String streamingSessionId, StreamingStatus status, 
                                                   String errorMessage) {
        TTSStreamingSession session = streamingSessionRepository.findByStreamingSessionId(streamingSessionId)
            .orElseThrow(() -> new RuntimeException("Streaming session not found: " + streamingSessionId));
        
        session.setStatus(status);
        if (errorMessage != null) {
            session.setErrorMessage(errorMessage);
        }
        
        if (status == StreamingStatus.COMPLETED) {
            session.setCompletedAt(LocalDateTime.now());
        } else if (status == StreamingStatus.FAILED) {
            session.setFailedAt(LocalDateTime.now());
        }
        
        return streamingSessionRepository.save(session);
    }
    
    public TTSStreamingSession updateStreamingProgress(String streamingSessionId, Integer chunkCount, 
                                                     Long totalBytes, Long durationMs) {
        TTSStreamingSession session = streamingSessionRepository.findByStreamingSessionId(streamingSessionId)
            .orElseThrow(() -> new RuntimeException("Streaming session not found: " + streamingSessionId));
        
        if (chunkCount != null) session.setChunkCount(chunkCount);
        if (totalBytes != null) session.setTotalBytes(totalBytes);
        if (durationMs != null) session.setDurationMs(durationMs);
        
        return streamingSessionRepository.save(session);
    }
    
    public List<TTSStreamingSession> getActiveStreamingSessions() {
        return streamingSessionRepository.findByStatus(StreamingStatus.IN_PROGRESS);
    }
    
    public List<TTSStreamingSession> getStuckStreamingSessions(int timeoutMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);
        return streamingSessionRepository.findStuckSessions(StreamingStatus.IN_PROGRESS, cutoff);
    }
    
    public void deactivateRoutingConfig(String routingId) {
        TTSRoutingConfig config = routingConfigRepository.findByRoutingId(routingId)
            .orElseThrow(() -> new RuntimeException("Routing config not found: " + routingId));
        
        config.setIsActive(false);
        config.setUpdatedAt(LocalDateTime.now());
        routingConfigRepository.save(config);
    }
    
    /**
     * TTS 제공업체별 설정 유효성 검증
     */
    private void validateProviderConfig(TTSProvider provider, String voiceId, String language) {
        switch (provider) {
            case OPENAI:
                validateOpenAIConfig(voiceId, language);
                break;
            case GOOGLE_CLOUD:
                validateGoogleCloudConfig(voiceId, language);
                break;
            case AWS_POLLY:
                validateAWSPollyConfig(voiceId, language);
                break;
            case AZURE_COGNITIVE:
                validateAzureConfig(voiceId, language);
                break;
            case ELEVENLABS:
                validateElevenLabsConfig(voiceId, language);
                break;
            case CUSTOM:
                validateCustomConfig(voiceId, language);
                break;
            case LOCAL:
                validateLocalConfig(voiceId, language);
                break;
            default:
                throw new RuntimeException("Unsupported TTS provider: " + provider);
        }
    }
    
    private void validateOpenAIConfig(String voiceId, String language) {
        // OpenAI 지원 음성: alloy, echo, fable, onyx, nova, shimmer
        String[] supportedVoices = {"alloy", "echo", "fable", "onyx", "nova", "shimmer"};
        if (voiceId != null && !java.util.Arrays.asList(supportedVoices).contains(voiceId.toLowerCase())) {
            throw new RuntimeException("Unsupported OpenAI voice: " + voiceId);
        }
    }
    
    private void validateGoogleCloudConfig(String voiceId, String language) {
        // Google Cloud TTS 언어 코드 검증 (예: en-US, ko-KR)
        if (language != null && !language.matches("^[a-z]{2}-[A-Z]{2}$")) {
            throw new RuntimeException("Invalid Google Cloud language format. Expected format: xx-XX (e.g., en-US, ko-KR)");
        }
    }
    
    private void validateAWSPollyConfig(String voiceId, String language) {
        // AWS Polly 기본 검증
        if (language != null && language.length() < 2) {
            throw new RuntimeException("Invalid AWS Polly language code");
        }
    }
    
    private void validateAzureConfig(String voiceId, String language) {
        // Azure Cognitive Services 기본 검증
        if (language != null && !language.matches("^[a-z]{2}-[A-Z]{2}$")) {
            throw new RuntimeException("Invalid Azure language format. Expected format: xx-XX");
        }
    }
    
    private void validateElevenLabsConfig(String voiceId, String language) {
        // ElevenLabs 기본 검증
        if (voiceId == null || voiceId.trim().isEmpty()) {
            throw new RuntimeException("ElevenLabs requires a valid voice ID");
        }
    }
    
    private void validateCustomConfig(String voiceId, String language) {
        // Custom TTS 제공업체 기본 검증
        // 사용자 정의 TTS의 경우 최소한의 검증만 수행
        if (voiceId != null && voiceId.trim().isEmpty()) {
            throw new RuntimeException("Custom TTS voice ID cannot be empty");
        }
    }
    
    private void validateLocalConfig(String voiceId, String language) {
        // Local TTS 엔진 기본 검증
        // 로컬 TTS의 경우 시스템 의존적이므로 기본적인 검증만 수행
        if (language != null && language.trim().isEmpty()) {
            throw new RuntimeException("Local TTS language cannot be empty");
        }
    }
}
