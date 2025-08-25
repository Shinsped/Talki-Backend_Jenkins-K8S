package com.langhakers.talki.config;

import com.langhakers.talki.websocket.TalkiWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class TALKiWebSocketConfig implements WebSocketConfigurer {

    private final TalkiWebSocketHandler webSocketHandler;

    @Autowired
    public TALKiWebSocketConfig(TalkiWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/talki-ws")
                .setAllowedOrigins("*"); // Configure CORS as needed
    }
}
