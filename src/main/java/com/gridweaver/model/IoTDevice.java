package com.gridweaver.model;

public class IoTDevice {
    private String deviceId;
    private String deviceType; // SOLAR_PANEL, BATTERY, WIND_TURBINE
    private Double latitude;
    private Double longitude;
    private Double powerOutput; // in kW
    private Double batteryLevel; // percentage (0-100)
    private String state; // CHARGING, DISCHARGING, IDLE, FAULT
    private Long timestamp;

    public IoTDevice() {
    }

    public IoTDevice(String deviceId, String deviceType, Double latitude, Double longitude,
                     Double powerOutput, Double batteryLevel, String state, Long timestamp) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.powerOutput = powerOutput;
        this.batteryLevel = batteryLevel;
        this.state = state;
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getPowerOutput() {
        return powerOutput;
    }

    public void setPowerOutput(Double powerOutput) {
        this.powerOutput = powerOutput;
    }

    public Double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
