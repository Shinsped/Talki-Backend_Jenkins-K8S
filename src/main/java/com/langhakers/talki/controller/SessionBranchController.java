package com.langhakers.talki.controller;

import com.langhakers.talki.entity.*;
import com.langhakers.talki.service.SessionBranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/branches")
@CrossOrigin(origins = "*")
public class SessionBranchController {
    
    @Autowired
    private SessionBranchService branchService;
    
    @PostMapping
    public ResponseEntity<SessionBranch> createBranch(@RequestBody Map<String, Object> request) {
        String sessionId = (String) request.get("sessionId");
        String branchName = (String) request.get("branchName");
        String description = (String) request.get("description");
        BranchType branchType = BranchType.valueOf((String) request.get("branchType"));
        String parentBranchId = (String) request.get("parentBranchId");
        String createdBy = (String) request.get("createdBy");
        
        try {
            SessionBranch branch = branchService.createBranch(sessionId, branchName, description, 
                                                            branchType, parentBranchId, createdBy);
            return ResponseEntity.ok(branch);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{branchId}")
    public ResponseEntity<SessionBranch> getBranch(@PathVariable String branchId) {
        return branchService.getBranchById(branchId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<SessionBranch>> getSessionBranches(@PathVariable String sessionId) {
        try {
            List<SessionBranch> branches = branchService.getSessionBranches(sessionId);
            return ResponseEntity.ok(branches);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/session/{sessionId}/active")
    public ResponseEntity<List<SessionBranch>> getActiveBranches(@PathVariable String sessionId) {
        try {
            List<SessionBranch> branches = branchService.getActiveBranches(sessionId);
            return ResponseEntity.ok(branches);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{branchId}/children")
    public ResponseEntity<List<SessionBranch>> getChildBranches(@PathVariable String branchId) {
        try {
            List<SessionBranch> branches = branchService.getChildBranches(branchId);
            return ResponseEntity.ok(branches);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{branchId}/utterances")
    public ResponseEntity<SessionBranch> addUtteranceToBranch(
            @PathVariable String branchId,
            @RequestBody Map<String, Object> request) {
        
        Long utteranceId = Long.valueOf(request.get("utteranceId").toString());
        String addedBy = (String) request.get("addedBy");
        
        try {
            SessionBranch branch = branchService.addUtteranceToBranch(branchId, utteranceId, addedBy);
            return ResponseEntity.ok(branch);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{branchId}/merge")
    public ResponseEntity<SessionBranch> mergeBranch(
            @PathVariable String branchId,
            @RequestBody Map<String, Object> request) {
        
        String mergedBy = (String) request.get("mergedBy");
        
        try {
            SessionBranch branch = branchService.mergeBranch(branchId, mergedBy);
            return ResponseEntity.ok(branch);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{branchId}/pause")
    public ResponseEntity<SessionBranch> pauseBranch(@PathVariable String branchId) {
        try {
            SessionBranch branch = branchService.pauseBranch(branchId);
            return ResponseEntity.ok(branch);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{branchId}/resume")
    public ResponseEntity<SessionBranch> resumeBranch(@PathVariable String branchId) {
        try {
            SessionBranch branch = branchService.resumeBranch(branchId);
            return ResponseEntity.ok(branch);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
