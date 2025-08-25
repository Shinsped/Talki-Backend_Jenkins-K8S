package com.langhakers.talki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TALKiTTSConfigDTO {
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("character_id")
    private String characterId;
    
    @JsonProperty("provider")
    private String provider;
    
    @JsonProperty("voice_id")
    private String voiceId;
    
    @JsonProperty("language_code")
    private String languageCode;
    
    @JsonProperty("speech_rate")
    private Float speechRate;
    
    @JsonProperty("pitch")
    private Float pitch;
    
    @JsonProperty("volume")
    private Float volume;
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getCharacterId() {
        return characterId;
    }
    
    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getVoiceId() {
        return voiceId;
    }
    
    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }
    
    public String getLanguageCode() {
        return languageCode;
    }
    
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
    
    public Float getSpeechRate() {
        return speechRate;
    }
    
    public void setSpeechRate(Float speechRate) {
        this.speechRate = speechRate;
    }
    
    public Float getPitch() {
        return pitch;
    }
    
    public void setPitch(Float pitch) {
        this.pitch = pitch;
    }
    
    public Float getVolume() {
        return volume;
    }
    
    public void setVolume(Float volume) {
        this.volume = volume;
    }
}
