package com.langhakers.talki.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AudioRequest {
    private String fileName;
    private Long durationMillis;
}

