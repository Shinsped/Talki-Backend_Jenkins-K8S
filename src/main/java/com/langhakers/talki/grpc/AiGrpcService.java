package com.langhakers.talki.grpc;

import com.langhakers.talki.grpcclient.proto.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
public class AiGrpcService extends AiServiceGrpc.AiServiceImplBase {

    // In-Memory DB
    private final Map<Long, AiResponse> aiStore = new ConcurrentHashMap<>();
    private long idCounter = 1;

    @Override
    public void createAi(AiRequest request, StreamObserver<AiResponse> responseObserver) {
        long newId = idCounter++;
        AiResponse newAi = AiResponse.newBuilder()
                .setId(newId)
                .setName(request.getName())
                .setDescription(request.getDescription())
                .build();
        aiStore.put(newId, newAi);

        responseObserver.onNext(newAi);
        responseObserver.onCompleted();
    }

    @Override
    public void getAiById(AiIdRequest request, StreamObserver<AiResponse> responseObserver) {
        AiResponse ai = aiStore.get(request.getId());
        if (ai != null) {
            responseObserver.onNext(ai);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("AI not found with ID: " + request.getId())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAllAis(GetAllAisRequest request, StreamObserver<AiList> responseObserver) {
        AiList aiList = AiList.newBuilder()
                .addAllAis(aiStore.values())
                .build();
        responseObserver.onNext(aiList);
        responseObserver.onCompleted();
    }

    @Override
    public void updateAiById(AiRequest request, StreamObserver<AiResponse> responseObserver) {
        long idToUpdate = request.getId();
        if (aiStore.containsKey(idToUpdate)) {
            AiResponse updatedAi = AiResponse.newBuilder()
                    .setId(idToUpdate)
                    .setName(request.getName())
                    .setDescription(request.getDescription())
                    .build();
            aiStore.put(idToUpdate, updatedAi);
            responseObserver.onNext(updatedAi);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("AI not found with ID: " + idToUpdate)
                    .asRuntimeException());
        }
    }
}
