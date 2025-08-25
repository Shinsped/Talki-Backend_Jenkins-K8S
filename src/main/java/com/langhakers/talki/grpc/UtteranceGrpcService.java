package com.langhakers.talki.grpc;

import com.langhakers.talki.audio.proto.UtteranceIdRequest;
import com.langhakers.talki.audio.proto.UtteranceList;
import com.langhakers.talki.audio.proto.UtteranceResponse;
import com.langhakers.talki.audio.proto.UtteranceRequest;
import com.langhakers.talki.audio.proto.DeleteUtteranceResponse;
import com.langhakers.talki.audio.proto.UtteranceServiceGrpc;

import com.langhakers.talki.entity.Speaker;
import com.langhakers.talki.service.AudioService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class UtteranceGrpcService extends UtteranceServiceGrpc.UtteranceServiceImplBase {

    private final AudioService audioService;

    @Autowired
    public UtteranceGrpcService(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public void createUtterance(UtteranceRequest request, StreamObserver<UtteranceResponse> responseObserver) {
        try {
            com.langhakers.talki.dto.UtteranceRequest dto = new com.langhakers.talki.dto.UtteranceRequest();
            dto.setAudioId(request.getAudioId());
            dto.setSpeaker(Speaker.valueOf(request.getSpeaker().name()));
            dto.setText(request.getText());
            dto.setStartMillis(request.getStartMillis());
            dto.setEndMillis(request.getEndMillis());

            com.langhakers.talki.dto.UtteranceResponse responseDto = audioService.createUtterance(dto);
            UtteranceResponse grpcResponse = convertToGrpcResponse(responseDto);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateUtteranceById(UtteranceRequest request, StreamObserver<UtteranceResponse> responseObserver) {
        responseObserver.onError(new UnsupportedOperationException("Update not implemented"));
    }

    @Override
    public void deleteUtteranceById(UtteranceIdRequest request, StreamObserver<DeleteUtteranceResponse> responseObserver) {
        responseObserver.onError(new UnsupportedOperationException("Delete not implemented"));
    }

    @Override
    public void getUtteranceById(UtteranceIdRequest request, StreamObserver<UtteranceResponse> responseObserver) {
        try {
            long utteranceId = request.getId();
            // AudioService에 ID로 Utterance 가져오는 메서드가 있다고 가정
            com.langhakers.talki.dto.UtteranceResponse dto = audioService.getUtteranceById(utteranceId);

            UtteranceResponse grpcResponse = convertToGrpcResponse(dto);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private UtteranceResponse convertToGrpcResponse(com.langhakers.talki.dto.UtteranceResponse dto) {
        UtteranceResponse.Builder builder = UtteranceResponse.newBuilder()
                .setId(dto.getId())
                .setAudioId(dto.getAudioId())
                .setSpeaker(com.langhakers.talki.audio.proto.Speaker.valueOf(dto.getSpeaker().name()))
                .setText(dto.getText())
                .setStartMillis(dto.getStartMillis())
                .setEndMillis(dto.getEndMillis());

        if (dto.getCreatedAt() != null) {
            builder.setCreatedAt(dto.getCreatedAt().toString());
        }

        return builder.build();
    }
}
