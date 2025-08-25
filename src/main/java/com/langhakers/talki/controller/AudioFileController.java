package com.langhakers.talki.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/audio/files")
public class AudioFileController {

    private final Path fileBasePath = Paths.get("./audio_files").toAbsolutePath().normalize();

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getAudioFile(@PathVariable String filename) {
        try {
            Path filePath = fileBasePath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = "audio/mpeg";  // 필요하면 확장자별 MIME 타입 구분 가능
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

