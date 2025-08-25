package com.langhakers.talki.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tts_streaming_session")
public class TTSStreamingSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routing_config_id", nullable = false)
    private TTSRoutingConfig routingConfig;
    
    @Column(nullable = false, unique = true)
    private String streamingSessionId;
    
    @Column(nullable = false)
    private String utteranceText;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StreamingStatus status;
    
    @Column(nullable = false)
    private LocalDateTime startedAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column
    private LocalDateTime failedAt;
    
    @Column
    private String errorMessage;
    
    @Column
    private Long audioDataId;
    
    @Column
    private String streamingUrl;
    
    @Column
    private Integer chunkCount;
    
    @Column
    private Long totalBytes;
    
    @Column
    private Long durationMs;
    
    public TTSStreamingSession() {
        this.startedAt = LocalDateTime.now();
        this.status = StreamingStatus.PENDING;
        this.chunkCount = 0;
        this.totalBytes = 0L;
    }
    
    public TTSStreamingSession(TTSRoutingConfig routingConfig, String streamingSessionId, String utteranceText) {
        this();
        this.routingConfig = routingConfig;
        this.streamingSessionId = streamingSessionId;
        this.utteranceText = utteranceText;
    }
}
