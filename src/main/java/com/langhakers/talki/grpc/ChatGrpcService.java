package com.langhakers.talki.grpc;

import com.langhakers.talki.dto.ChatMessageMapper;
import com.langhakers.talki.dto.MessageDTO;
import com.langhakers.talki.service.ChatService;
import com.langhakers.talki.websocket.grpc.proto.ChatServiceGrpc;
import com.langhakers.talki.websocket.grpc.proto.GrpcChatMessage;
import com.langhakers.talki.websocket.grpc.proto.ChatMessageRequest;
import com.langhakers.talki.websocket.grpc.proto.ChatMessageResponse;
import com.langhakers.talki.websocket.grpc.proto.ChatMessageIdRequest;
import com.langhakers.talki.websocket.grpc.proto.ChatMessageList;
import com.langhakers.talki.websocket.grpc.proto.DeleteChatMessageResponse;
import com.langhakers.talki.websocket.grpc.proto.Empty;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * gRPC 서비스 구현체로, chat.proto에 정의된 ChatService의 실제 로직을 처리합니다.
 * 기존의 ChatService(비즈니스 로직)와 ChatMessageMapper(데이터 변환)를 주입받아 사용합니다.
 */
@GrpcService
public class ChatGrpcService extends ChatServiceGrpc.ChatServiceImplBase {

    private final ChatService chatService;
    private final ChatMessageMapper chatMessageMapper;

    @Autowired
    public ChatGrpcService(ChatService chatService, ChatMessageMapper chatMessageMapper) {
        this.chatService = chatService;
        this.chatMessageMapper = chatMessageMapper;
    }

