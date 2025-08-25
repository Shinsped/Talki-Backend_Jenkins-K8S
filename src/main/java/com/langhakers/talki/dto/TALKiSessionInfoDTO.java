package com.langhakers.talki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TALKiSessionInfoDTO {
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("session_name")
    private String sessionName;
    
    @JsonProperty("session_type")
    private String sessionType;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("max_participants")
    private Integer maxParticipants;
    
    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
}
