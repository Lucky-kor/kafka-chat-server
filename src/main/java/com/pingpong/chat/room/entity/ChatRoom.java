package com.pingpong.chat.room.entity;

import com.pingpong.user.entity.ChatUser;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_list")
@Entity
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 채팅방 고유 ID
     */
    @Column(nullable = false, unique = true)
    private String chatRoomId;

    /**
     * 그룹 채팅일 경우의 채팅방 이름
     */
    @Column(nullable = false)
    private String chatRoomName;

    /**
     * 채팅방에 속한 사용자 목록
     */
    @ManyToMany
    @JoinTable(
            name = "chat_room_users",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<ChatUser> participants;

    /**
     * 마지막 메시지 내용 (미리보기용)
     */
    private String lastMessage;

    /**
     * 마지막 활성화 시간
     */
    @Column(nullable = false)
    private LocalDateTime lastActive;

    /**
     * 그룹 채팅 여부
     */
    @Column(nullable = false)
    private String topic;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "unread_messages", joinColumns = @JoinColumn(name = "chat_room_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "unread_count")
    private Map<String, Integer> unreadMessageCount = new HashMap<>(); // 초기화

    /**
     * 사용자별 읽지 않은 메시지 수 업데이트 메서드
     */
    public void incrementUnreadCount(String userId) {
        unreadMessageCount.merge(userId, 1, Integer::sum);
    }

    public void resetUnreadCount(String userId) {
        unreadMessageCount.put(userId, 0);
    }

    /**
     * 참가자 제거 메서드
     */
    public void removeParticipant(ChatUser user) {
        participants.remove(user);
    }

    /**
     * 참가자 추가 메서드
     */
    public void addParticipant(ChatUser user) {
        participants.add(user);
    }

    /**
     * 마지막 메시지 및 마지막 활성화 시간 업데이트
     */
    public void updateLastMessage(String message) {
        this.lastMessage = message;
        this.lastActive = LocalDateTime.now();
    }

    public void setParticipants(List<ChatUser> participants) {
        this.participants = participants;
    }
}
