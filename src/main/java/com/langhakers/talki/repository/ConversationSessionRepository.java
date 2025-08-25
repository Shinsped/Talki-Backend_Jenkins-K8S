package com.langhakers.talki.repository;

import com.langhakers.talki.entity.ConversationSession;
import com.langhakers.talki.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {
    
    Optional<ConversationSession> findBySessionId(String sessionId);
    
    List<ConversationSession> findByStatus(SessionStatus status);
    
    List<ConversationSession> findByStatusAndCreatedAtAfter(SessionStatus status, LocalDateTime after);
    
    @Query("SELECT cs FROM ConversationSession cs WHERE cs.currentParticipants < cs.maxParticipants AND cs.status = :status")
    List<ConversationSession> findAvailableSessions(@Param("status") SessionStatus status);
    
    @Query("SELECT cs FROM ConversationSession cs WHERE cs.status = :status AND cs.createdAt BETWEEN :startDate AND :endDate")
    List<ConversationSession> findByStatusAndDateRange(@Param("status") SessionStatus status, 
                                                      @Param("startDate") LocalDateTime startDate, 
                                                      @Param("endDate") LocalDateTime endDate);
    
    long countByStatus(SessionStatus status);
}
