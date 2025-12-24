package com.example.psyaihealer.therapy;

import com.example.psyaihealer.dto.ChatResponse;
import com.example.psyaihealer.multiagent.MultiAgentService;
import com.example.psyaihealer.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TherapyService {

    private final TherapySessionRepository sessionRepository;
    private final TherapyMessageRepository messageRepository;
    private final MultiAgentService multiAgentService;

    public TherapyService(TherapySessionRepository sessionRepository,
                          TherapyMessageRepository messageRepository,
                          MultiAgentService multiAgentService) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.multiAgentService = multiAgentService;
    }

    public TherapySession createSession(User user, String topic) {
        TherapySession session = new TherapySession(user, topic);
        return sessionRepository.save(session);
    }

    public List<TherapySession> sessionsFor(User user) {
        return sessionRepository.findByUser(user);
    }

    public List<TherapyMessage> messages(Long sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public ChatResponse processMessage(TherapySession session, String content) {
        TherapyMessage userMsg = new TherapyMessage(session, MessageSender.USER, content);
        messageRepository.save(userMsg);

        MultiAgentService.AgentResult result = multiAgentService.process(content);
        TherapyMessage agentMsg = new TherapyMessage(session, MessageSender.AGENT, result.reply());
        messageRepository.save(agentMsg);
        return new ChatResponse(session.getId(), result.reply(), result.strategy());
    }

    public TherapySession getSessionOrThrow(Long sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("会话不存在"));
    }

    public void ensureOwnership(TherapySession session, User user) {
        if (!session.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("会话权限不足");
        }
    }
}
