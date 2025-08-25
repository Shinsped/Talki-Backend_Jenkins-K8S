package com.langhakers.talki.repository;

import com.langhakers.talki.entity.AudioData;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AudioRepository extends JpaRepository<AudioData, Long> {
}

