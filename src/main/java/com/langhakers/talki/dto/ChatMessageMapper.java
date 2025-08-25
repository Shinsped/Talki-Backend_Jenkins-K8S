package com.langhakers.talki.dto;

import com.langhakers.talki.entity.ChatMessage;
import com.langhakers.talki.dto.MessageDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatMessageMapper {

    @Mapping(target = "timestamp", source = "timestamp")
    MessageDTO entityToDto(ChatMessage entity);

    @Mapping(target = "timestamp", source = "timestamp")
    ChatMessage dtoToEntity(MessageDTO dto);

    // gRPC long → LocalDateTime
    default LocalDateTime map(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    // LocalDateTime → gRPC long
    default long map(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}

