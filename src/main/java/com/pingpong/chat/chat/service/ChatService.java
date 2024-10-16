package com.pingpong.chat.chat.service;

import com.pingpong.chat.message.dto.ChatMessageDto;
import com.pingpong.chat.room.entity.ChatRoom;
import com.pingpong.chat.room.repository.ChatRoomRepository;
import com.pingpong.user.dto.ChatUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    // KafkaTemplate을 사용하여 메시지를 Kafka로 전송
    private final KafkaTemplate<String, ChatMessageDto> kafkaTemplate;
    // 채팅방 정보를 저장하는 레포지토리
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 메시지를 처리하고 Kafka로 전송하는 메서드
     * @param message ChatMessageDto 객체로 전송할 메시지 정보
     */
    @Transactional
    public void processMessage(ChatMessageDto message) {
        String kafkaTopic = message.getTopic();

        // Kafka 토픽이 정의되어 있는지 확인
        if (kafkaTopic != null && !kafkaTopic.isEmpty()) {
            // 1대1 채팅이면 'one-to-one-chat' 토픽으로 전송
            if (kafkaTopic.equals("one")) {
                kafkaTemplate.send("one-to-one-chat", message.getChatRoomId(), message);
                log.info("ChatService one to one : {}", kafkaTopic);
            }
            // 그룹 채팅이면 'one-to-many-chat' 토픽으로 전송
            else if (kafkaTopic.equals("many")) {
                kafkaTemplate.send("one-to-many-chat", message.getChatRoomId(), message);
                log.info("ChatService one to many: {}", kafkaTopic);
            }
        } else {
            log.error("ChatService processMessage 실패");
        }

        // 메시지에 파일 URL이 있을 경우 이를 출력
        if (message.getFileUrl() != null && !message.getFileUrl().isEmpty()) {
            System.out.println("File attached: " + message.getFileUrl());
        }

        messagingTemplate.convertAndSend("/topic/chatRoom/" + message.getChatRoomId(), message);

        // 채팅방 정보를 업데이트
        updateChatRoomInfo(message);
    }

    /**
     * 채팅방 정보를 업데이트하는 메서드 (마지막 메시지, 마지막 활성화 시간, 읽지 않은 메시지 카운트 등)
     * @param message ChatMessageDto 객체로 채팅방에 전송된 메시지 정보
     */
    private void updateChatRoomInfo(ChatMessageDto message) {
        // 메시지에 포함된 채팅방 ID를 기반으로 채팅방 정보를 조회
        ChatRoom chatRoom = chatRoomRepository.findByChatRoomId(message.getChatRoomId());

        // 채팅방이 존재하지 않으면 예외 발생
        if (chatRoom == null) {
            throw new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + message.getChatRoomId());
        }

        // 채팅방의 마지막 메시지와 마지막 활성화 시간을 업데이트
        chatRoom.setLastMessage(message.getContent());
        chatRoom.setLastActive(LocalDateTime.now());

        // 채팅방 참가자 정보를 가져와 ChatUserDto로 변환
        List<ChatUserDto> participantDtos = chatRoom.getParticipants()
                .stream()
                .map(participant -> new ChatUserDto(participant.getId(), participant.getUserId(), participant.getName(), participant.getProfile())) // UserEntity -> UserDto 변환
                .collect(Collectors.toList());

        // 각 참가자에게 읽지 않은 메시지 카운트를 증가
        for (ChatUserDto participantDto : participantDtos) {
            // 메시지 발신자가 아닌 참가자에게만 적용
            if (!participantDto.getUserId().equals(message.getSenderId())) {
                chatRoom.incrementUnreadCount(participantDto.getUserId());
            }
        }

        // 변경된 채팅방 정보를 저장
        chatRoomRepository.save(chatRoom);

        // 채팅방 정보 업데이트 로그 출력
        log.info("채팅방 정보 업데이트: 채팅방 ID = {}, 마지막 메시지 = {}, 마지막 활성화 시간 = {}",
                chatRoom.getChatRoomId(), chatRoom.getLastMessage(), chatRoom.getLastActive());
    }
}
