package com.langhakers.talki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TALKiParticipantInfoDTO {
    @JsonProperty("participant_id")
    private String participantId;
    
    @JsonProperty("participant_name")
    private String participantName;
    
    @JsonProperty("participant_type")
    private String participantType;
    
    @JsonProperty("joined_at")
    private String joinedAt;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    // Getters and Setters
    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }
    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    public String getParticipantType() { return participantType; }
    public void setParticipantType(String participantType) { this.participantType = participantType; }
    public String getJoinedAt() { return joinedAt; }
    public void setJoinedAt(String joinedAt) { this.joinedAt = joinedAt; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
