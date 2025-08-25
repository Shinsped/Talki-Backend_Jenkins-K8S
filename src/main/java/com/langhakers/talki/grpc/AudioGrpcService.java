package com.langhakers.talki.grpc;

import com.langhakers.talki.audio.proto.AudioRequest;
import com.langhakers.talki.audio.proto.AudioResponse;
import com.langhakers.talki.audio.proto.AudioIdRequest;
import com.langhakers.talki.audio.proto.AudioList;
import com.langhakers.talki.audio.proto.DeleteAudioResponse;
import com.langhakers.talki.audio.proto.GetAllAudiosRequest;
import com.langhakers.talki.audio.proto.AudioServiceGrpc;

import com.langhakers.talki.service.AudioService;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class AudioGrpcService extends AudioServiceGrpc.AudioServiceImplBase {

    private final AudioService audioService;

    @Autowired
    public AudioGrpcService(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public void createAudio(AudioRequest request, StreamObserver<AudioResponse> responseObserver) {
        try {
            String tempFileUrl = "/grpc-uploads/" + request.getFileName();

            com.langhakers.talki.dto.AudioRequest dto = new com.langhakers.talki.dto.AudioRequest();
            dto.setFileName(request.getFileName());
            dto.setDurationMillis(request.getDurationMillis());

            com.langhakers.talki.dto.AudioResponse createdAudio = audioService.createAudio(dto, tempFileUrl);
            AudioResponse grpcResponse = convertToGrpcResponse(createdAudio);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getAudioById(AudioIdRequest request, StreamObserver<AudioResponse> responseObserver) {
        try {
            com.langhakers.talki.dto.AudioResponse foundAudio = audioService.getAudioById(request.getId());
            AudioResponse grpcResponse = convertToGrpcResponse(foundAudio);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getAllAudios(GetAllAudiosRequest request, StreamObserver<AudioList> responseObserver) {
        try {
            List<com.langhakers.talki.dto.AudioResponse> allAudios = audioService.getAllAudios();
            List<AudioResponse> grpcAudios = allAudios.stream()
                    .map(this::convertToGrpcResponse)
                    .collect(Collectors.toList());

            AudioList audioList = AudioList.newBuilder().addAllAudios(grpcAudios).build();
            responseObserver.onNext(audioList);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteAudioById(AudioIdRequest request, StreamObserver<DeleteAudioResponse> responseObserver) {
        try {
            audioService.deleteAudioById(request.getId());
            DeleteAudioResponse response = DeleteAudioResponse.newBuilder()
                    .setMessage("Audio with ID " + request.getId() + " deleted successfully.")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateAudioById(AudioRequest request, StreamObserver<AudioResponse> responseObserver) {
        try {
            long audioIdToUpdate = 1L;

            com.langhakers.talki.dto.AudioRequest dto = new com.langhakers.talki.dto.AudioRequest();
            dto.setFileName(request.getFileName());
            dto.setDurationMillis(request.getDurationMillis());

            com.langhakers.talki.dto.AudioResponse updatedAudio = audioService.updateAudio(audioIdToUpdate, dto);
            AudioResponse grpcResponse = convertToGrpcResponse(updatedAudio);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private AudioResponse convertToGrpcResponse(com.langhakers.talki.dto.AudioResponse dto) {
        AudioResponse.Builder builder = AudioResponse.newBuilder()
                .setId(dto.getId())
                .setFileName(dto.getFileName())
                .setDurationMillis(dto.getDurationMillis());

        if (dto.getFileUrl() != null) {
            builder.setFileUrl(dto.getFileUrl());
        }
        if (dto.getCreatedAt() != null) {
            builder.setCreatedAt(dto.getCreatedAt().toString());
        }
        return builder.build();
    }
}
