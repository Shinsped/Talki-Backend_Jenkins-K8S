package com.langhakers.talki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.langhakers.talki.dto.MessageDTO;
import com.langhakers.talki.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/ws")
public class WebSocketController {

    private final ChatService chatService;

    @Autowired
    public WebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/message")
    public ResponseEntity<Void> sendMessage(@RequestBody MessageDTO messageDTO) {
        chatService.saveMessage(messageDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/{roomId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable String roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId));
    }
}

