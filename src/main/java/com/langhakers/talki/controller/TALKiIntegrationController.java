package com.langhakers.talki.controller;

import com.langhakers.talki.dto.*;
import com.langhakers.talki.service.TALKiSimpleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/talki")
@CrossOrigin(origins = "*")
public class TALKiIntegrationController {
    
    private final TALKiSimpleService talkiService;
    
    @Autowired
    public TALKiIntegrationController(TALKiSimpleService talkiService) {
        this.talkiService = talkiService;
    }
    
    @PostMapping("/session/join")
    public ResponseEntity<TALKiResponseDTO> joinSession(@RequestBody TALKiJoinSessionDTO request) {
        TALKiResponseDTO response = talkiService.handleJoinSession(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/branch/create")
    public ResponseEntity<TALKiResponseDTO> createBranch(@RequestBody TALKiCreateBranchDTO request) {
        TALKiResponseDTO response = talkiService.handleCreateBranch(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/message/character")
    public ResponseEntity<TALKiResponseDTO> sendCharacterMessage(@RequestBody TALKiCharacterMessageDTO request) {
        TALKiResponseDTO response = talkiService.handleCharacterMessage(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/tts/configure")
    public ResponseEntity<TALKiResponseDTO> configureTTS(@RequestBody TALKiTTSConfigDTO request) {
        TALKiResponseDTO response = talkiService.handleTTSConfiguration(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<TALKiResponseDTO> healthCheck() {
        TALKiResponseDTO response = TALKiResponseDTO.success("HEALTH_OK", "TALKi Integration Service is running");
        return ResponseEntity.ok(response);
    }
}
