package com.example.psyaihealer.therapy;

import com.example.psyaihealer.dto.ChatMessage;
import com.example.psyaihealer.dto.ChatResponse;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.psyaihealer.user.User;
import com.example.psyaihealer.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final TherapyService therapyService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public ChatWebSocketController(TherapyService therapyService,
                                   SimpMessagingTemplate messagingTemplate,
                                   UserRepository userRepository) {
        this.therapyService = therapyService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat/{sessionId}")
    public void handleChat(@DestinationVariable Long sessionId, ChatMessage message, Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("WebSocket 未认证：请在 STOMP CONNECT 里携带 Authorization: Bearer <token>");
        }
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        TherapySession session = therapyService.getSessionOrThrow(sessionId);
        therapyService.ensureOwnership(session, user);
        ChatResponse response = therapyService.processMessage(session, message.getContent());
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, response);
    }
}
