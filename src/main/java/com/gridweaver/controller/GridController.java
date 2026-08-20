package com.gridweaver.controller;

import com.gridweaver.websocket.IoTWebSocketHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/grid")
public class GridController {

    private final IoTWebSocketHandler webSocketHandler;

    public GridController(IoTWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @GetMapping("/status")
    public Map<String, Object> getGridStatus() {
        return Map.of(
            "status", "operational",
            "activeConnections", webSocketHandler.getConnectionCount(),
            "virtualThreadsEnabled", true,
            "timestamp", System.currentTimeMillis()
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
