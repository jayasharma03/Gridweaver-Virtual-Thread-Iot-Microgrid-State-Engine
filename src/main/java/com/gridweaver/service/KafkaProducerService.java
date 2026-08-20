package com.gridweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridweaver.model.IoTDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConditionalOnProperty(name = "gridweaver.kafka.enabled", havingValue = "true")
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${gridweaver.kafka.topics.ingestion:iot-telemetry}")
    private String ingestionTopic;
    
    @Value("${gridweaver.kafka.topics.state-transitions:state-transitions}")
    private String stateTransitionsTopic;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendDeviceTelemetry(IoTDevice device) {
        try {
            String deviceJson = objectMapper.writeValueAsString(device);
            kafkaTemplate.send(ingestionTopic, device.getDeviceId(), deviceJson)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.debug("Sent telemetry for device {} to Kafka", device.getDeviceId());
                    } else {
                        logger.error("Failed to send telemetry for device {}: {}", 
                            device.getDeviceId(), ex.getMessage());
                    }
                });
        } catch (Exception e) {
            logger.error("Error serializing device data for Kafka: {}", e.getMessage(), e);
        }
    }

    public void sendStateTransition(String deviceId, String fromState, String toState, String event) {
        try {
            Map<String, Object> transitionData = Map.of(
                "deviceId", deviceId,
                "fromState", fromState,
                "toState", toState,
                "event", event,
                "timestamp", System.currentTimeMillis()
            );
            
            String transitionJson = objectMapper.writeValueAsString(transitionData);
            kafkaTemplate.send(stateTransitionsTopic, deviceId, transitionJson)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("State transition for device {}: {} -> {} (event: {})", 
                            deviceId, fromState, toState, event);
                    } else {
                        logger.error("Failed to send state transition for device {}: {}", 
                            deviceId, ex.getMessage());
                    }
                });
        } catch (Exception e) {
            logger.error("Error serializing state transition for Kafka: {}", e.getMessage(), e);
        }
    }
}