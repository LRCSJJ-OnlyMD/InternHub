package com.internhub.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

/**
 * WebSocket security configuration. Authenticates WebSocket connections using
 * JWT tokens.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Slf4j
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extract JWT token from header
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            // Validate token and extract user info
                            if (jwtTokenProvider.validateToken(token)) {
                                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                                String role = jwtTokenProvider.getRoleFromToken(token);

                                // Create authentication
                                UsernamePasswordAuthenticationToken auth
                                        = new UsernamePasswordAuthenticationToken(
                                                userId.toString(),
                                                null,
                                                Collections.singletonList(new SimpleGrantedAuthority(role))
                                        );

                                accessor.setUser(auth);
                                SecurityContextHolder.getContext().setAuthentication(auth);
                                log.debug("WebSocket authenticated for user: {}", userId);
                            }
                        } catch (Exception e) {
                            log.warn("WebSocket authentication failed: {}", e.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }
}
