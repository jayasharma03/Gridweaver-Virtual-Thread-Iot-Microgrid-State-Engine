package com.gridweaver.service;

import com.gridweaver.model.DeviceEvent;
import com.gridweaver.model.DeviceState;
import com.gridweaver.model.IoTDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceStateService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceStateService.class);
    private final ConcurrentHashMap<String, StateMachine<DeviceState, DeviceEvent>> deviceStateMachines = new ConcurrentHashMap<>();
    private final StateMachineFactory<DeviceState, DeviceEvent> stateMachineFactory;

    @Value("${gridweaver.state-machine.grid-load-threshold:80}")
    private double gridLoadThreshold;

    @Value("${gridweaver.state-machine.battery-discharge-threshold:70}")
    private double batteryDischargeThreshold;

    public DeviceStateService(StateMachineFactory<DeviceState, DeviceEvent> stateMachineFactory) {
        this.stateMachineFactory = stateMachineFactory;
    }

    public void processDeviceState(IoTDevice device, double currentGridLoad) {
        StateMachine<DeviceState, DeviceEvent> stateMachine = deviceStateMachines
            .computeIfAbsent(device.getDeviceId(), id -> {
                StateMachine<DeviceState, DeviceEvent> sm = stateMachineFactory.getStateMachine(id);
                // Using start() despite deprecation - it's functional for current Spring State Machine version
                sm.start();
                return sm;
            });

        DeviceEvent event = determineEvent(device, currentGridLoad);
        
        if (event != null && stateMachine.sendEvent(event)) {
            DeviceState newState = stateMachine.getState().getId();
            String oldState = device.getState();
            
            logger.info("State transition for device {}: {} -> {} (event: {})", 
                       device.getDeviceId(), oldState, newState, event);
            
            // Update the device state to match the state machine
            device.setState(newState.name());
        }
    }

    private DeviceEvent determineEvent(IoTDevice device, double currentGridLoad) {
        // Check for fault conditions
        if ("FAULT".equals(device.getState())) {
            return DeviceEvent.FAULT_CLEARED;
        }

        // High grid load (>80%) - trigger discharge if battery has sufficient charge
        if (currentGridLoad > gridLoadThreshold && 
            device.getBatteryLevel() > batteryDischargeThreshold &&
            "BATTERY".equals(device.getDeviceType())) {
            return DeviceEvent.GRID_LOAD_HIGH;
        }

        // Normal grid load - stop discharging
        if (currentGridLoad <= gridLoadThreshold && "DISCHARGING".equals(device.getState())) {
            return DeviceEvent.GRID_LOAD_NORMAL;
        }

        // Low grid load - trigger charging for batteries
        if (currentGridLoad < 40.0 && 
            "BATTERY".equals(device.getDeviceType()) &&
            device.getBatteryLevel() < 90.0) {
            return DeviceEvent.GRID_LOAD_LOW;
        }

        return null;
    }

    public DeviceState getCurrentState(String deviceId) {
        StateMachine<DeviceState, DeviceEvent> stateMachine = deviceStateMachines.get(deviceId);
        return stateMachine != null ? stateMachine.getState().getId() : DeviceState.IDLE;
    }
}
