package com.langhakers.talki.service;

import com.langhakers.talki.entity.AudioData;
import com.langhakers.talki.entity.Utterance;
import com.langhakers.talki.dto.AudioRequest;
import com.langhakers.talki.dto.AudioResponse;
import com.langhakers.talki.dto.UtteranceRequest;
import com.langhakers.talki.dto.UtteranceResponse;
import com.langhakers.talki.repository.AudioRepository;
import com.langhakers.talki.repository.UtteranceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AudioService {

    private final AudioRepository audioRepository;
    private final UtteranceRepository utteranceRepository;

    @Transactional
    public AudioResponse createAudio(AudioRequest request, String fileUrl) {
        AudioData audio = new AudioData();
        audio.setFileName(request.getFileName());
        audio.setFileUrl(fileUrl);
        audio.setDurationMillis(request.getDurationMillis());
        audio.setCreatedAt(LocalDateTime.now());

        AudioData saved = audioRepository.save(audio);

        return AudioResponse.builder()
                .id(saved.getId())
                .fileName(saved.getFileName())
                .fileUrl(saved.getFileUrl())
                .durationMillis(saved.getDurationMillis())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public AudioResponse getAudioById(Long id) {
        AudioData audio = audioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Audio not found with id: " + id));

        return AudioResponse.builder()
                .id(audio.getId())
                .fileName(audio.getFileName())
                .fileUrl(audio.getFileUrl())
                .durationMillis(audio.getDurationMillis())
                .createdAt(audio.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AudioResponse> getAllAudios() {
        return audioRepository.findAll().stream()
                .map(audio -> AudioResponse.builder()
                        .id(audio.getId())
                        .fileName(audio.getFileName())
                        .fileUrl(audio.getFileUrl())
                        .durationMillis(audio.getDurationMillis())
                        .createdAt(audio.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAudioById(Long id) {
        if (!audioRepository.existsById(id)) {
            throw new RuntimeException("Audio not found with id: " + id);
        }
        audioRepository.deleteById(id);
    }

    @Transactional
    public AudioResponse updateAudio(Long id, AudioRequest request) {
        AudioData existingAudio = audioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Audio not found with id: " + id));

        existingAudio.setFileName(request.getFileName());
        existingAudio.setDurationMillis(request.getDurationMillis());

        AudioData updatedAudio = audioRepository.save(existingAudio);

        return AudioResponse.builder()
                .id(updatedAudio.getId())
                .fileName(updatedAudio.getFileName())
                .fileUrl(updatedAudio.getFileUrl())
                .durationMillis(updatedAudio.getDurationMillis())
                .createdAt(updatedAudio.getCreatedAt())
                .build();
    }

    @Transactional
    public UtteranceResponse createUtterance(UtteranceRequest request) {
        AudioData audio = audioRepository.findById(request.getAudioId())
                .orElseThrow(() -> new RuntimeException("Audio not found with id: " + request.getAudioId()));

        Utterance utterance = new Utterance();
        utterance.setAudioData(audio);
        utterance.setSpeaker(request.getSpeaker());
        utterance.setText(request.getText());
        utterance.setStartMillis(request.getStartMillis());
        utterance.setEndMillis(request.getEndMillis());
        utterance.setCreatedAt(LocalDateTime.now());

        Utterance saved = utteranceRepository.save(utterance);

        UtteranceResponse response = new UtteranceResponse();
        response.setId(saved.getId());
        response.setAudioId(saved.getAudioData().getId());
        response.setSpeaker(saved.getSpeaker());
        response.setText(saved.getText());
        response.setStartMillis(saved.getStartMillis());
        response.setEndMillis(saved.getEndMillis());
        response.setCreatedAt(saved.getCreatedAt());

        return response;
    }

    @Transactional(readOnly = true)
    public List<UtteranceResponse> getUtterancesByAudioId(Long audioId) {
        List<Utterance> utterances = utteranceRepository.findAllByAudioDataId(audioId);

        return utterances.stream().map(u -> {
            UtteranceResponse response = new UtteranceResponse();
            response.setId(u.getId());
            response.setAudioId(u.getAudioData().getId());
            response.setSpeaker(u.getSpeaker());
            response.setText(u.getText());
            response.setStartMillis(u.getStartMillis());
            response.setEndMillis(u.getEndMillis());
            response.setCreatedAt(u.getCreatedAt());
            return response;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UtteranceResponse getUtteranceById(Long utteranceId) {
        Utterance utterance = utteranceRepository.findById(utteranceId)
                .orElseThrow(() -> new RuntimeException("Utterance not found with id: " + utteranceId));

        UtteranceResponse response = new UtteranceResponse();
        response.setId(utterance.getId());
        response.setAudioId(utterance.getAudioData().getId());
        response.setSpeaker(utterance.getSpeaker());
        response.setText(utterance.getText());
        response.setStartMillis(utterance.getStartMillis());
        response.setEndMillis(utterance.getEndMillis());
        response.setCreatedAt(utterance.getCreatedAt());

        return response;
    }
    /**
     * 비동기 오디오 처리 메서드
     */
    @Async
    public CompletableFuture<AudioResponse> processAudioAsync(AudioRequest request, String fileUrl) {
        try {
            // 오디오 처리 시뮬레이션 (실제로는 음성 인식, 변환 등의 작업)
            Thread.sleep(100); // 실제 처리 시간 시뮬레이션
            
            AudioResponse response = createAudio(request, fileUrl);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            CompletableFuture<AudioResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * 대량 오디오 파일 비동기 처리
     */
    @Async
    public CompletableFuture<List<AudioResponse>> processBatchAudioAsync(List<AudioRequest> requests, List<String> fileUrls) {
        try {
            List<AudioResponse> responses = new ArrayList<>();
            
            for (int i = 0; i < requests.size(); i++) {
                AudioRequest request = requests.get(i);
                String fileUrl = i < fileUrls.size() ? fileUrls.get(i) : null;
                
                if (fileUrl != null) {
                    AudioResponse response = createAudio(request, fileUrl);
                    responses.add(response);
                }
            }
            
            return CompletableFuture.completedFuture(responses);
        } catch (Exception e) {
            CompletableFuture<List<AudioResponse>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * 오디오 메타데이터 추출 비동기 처리
     */
    @Async
    public CompletableFuture<AudioResponse> extractAudioMetadataAsync(Long audioId) {
        try {
            AudioData audio = audioRepository.findById(audioId)
                .orElseThrow(() -> new RuntimeException("Audio not found with id: " + audioId));
            
            // 메타데이터 추출 시뮬레이션
            Thread.sleep(50);
            
            AudioResponse response = AudioResponse.builder()
                .id(audio.getId())
                .fileName(audio.getFileName())
                .fileUrl(audio.getFileUrl())
                .durationMillis(audio.getDurationMillis())
                .createdAt(audio.getCreatedAt())
                .build();
                
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            CompletableFuture<AudioResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}

