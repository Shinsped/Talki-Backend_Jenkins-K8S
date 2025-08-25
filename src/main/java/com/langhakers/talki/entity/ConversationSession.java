package com.langhakers.talki.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "conversation_session")
public class ConversationSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String sessionId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;
    
    @Column(nullable = false)
    private Integer maxParticipants;
    
    @Column(nullable = false)
    private Integer currentParticipants;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType sessionType;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime endedAt;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SessionBranch> branches = new ArrayList<>();
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SessionParticipant> participants = new ArrayList<>();
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TTSRoutingConfig> ttsConfigs = new ArrayList<>();
    
    public ConversationSession() {
        this.createdAt = LocalDateTime.now();
        this.status = SessionStatus.ACTIVE;
        this.sessionType = SessionType.MULTI_PARTY;
        this.currentParticipants = 0;
    }
    
    public ConversationSession(String sessionId, String title, Integer maxParticipants) {
        this();
        this.sessionId = sessionId;
        this.title = title;
        this.maxParticipants = maxParticipants;
    }
    
    // Custom getter methods for DTO mapping
    public String getSessionName() {
        return this.title;
    }
    
    public SessionType getSessionType() {
        return this.sessionType;
    }
    
    public Boolean getIsActive() {
        return this.status == SessionStatus.ACTIVE;
    }
}
