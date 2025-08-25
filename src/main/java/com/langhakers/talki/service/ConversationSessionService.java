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
public class ConversationSessionService {
    
    @Autowired
    private ConversationSessionRepository sessionRepository;
    
    @Autowired
    private SessionParticipantRepository participantRepository;
    
    @Autowired
    private SessionBranchRepository branchRepository;
    
    public ConversationSession createSession(String title, String description, Integer maxParticipants) {
        String sessionId = UUID.randomUUID().toString();
        ConversationSession session = new ConversationSession(sessionId, title, maxParticipants);
        session.setDescription(description);
        
        ConversationSession savedSession = sessionRepository.save(session);
        
        // Create main branch
        createMainBranch(savedSession);
        
        return savedSession;
    }
    
    public Optional<ConversationSession> getSessionById(String sessionId) {
        return sessionRepository.findBySessionId(sessionId);
    }
    
    public List<ConversationSession> getActiveSessions() {
        return sessionRepository.findByStatus(SessionStatus.ACTIVE);
    }
    
    public List<ConversationSession> getAvailableSessions() {
        return sessionRepository.findAvailableSessions(SessionStatus.ACTIVE);
    }
    
    public ConversationSession addParticipant(String sessionId, String participantId, String participantName, 
                                            ParticipantType type, ParticipantRole role) {
        ConversationSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        
        if (session.getCurrentParticipants() >= session.getMaxParticipants()) {
            throw new RuntimeException("Session is full");
        }
        
        SessionParticipant participant = new SessionParticipant(session, participantId, participantName, type, role);
        participantRepository.save(participant);
        
        session.setCurrentParticipants(session.getCurrentParticipants() + 1);
        return sessionRepository.save(session);
    }
    
    public ConversationSession removeParticipant(String sessionId, String participantId) {
        ConversationSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        
        SessionParticipant participant = participantRepository.findBySessionAndParticipantId(session, participantId)
            .orElseThrow(() -> new RuntimeException("Participant not found"));
        
        participant.setIsActive(false);
        participant.setLeftAt(LocalDateTime.now());
        participantRepository.save(participant);
        
        session.setCurrentParticipants(Math.max(0, session.getCurrentParticipants() - 1));
        return sessionRepository.save(session);
    }
    
    public ConversationSession endSession(String sessionId) {
        ConversationSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        
        session.setStatus(SessionStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());
        
        // Deactivate all participants
        List<SessionParticipant> activeParticipants = participantRepository.findActiveParticipants(session);
        activeParticipants.forEach(p -> {
            p.setIsActive(false);
            p.setLeftAt(LocalDateTime.now());
        });
        participantRepository.saveAll(activeParticipants);
        
        return sessionRepository.save(session);
    }
    
    private void createMainBranch(ConversationSession session) {
        String branchId = UUID.randomUUID().toString();
        SessionBranch mainBranch = new SessionBranch(branchId, session, "Main", BranchType.MAIN);
        mainBranch.setSequenceOrder(0);
        branchRepository.save(mainBranch);
    }
}
