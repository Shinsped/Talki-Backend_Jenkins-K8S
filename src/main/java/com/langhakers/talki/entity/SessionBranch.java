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
@Table(name = "session_branch")
public class SessionBranch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String branchId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationSession session;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_branch_id")
    private SessionBranch parentBranch;
    
    @OneToMany(mappedBy = "parentBranch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SessionBranch> childBranches = new ArrayList<>();
    
    @Column(nullable = false)
    private String branchName;
    
    @Column(columnDefinition = "TEXT")
    private String branchDescription;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BranchType branchType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BranchStatus branchStatus;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime mergedAt;
    
    @Column
    private String createdBy;
    
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BranchUtterance> utterances = new ArrayList<>();
    
    @Column(nullable = false)
    private Integer sequenceOrder;
    
    @Column(nullable = false)
    private Boolean isActive;
    
    public SessionBranch() {
        this.createdAt = LocalDateTime.now();
        this.branchStatus = BranchStatus.ACTIVE;
        this.isActive = true;
    }
    
    public SessionBranch(String branchId, ConversationSession session, String branchName, BranchType branchType) {
        this();
        this.branchId = branchId;
        this.session = session;
        this.branchName = branchName;
        this.branchType = branchType;
    }
}
