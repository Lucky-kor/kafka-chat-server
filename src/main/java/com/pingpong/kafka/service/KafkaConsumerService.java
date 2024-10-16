package com.pingpong.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingpong.chat.message.dto.ChatMessageDto;
import com.pingpong.chat.message.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatMessageRepository chatMessageRepository;
    private final ConcurrentHashMap<String, List<ChatMessageDto>> chatMessagesMap = new ConcurrentHashMap<>();

    @KafkaListener(topics = "one-to-one-chat", groupId = "chat-group")
    public void consumeOne(ConsumerRecord<String, String> consumerRecord, Consumer<?, ?> consumer) {
        try {
            String value = consumerRecord.value();

            // JSON 문자열에서 이중 인용부호와 이스케이프 문자 제거
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            // 추가적인 이스케이프 문자 제거
            value = value.replace("\\\"", "\"");

            log.info("수신된 메시지: {}", value);

            ChatMessageDto chatMessageDto = objectMapper.readValue(value, ChatMessageDto.class);

            // 메시지를 MongoDB에 저장
            chatMessageRepository.save(chatMessageDto);

            String chatRoomId = chatMessageDto.getChatRoomId();

            chatMessagesMap.computeIfAbsent(chatRoomId, k -> new ArrayList<>()).add(chatMessageDto);

            log.info("현재 채팅방 메시지 수: {}", chatMessagesMap.get(chatRoomId).size());

            messagingTemplate.convertAndSend("/topic/messages/" + chatRoomId, chatMessageDto);
            log.info("일대일 메시지 수신 및 전송 : {}", chatMessageDto);
        } catch (Exception e) {
            log.error("일대일 메시지를 처리하는 중 오류가 발생했습니다.", e);
        }
    }



    @KafkaListener(topics = "one-to-many-chat", groupId = "chat-group")
    public void consumeMany(ConsumerRecord<String, String> consumerRecord, Consumer<?, ?> consumer) {
        try {
            ChatMessageDto chatMessageDto = objectMapper.readValue(consumerRecord.value(), ChatMessageDto.class);
            String chatRoomId = chatMessageDto.getChatRoomId();

            chatMessagesMap.computeIfAbsent(chatRoomId, k -> new ArrayList<>()).add(chatMessageDto);

            messagingTemplate.convertAndSend("/topic/group/" + chatRoomId, chatMessageDto);
            log.info("일대다 메시지 수신 및 전송 : {}", chatMessageDto);
        } catch (Exception e) {
            log.error("일대다 메시지를 처리하는 중 오류가 발생했습니다.", e);
        }
    }

    public void sendMessage(String topic, ChatMessageDto chatMessageDto) {
        try {
            String message = objectMapper.writeValueAsString(chatMessageDto);
            kafkaTemplate.send(topic, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("메시지를 JSON으로 변환하는 중 오류가 발생했습니다.", e);
        }
    }

    public List<ChatMessageDto> getChatHistory(String chatRoomId) {
        return chatMessagesMap.getOrDefault(chatRoomId, new ArrayList<>());
    }
}