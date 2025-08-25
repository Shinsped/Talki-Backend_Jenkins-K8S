package com.langhakers.talki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TALKiJoinSessionDTO {
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("participant_id")
    private String participantId;
    
    @JsonProperty("participant_name")
    private String participantName;
    
    @JsonProperty("participant_type")
    private String participantType;
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getParticipantId() {
        return participantId;
    }
    
    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }
    
    public String getParticipantName() {
        return participantName;
    }
    
    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }
    
    public String getParticipantType() {
        return participantType;
    }
    
    public void setParticipantType(String participantType) {
        this.participantType = participantType;
    }
}
