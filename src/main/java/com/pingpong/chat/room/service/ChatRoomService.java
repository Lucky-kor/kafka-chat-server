package com.pingpong.chat.room.service;

import com.pingpong.chat.message.dto.ChatMessageDto;
import com.pingpong.chat.room.dto.ChatRoomDto;
import com.pingpong.chat.room.entity.ChatRoom;
import com.pingpong.chat.room.repository.ChatRoomRepository;
import com.pingpong.chat.message.repository.ChatMessageRepository;
import com.pingpong.user.entity.ChatUser;
import com.pingpong.user.repository.ChatUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatRoomService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ChatUserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final Map<String, ChatRoomDto> chatRoomMap = new ConcurrentHashMap<>();
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatRoomService(KafkaTemplate<String, Object> kafkaTemplate, ChatUserRepository userRepository, ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * 채팅방 생성
     */
    public void createChatRoom(ChatRoomDto chatRoomDto) {
        String topic = chatRoomDto.getTopic();
        if (topic != null && !topic.isEmpty()) {
            LocalDateTime lastActive = chatRoomDto.getLastActive() != null ? chatRoomDto.getLastActive() : LocalDateTime.now();

            List<ChatUser> participants = chatRoomDto.getParticipants().stream()
                    .map(participantDto -> userRepository.findByUserId(participantDto.getUserId())
                            .orElseGet(() -> userRepository.save(ChatUser.builder()
                                    .userId(participantDto.getUserId())
                                    .name(participantDto.getName())
                                    .build())))
                    .collect(Collectors.toList());

            ChatRoom chatRoom = ChatRoom.builder()
                    .chatRoomId(chatRoomDto.getChatRoomId())
                    .chatRoomName(chatRoomDto.getChatRoomName())
                    .lastMessage(chatRoomDto.getLastMessage())
                    .topic(topic)
                    .lastActive(lastActive)
                    .participants(participants)
                    .build();

            chatRoomMap.put(chatRoomDto.getChatRoomId(), chatRoomDto);
            kafkaTemplate.send(topic, chatRoomDto);

            chatRoomRepository.save(chatRoom);
            log.info("새로운 채팅방을 생성합니다 : {}", chatRoomDto);
        } else {
            log.error("채팅방의 topic이 없습니다 : ChatRoomService");
        }
    }

    /**
     * 모든 채팅방 조회
     */
    public List<ChatRoomDto> getAllChatRooms() {
        return new ArrayList<>(chatRoomMap.values());
    }

    /**
     * 특정 사용자의 채팅방 조회
     */
    public List<ChatRoom> getChatRoomsForUser(String userId) {
        return chatRoomRepository.findByParticipantsUserId(userId);
    }

    /**
     * 채팅방 나가기
     */
    public void exitChatRoom(String chatRoomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findByChatRoomId(chatRoomId);
        if (chatRoom == null) {
            throw new IllegalArgumentException("채팅방을 찾지 못했습니다 : " + chatRoomId);
        }

        ChatUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾지 못했습니다 : " + userId));

        List<ChatUser> updatedParticipants = chatRoom.getParticipants().stream()
                .filter(participant -> !participant.getUserId().equals(userId))
                .collect(Collectors.toList());

        ChatRoom updatedChatRoom = ChatRoom.builder()
                .id(chatRoom.getId())
                .chatRoomId(chatRoom.getChatRoomId())
                .chatRoomName(chatRoom.getChatRoomName())
                .lastMessage(chatRoom.getLastMessage())
                .topic(chatRoom.getTopic())
                .lastActive(chatRoom.getLastActive())
                .participants(updatedParticipants)
                .unreadMessageCount(chatRoom.getUnreadMessageCount())
                .build();

        chatRoomRepository.save(updatedChatRoom);
        log.info("유저 {}의 채팅방 번호 {}", userId, chatRoomId);
    }

    /**
     * 모든 채팅방 나가기
     */
    public void exitChatRoomAll(String userId) {
        List<ChatRoom> userChatRooms = chatRoomRepository.findByParticipantsUserId(userId);

        userChatRooms.forEach(chatRoom -> {
            List<ChatUser> updatedParticipants = chatRoom.getParticipants().stream()
                    .filter(participant -> !participant.getUserId().equals(userId))
                    .collect(Collectors.toList());

            ChatRoom updatedChatRoom = ChatRoom.builder()
                    .id(chatRoom.getId())
                    .chatRoomId(chatRoom.getChatRoomId())
                    .chatRoomName(chatRoom.getChatRoomName())
                    .lastMessage(chatRoom.getLastMessage())
                    .topic(chatRoom.getTopic())
                    .lastActive(chatRoom.getLastActive())
                    .participants(updatedParticipants)
                    .unreadMessageCount(chatRoom.getUnreadMessageCount())
                    .build();

            chatRoomRepository.save(updatedChatRoom);
        });
    }

    /**
     * 채팅방 메시지 상세 조회
     */
    public List<ChatMessageDto> getMessageDetail(String chatRoomId) {
        log.info("Fetching messages for chatRoomId: {}", chatRoomId);
        List<ChatMessageDto> messages = chatMessageRepository.findByChatRoomId(chatRoomId);
        log.info("Fetched messages: {}", messages);
        return messages;
    }
}