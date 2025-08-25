package com.langhakers.talki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TALKiResponseDTO {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private Object data;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    public TALKiResponseDTO() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public TALKiResponseDTO(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public static TALKiResponseDTO success(String message, Object data) {
        return new TALKiResponseDTO("SUCCESS", message, data);
    }
    
    public static TALKiResponseDTO error(String message, Object data) {
        return new TALKiResponseDTO("ERROR", message, data);
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
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
