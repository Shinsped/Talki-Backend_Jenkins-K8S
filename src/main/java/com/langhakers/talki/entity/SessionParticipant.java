package com.langhakers.talki.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "session_participant")
public class SessionParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationSession session;
    
    @Column(nullable = false)
    private String participantId;
    
    @Column(nullable = false)
    private String participantName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantType participantType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;
    
    @Column(nullable = false)
    private LocalDateTime joinedAt;
    
    @Column
    private LocalDateTime leftAt;
    
    @Column(nullable = false)
    private Boolean isActive;
    
    @Column
    private String connectionId;
    
    @Column(columnDefinition = "TEXT")
    private String preferences;
    
    public SessionParticipant() {
        this.joinedAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    public SessionParticipant(ConversationSession session, String participantId, String participantName, 
                            ParticipantType participantType, ParticipantRole role) {
        this();
        this.session = session;
        this.participantId = participantId;
        this.participantName = participantName;
        this.participantType = participantType;
        this.role = role;
    }
}
