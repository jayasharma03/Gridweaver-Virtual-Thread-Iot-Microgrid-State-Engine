package com.gridweaver.controller;

import com.gridweaver.model.IoTDevice;
import com.gridweaver.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);
    
    @Autowired(required = false)
    private AIService aiService;

    @PostMapping("/forecast")
    public ResponseEntity<?> getPowerForecast(@RequestBody Map<String, Object> request) {
        try {
            String deviceId = (String) request.get("deviceId");
            int hoursAhead = request.containsKey("hoursAhead") ? 
                (Integer) request.get("hoursAhead") : 24;
            
            if (aiService == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Service not available");
                response.put("forecast_hours", hoursAhead);
                response.put("confidence", 0.0);
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> forecast = aiService.getPowerForecast(deviceId, hoursAhead);
            return ResponseEntity.ok(forecast);
            
        } catch (Exception e) {
            logger.error("Error generating power forecast", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate forecast: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/anomaly")
    public ResponseEntity<?> detectAnomalies(@RequestBody Map<String, Object> request) {
        try {
            String deviceId = (String) request.get("deviceId");
            
            if (aiService == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Service not available");
                response.put("has_anomaly", false);
                response.put("anomaly_score", 0.0);
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> anomalies = aiService.detectAnomalies(deviceId);
            return ResponseEntity.ok(anomalies);
            
        } catch (Exception e) {
            logger.error("Error detecting anomalies", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to detect anomalies: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/performance")
    public ResponseEntity<?> analyzePerformance(@RequestBody Map<String, Object> request) {
        try {
            String deviceId = (String) request.get("deviceId");
            
            if (aiService == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Service not available");
                response.put("performance_ratio", 0.0);
                response.put("efficiency", 0.0);
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> performance = aiService.analyzePerformance(deviceId);
            return ResponseEntity.ok(performance);
            
        } catch (Exception e) {
            logger.error("Error analyzing performance", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to analyze performance: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/battery-optimization")
    public ResponseEntity<?> optimizeBattery(@RequestBody Map<String, Object> request) {
        try {
            String deviceId = (String) request.get("deviceId");
            
            if (aiService == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Service not available");
                response.put("action", "not_applicable");
                response.put("reason", "AI Service not available");
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> optimization = aiService.optimizeBattery(deviceId);
            return ResponseEntity.ok(optimization);
            
        } catch (Exception e) {
            logger.error("Error optimizing battery", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to optimize battery: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/process-telemetry")
    public ResponseEntity<?> processTelemetry(@RequestBody IoTDevice device) {
        try {
            if (aiService == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Service not available");
                response.put("processed", false);
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> results = aiService.processTelemetry(device);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Error processing telemetry", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to process telemetry: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getAIStatus() {
        boolean aiAvailable = aiService != null;
        
        Map<String, Object> response = new HashMap<>();
        response.put("ai_service_available", aiAvailable);
        response.put("service_status", aiAvailable ? "active" : "not_configured");
        
        if (aiAvailable) {
            Map<String, Boolean> capabilities = new HashMap<>();
            capabilities.put("power_forecasting", true);
            capabilities.put("anomaly_detection", true);
            capabilities.put("performance_analysis", true);
            capabilities.put("battery_optimization", true);
            response.put("capabilities", capabilities);
        } else {
            response.put("capabilities", new HashMap<>());
        }
        
        return ResponseEntity.ok(response);
    }
}