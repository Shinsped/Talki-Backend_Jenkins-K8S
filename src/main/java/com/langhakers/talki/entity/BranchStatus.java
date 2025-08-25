package com.langhakers.talki.entity;

public enum BranchStatus {
    ACTIVE,     // Branch is currently active
    PAUSED,     // Branch is temporarily paused
    MERGED,     // Branch has been merged back to parent
    ARCHIVED,   // Branch is archived but preserved
    DELETED     // Branch is marked for deletion
}
