package com.example.forum.security.websocket;

import com.example.forum.security.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebsocketAuthInterceptor implements ChannelInterceptor {

    private final JWTService jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(StompCommand.CONNECT.equals(accessor.getCommand())){
            log.info("Starting authorize Websocket connection");

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Missing or invalid Authorization header");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            try{
                Long userId = jwtUtils.extractUserId(token);
                jwtUtils.validateToken(token);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());

                accessor.setUser(authenticationToken);
                log.info("Authorized Websocket successfully for User ID: {}", userId);
            } catch (Exception e){
                log.error("Authorized Websocket Fail: {}",e.getMessage());
                throw new IllegalArgumentException("Invalid Token");
            }
        }
        return message;
    }

}
