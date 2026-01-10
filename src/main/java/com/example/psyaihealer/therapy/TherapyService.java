package com.example.psyaihealer.therapy;

import com.example.psyaihealer.dto.ChatResponse;
import com.example.psyaihealer.llm.TherapyLlmService;
import com.example.psyaihealer.multiagent.MultiAgentService;
import com.example.psyaihealer.profile.UserProfile;
import com.example.psyaihealer.profile.UserProfileInsights;
import com.example.psyaihealer.profile.UserProfileService;
import com.example.psyaihealer.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TherapyService {

    private static final Logger log = LoggerFactory.getLogger(TherapyService.class);

    private final TherapySessionRepository sessionRepository;
    private final TherapyMessageRepository messageRepository;
    private final TherapyLlmService therapyLlmService;
    private final MultiAgentService multiAgentService;
    private final UserProfileService profileService;

    public TherapyService(TherapySessionRepository sessionRepository,
                          TherapyMessageRepository messageRepository,
                          TherapyLlmService therapyLlmService,
                          MultiAgentService multiAgentService,
                          UserProfileService profileService) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.therapyLlmService = therapyLlmService;
        this.multiAgentService = multiAgentService;
        this.profileService = profileService;
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

        UserProfile profile = null;
        try {
            profile = profileService.getOrCreate(session.getUser());
        } catch (Exception ignored) {
            // Profile is optional; do not block chat if profile read fails.
        }

        // 优先使用真实 LLM；如果未配置则降级为内置的多策略占位实现
        var history = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        TherapyLlmService.LlmResult result = therapyLlmService.generateReply(session.getTopic(), history, profile);
        if (result == null || result.reply() == null || result.reply().isBlank()) {
            log.warn("Therapy LLM reply is empty; falling back to MultiAgentService. sessionId={}", session.getId());
            MultiAgentService.AgentResult fallback = multiAgentService.process(content, profile);
            TherapyMessage agentMsg = new TherapyMessage(session, MessageSender.AGENT, fallback.reply());
            messageRepository.save(agentMsg);

            triggerConversationProfileUpdate(session.getUser(), session.getTopic(), history, agentMsg);
            return new ChatResponse(session.getId(), fallback.reply(), fallback.strategy());
        }

        TherapyMessage agentMsg = new TherapyMessage(session, MessageSender.AGENT, result.reply());
        messageRepository.save(agentMsg);

        triggerConversationProfileUpdate(session.getUser(), session.getTopic(), history, agentMsg);
        return new ChatResponse(session.getId(), result.reply(), result.strategy());
    }

    private void triggerConversationProfileUpdate(User user, String topic, List<TherapyMessage> historyBeforeAgent, TherapyMessage agentMsg) {
        if (user == null) return;

        // Run in background so chat latency isn't dominated by profile extraction.
        CompletableFuture.runAsync(() -> {
            try {
                UserProfile profile = null;
                try {
                    profile = profileService.getOrCreate(user);
                } catch (Exception ignored) {
                    profile = null;
                }

                List<TherapyMessage> history = new ArrayList<>();
                if (historyBeforeAgent != null) {
                    history.addAll(historyBeforeAgent);
                }
                if (agentMsg != null) {
                    history.add(agentMsg);
                }

                UserProfileInsights insights = therapyLlmService.extractProfileInsights(topic, history, profile);
                if (insights != null) {
                    profileService.applyConversationInsights(user, insights);
                }
            } catch (Exception e) {
                log.debug("Conversation profile update skipped: {}", e.getMessage());
            }
        });
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
