package com.pingpong.chat.room.dto;

import com.pingpong.user.dto.ChatUserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDto {
    private String chatRoomId;
    private String chatRoomName;
    private List<ChatUserDto> participants;
    private String lastMessage;
    private LocalDateTime lastActive;
    private String topic;
    private Map<String, Integer> unreadMessageCount;
}
