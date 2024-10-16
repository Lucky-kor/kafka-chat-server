package com.pingpong.chat.message.repository;

import com.pingpong.chat.message.dto.ChatMessageDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.List;

@EnableMongoRepositories
public interface ChatMessageRepository extends MongoRepository<ChatMessageDto, String> {
    List<ChatMessageDto> findByChatRoomId(String chatRoomId);
}
