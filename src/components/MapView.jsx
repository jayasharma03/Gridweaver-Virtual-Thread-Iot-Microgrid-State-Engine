import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import HeatmapLayer from './HeatmapLayer';

// Fix for default marker icons in React Leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

const createCustomIcon = (color) => {
  return L.divIcon({
    className: 'custom-marker',
    html: `<div style="
      background-color: ${color};
      width: 20px;
      height: 20px;
      border-radius: 50%;
      border: 3px solid white;
      box-shadow: 0 2px 5px rgba(0,0,0,0.3);
    "></div>`,
    iconSize: [20, 20],
    iconAnchor: [10, 10],
  });
};

const getStateColor = (state) => {
  switch (state) {
    case 'CHARGING':
      return '#00ff88';
    case 'DISCHARGING':
      return '#ff6b6b';
    case 'FAULT':
      return '#ff0000';
    case 'IDLE':
    default:
      return '#00d4ff';
  }
};

const getDeviceTypeColor = (type) => {
  switch (type) {
    case 'SOLAR_PANEL':
      return '#ffd93d';
    case 'BATTERY':
      return '#6bcb77';
    case 'WIND_TURBINE':
      return '#4d96ff';
    default:
      return '#888888';
  }
};

function MapBounds({ devices }) {
  const map = useMap();

  useEffect(() => {
    if (devices.length > 0) {
      const bounds = L.latLngBounds(devices.map(d => [d.latitude, d.longitude]));
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [devices, map]);

  return null;
}

function MapView({ devices }) {
  const [mockDevices, setMockDevices] = useState([]);
  const [showHeatmap, setShowHeatmap] = useState(false);

  // Generate mock city grid devices if none provided
  useEffect(() => {
    if (devices.length === 0) {
      const generated = generateMockCityGrid();
      setMockDevices(generated);
    } else {
      setMockDevices(devices);
    }
  }, [devices]);

  const displayDevices = devices.length > 0 ? devices : mockDevices;

  return (
    <div className="map-container">
      <div className="map-controls">
        <button 
          className={`map-toggle-btn ${showHeatmap ? 'active' : ''}`}
          onClick={() => setShowHeatmap(!showHeatmap)}
        >
          {showHeatmap ? 'Show Markers' : 'Show Heatmap'}
        </button>
      </div>
      <MapContainer
        center={[40.7128, -74.0060]}
        zoom={13}
        style={{ height: '100%', width: '100%' }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MapBounds devices={displayDevices} />
        
        {showHeatmap ? (
          <HeatmapLayer devices={displayDevices} />
        ) : (
          displayDevices.map((device) => (
            <Marker
              key={device.deviceId}
              position={[device.latitude, device.longitude]}
              icon={createCustomIcon(getStateColor(device.state))}
            >
              <Popup>
                <div className="popup-content">
                  <h3>{device.deviceId}</h3>
                  <p><strong>Type:</strong> {device.deviceType}</p>
                  <p><strong>State:</strong> {device.state}</p>
                  <p><strong>Power:</strong> {device.powerOutput?.toFixed(2)} kW</p>
                  {device.batteryLevel !== undefined && (
                    <p><strong>Battery:</strong> {device.batteryLevel.toFixed(1)}%</p>
                  )}
                </div>
              </Popup>
            </Marker>
          ))
        )}
      </MapContainer>
    </div>
  );
}

// Generate mock city grid for initial display
function generateMockCityGrid() {
  const devices = [];
  const baseLat = 40.7128;
  const baseLng = -74.0060;
  
  const deviceTypes = ['SOLAR_PANEL', 'BATTERY', 'WIND_TURBINE'];
  const states = ['CHARGING', 'DISCHARGING', 'IDLE'];
  
  for (let i = 0; i < 50; i++) {
    const lat = baseLat + (Math.random() - 0.5) * 0.1;
    const lng = baseLng + (Math.random() - 0.5) * 0.1;
    
    devices.push({
      deviceId: `DEVICE-${String(i + 1).padStart(5, '0')}`,
      deviceType: deviceTypes[Math.floor(Math.random() * deviceTypes.length)],
      latitude: lat,
      longitude: lng,
      powerOutput: Math.random() * 50,
      batteryLevel: Math.random() * 100,
      state: states[Math.floor(Math.random() * states.length)],
      timestamp: Date.now()
    });
  }
  
  return devices;
}

export default MapView;
