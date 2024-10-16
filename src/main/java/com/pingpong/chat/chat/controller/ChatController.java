package com.pingpong.chat.chat.controller;

import com.pingpong.chat.chat.service.ChatService;
import com.pingpong.chat.message.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatController {
    private final ChatService charService;

    @MessageMapping("/chat")
    public void handleChatMessage(ChatMessageDto message) {
        charService.processMessage(message);

    }
}