    /**
     * 새로운 채팅 메시지를 생성(저장)합니다.
     */
    @Override
    public void createChatMessage(ChatMessageRequest request, StreamObserver<ChatMessageResponse> responseObserver) {
        try {
            MessageDTO dto = new MessageDTO();
            dto.setRoomId(request.getRoomId());
            dto.setSenderId(request.getSenderId());
            dto.setSenderName(request.getSenderName());
            dto.setContent(request.getContent());
            // gRPC의 long timestamp를 LocalDateTime으로 변환
            dto.setTimestamp(chatMessageMapper.map(request.getTimestamp()));

            MessageDTO savedDto = chatService.saveMessage(dto);
            ChatMessageResponse grpcResponse = convertToGrpcResponse(savedDto);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL.withDescription("Failed to create chat message: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * ID로 특정 채팅 메시지를 조회합니다.
     * (현재 ChatService에 해당 기능이 없으므로 미구현 상태로 남겨둡니다.)
     */
    @Override
    public void getChatMessageById(ChatMessageIdRequest request, StreamObserver<ChatMessageResponse> responseObserver) {
        // ChatService에 ID로 메시지를 직접 조회하는 기능이 없으므로, 미구현(UNIMPLEMENTED) 상태를 반환합니다.
        // 필요시 ChatRepository 및 ChatService에 findById 로직을 추가해야 합니다.
        responseObserver.onError(Status.UNIMPLEMENTED.withDescription("Method getChatMessageById is not implemented").asRuntimeException());
    }

    /**
     * 모든 채팅 메시지를 조회합니다.
     * (실제로는 부하가 크므로, 특정 방의 메시지를 조회하는 로직으로 대체 구현합니다.)
     */
    @Override
    public void getAllChatMessages(Empty request, StreamObserver<ChatMessageList> responseObserver) {
        // 모든 메시지를 가져오는 것은 위험하므로, 특정 방(예: "default")의 메시지를 가져오는 것으로 대체합니다.
        try {
            String tempRoomId = "default"; // 예시용 기본 룸 ID
            List<MessageDTO> messages = chatService.getMessages(tempRoomId);
            List<ChatMessageResponse> grpcMessages = messages.stream()
                    .map(this::convertToGrpcResponse)
                    .collect(Collectors.toList());

            ChatMessageList list = ChatMessageList.newBuilder().addAllChatMessages(grpcMessages).build();
            responseObserver.onNext(list);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL.withDescription("Failed to get chat messages: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * ID로 특정 채팅 메시지를 삭제합니다.
     */
    @Override
    public void deleteChatMessageById(ChatMessageIdRequest request, StreamObserver<DeleteChatMessageResponse> responseObserver) {
        try {
            chatService.deleteMessage(request.getId());
            DeleteChatMessageResponse response = DeleteChatMessageResponse.newBuilder()
                    .setMessage("Chat message with ID " + request.getId() + " deleted successfully.")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL.withDescription("Failed to delete chat message: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * ID로 특정 채팅 메시지를 수정합니다.
     */
    @Override
    public void updateChatMessageById(ChatMessageRequest request, StreamObserver<ChatMessageResponse> responseObserver) {
        try {
            MessageDTO dto = new MessageDTO();
            dto.setRoomId(request.getRoomId());
            dto.setSenderId(request.getSenderId());
            dto.setSenderName(request.getSenderName());
            dto.setContent(request.getContent());
            dto.setTimestamp(chatMessageMapper.map(request.getTimestamp()));

            MessageDTO updatedDto = chatService.updateMessage(request.getId(), dto);
            ChatMessageResponse grpcResponse = convertToGrpcResponse(updatedDto);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
             responseObserver.onError(
                Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL.withDescription("Failed to update chat message: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * 양방향 스트리밍을 통해 실시간 채팅을 처리합니다.
     * 클라이언트가 메시지를 보낼 때마다 서버는 해당 메시지를 다시 클라이언트에게 보내는 '에코' 로직을 구현합니다.
     * 실제 애플리케이션에서는 특정 방의 모든 참여자에게 브로드캐스팅하는 로직이 필요합니다.
     */
    @Override
    public StreamObserver<GrpcChatMessage> chat(StreamObserver<GrpcChatMessage> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(GrpcChatMessage message) {
                // 클라이언트로부터 메시지 수신
                System.out.println("gRPC Chat Stream | Received: [" + message.getSenderName() + "] " + message.getContent());

                // TODO: 수신된 메시지를 DB에 저장하는 로직 (chatService.saveMessage) 추가 가능

                // TODO: 같은 채팅방의 다른 클라이언트들에게 메시지를 브로드캐스팅하는 로직 필요

                // 현재는 받은 메시지를 그대로 다시 보내는 에코(Echo) 기능만 수행
                responseObserver.onNext(message);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("gRPC Chat Stream | Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                // 클라이언트가 스트림 전송을 완료했을 때 호출됨
                System.out.println("gRPC Chat Stream | Client completed.");
                responseObserver.onCompleted();
            }
        };
    }

    /**
     * MessageDTO(Java DTO)를 ChatMessageResponse(gRPC Message)로 변환하는 헬퍼 메서드입니다.
     * @param dto 변환할 DTO 객체
     * @return 변환된 gRPC 메시지 객체
     */
    private ChatMessageResponse convertToGrpcResponse(MessageDTO dto) {
        ChatMessageResponse.Builder builder = ChatMessageResponse.newBuilder();
        
        // ChatMessageResponse.proto에는 id 필드가 없으므로 설정하지 않습니다.
        if (dto.getRoomId() != null) builder.setRoomId(dto.getRoomId());
        if (dto.getSenderId() != null) builder.setSenderId(dto.getSenderId());
        if (dto.getSenderName() != null) builder.setSenderName(dto.getSenderName());
        if (dto.getContent() != null) builder.setContent(dto.getContent());
        if (dto.getTimestamp() != null) {
            // LocalDateTime을 gRPC의 long timestamp로 변환
            builder.setTimestamp(chatMessageMapper.map(dto.getTimestamp()));
        }
        
        return builder.build();
    }
}
