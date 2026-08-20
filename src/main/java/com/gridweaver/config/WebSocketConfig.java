package com.gridweaver.config;

import com.gridweaver.websocket.IoTWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final IoTWebSocketHandler ioTWebSocketHandler;

    public WebSocketConfig(IoTWebSocketHandler ioTWebSocketHandler) {
        this.ioTWebSocketHandler = ioTWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ioTWebSocketHandler, "/ws/iot")
                .setAllowedOrigins("*");
    }
}
