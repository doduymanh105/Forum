package com.example.forum.dto.response.chatResponseDto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private ChatOverview chat;
    private List<MemberResponse> activeParticipants;
    private CustomPageable<MessageResponse> messages;
}
