package com.gridweaver.service;

import com.gridweaver.model.IoTDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private final Random random = new Random();
    
    // In production, this would call the Python AI service
    // For now, providing mock AI functionality
    
    public Map<String, Object> getPowerForecast(String deviceId, int hoursAhead) {
        logger.info("Generating power forecast for device: {}, hours ahead: {}", deviceId, hoursAhead);
        
        // Generate mock forecast data
        Map<String, Object> forecast = new HashMap<>();
        forecast.put("device_id", deviceId);
        forecast.put("forecast_hours", hoursAhead);
        forecast.put("confidence", 0.75);
        forecast.put("method", "mock_forecast");
        
        // Generate hourly forecast
        java.util.List<Map<String, Object>> hourlyForecast = new java.util.ArrayList<>();
        double totalEnergy = 0.0;
        
        for (int hour = 0; hour < hoursAhead; hour++) {
            double solarFactor = calculateSolarFactor(hour);
            double predictedPower = 25.0 * solarFactor + (random.nextDouble() - 0.5) * 5.0;
            predictedPower = Math.max(0, predictedPower);
            
            Map<String, Object> hourData = new HashMap<>();
            hourData.put("hour", hour);
            hourData.put("predicted_power", Math.round(predictedPower * 100.0) / 100.0);
            hourData.put("timestamp", java.time.LocalDateTime.now().plusHours(hour).toString());
            
            hourlyForecast.add(hourData);
            totalEnergy += predictedPower;
        }
        
        forecast.put("hourly_forecast", hourlyForecast);
        forecast.put("total_energy", Math.round(totalEnergy * 100.0) / 100.0);
        
        return forecast;
    }
    
    private double calculateSolarFactor(int hour) {
        // Simple solar pattern based on time of day
        int currentHour = java.time.LocalDateTime.now().getHour();
        int targetHour = (currentHour + hour) % 24;
        
        if (6 <= targetHour && targetHour <= 18) {
            // Bell curve centered at noon
            return Math.exp(-Math.pow(targetHour - 12, 2) / 8.0);
        } else {
            return 0.1; // Minimal generation at night
        }
    }
    
    public Map<String, Object> detectAnomalies(String deviceId) {
        logger.info("Detecting anomalies for device: {}", deviceId);
        
        // Generate mock anomaly detection
        boolean hasAnomaly = random.nextDouble() < 0.1; // 10% chance of anomaly
        double anomalyScore = hasAnomaly ? random.nextDouble() * 3 + 2.5 : random.nextDouble();
        
        Map<String, Object> result = new HashMap<>();
        result.put("device_id", deviceId);
        result.put("has_anomaly", hasAnomaly);
        result.put("anomaly_score", Math.round(anomalyScore * 100.0) / 100.0);
        result.put("anomaly_type", hasAnomaly ? 
            (random.nextBoolean() ? "low_output" : "high_output") : null);
        result.put("message", String.format("Z-score: %.2f", anomalyScore));
        
        return result;
    }
    
    public Map<String, Object> analyzePerformance(String deviceId) {
        logger.info("Analyzing performance for device: {}", deviceId);
        
        // Generate mock performance analysis
        double performanceRatio = 70 + random.nextDouble() * 25; // 70-95%
        double efficiency = performanceRatio * 0.95;
        
        Map<String, Object> result = new HashMap<>();
        result.put("device_id", deviceId);
        result.put("performance_ratio", Math.round(performanceRatio * 100.0) / 100.0);
        result.put("efficiency", Math.round(efficiency * 100.0) / 100.0);
        result.put("average_power", Math.round((20 + random.nextDouble() * 10) * 100.0) / 100.0);
        result.put("max_power", Math.round((25 + random.nextDouble() * 5) * 100.0) / 100.0);
        result.put("min_power", Math.round((15 + random.nextDouble() * 5) * 100.0) / 100.0);
        result.put("status", performanceRatio > 80 ? "good" : 
            performanceRatio > 50 ? "degraded" : "poor");
        
        return result;
    }
    
    public Map<String, Object> optimizeBattery(String deviceId) {
        logger.info("Optimizing battery for device: {}", deviceId);
        
        // Generate mock battery optimization
        double currentSoc = random.nextDouble() * 100;
        String currentAction = currentSoc < 30 ? "charging" : 
            currentSoc > 70 ? "discharging" : "idle";
        
        String recommendation;
        String reason;
        
        if (currentSoc < 20) {
            recommendation = "charge";
            reason = "Battery critically low";
        } else if (currentSoc < 40) {
            recommendation = "charge";
            reason = "Battery low, recommend charging";
        } else if (currentSoc > 80) {
            recommendation = "discharge";
            reason = "Battery high, can discharge to grid";
        } else {
            recommendation = "idle";
            reason = "Battery at optimal level";
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("device_id", deviceId);
        result.put("current_action", currentAction);
        result.put("recommended_action", recommendation);
        result.put("reason", reason);
        result.put("current_soc", Math.round(currentSoc * 100.0) / 100.0);
        result.put("optimal_soc_range", java.util.List.of(40, 80));
        result.put("efficiency_gain", Math.round(Math.abs(currentSoc - 60) * 0.1 * 100.0) / 100.0);
        
        return result;
    }
    
    public Map<String, Object> processTelemetry(IoTDevice device) {
        logger.info("Processing telemetry for device: {}", device.getDeviceId());
        
        // Process all AI analyses
        Map<String, Object> results = new HashMap<>();
        results.put("device_id", device.getDeviceId());
        results.put("timestamp", java.time.LocalDateTime.now().toString());
        results.put("power_forecast", getPowerForecast(device.getDeviceId(), 24));
        results.put("anomaly_detection", detectAnomalies(device.getDeviceId()));
        results.put("performance_analysis", analyzePerformance(device.getDeviceId()));
        results.put("battery_optimization", optimizeBattery(device.getDeviceId()));
        
        return results;
    }
}