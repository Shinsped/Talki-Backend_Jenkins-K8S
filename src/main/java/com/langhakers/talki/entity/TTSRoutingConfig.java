package com.langhakers.talki.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tts_routing_config")
public class TTSRoutingConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationSession session;
    
    @Column(nullable = false)
    private String routingId;
    
    @Column(nullable = false)
    private String participantId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TTSProvider provider;
    
    @Column(nullable = false)
    private String voiceId;
    
    @Column(nullable = false)
    private String language;
    
    @Column(nullable = false)
    private Float speed;
    
    @Column(nullable = false)
    private Float pitch;
    
    @Column(nullable = false)
    private Float volume;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AudioFormat audioFormat;
    
    @Column(nullable = false)
    private Integer sampleRate;
    
    @Column(nullable = false)
    private String streamingEndpoint;
    
    @Column(columnDefinition = "TEXT")
    private String routingRules;
    
    @Column(nullable = false)
    private Boolean isActive;
    
    @Column(nullable = false)
    private Integer priority;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    public TTSRoutingConfig() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.speed = 1.0f;
        this.pitch = 1.0f;
        this.volume = 1.0f;
        this.priority = 1;
    }
}
