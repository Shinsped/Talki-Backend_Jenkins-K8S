package com.langhakers.talki.repository;

import com.langhakers.talki.entity.AIConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AIConversationRepository extends JpaRepository<AIConversation, Long> {

    List<AIConversation> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    List<AIConversation> findByUserIdOrderByCreatedAtDesc(String userId);

    List<AIConversation> findBySessionIdAndUserIdOrderByCreatedAtAsc(String sessionId, String userId);

    @Query("SELECT a FROM AIConversation a WHERE a.sessionId = :sessionId AND a.createdAt BETWEEN :startTime AND :endTime ORDER BY a.createdAt ASC")
    List<AIConversation> findBySessionIdAndTimeRange(@Param("sessionId") String sessionId, 
                                                     @Param("startTime") LocalDateTime startTime, 
                                                     @Param("endTime") LocalDateTime endTime);

    @Query("SELECT a FROM AIConversation a WHERE a.messageType IN :messageTypes ORDER BY a.createdAt DESC")
    List<AIConversation> findByMessageTypeIn(@Param("messageTypes") List<String> messageTypes);

    long countBySessionId(String sessionId);

    void deleteBySessionId(String sessionId);
}

