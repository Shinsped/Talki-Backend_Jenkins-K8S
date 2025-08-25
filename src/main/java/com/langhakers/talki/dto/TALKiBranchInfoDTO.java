package com.langhakers.talki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TALKiBranchInfoDTO {
    @JsonProperty("branch_id")
    private String branchId;
    
    @JsonProperty("branch_name")
    private String branchName;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("branch_type")
    private String branchType;
    
    @JsonProperty("created_by")
    private String createdBy;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("parent_branch_id")
    private String parentBranchId;
    
    // Getters and Setters
    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBranchType() { return branchType; }
    public void setBranchType(String branchType) { this.branchType = branchType; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public String getParentBranchId() { return parentBranchId; }
    public void setParentBranchId(String parentBranchId) { this.parentBranchId = parentBranchId; }
}
