package com.nukkadseva.nukkadsevabackend.config;

import com.nukkadseva.nukkadsevabackend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

/**
 * Intercepts STOMP CONNECT frames to validate the JWT token.
 * Sets the authenticated user's profileId as the Principal name,
 * which is used by SimpMessagingTemplate.convertAndSendToUser().
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract JWT from the Authorization native header
            List<String> authHeaders = accessor.getNativeHeader("Authorization");

            if (authHeaders != null && !authHeaders.isEmpty()) {
                String token = authHeaders.get(0);
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }

                try {
                    String email = jwtUtil.extractEmail(token);
                    if (email != null && jwtUtil.validateToken(token, email)) {
                        String role = jwtUtil.extractRole(token);
                        Long profileId = jwtUtil.extractProfileId(token);

                        // Use profileId as the principal name — this is what
                        // convertAndSendToUser() uses to route messages
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                String.valueOf(profileId),
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                        accessor.setUser(auth);
                        log.debug("WebSocket CONNECT authenticated for profileId={}", profileId);
                    } else {
                        log.warn("WebSocket CONNECT: invalid JWT token");
                        throw new IllegalArgumentException("Invalid JWT token");
                    }
                } catch (Exception e) {
                    log.error("WebSocket authentication failed: {}", e.getMessage());
                    throw new IllegalArgumentException("WebSocket authentication failed", e);
                }
            } else {
                log.warn("WebSocket CONNECT: no Authorization header found");
                throw new IllegalArgumentException("Missing Authorization header");
            }
        }

        return message;
    }
}
