import React, { useState, useEffect, useRef } from 'react';
import './EventLogPanel.css';

function EventLogPanel() {
  const [events, setEvents] = useState([]);
  const [isExpanded, setIsExpanded] = useState(false);
  const [filter, setFilter] = useState('ALL');
  const eventsEndRef = useRef(null);

  // Auto-scroll to bottom when new events arrive
  const scrollToBottom = () => {
    eventsEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [events]);

  // Simulate receiving state transition events
  useEffect(() => {
    const eventTypes = ['STATE_TRANSITION', 'FAULT_DETECTED', 'FAULT_CLEARED', 'GRID_LOAD_HIGH', 'GRID_LOAD_NORMAL'];
    const deviceTypes = ['SOLAR_PANEL', 'BATTERY', 'WIND_TURBINE'];
    const states = ['CHARGING', 'DISCHARGING', 'IDLE', 'FAULT'];

    // Add initial mock events
    const initialEvents = [];
    for (let i = 0; i < 10; i++) {
      initialEvents.push(generateMockEvent(i, eventTypes, deviceTypes, states));
    }
    setEvents(initialEvents);

    // Simulate new events coming in
    const interval = setInterval(() => {
      const newEvent = generateMockEvent(
        events.length,
        eventTypes,
        deviceTypes,
        states
      );
      setEvents(prev => {
        const updated = [newEvent, ...prev];
        // Keep only last 100 events to prevent memory issues
        return updated.slice(0, 100);
      });
    }, 3000); // New event every 3 seconds

    return () => clearInterval(interval);
  }, []);

  const generateMockEvent = (id, eventTypes, deviceTypes, states) => {
    const eventType = eventTypes[Math.floor(Math.random() * eventTypes.length)];
    const deviceType = deviceTypes[Math.floor(Math.random() * deviceTypes.length)];
    const deviceId = `DEVICE-${String(Math.floor(Math.random() * 1000) + 1).padStart(5, '0')}`;
    const fromState = states[Math.floor(Math.random() * states.length)];
    const toState = states[Math.floor(Math.random() * states.length)];
    
    return {
      id: id,
      timestamp: new Date().toISOString(),
      eventType: eventType,
      deviceId: deviceId,
      deviceType: deviceType,
      fromState: fromState,
      toState: toState,
      details: `${eventType} for ${deviceType} ${deviceId}`
    };
  };

  const getEventTypeColor = (eventType) => {
    switch (eventType) {
      case 'STATE_TRANSITION':
        return '#4a90e2';
      case 'FAULT_DETECTED':
        return '#e74c3c';
      case 'FAULT_CLEARED':
        return '#27ae60';
      case 'GRID_LOAD_HIGH':
        return '#f39c12';
      case 'GRID_LOAD_NORMAL':
        return '#2ecc71';
      default:
        return '#95a5a6';
    }
  };

  const filteredEvents = filter === 'ALL' 
    ? events 
    : events.filter(event => event.eventType === filter);

  const getEventCount = (type) => {
    return events.filter(e => e.eventType === type).length;
  };

  return (
    <div className={`event-log-panel ${isExpanded ? 'expanded' : 'collapsed'}`}>
      <div className="event-log-header">
        <h3>Event Log</h3>
        <button 
          className="toggle-btn"
          onClick={() => setIsExpanded(!isExpanded)}
        >
          {isExpanded ? '▼' : '▲'}
        </button>
      </div>

      {isExpanded && (
        <>
          <div className="event-log-filters">
            <button 
              className={`filter-btn ${filter === 'ALL' ? 'active' : ''}`}
              onClick={() => setFilter('ALL')}
            >
              All ({events.length})
            </button>
            <button 
              className={`filter-btn ${filter === 'STATE_TRANSITION' ? 'active' : ''}`}
              onClick={() => setFilter('STATE_TRANSITION')}
            >
              Transitions ({getEventCount('STATE_TRANSITION')})
            </button>
            <button 
              className={`filter-btn ${filter === 'FAULT_DETECTED' ? 'active' : ''}`}
              onClick={() => setFilter('FAULT_DETECTED')}
            >
              Faults ({getEventCount('FAULT_DETECTED')})
            </button>
            <button 
              className={`filter-btn ${filter === 'GRID_LOAD_HIGH' ? 'active' : ''}`}
              onClick={() => setFilter('GRID_LOAD_HIGH')}
            >
              High Load ({getEventCount('GRID_LOAD_HIGH')})
            </button>
          </div>

          <div className="event-log-content">
            {filteredEvents.length === 0 ? (
              <div className="no-events">No events to display</div>
            ) : (
              filteredEvents.map((event) => (
                <div key={event.id} className="event-item">
                  <div className="event-header">
                    <span 
                      className="event-type-badge"
                      style={{ backgroundColor: getEventTypeColor(event.eventType) }}
                    >
                      {event.eventType}
                    </span>
                    <span className="event-timestamp">
                      {new Date(event.timestamp).toLocaleTimeString()}
                    </span>
                  </div>
                  <div className="event-details">
                    <span className="device-id">{event.deviceId}</span>
                    <span className="device-type">{event.deviceType}</span>
                    {event.fromState && event.toState && (
                      <span className="state-transition">
                        {event.fromState} → {event.toState}
                      </span>
                    )}
                  </div>
                </div>
              ))
            )}
            <div ref={eventsEndRef} />
          </div>

          <div className="event-log-footer">
            <span className="event-count">
              Showing {filteredEvents.length} of {events.length} events
            </span>
            <button 
              className="clear-btn"
              onClick={() => setEvents([])}
            >
              Clear Log
            </button>
          </div>
        </>
      )}
    </div>
  );
}

export default EventLogPanel;