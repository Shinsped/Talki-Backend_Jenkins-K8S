package com.langhakers.talki.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "metadata")
public class Metadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private Long timestamp;

    @Column(nullable = false)
    private String userId;

    private String serverVersion;

    @Column(nullable = false)
    private String type; // e.g., final_user_request, final_assistant_answer

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload; // JSON string of the message

    public Metadata(String sessionId, Long timestamp, String userId, String serverVersion, String type, String payload) {
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.userId = userId;
        this.serverVersion = serverVersion;
        this.type = type;
        this.payload = payload;
    }

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = Instant.now().toEpochMilli();
        }
    }
}

