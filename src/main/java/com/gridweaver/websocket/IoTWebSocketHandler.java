package com.gridweaver.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridweaver.model.IoTDevice;
import com.gridweaver.service.DeviceStateService;
import com.gridweaver.service.RegionalBalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class IoTWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(IoTWebSocketHandler.class);
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final AtomicLong connectionCount = new AtomicLong(0);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService virtualThreadExecutor;
    private final DeviceStateService deviceStateService;
    private RegionalBalanceService regionalBalanceService;

    public IoTWebSocketHandler(ExecutorService virtualThreadExecutor, DeviceStateService deviceStateService) {
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.deviceStateService = deviceStateService;
    }
    
    @Autowired(required = false)
    public void setRegionalBalanceService(RegionalBalanceService regionalBalanceService) {
        this.regionalBalanceService = regionalBalanceService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        long count = connectionCount.incrementAndGet();
        sessions.put(session.getId(), session);
        logger.info("WebSocket connection established. Session ID: {}, Total connections: {}", 
                    session.getId(), count);
        
        // Send welcome message
        session.sendMessage(new TextMessage(
            objectMapper.writeValueAsString(new ConnectionStatus("CONNECTED", count))
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming IoT device data using virtual thread
        virtualThreadExecutor.submit(() -> {
            try {
                String payload = message.getPayload();
                IoTDevice device = objectMapper.readValue(payload, IoTDevice.class);
                logger.debug("Received data from device {}: state={}, power={}kW", 
                            device.getDeviceId(), device.getState(), device.getPowerOutput());
                
                // Process device data (will be enhanced with State Machine in Week 2)
                processDeviceData(device);
                
                // Broadcast to all connected dashboard clients
                broadcastDeviceUpdate(device);
                
            } catch (Exception e) {
                logger.error("Error processing message: {}", e.getMessage(), e);
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        long count = connectionCount.decrementAndGet();
        logger.info("WebSocket connection closed. Session ID: {}, Total connections: {}", 
                    session.getId(), count);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error for session {}: {}", 
                    session.getId(), exception.getMessage(), exception);
        sessions.remove(session.getId());
    }

    private void processDeviceData(IoTDevice device) {
        // Direct processing with state machine integration
        logger.info("Processing device: {} | Type: {} | State: {} | Power: {}kW | Battery: {}%",
                   device.getDeviceId(), device.getDeviceType(), device.getState(), 
                   device.getPowerOutput(), device.getBatteryLevel());
        
        double currentGridLoad = calculateSimulatedGridLoad();
        deviceStateService.processDeviceState(device, currentGridLoad);
        
        // Week 4: Register device for regional balancing
        if (regionalBalanceService != null) {
            regionalBalanceService.updateDevice(device);
        }
    }
    
    private double calculateSimulatedGridLoad() {
        // Simulate grid load calculation based on connection count
        int totalConnections = (int) connectionCount.get();
        if (totalConnections == 0) return 50.0; // Default moderate load
        
        // Simulate higher load when more devices are connected
        return Math.min(95.0, 50.0 + (totalConnections * 0.5));
    }
    
    // Week 4: Scheduled regional power balancing (every 30 seconds)
    @Scheduled(fixedRate = 30000)
    public void performRegionalBalancing() {
        if (regionalBalanceService != null && connectionCount.get() > 0) {
            logger.info("Executing scheduled regional power balancing...");
            regionalBalanceService.balanceRegionalPower();
        }
    }

    private void broadcastDeviceUpdate(IoTDevice device) {
        String message;
        try {
            message = objectMapper.writeValueAsString(device);
            TextMessage textMessage = new TextMessage(message);
            
            sessions.values().forEach(session -> {
                if (session.isOpen()) {
                    try {
                        synchronized (session) {
                            session.sendMessage(textMessage);
                        }
                    } catch (IOException e) {
                        logger.error("Error broadcasting to session {}: {}", 
                                    session.getId(), e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Error broadcasting device update: {}", e.getMessage(), e);
        }
    }

    public long getConnectionCount() {
        return connectionCount.get();
    }

    public static ConcurrentHashMap<String, WebSocketSession> getSessions() {
        return sessions;
    }

    record ConnectionStatus(String status, long connectionCount) {}
}
