package com.langhakers.talki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TALKiMessageDTO {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("data")
    private Object data;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    public TALKiMessageDTO() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public TALKiMessageDTO(String type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
