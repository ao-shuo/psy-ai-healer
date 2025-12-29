package com.example.psyaihealer.config;

import com.example.psyaihealer.security.JwtService;
import com.example.psyaihealer.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompJwtChannelInterceptor.class);

    private final JwtService jwtService;
    private final UserService userService;

    private static final String SESSION_AUTH_KEY = "WS_AUTH";

    public StompJwtChannelInterceptor(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // For CONNECT, the client may send Authorization in STOMP connectHeaders.
        // For SEND/SUBSCRIBE, many clients do NOT repeat those headers; therefore we store auth in session.
        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        if (command == StompCommand.SEND || command == StompCommand.SUBSCRIBE) {
            if (accessor.getUser() == null) {
                Authentication sessionAuth = getSessionAuth(accessor);
                if (sessionAuth != null) {
                    accessor.setUser(sessionAuth);
                    accessor.setLeaveMutable(true);
                    return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
                }
            }
        }

        if (command == StompCommand.CONNECT) {
            accessor.setLeaveMutable(true);
            String authHeader = firstHeader(accessor, "Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new AccessDeniedException("WebSocket 未认证：缺少 Authorization: Bearer <token>");
            }

            String jwt = authHeader.substring(7);
            try {
                String username = jwtService.extractUsername(jwt);
                UserDetails userDetails = userService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    accessor.setUser(authentication);
                    putSessionAuth(accessor, authentication);
                    return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
                }
                throw new AccessDeniedException("WebSocket 未认证：token 无效");
            } catch (Exception ex) {
                log.debug("STOMP CONNECT token invalid: {}", ex.getMessage());
                if (ex instanceof AccessDeniedException ade) {
                    throw ade;
                }
                throw new AccessDeniedException("WebSocket 未认证：token 解析失败");
            }
        }

        return message;
    }

    private static void putSessionAuth(StompHeaderAccessor accessor, Authentication authentication) {
        if (accessor.getSessionAttributes() == null) {
            return;
        }
        accessor.getSessionAttributes().put(SESSION_AUTH_KEY, authentication);
    }

    private static Authentication getSessionAuth(StompHeaderAccessor accessor) {
        if (accessor.getSessionAttributes() == null) {
            return null;
        }
        Object v = accessor.getSessionAttributes().get(SESSION_AUTH_KEY);
        if (v instanceof Authentication a) {
            return a;
        }
        return null;
    }

    private static String firstHeader(StompHeaderAccessor accessor, String name) {
        if (accessor == null || name == null) return null;
        // STOMP headers are case-sensitive by convention, but some clients vary.
        String v = accessor.getFirstNativeHeader(name);
        if (v != null) return v;
        v = accessor.getFirstNativeHeader(name.toLowerCase());
        if (v != null) return v;
        return accessor.getFirstNativeHeader(name.toUpperCase());
    }
}
