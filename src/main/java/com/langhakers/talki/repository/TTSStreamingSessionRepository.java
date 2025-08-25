package com.langhakers.talki.repository;

import com.langhakers.talki.entity.TTSStreamingSession;
import com.langhakers.talki.entity.TTSRoutingConfig;
import com.langhakers.talki.entity.StreamingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TTSStreamingSessionRepository extends JpaRepository<TTSStreamingSession, Long> {
    
    Optional<TTSStreamingSession> findByStreamingSessionId(String streamingSessionId);
    
    List<TTSStreamingSession> findByRoutingConfigAndStatus(TTSRoutingConfig routingConfig, StreamingStatus status);
    
    List<TTSStreamingSession> findByStatus(StreamingStatus status);
    
    @Query("SELECT tss FROM TTSStreamingSession tss WHERE tss.status = :status AND tss.startedAt < :before")
    List<TTSStreamingSession> findStuckSessions(@Param("status") StreamingStatus status, 
                                               @Param("before") LocalDateTime before);
    
    @Query("SELECT tss FROM TTSStreamingSession tss WHERE tss.routingConfig.session.id = :sessionId ORDER BY tss.startedAt DESC")
    List<TTSStreamingSession> findBySessionIdOrderByStartedAtDesc(@Param("sessionId") Long sessionId);
    
    long countByRoutingConfigAndStatus(TTSRoutingConfig routingConfig, StreamingStatus status);
}
