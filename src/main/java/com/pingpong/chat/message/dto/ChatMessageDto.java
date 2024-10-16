package com.pingpong.chat.message.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pingpong.config.LocalDateTimeDeserializer;
import com.pingpong.user.dto.ChatUserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessageDto {
    private String chatRoomId;
    private String senderId;
    private String senderName;
    private List<ChatUserDto> recipientId;
    private String content;
    private String announcement;
    private String fileUrl;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime timestamp;

    private String topic;
    private String profile;
}

