package com.langhakers.talki.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetadataRequest {
    private String sessionId;
    private String userId;
    private String serverVersion;
    private String type;
    private String payload;
}

