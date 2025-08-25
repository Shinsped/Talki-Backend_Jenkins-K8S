package com.langhakers.talki.repository;

import com.langhakers.talki.entity.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetadataRepository extends JpaRepository<Metadata, Long> {
    // 추가적인 쿼리 메서드 필요시 선언
}

