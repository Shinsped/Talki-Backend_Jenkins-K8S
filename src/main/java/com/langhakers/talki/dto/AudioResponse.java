package com.langhakers.talki.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@Builder
public class AudioResponse {
    private Long id;
    private String fileName;
    private String fileUrl;
    private Long durationMillis;
    private LocalDateTime createdAt;
}

