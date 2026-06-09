package com.example.forum.dto.response.chatResponseDto;

import com.example.forum.entity.Enum.MessageType;
import com.example.forum.entity.Message;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private UserResponse sender;
    private String content;
    private MessageType type;
    private Long replyToMessageId;
    private boolean isEdited;
    private boolean isDeleted;
    private LocalDateTime sendAt;
    private LocalDateTime updatedAt;

    public static MessageResponse mapToMessageResponse(Message message){
        return MessageResponse.builder()
                .id(message.getId())
                .sender(UserResponse.mapToUserResponse(message.getSender()))
                .content(message.isDeleted() ? "This message has been recalled" : message.getContent())
                .type(message.getType())
                .replyToMessageId(message.getReplyToMessageId())
                .isEdited(message.isEdited())
                .isDeleted(message.isDeleted())
                .sendAt(message.getSendAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}
