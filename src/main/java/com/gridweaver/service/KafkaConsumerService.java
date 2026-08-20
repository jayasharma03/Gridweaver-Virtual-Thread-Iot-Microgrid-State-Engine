package com.gridweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridweaver.model.IoTDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "gridweaver.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    
    private final DeviceStateService deviceStateService;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaConsumerService(DeviceStateService deviceStateService, ObjectMapper objectMapper) {
        this.deviceStateService = deviceStateService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "${gridweaver.kafka.topics.ingestion:iot-telemetry}",
        groupId = "${spring.kafka.consumer.group-id:gridweaver-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDeviceTelemetry(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            IoTDevice device = objectMapper.readValue(message, IoTDevice.class);
            logger.debug("Consumed telemetry from Kafka for device: {}", device.getDeviceId());
            
            // Process through state machine with simulated grid load
            double currentGridLoad = calculateSimulatedGridLoad();
            deviceStateService.processDeviceState(device, currentGridLoad);
            
            // Acknowledge message processing
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
        } catch (Exception e) {
            logger.error("Error processing Kafka message: {}", e.getMessage(), e);
            // In production, might want to implement retry logic or dead letter queue
        }
    }

    private double calculateSimulatedGridLoad() {
        // Simulate grid load - in production this would come from actual grid monitoring
        return 75.0; // Default to moderate load
    }
}