package com.langhakers.talki.entity;

public enum StreamingStatus {
    PENDING,        // Streaming request is pending
    IN_PROGRESS,    // Currently streaming
    COMPLETED,      // Streaming completed successfully
    FAILED,         // Streaming failed
    CANCELLED,      // Streaming was cancelled
    TIMEOUT         // Streaming timed out
}
