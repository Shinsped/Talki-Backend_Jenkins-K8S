package com.langhakers.talki.repository;

import com.langhakers.talki.entity.SessionBranch;
import com.langhakers.talki.entity.BranchStatus;
import com.langhakers.talki.entity.BranchType;
import com.langhakers.talki.entity.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionBranchRepository extends JpaRepository<SessionBranch, Long> {
    
    Optional<SessionBranch> findByBranchId(String branchId);
    
    List<SessionBranch> findBySessionAndBranchStatus(ConversationSession session, BranchStatus status);
    
    List<SessionBranch> findBySessionOrderBySequenceOrder(ConversationSession session);
    
    List<SessionBranch> findByParentBranch(SessionBranch parentBranch);
    
    List<SessionBranch> findByBranchType(BranchType branchType);
    
    @Query("SELECT sb FROM SessionBranch sb WHERE sb.session = :session AND sb.parentBranch IS NULL")
    List<SessionBranch> findMainBranches(@Param("session") ConversationSession session);
    
    @Query("SELECT sb FROM SessionBranch sb WHERE sb.session = :session AND sb.branchStatus = :status ORDER BY sb.sequenceOrder")
    List<SessionBranch> findActiveBranchesBySession(@Param("session") ConversationSession session, 
                                                   @Param("status") BranchStatus status);
    
    long countBySessionAndBranchStatus(ConversationSession session, BranchStatus status);
}
