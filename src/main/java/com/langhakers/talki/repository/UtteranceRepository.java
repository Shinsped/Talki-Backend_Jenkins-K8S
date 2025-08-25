package com.langhakers.talki.repository;

import com.langhakers.talki.entity.Utterance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UtteranceRepository extends JpaRepository<Utterance, Long> {
    List<Utterance> findAllByAudioDataId(Long audioId);
}

