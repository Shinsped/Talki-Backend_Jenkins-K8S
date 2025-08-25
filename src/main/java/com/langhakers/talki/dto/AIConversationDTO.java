package com.langhakers.talki.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AIConversationDTO {

    @JsonProperty("type")
    private String type; // final_user_request, final_assistant_answer, etc.

    @JsonProperty("content")
    private String content;

    @JsonProperty("role")
    private String role; // assistant, AI1_Mia, AI2_Leo, user

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("client_timestamp")
    private Long clientTimestamp;

    @JsonProperty("server_timestamp")
    private Long serverTimestamp;

    @JsonProperty("metadata")
    private String metadata; // JSON 형태의 추가 메타데이터

    // AI 서버에서 보내는 기본 데이터 구조에 맞는 생성자
    public AIConversationDTO(String type, String content, String role) {
        this.type = type;
        this.content = content;
        this.role = role;
    }
}

