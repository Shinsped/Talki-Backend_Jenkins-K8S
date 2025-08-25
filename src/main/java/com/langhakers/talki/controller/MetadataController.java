package com.langhakers.talki.controller;

import com.langhakers.talki.dto.MetadataRequest;
import com.langhakers.talki.dto.MetadataResponse;
import com.langhakers.talki.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metadata")
public class MetadataController {
    private final MetadataService metadataService;

    @Autowired
    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @PostMapping("")
    public ResponseEntity<MetadataResponse> saveMetadata(@RequestBody MetadataRequest request) {
        MetadataResponse response = metadataService.saveMetadata(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity<List<MetadataResponse>> getAllMetadata() {
        List<MetadataResponse> metadataList = metadataService.getAllMetadata();
        return ResponseEntity.ok(metadataList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetadataResponse> getMetadataById(@PathVariable Long id) {
        MetadataResponse metadata = metadataService.getMetadataById(id);
        return ResponseEntity.ok(metadata);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetadataById(@PathVariable Long id) {
        metadataService.deleteMetadataById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetadataResponse> updateMetadataById(@PathVariable Long id, @RequestBody MetadataRequest request) {
        MetadataResponse updatedMetadata = metadataService.updateMetadata(id, request);
        return ResponseEntity.ok(updatedMetadata);
    }
}

