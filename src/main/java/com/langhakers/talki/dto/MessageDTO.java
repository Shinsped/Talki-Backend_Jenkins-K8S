package com.langhakers.talki.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String senderId;
    private String senderName;
    private String roomId;
    private String content;
    private LocalDateTime timestamp;
}

