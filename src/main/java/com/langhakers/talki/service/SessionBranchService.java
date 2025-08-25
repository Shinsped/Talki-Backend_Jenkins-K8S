package com.langhakers.talki.service;

import com.langhakers.talki.entity.*;
import com.langhakers.talki.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SessionBranchService {
    
    @Autowired
    private SessionBranchRepository branchRepository;
    
    @Autowired
    private ConversationSessionRepository sessionRepository;
    
    @Autowired
    private BranchUtteranceRepository branchUtteranceRepository;
    
    public SessionBranch createBranch(String sessionId, String branchName, String description, 
                                    BranchType branchType, String parentBranchId, String createdBy) {
        ConversationSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        
        String branchId = UUID.randomUUID().toString();
        SessionBranch branch = new SessionBranch(branchId, session, branchName, branchType);
        branch.setBranchDescription(description);
        branch.setCreatedBy(createdBy);
        
        if (parentBranchId != null) {
            SessionBranch parentBranch = branchRepository.findByBranchId(parentBranchId)
                .orElseThrow(() -> new RuntimeException("Parent branch not found: " + parentBranchId));
            branch.setParentBranch(parentBranch);
        }
        
        // Set sequence order
        List<SessionBranch> existingBranches = branchRepository.findBySessionOrderBySequenceOrder(session);
        branch.setSequenceOrder(existingBranches.size());
        
        return branchRepository.save(branch);
    }
    
    public Optional<SessionBranch> getBranchById(String branchId) {
        return branchRepository.findByBranchId(branchId);
    }
    
    public List<SessionBranch> getSessionBranches(String sessionId) {
        ConversationSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        return branchRepository.findBySessionOrderBySequenceOrder(session);
    }
    
    public List<SessionBranch> getActiveBranches(String sessionId) {
        ConversationSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        return branchRepository.findActiveBranchesBySession(session, BranchStatus.ACTIVE);
    }
    
    public List<SessionBranch> getChildBranches(String parentBranchId) {
        SessionBranch parentBranch = branchRepository.findByBranchId(parentBranchId)
            .orElseThrow(() -> new RuntimeException("Branch not found: " + parentBranchId));
        return branchRepository.findByParentBranch(parentBranch);
    }
    
    public SessionBranch addUtteranceToBranch(String branchId, Long utteranceId, String addedBy) {
        SessionBranch branch = branchRepository.findByBranchId(branchId)
            .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
        
        // Get sequence order for the new utterance
        List<BranchUtterance> existingUtterances = branchUtteranceRepository.findByBranchOrderBySequenceOrder(branch);
        int sequenceOrder = existingUtterances.size();
        
        BranchUtterance branchUtterance = new BranchUtterance();
        branchUtterance.setBranch(branch);
        branchUtterance.setSequenceOrder(sequenceOrder);
        branchUtterance.setAddedBy(addedBy);
        
        branchUtteranceRepository.save(branchUtterance);
        
        return branch;
    }
    
    public SessionBranch mergeBranch(String branchId, String mergedBy) {
        SessionBranch branch = branchRepository.findByBranchId(branchId)
            .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
        
        if (branch.getBranchType() == BranchType.MAIN) {
            throw new RuntimeException("Cannot merge main branch");
        }
        
        branch.setBranchStatus(BranchStatus.MERGED);
        branch.setMergedAt(LocalDateTime.now());
        
        return branchRepository.save(branch);
    }
    
    public SessionBranch pauseBranch(String branchId) {
        SessionBranch branch = branchRepository.findByBranchId(branchId)
            .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
        
        branch.setBranchStatus(BranchStatus.PAUSED);
        return branchRepository.save(branch);
    }
    
    public SessionBranch resumeBranch(String branchId) {
        SessionBranch branch = branchRepository.findByBranchId(branchId)
            .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
        
        branch.setBranchStatus(BranchStatus.ACTIVE);
        return branchRepository.save(branch);
    }
}
