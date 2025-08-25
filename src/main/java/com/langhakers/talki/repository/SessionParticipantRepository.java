package com.langhakers.talki.repository;

import com.langhakers.talki.entity.SessionParticipant;
import com.langhakers.talki.entity.ConversationSession;
import com.langhakers.talki.entity.ParticipantType;
import com.langhakers.talki.entity.ParticipantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, Long> {
    
    Optional<SessionParticipant> findBySessionAndParticipantId(ConversationSession session, String participantId);
    
    List<SessionParticipant> findBySessionAndIsActive(ConversationSession session, Boolean isActive);
    
    List<SessionParticipant> findByParticipantIdAndIsActive(String participantId, Boolean isActive);
    
    List<SessionParticipant> findBySessionAndParticipantType(ConversationSession session, ParticipantType participantType);
    
    List<SessionParticipant> findBySessionAndRole(ConversationSession session, ParticipantRole role);
    
    @Query("SELECT sp FROM SessionParticipant sp WHERE sp.session = :session AND sp.isActive = true")
    List<SessionParticipant> findActiveParticipants(@Param("session") ConversationSession session);
    
    @Query("SELECT sp FROM SessionParticipant sp WHERE sp.session = :session AND sp.role = :role AND sp.isActive = true")
    List<SessionParticipant> findActiveParticipantsByRole(@Param("session") ConversationSession session, 
                                                         @Param("role") ParticipantRole role);
    
    long countBySessionAndIsActive(ConversationSession session, Boolean isActive);
}
