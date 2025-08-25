package com.langhakers.talki.controller;

import com.langhakers.talki.dto.AudioRequest;
import com.langhakers.talki.dto.AudioResponse;
import com.langhakers.talki.dto.UtteranceRequest;
import com.langhakers.talki.dto.UtteranceResponse;
import com.langhakers.talki.service.AudioService;
import com.langhakers.talki.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/audio")
public class AudioController {

    private final AudioService audioService;
    private final FileStorageUtil fileStorageUtil;

    @PostMapping("/upload")
    public ResponseEntity<AudioResponse> uploadAudioFile(@RequestParam("file") MultipartFile file,
                                                         @RequestParam("durationMillis") Long durationMillis) throws IOException {
        String storedFileName = fileStorageUtil.saveFile(file);
        String fileUrl = "/audio/files/" + storedFileName;

        AudioRequest request = new AudioRequest();
        request.setFileName(storedFileName);
        request.setDurationMillis(durationMillis);

        AudioResponse response = audioService.createAudio(request, fileUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/utterance")
    public ResponseEntity<UtteranceResponse> createUtterance(@RequestBody UtteranceRequest request) {
        UtteranceResponse response = audioService.createUtterance(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{audioId}/utterances")
    public ResponseEntity<List<UtteranceResponse>> getUtterances(@PathVariable Long audioId) {
        List<UtteranceResponse> utterances = audioService.getUtterancesByAudioId(audioId);
        return ResponseEntity.ok(utterances);
    }

    @GetMapping("/{audioId}")
    public ResponseEntity<AudioResponse> getAudioById(@PathVariable Long audioId) {
        AudioResponse audio = audioService.getAudioById(audioId);
        return ResponseEntity.ok(audio);
    }

    @GetMapping("")
    public ResponseEntity<List<AudioResponse>> getAllAudios() {
        List<AudioResponse> audios = audioService.getAllAudios();
        return ResponseEntity.ok(audios);
    }

    @DeleteMapping("/{audioId}")
    public ResponseEntity<Void> deleteAudioById(@PathVariable Long audioId) {
        audioService.deleteAudioById(audioId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{audioId}")
    public ResponseEntity<AudioResponse> updateAudioById(@PathVariable Long audioId, @RequestBody AudioRequest request) {
        AudioResponse updatedAudio = audioService.updateAudio(audioId, request);
        return ResponseEntity.ok(updatedAudio);
    }
}

