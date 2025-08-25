package com.langhakers.talki.controller;

import com.langhakers.talki.entity.*;
import com.langhakers.talki.service.ConversationSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class ConversationSessionController {
    
    @Autowired
    private ConversationSessionService sessionService;
    
    @PostMapping
    public ResponseEntity<ConversationSession> createSession(@RequestBody Map<String, Object> request) {
        String title = (String) request.get("title");
        String description = (String) request.get("description");
        Integer maxParticipants = (Integer) request.get("maxParticipants");
        
        ConversationSession session = sessionService.createSession(title, description, maxParticipants);
        return ResponseEntity.ok(session);
    }
    
    @GetMapping("/{sessionId}")
    public ResponseEntity<ConversationSession> getSession(@PathVariable String sessionId) {
        return sessionService.getSessionById(sessionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<ConversationSession>> getActiveSessions() {
        List<ConversationSession> sessions = sessionService.getActiveSessions();
        return ResponseEntity.ok(sessions);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<ConversationSession>> getAvailableSessions() {
        List<ConversationSession> sessions = sessionService.getAvailableSessions();
        return ResponseEntity.ok(sessions);
    }
    
    @PostMapping("/{sessionId}/participants")
    public ResponseEntity<ConversationSession> addParticipant(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> request) {
        
        String participantId = (String) request.get("participantId");
        String participantName = (String) request.get("participantName");
        ParticipantType type = ParticipantType.valueOf((String) request.get("type"));
        ParticipantRole role = ParticipantRole.valueOf((String) request.get("role"));
        
        try {
            ConversationSession session = sessionService.addParticipant(sessionId, participantId, participantName, type, role);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{sessionId}/participants/{participantId}")
    public ResponseEntity<ConversationSession> removeParticipant(
            @PathVariable String sessionId,
            @PathVariable String participantId) {
        
        try {
            ConversationSession session = sessionService.removeParticipant(sessionId, participantId);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<ConversationSession> endSession(@PathVariable String sessionId) {
        try {
            ConversationSession session = sessionService.endSession(sessionId);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
