package com.example.psyaihealer.therapy;

import com.example.psyaihealer.dto.ChatMessage;
import com.example.psyaihealer.dto.ChatResponse;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final TherapyService therapyService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(TherapyService therapyService, SimpMessagingTemplate messagingTemplate) {
        this.therapyService = therapyService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{sessionId}")
    public void handleChat(@DestinationVariable Long sessionId, ChatMessage message) {
        TherapySession session = therapyService.getSessionOrThrow(sessionId);
        ChatResponse response = therapyService.processMessage(session, message.getContent());
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, response);
    }
}
