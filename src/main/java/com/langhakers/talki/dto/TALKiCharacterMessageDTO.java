package com.langhakers.talki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TALKiCharacterMessageDTO {
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("branch_id")
    private String branchId;
    
    @JsonProperty("character_id")
    private String characterId;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("message_type")
    private String messageType;
    
    @JsonProperty("emotion")
    private String emotion;
    
    @JsonProperty("animation")
    private String animation;
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getBranchId() {
        return branchId;
    }
    
    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }
    
    public String getCharacterId() {
        return characterId;
    }
    
    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getEmotion() {
        return emotion;
    }
    
    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }
    
    public String getAnimation() {
        return animation;
    }
    
    public void setAnimation(String animation) {
        this.animation = animation;
    }
}
