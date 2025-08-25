package com.langhakers.talki.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "branch_utterance")
public class BranchUtterance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private SessionBranch branch;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utterance_id", nullable = false)
    private Utterance utterance;
    
    @Column(nullable = false)
    private Integer sequenceOrder;
    
    @Column(nullable = false)
    private LocalDateTime addedAt;
    
    @Column
    private String addedBy;
    
    @Column(columnDefinition = "TEXT")
    private String branchContext;
    
    // Additional fields for character messages
    @Column
    private String utteranceId;
    
    @Column
    private String speakerId;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column
    private UtteranceType utteranceType;
    
    @Column
    private LocalDateTime timestamp;
    
    @Column
    private Integer sequenceNumber;
    
    public BranchUtterance() {
        this.addedAt = LocalDateTime.now();
    }
    
    public BranchUtterance(SessionBranch branch, Utterance utterance, Integer sequenceOrder) {
        this();
        this.branch = branch;
        this.utterance = utterance;
        this.sequenceOrder = sequenceOrder;
    }
}
