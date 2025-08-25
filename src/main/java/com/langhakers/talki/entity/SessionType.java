package com.langhakers.talki.entity;

/**
 * Enum representing different types of conversation sessions in the TALKi system.
 */
public enum SessionType {
    /**
     * A session with multiple AI participants
     */
    MULTI_PARTY,
    
    /**
     * A session with two AI participants (traditional TALKi mode)
     */
    DUAL_PARTY,
    
    /**
     * A session with a single AI participant
     */
    SINGLE_PARTY,
    
    /**
     * A session with mixed human and AI participants
     */
    MIXED_PARTY
}
