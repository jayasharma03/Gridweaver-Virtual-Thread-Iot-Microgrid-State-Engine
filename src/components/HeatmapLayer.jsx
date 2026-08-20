import React, { useEffect, useRef } from 'react';
import { useMap } from 'react-leaflet';
import L from 'leaflet';

const HeatmapLayer = ({ devices }) => {
  const map = useMap();
  const heatmapLayerRef = useRef(null);

  useEffect(() => {
    if (!map) return;

    // Remove existing heatmap layer
    if (heatmapLayerRef.current) {
      map.removeLayer(heatmapLayerRef.current);
    }

    // Create heatmap-like visualization using circle markers
    const heatmapGroup = L.layerGroup();

    devices.forEach(device => {
      const intensity = calculateIntensity(device);
      const radius = calculateRadius(device);
      const color = getColorForIntensity(intensity);

      const circle = L.circleMarker([device.latitude, device.longitude], {
        radius: radius,
        fillColor: color,
        color: color,
        weight: 1,
        opacity: 0.6,
        fillOpacity: 0.4
      });

      circle.bindPopup(`
        <div class="heatmap-popup">
          <h3>${device.deviceId}</h3>
          <p><strong>Type:</strong> ${device.deviceType}</p>
          <p><strong>Power:</strong> ${device.powerOutput?.toFixed(2)} kW</p>
          <p><strong>Intensity:</strong> ${(intensity * 100).toFixed(1)}%</p>
        </div>
      `);

      heatmapGroup.addLayer(circle);
    });

    heatmapLayerRef.current = heatmapGroup;
    heatmapGroup.addTo(map);

    return () => {
      if (heatmapLayerRef.current) {
        map.removeLayer(heatmapLayerRef.current);
      }
    };
  }, [map, devices]);

  const calculateIntensity = (device) => {
    // Calculate intensity based on power output and device type
    let baseIntensity = 0;
    
    switch (device.deviceType) {
      case 'SOLAR_PANEL':
        baseIntensity = Math.min(device.powerOutput / 50, 1); // Max 50kW
        break;
      case 'WIND_TURBINE':
        baseIntensity = Math.min(device.powerOutput / 80, 1); // Max 80kW
        break;
      case 'BATTERY':
        // Intensity based on battery level and state
        const batteryLevel = device.batteryLevel || 0;
        baseIntensity = batteryLevel / 100;
        if (device.state === 'DISCHARGING') {
          baseIntensity *= 1.2; // Boost intensity when discharging
        }
        break;
      default:
        baseIntensity = 0.5;
    }

    return Math.min(Math.max(baseIntensity, 0), 1);
  };

  const calculateRadius = (device) => {
    // Radius based on device type and power
    const baseRadius = 20;
    
    switch (device.deviceType) {
      case 'SOLAR_PANEL':
        return baseRadius + (device.powerOutput / 2);
      case 'WIND_TURBINE':
        return baseRadius + (device.powerOutput / 1.5);
      case 'BATTERY':
        return baseRadius + ((device.batteryLevel || 0) / 3);
      default:
        return baseRadius;
    }
  };

  const getColorForIntensity = (intensity) => {
    // Color gradient from blue (low) to red (high)
    if (intensity < 0.25) {
      return `rgba(0, 100, 255, ${0.3 + intensity})`;
    } else if (intensity < 0.5) {
      return `rgba(0, 200, 200, ${0.3 + intensity})`;
    } else if (intensity < 0.75) {
      return `rgba(255, 200, 0, ${0.3 + intensity})`;
    } else {
      return `rgba(255, 50, 50, ${0.3 + intensity})`;
    }
  };

  return null;
};

export default HeatmapLayer;