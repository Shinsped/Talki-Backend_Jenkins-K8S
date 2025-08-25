package com.langhakers.talki.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private String senderId;

    private String senderName;

    @Column(columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public ChatMessage(String roomId, String senderId, String senderName, String content) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
    }

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

