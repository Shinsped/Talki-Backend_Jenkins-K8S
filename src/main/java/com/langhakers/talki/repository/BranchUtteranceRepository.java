package com.langhakers.talki.repository;

import com.langhakers.talki.entity.BranchUtterance;
import com.langhakers.talki.entity.SessionBranch;
import com.langhakers.talki.entity.Utterance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchUtteranceRepository extends JpaRepository<BranchUtterance, Long> {
    
    List<BranchUtterance> findByBranchOrderBySequenceOrder(SessionBranch branch);
    
    List<BranchUtterance> findByUtterance(Utterance utterance);
    
    long countByBranch(SessionBranch branch);
    
    @Query("SELECT MAX(bu.sequenceOrder) FROM BranchUtterance bu WHERE bu.branch.id = :branchId")
    Optional<Integer> findMaxSequenceNumberByBranchId(@Param("branchId") String branchId);
}
