package com.pingpong.chat.room.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pingpong.chat.message.dto.ChatMessageDto;
import com.pingpong.chat.room.dto.ChatRoomDto;
import com.pingpong.chat.room.entity.ChatRoom;
import com.pingpong.chat.room.service.ChatRoomService;
import com.pingpong.kafka.service.KafkaConsumerService;
import com.pingpong.kafka.service.KafkaProducerService;
import com.pingpong.user.dto.ChatUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/chat")
@RequiredArgsConstructor
@RestController
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final KafkaConsumerService kafkaConsumerService;
    private final KafkaProducerService kafkaProducerService;

    /**
     * 채팅방 생성
     */
    @PostMapping("/create")
    public ChatRoomDto createChatRoom(@RequestBody ChatRoomDto chatRoomDto) {
        String uuid = java.util.UUID.randomUUID().toString();
        chatRoomDto.setChatRoomId(uuid);
        chatRoomService.createChatRoom(chatRoomDto);
        return chatRoomDto;
    }

    /**
     * 일대일 채팅 메시지 전송
     */
    @PostMapping("/send/one-to-one")
    public ResponseEntity<Void> sendOneToOneMessage(@RequestBody ChatMessageDto messageDto) throws JsonProcessingException {
        kafkaProducerService.sendMessage("one-to-one-chat", messageDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 일대다 채팅 메시지 전송
     */
    @PostMapping("/send/one-to-many")
    public ResponseEntity<Void> sendOneToManyMessage(@RequestBody ChatMessageDto messageDto) throws JsonProcessingException {
        kafkaProducerService.sendMessage("one-to-many-chat", messageDto);
        return ResponseEntity.ok().build();
    }

    /**
     * history 조회
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/history")
    public List<ChatMessageDto> getChatHistory(@RequestParam String chatRoomId) {
        log.info("ChatRoomId: {}", chatRoomId);
        return chatRoomService.getMessageDetail(chatRoomId);
    }

    /**
     * 채팅방 목록 조회
     */
    @PostMapping("/list")
    @CrossOrigin(origins = "*")
    public ResponseEntity<List<ChatRoom>> getUserChatRooms(@RequestBody ChatUserDto userDto) {
        String userId = userDto.getUserId();
        List<ChatRoom> userChatRooms = chatRoomService.getChatRoomsForUser(userId);
        return ResponseEntity.ok(userChatRooms);
    }

    /**
     * 특정 채팅방 나가기
     */
    @DeleteMapping("/delete")
    public void exitChatRoom(@RequestParam String chatRoomId,
                             @RequestParam String userId) {

        chatRoomService.exitChatRoom(chatRoomId, userId);
    }

    /**
     * 모든  채팅방 나가기
     */
    @DeleteMapping("/exit")
    public void exitChatRoom(@RequestParam String userId){
        chatRoomService.exitChatRoomAll(userId);
    }
}
