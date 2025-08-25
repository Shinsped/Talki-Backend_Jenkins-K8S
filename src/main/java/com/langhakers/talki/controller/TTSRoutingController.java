package com.langhakers.talki.controller;

import com.langhakers.talki.entity.*;
import com.langhakers.talki.service.TTSRoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tts")
@CrossOrigin(origins = "*")
public class TTSRoutingController {
    
    @Autowired
    private TTSRoutingService ttsRoutingService;
    
    @PostMapping("/routing-config")
    public ResponseEntity<TTSRoutingConfig> createRoutingConfig(@RequestBody Map<String, Object> request) {
        String sessionId = (String) request.get("sessionId");
        String participantId = (String) request.get("participantId");
        TTSProvider provider = TTSProvider.valueOf((String) request.get("provider"));
        String voiceId = (String) request.get("voiceId");
        String language = (String) request.get("language");
        String streamingEndpoint = (String) request.get("streamingEndpoint");
        
        try {
            TTSRoutingConfig config = ttsRoutingService.createRoutingConfig(
                sessionId, participantId, provider, voiceId, language, streamingEndpoint);
            return ResponseEntity.ok(config);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/routing-config/{routingId}")
    public ResponseEntity<TTSRoutingConfig> getRoutingConfig(@PathVariable String routingId) {
        return ttsRoutingService.getRoutingConfig(routingId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/routing-config/session/{sessionId}")
    public ResponseEntity<List<TTSRoutingConfig>> getSessionRoutingConfigs(@PathVariable String sessionId) {
        try {
            List<TTSRoutingConfig> configs = ttsRoutingService.getActiveConfigsForSession(sessionId);
            return ResponseEntity.ok(configs);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/routing-config/session/{sessionId}/participant/{participantId}")
    public ResponseEntity<List<TTSRoutingConfig>> getParticipantRoutingConfigs(
            @PathVariable String sessionId,
            @PathVariable String participantId) {
        try {
            List<TTSRoutingConfig> configs = ttsRoutingService.getActiveConfigsForParticipant(sessionId, participantId);
            return ResponseEntity.ok(configs);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/routing-config/{routingId}")
    public ResponseEntity<TTSRoutingConfig> updateRoutingConfig(
            @PathVariable String routingId,
            @RequestBody Map<String, Object> request) {
        
        Float speed = request.get("speed") != null ? Float.valueOf(request.get("speed").toString()) : null;
        Float pitch = request.get("pitch") != null ? Float.valueOf(request.get("pitch").toString()) : null;
        Float volume = request.get("volume") != null ? Float.valueOf(request.get("volume").toString()) : null;
        AudioFormat audioFormat = request.get("audioFormat") != null ? 
            AudioFormat.valueOf((String) request.get("audioFormat")) : null;
        Integer sampleRate = request.get("sampleRate") != null ? 
            Integer.valueOf(request.get("sampleRate").toString()) : null;
        
        try {
            TTSRoutingConfig config = ttsRoutingService.updateRoutingConfig(
                routingId, speed, pitch, volume, audioFormat, sampleRate);
            return ResponseEntity.ok(config);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/routing-config/{routingId}")
    public ResponseEntity<Void> deactivateRoutingConfig(@PathVariable String routingId) {
        try {
            ttsRoutingService.deactivateRoutingConfig(routingId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/streaming")
    public ResponseEntity<TTSStreamingSession> startStreaming(@RequestBody Map<String, Object> request) {
        String routingId = (String) request.get("routingId");
        String utteranceText = (String) request.get("utteranceText");
        
        try {
            TTSStreamingSession session = ttsRoutingService.startStreaming(routingId, utteranceText);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/streaming/{streamingSessionId}/status")
    public ResponseEntity<TTSStreamingSession> updateStreamingStatus(
            @PathVariable String streamingSessionId,
            @RequestBody Map<String, Object> request) {
        
        StreamingStatus status = StreamingStatus.valueOf((String) request.get("status"));
        String errorMessage = (String) request.get("errorMessage");
        
        try {
            TTSStreamingSession session = ttsRoutingService.updateStreamingStatus(
                streamingSessionId, status, errorMessage);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/streaming/{streamingSessionId}/progress")
    public ResponseEntity<TTSStreamingSession> updateStreamingProgress(
            @PathVariable String streamingSessionId,
            @RequestBody Map<String, Object> request) {
        
        Integer chunkCount = request.get("chunkCount") != null ? 
            Integer.valueOf(request.get("chunkCount").toString()) : null;
        Long totalBytes = request.get("totalBytes") != null ? 
            Long.valueOf(request.get("totalBytes").toString()) : null;
        Long durationMs = request.get("durationMs") != null ? 
            Long.valueOf(request.get("durationMs").toString()) : null;
        
        try {
            TTSStreamingSession session = ttsRoutingService.updateStreamingProgress(
                streamingSessionId, chunkCount, totalBytes, durationMs);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/streaming/active")
    public ResponseEntity<List<TTSStreamingSession>> getActiveStreamingSessions() {
        List<TTSStreamingSession> sessions = ttsRoutingService.getActiveStreamingSessions();
        return ResponseEntity.ok(sessions);
    }
    
    @GetMapping("/streaming/stuck")
    public ResponseEntity<List<TTSStreamingSession>> getStuckStreamingSessions(
            @RequestParam(defaultValue = "30") int timeoutMinutes) {
        List<TTSStreamingSession> sessions = ttsRoutingService.getStuckStreamingSessions(timeoutMinutes);
        return ResponseEntity.ok(sessions);
    }
}
