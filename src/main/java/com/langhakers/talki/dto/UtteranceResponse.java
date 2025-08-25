package com.langhakers.talki.dto;

import com.langhakers.talki.entity.Speaker;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UtteranceResponse {
    private Long id;
    private Long audioId;
    private Speaker speaker;
    private String text;
    private Long startMillis;
    private Long endMillis;
    private LocalDateTime createdAt;
}

