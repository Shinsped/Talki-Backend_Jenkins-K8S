package com.langhakers.talki.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import com.langhakers.talki.websocket.TalkiWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private TalkiWebSocketHandler talkiWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(talkiWebSocketHandler, "/ws", "/ws/{roomId}").setAllowedOrigins("*");
    }
}

