package com.langhakers.talki.dto;

import com.langhakers.talki.entity.Speaker;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UtteranceRequest {
    private Long audioId;
    private Speaker speaker;
    private String text;
    private Long startMillis;
    private Long endMillis;
}

