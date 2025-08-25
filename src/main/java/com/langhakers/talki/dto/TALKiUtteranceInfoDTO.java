package com.langhakers.talki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TALKiUtteranceInfoDTO {
    @JsonProperty("utterance_id")
    private String utteranceId;
    
    @JsonProperty("branch_id")
    private String branchId;
    
    @JsonProperty("speaker_id")
    private String speakerId;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("utterance_type")
    private String utteranceType;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("sequence_number")
    private Integer sequenceNumber;
    
    // Getters and Setters
    public String getUtteranceId() { return utteranceId; }
    public void setUtteranceId(String utteranceId) { this.utteranceId = utteranceId; }
    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getSpeakerId() { return speakerId; }
    public void setSpeakerId(String speakerId) { this.speakerId = speakerId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getUtteranceType() { return utteranceType; }
    public void setUtteranceType(String utteranceType) { this.utteranceType = utteranceType; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }
}
