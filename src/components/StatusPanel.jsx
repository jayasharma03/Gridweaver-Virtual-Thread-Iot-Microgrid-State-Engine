import React from 'react';
import './StatusPanel.css';

function StatusPanel({ gridStatus, connectionCount, deviceCount }) {
  const getGridLoadColor = (load) => {
    if (load > 80) return '#ff6b6b';
    if (load > 60) return '#ffd93d';
    return '#00ff88';
  };

  const mockGridLoad = gridStatus ? Math.random() * 100 : 0;

  return (
    <div className="status-panel">
      <h2>Grid Status</h2>
      
      <div className="status-metrics">
        <div className="metric-card">
          <div className="metric-label">Active Connections</div>
          <div className="metric-value">{connectionCount.toLocaleString()}</div>
          <div className="metric-sub">Virtual Threads</div>
        </div>

        <div className="metric-card">
          <div className="metric-label">Active Devices</div>
          <div className="metric-value">{deviceCount.toLocaleString()}</div>
          <div className="metric-sub">IoT Nodes</div>
        </div>

        <div className="metric-card">
          <div className="metric-label">Grid Load</div>
          <div 
            className="metric-value"
            style={{ color: getGridLoadColor(mockGridLoad) }}
          >
            {mockGridLoad.toFixed(1)}%
          </div>
          <div className="metric-sub">System Capacity</div>
        </div>

        <div className="metric-card">
          <div className="metric-label">System Status</div>
          <div 
            className="metric-value"
            style={{ color: gridStatus?.status === 'operational' ? '#00ff88' : '#ff6b6b' }}
          >
            {gridStatus?.status || 'Unknown'}
          </div>
          <div className="metric-sub">
            {gridStatus?.virtualThreadsEnabled ? 'Virtual Threads Enabled' : 'Traditional Threads'}
          </div>
        </div>
      </div>

      <div className="legend">
        <h3>Device States</h3>
        <div className="legend-items">
          <div className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#00ff88' }}></span>
            <span>Charging</span>
          </div>
          <div className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#ff6b6b' }}></span>
            <span>Discharging</span>
          </div>
          <div className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#00d4ff' }}></span>
            <span>Idle</span>
          </div>
          <div className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#ff0000' }}></span>
            <span>Fault</span>
          </div>
        </div>
      </div>

      <div className="legend">
        <h3>Device Types</h3>
        <div className="legend-items">
          <div className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#ffd93d' }}></span>
            <span>Solar Panel</span>
          </div>
          <div className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#6bcb77' }}></span>
            <span>Battery</span>
          </div>
          <div className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#4d96ff' }}></span>
            <span>Wind Turbine</span>
          </div>
        </div>
      </div>
    </div>
  );
}

export default StatusPanel;
