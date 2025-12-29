package com.example.psyaihealer.therapy;

import com.example.psyaihealer.dto.ChatResponse;
import com.example.psyaihealer.llm.TherapyLlmService;
import com.example.psyaihealer.multiagent.MultiAgentService;
import com.example.psyaihealer.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TherapyService {

    private static final Logger log = LoggerFactory.getLogger(TherapyService.class);

    private final TherapySessionRepository sessionRepository;
    private final TherapyMessageRepository messageRepository;
    private final TherapyLlmService therapyLlmService;
    private final MultiAgentService multiAgentService;

    public TherapyService(TherapySessionRepository sessionRepository,
                          TherapyMessageRepository messageRepository,
                          TherapyLlmService therapyLlmService,
                          MultiAgentService multiAgentService) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.therapyLlmService = therapyLlmService;
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

        // 优先使用真实 LLM；如果未配置则降级为内置的多策略占位实现
        var history = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        TherapyLlmService.LlmResult result = therapyLlmService.generateReply(session.getTopic(), history);
        if (result == null || result.reply() == null || result.reply().isBlank()) {
            log.warn("Therapy LLM reply is empty; falling back to MultiAgentService. sessionId={}", session.getId());
            MultiAgentService.AgentResult fallback = multiAgentService.process(content);
            TherapyMessage agentMsg = new TherapyMessage(session, MessageSender.AGENT, fallback.reply());
            messageRepository.save(agentMsg);
            return new ChatResponse(session.getId(), fallback.reply(), fallback.strategy());
        }

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
