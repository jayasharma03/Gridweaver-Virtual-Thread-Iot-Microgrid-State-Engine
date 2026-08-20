import React, { useState, useEffect } from 'react';
import './AIPredictionsPanel.css';

function AIPredictionsPanel({ deviceId }) {
  const [aiStatus, setAiStatus] = useState(null);
  const [forecast, setForecast] = useState(null);
  const [anomalies, setAnomalies] = useState(null);
  const [performance, setPerformance] = useState(null);
  const [batteryOptimization, setBatteryOptimization] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchAIStatus();
    if (deviceId) {
      fetchAIData();
    }
  }, [deviceId]);

  const fetchAIStatus = async () => {
    try {
      const response = await fetch('/api/ai/status');
      const data = await response.json();
      setAiStatus(data);
    } catch (error) {
      console.error('Error fetching AI status:', error);
    }
  };

  const fetchAIData = async () => {
    setLoading(true);
    try {
      // Fetch all AI data in parallel
      const [forecastRes, anomalyRes, perfRes, battRes] = await Promise.all([
        fetch('/api/ai/forecast', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ deviceId, hoursAhead: 24 })
        }),
        fetch('/api/ai/anomaly', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ deviceId })
        }),
        fetch('/api/ai/performance', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ deviceId })
        }),
        fetch('/api/ai/battery-optimization', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ deviceId })
        })
      ]);

      const forecastData = await forecastRes.json();
      const anomalyData = await anomalyRes.json();
      const perfData = await perfRes.json();
      const battData = await battRes.json();

      setForecast(forecastData);
      setAnomalies(anomalyData);
      setPerformance(perfData);
      setBatteryOptimization(battData);
    } catch (error) {
      console.error('Error fetching AI data:', error);
    } finally {
      setLoading(false);
    }
  };

  const renderForecastChart = () => {
    if (!forecast || !forecast.hourly_forecast) return null;

    const maxPower = Math.max(...forecast.hourly_forecast.map(h => h.predicted_power));
    
    return (
      <div className="forecast-chart">
        <h4>24-Hour Power Forecast</h4>
        <div className="chart-container">
          {forecast.hourly_forecast.map((hour, index) => (
            <div 
              key={index} 
              className="chart-bar"
              style={{
                height: `${(hour.predicted_power / maxPower) * 100}%`,
                backgroundColor: hour.predicted_power > 15 ? '#4CAF50' : 
                               hour.predicted_power > 5 ? '#FFC107' : '#F44336'
              }}
              title={`${hour.hour}:00 - ${hour.predicted_power.toFixed(2)} kW`}
            />
          ))}
        </div>
        <div className="chart-legend">
          <span className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#4CAF50' }}></span>
            High (&gt;15kW)
          </span>
          <span className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#FFC107' }}></span>
            Medium (5-15kW)
          </span>
          <span className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#F44336' }}></span>
            Low (&lt;5kW)
          </span>
        </div>
      </div>
    );
  };

  const renderAnomalyStatus = () => {
    if (!anomalies) return null;

    return (
      <div className={`anomaly-status ${anomalies.has_anomaly ? 'anomaly-detected' : 'normal'}`}>
        <h4>Anomaly Detection</h4>
        <div className="status-indicator">
          <span className={`indicator ${anomalies.has_anomaly ? 'warning' : 'ok'}`}>
            {anomalies.has_anomaly ? '⚠️' : '✅'}
          </span>
          <span className="status-text">
            {anomalies.has_anomaly ? 'Anomaly Detected' : 'Normal Operation'}
          </span>
        </div>
        {anomalies.has_anomaly && (
          <div className="anomaly-details">
            <p><strong>Type:</strong> {anomalies.anomaly_type}</p>
            <p><strong>Score:</strong> {anomalies.anomaly_score}</p>
            <p><strong>Message:</strong> {anomalies.message}</p>
          </div>
        )}
      </div>
    );
  };

  const renderPerformanceMetrics = () => {
    if (!performance) return null;

    return (
      <div className="performance-metrics">
        <h4>Performance Analysis</h4>
        <div className="metric-grid">
          <div className="metric-card">
            <span className="metric-label">Performance Ratio</span>
            <span className="metric-value">{performance.performance_ratio}%</span>
          </div>
          <div className="metric-card">
            <span className="metric-label">Efficiency</span>
            <span className="metric-value">{performance.efficiency}%</span>
          </div>
          <div className="metric-card">
            <span className="metric-label">Avg Power</span>
            <span className="metric-value">{performance.average_power} kW</span>
          </div>
          <div className="metric-card">
            <span className="metric-label">Status</span>
            <span className={`metric-value status-${performance.status}`}>
              {performance.status}
            </span>
          </div>
        </div>
      </div>
    );
  };

  const renderBatteryOptimization = () => {
    if (!batteryOptimization) return null;

    return (
      <div className="battery-optimization">
        <h4>Battery Optimization</h4>
        <div className="battery-status">
          <div className="soc-display">
            <span className="soc-label">Current SOC:</span>
            <span className="soc-value">{batteryOptimization.current_soc}%</span>
          </div>
          <div className="soc-bar">
            <div 
              className="soc-fill"
              style={{ width: `${batteryOptimization.current_soc}%` }}
            />
          </div>
        </div>
        <div className="optimization-recommendation">
          <p><strong>Current:</strong> {batteryOptimization.current_action}</p>
          <p><strong>Recommended:</strong> {batteryOptimization.recommended_action}</p>
          <p><strong>Reason:</strong> {batteryOptimization.reason}</p>
          <p><strong>Efficiency Gain:</strong> {batteryOptimization.efficiency_gain}%</p>
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="ai-predictions-panel">
        <h3>AI Predictions</h3>
        <div className="loading">Loading AI data...</div>
      </div>
    );
  }

  return (
    <div className="ai-predictions-panel">
      <div className="ai-header">
        <h3>AI Predictions & Analytics</h3>
        {aiStatus && (
          <span className={`ai-status-badge ${aiStatus.ai_service_available ? 'active' : 'inactive'}`}>
            {aiStatus.ai_service_available ? '🤖 AI Active' : '⚠️ AI Inactive'}
          </span>
        )}
      </div>

      {aiStatus && !aiStatus.ai_service_available && (
        <div className="ai-unavailable">
          <p>AI Service is not currently available. Running in simulation mode.</p>
        </div>
      )}

      <div className="ai-content">
        {renderForecastChart()}
        {renderAnomalyStatus()}
        {renderPerformanceMetrics()}
        {renderBatteryOptimization()}
      </div>

      <div className="ai-footer">
        <button onClick={fetchAIData} className="refresh-btn">
          Refresh AI Data
        </button>
        <span className="last-updated">
          Last updated: {new Date().toLocaleTimeString()}
        </span>
      </div>
    </div>
  );
}

export default AIPredictionsPanel;