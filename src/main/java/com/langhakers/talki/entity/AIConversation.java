package com.langhakers.talki.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ai_conversation")
public class AIConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "message_type", nullable = false)
    private String messageType; // final_user_request, final_assistant_answer, etc.

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "role")
    private String role; // assistant, AI1_Mia, AI2_Leo, user

    @Column(name = "client_timestamp")
    private Long clientTimestamp;

    @Column(name = "server_timestamp")
    private Long serverTimestamp;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON 형태의 추가 메타데이터

    public AIConversation(String sessionId, String userId, String messageType, String content, String role) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.messageType = messageType;
        this.content = content;
        this.role = role;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (serverTimestamp == null) {
            serverTimestamp = System.currentTimeMillis();
        }
    }
}

