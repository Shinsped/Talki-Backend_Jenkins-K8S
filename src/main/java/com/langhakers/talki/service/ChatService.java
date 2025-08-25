package com.langhakers.talki.service;

import com.langhakers.talki.entity.ChatMessage;
import com.langhakers.talki.dto.MessageDTO;
import com.langhakers.talki.repository.ChatMessageRepository;
import com.langhakers.talki.dto.ChatMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;

    @Transactional
    public MessageDTO saveMessage(MessageDTO messageDTO) {
        ChatMessage chatMessage = chatMessageMapper.dtoToEntity(messageDTO);
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return chatMessageMapper.entityToDto(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(String roomId) {
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId).stream()
                .map(chatMessageMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMessage(Long id) {
        chatMessageRepository.deleteById(id);
    }

    @Transactional
    public MessageDTO updateMessage(Long id, MessageDTO messageDTO) {
        ChatMessage existingMessage = chatMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));

        existingMessage.setRoomId(messageDTO.getRoomId());
        existingMessage.setSenderId(messageDTO.getSenderId());
        existingMessage.setSenderName(messageDTO.getSenderName());
        existingMessage.setContent(messageDTO.getContent());
        existingMessage.setTimestamp(messageDTO.getTimestamp());

        ChatMessage updatedMessage = chatMessageRepository.save(existingMessage);
        return chatMessageMapper.entityToDto(updatedMessage);
    }
}

