package com.langhakers.talki.service;

import com.langhakers.talki.entity.Metadata;
import com.langhakers.talki.dto.MetadataRequest;
import com.langhakers.talki.dto.MetadataResponse;
import com.langhakers.talki.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetadataService {
    private final MetadataRepository metadataRepository;

    @Autowired
    public MetadataService(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Transactional
    public MetadataResponse saveMetadata(MetadataRequest request) {
        Metadata metadata = new Metadata();
        metadata.setSessionId(request.getSessionId());
        metadata.setTimestamp(Instant.now().toEpochMilli());
        metadata.setUserId(request.getUserId());
        metadata.setServerVersion(request.getServerVersion());
        metadata.setType(request.getType());
        metadata.setPayload(request.getPayload());
        
        Metadata saved = metadataRepository.save(metadata);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MetadataResponse> getAllMetadata() {
        return metadataRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MetadataResponse getMetadataById(Long id) {
        Metadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Metadata not found with id: " + id));
        return toResponse(metadata);
    }

    @Transactional
    public void deleteMetadataById(Long id) {
        if (!metadataRepository.existsById(id)) {
            throw new RuntimeException("Metadata not found with id: " + id);
        }
        metadataRepository.deleteById(id);
    }

    @Transactional
    public MetadataResponse updateMetadata(Long id, MetadataRequest request) {
        Metadata existingMetadata = metadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Metadata not found with id: " + id));

        existingMetadata.setSessionId(request.getSessionId());
        existingMetadata.setUserId(request.getUserId());
        existingMetadata.setServerVersion(request.getServerVersion());
        existingMetadata.setType(request.getType());
        existingMetadata.setPayload(request.getPayload());

        Metadata updatedMetadata = metadataRepository.save(existingMetadata);
        return toResponse(updatedMetadata);
    }

    private MetadataResponse toResponse(Metadata metadata) {
        MetadataResponse response = new MetadataResponse();
        response.setId(metadata.getId());
        response.setSessionId(metadata.getSessionId());
        response.setTimestamp(metadata.getTimestamp());
        response.setUserId(metadata.getUserId());
        response.setServerVersion(metadata.getServerVersion());
        response.setType(metadata.getType());
        response.setPayload(metadata.getPayload());
        return response;
    }
}

