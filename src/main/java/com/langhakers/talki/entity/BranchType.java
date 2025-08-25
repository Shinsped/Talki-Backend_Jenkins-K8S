package com.langhakers.talki.entity;

public enum BranchType {
    MAIN,           // Main conversation thread
    TOPIC_SPLIT,    // Branch created for topic divergence
    PRIVATE_CHAT,   // Private conversation branch
    AI_GENERATED,   // AI-suggested conversation branch
    USER_CREATED,   // User-initiated branch
    PARALLEL        // Parallel conversation thread
}
