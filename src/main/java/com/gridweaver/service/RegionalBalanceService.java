package com.gridweaver.service;

import com.gridweaver.model.IoTDevice;
import com.gridweaver.model.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegionalBalanceService {

    private static final Logger logger = LoggerFactory.getLogger(RegionalBalanceService.class);
    
    private final Map<Region, List<IoTDevice>> regionalDevices = new ConcurrentHashMap<>();
    private final Map<Region, Double> regionalPowerBalance = new ConcurrentHashMap<>();
    
    public void registerDevice(IoTDevice device) {
        Region region = determineRegion(device);
        regionalDevices.computeIfAbsent(region, k -> new ArrayList<>()).add(device);
        updateRegionalBalance(region);
        logger.debug("Registered device {} in region {}", device.getDeviceId(), region);
    }
    
    public void unregisterDevice(IoTDevice device) {
        Region region = determineRegion(device);
        List<IoTDevice> devices = regionalDevices.get(region);
        if (devices != null) {
            devices.removeIf(d -> d.getDeviceId().equals(device.getDeviceId()));
            updateRegionalBalance(region);
        }
    }
    
    public void updateDevice(IoTDevice device) {
        Region region = determineRegion(device);
        updateRegionalBalance(region);
    }
    
    public void balanceRegionalPower() {
        logger.info("Starting regional power balancing...");
        
        // Calculate power surplus/deficit for each region
        Map<Region, Double> powerImbalance = calculatePowerImbalance();
        
        // Identify regions with surplus and deficit
        List<Region> surplusRegions = new ArrayList<>();
        List<Region> deficitRegions = new ArrayList<>();
        
        powerImbalance.forEach((region, imbalance) -> {
            if (imbalance > 10.0) { // Threshold for significant surplus
                surplusRegions.add(region);
            } else if (imbalance < -10.0) { // Threshold for significant deficit
                deficitRegions.add(region);
            }
        });
        
        // Simulate power transfer from surplus to deficit regions
        for (Region surplusRegion : surplusRegions) {
            for (Region deficitRegion : deficitRegions) {
                double transferAmount = calculateTransferAmount(surplusRegion, deficitRegion, powerImbalance);
                if (transferAmount > 0) {
                    executePowerTransfer(surplusRegion, deficitRegion, transferAmount);
                }
            }
        }
        
        logger.info("Regional power balancing completed. Surplus regions: {}, Deficit regions: {}", 
                   surplusRegions.size(), deficitRegions.size());
    }
    
    private Region determineRegion(IoTDevice device) {
        // Simple geographic region determination based on coordinates
        // Using NYC coordinates as reference: 40.7128, -74.0060
        double lat = device.getLatitude();
        double lng = device.getLongitude();
        
        double baseLat = 40.7128;
        double baseLng = -74.0060;
        
        if (lat > baseLat + 0.05) {
            return Region.NORTH;
        } else if (lat < baseLat - 0.05) {
            return Region.SOUTH;
        } else if (lng > baseLng + 0.05) {
            return Region.EAST;
        } else if (lng < baseLng - 0.05) {
            return Region.WEST;
        } else {
            return Region.CENTRAL;
        }
    }
    
    private void updateRegionalBalance(Region region) {
        List<IoTDevice> devices = regionalDevices.get(region);
        if (devices == null || devices.isEmpty()) {
            regionalPowerBalance.put(region, 0.0);
            return;
        }
        
        double totalPower = devices.stream()
            .mapToDouble(device -> {
                if (device.getState().equals("DISCHARGING")) {
                    return device.getPowerOutput();
                } else if (device.getState().equals("CHARGING")) {
                    return -device.getPowerOutput(); // Charging consumes power
                }
                return 0.0;
            })
            .sum();
        
        regionalPowerBalance.put(region, totalPower);
    }
    
    private Map<Region, Double> calculatePowerImbalance() {
        Map<Region, Double> imbalance = new HashMap<>();
        
        // Calculate average power across all regions
        double averagePower = regionalPowerBalance.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        // Calculate imbalance for each region
        regionalPowerBalance.forEach((region, power) -> {
            imbalance.put(region, power - averagePower);
        });
        
        return imbalance;
    }
    
    private double calculateTransferAmount(Region surplusRegion, Region deficitRegion, 
                                        Map<Region, Double> powerImbalance) {
        double surplus = powerImbalance.get(surplusRegion);
        double deficit = Math.abs(powerImbalance.get(deficitRegion));
        
        // Transfer the minimum of surplus and deficit, with efficiency loss
        return Math.min(surplus, deficit) * 0.9; // 10% transmission loss
    }
    
    private void executePowerTransfer(Region fromRegion, Region toRegion, double amount) {
        logger.info("Transferring {} kW from {} to {}", String.format("%.2f", amount), fromRegion, toRegion);
        
        // In a real implementation, this would:
        // 1. Coordinate with grid operators
        // 2. Adjust device states in source region (reduce discharge)
        // 3. Adjust device states in target region (increase discharge)
        // 4. Update power flow routing
        
        // For simulation, just log the transfer
        updateRegionalBalance(fromRegion);
        updateRegionalBalance(toRegion);
    }
    
    public Map<Region, Double> getRegionalPowerBalance() {
        return new HashMap<>(regionalPowerBalance);
    }
    
    public Map<Region, Integer> getRegionalDeviceCount() {
        Map<Region, Integer> counts = new HashMap<>();
        regionalDevices.forEach((region, devices) -> {
            counts.put(region, devices.size());
        });
        return counts;
    }
}