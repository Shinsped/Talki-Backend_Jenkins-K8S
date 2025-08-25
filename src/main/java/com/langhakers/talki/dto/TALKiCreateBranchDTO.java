package com.langhakers.talki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TALKiCreateBranchDTO {
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("branch_name")
    private String branchName;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("branch_type")
    private String branchType;
    
    @JsonProperty("parent_branch_id")
    private String parentBranchId;
    
    @JsonProperty("created_by")
    private String createdBy;
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getBranchName() {
        return branchName;
    }
    
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getBranchType() {
        return branchType;
    }
    
    public void setBranchType(String branchType) {
        this.branchType = branchType;
    }
    
    public String getParentBranchId() {
        return parentBranchId;
    }
    
    public void setParentBranchId(String parentBranchId) {
        this.parentBranchId = parentBranchId;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
