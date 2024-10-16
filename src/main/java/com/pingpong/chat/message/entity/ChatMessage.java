package com.pingpong.chat.message.entity;

import com.pingpong.user.entity.ChatUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ChatMessage {

    @Id
    private String id;

    private String chatRoomId;
    private String senderId;
    private String senderName;

    @ManyToMany
    @JoinTable(
            name = "chat_message_recipients",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<ChatUser> recipients;

    private String content;
    private String announcement;
    private String fileUrl;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    private String topic;
}