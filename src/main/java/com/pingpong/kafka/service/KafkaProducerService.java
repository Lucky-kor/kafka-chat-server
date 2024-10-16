package com.pingpong.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingpong.chat.message.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, ChatMessageDto messageDto) throws JsonProcessingException {
        String message = convertToJson(messageDto);
        kafkaTemplate.send(topic, message);
    }

    private String convertToJson(ChatMessageDto messageDto) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(messageDto);
    }
}
