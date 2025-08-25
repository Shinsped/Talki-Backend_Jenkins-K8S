package com.langhakers.talki.repository;

import com.langhakers.talki.entity.TTSRoutingConfig;
import com.langhakers.talki.entity.ConversationSession;
import com.langhakers.talki.entity.TTSProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TTSRoutingConfigRepository extends JpaRepository<TTSRoutingConfig, Long> {
    
    Optional<TTSRoutingConfig> findByRoutingId(String routingId);
    
    List<TTSRoutingConfig> findBySessionAndIsActive(ConversationSession session, Boolean isActive);
    
    List<TTSRoutingConfig> findByParticipantIdAndIsActive(String participantId, Boolean isActive);
    
    List<TTSRoutingConfig> findByProviderAndIsActive(TTSProvider provider, Boolean isActive);
    
    @Query("SELECT trc FROM TTSRoutingConfig trc WHERE trc.session = :session AND trc.participantId = :participantId AND trc.isActive = true ORDER BY trc.priority ASC")
    List<TTSRoutingConfig> findActiveConfigsBySessionAndParticipant(@Param("session") ConversationSession session, 
                                                                   @Param("participantId") String participantId);
    
    @Query("SELECT trc FROM TTSRoutingConfig trc WHERE trc.session = :session AND trc.isActive = true ORDER BY trc.priority ASC")
    List<TTSRoutingConfig> findActiveConfigsBySession(@Param("session") ConversationSession session);
    
    Optional<TTSRoutingConfig> findTopBySessionAndParticipantIdAndIsActiveTrueOrderByPriorityAsc(ConversationSession session, String participantId);
}
