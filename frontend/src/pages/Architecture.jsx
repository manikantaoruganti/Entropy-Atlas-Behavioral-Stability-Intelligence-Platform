import React, { useState } from 'react';
import { useSystemHealth, useRawPrometheusMetrics } from '../api/hooks';
import {
  Radio,
  MessageSquare,
  Filter,
  Activity,
  GitBranch,
  Shield,
  RotateCcw,
  Lightbulb,
  Database,
  HardDrive,
  BarChart3,
  LineChart,
  Monitor,
  ChevronRight,
  CheckCircle,
  XCircle,
  AlertTriangle,
} from 'lucide-react';

const NODES = [
  { id: 'producers', name: 'Producers', category: 'Ingestion Source', icon: Radio, description: 'External event producers pushing behavioral signals into the ingestion pipeline via HTTP or message queue.' },
  { id: 'kafka', name: 'Kafka', category: 'Ingestion Pipeline', icon: MessageSquare, description: 'Distributed streaming platform providing durable, ordered event transport between producers and processing stages.' },
  { id: 'extraction', name: 'Extraction', category: 'Preprocessing', icon: Filter, description: 'Feature extraction stage that parses raw events and derives behavioral attributes for downstream analysis.' },
  { id: 'entropy', name: 'Entropy Engine', category: 'Processor', icon: Activity, description: 'Core computation engine calculating Shannon entropy over behavioral feature distributions per entity.' },
  { id: 'drift', name: 'Drift Detector', category: 'Processor', icon: GitBranch, description: 'Statistical drift detection module comparing current behavioral distributions against historical baselines.' },
  { id: 'stability', name: 'Stability Analyzer', category: 'Processor', icon: Shield, description: 'Stability scoring engine that classifies entities into stable, volatile, or transitioning behavioral states.' },
  { id: 'replay', name: 'Replay Engine', category: 'Processor', icon: RotateCcw, description: 'Deterministic replay subsystem for re-processing historical event windows and validating computation consistency.' },
  { id: 'explainability', name: 'Explainability', category: 'Processor', icon: Lightbulb, description: 'Drift explanation generator providing human-readable attribution of behavioral changes to specific features.' },
  { id: 'redis', name: 'Redis', category: 'Storage', icon: Database, description: 'In-memory data store used for caching entity state, recent entropy windows, and hot-path lookups.' },
  { id: 'postgres', name: 'PostgreSQL', category: 'Storage', icon: HardDrive, description: 'Primary relational store for entity profiles, historical entropy series, drift events, and replay audit logs.' },
  { id: 'prometheus', name: 'Prometheus', category: 'Observability', icon: BarChart3, description: 'Metrics collection and time-series database scraping JVM, Kafka, and application-level operational metrics.' },
  { id: 'grafana', name: 'Grafana', category: 'Observability', icon: LineChart, description: 'Visualization and alerting platform rendering operational dashboards from Prometheus metric data.' },
  { id: 'frontend', name: 'Frontend', category: 'Visualization', icon: Monitor, description: 'React-based behavioral intelligence UI for topology exploration, entropy analysis, drift investigation, and replay.' },
];

const CATEGORY_COLORS = {
  'Ingestion Source': '#60a5fa',
  'Ingestion Pipeline': '#f59e0b',
  'Preprocessing': '#a78bfa',
  'Processor': '#34d399',
  'Storage': '#f87171',
  'Observability': '#fbbf24',
  'Visualization': '#38bdf8',
};

function getHealthStatus(nodeId, health) {
  if (!health) return null;

  if (nodeId === 'redis' && health.redis) {
    return health.redis.status;
  }
  if (nodeId === 'postgres' && health.postgres) {
    return health.postgres.status;
  }
  if (nodeId === 'kafka' && health.kafka) {
    return health.kafka.status;
  }

  return null;
}

function StatusBadge({ status }) {
  if (!status) {
    return (
      <span className="badge-neutral" style={{ display: 'inline-flex', alignItems: 'center', gap: 4, fontSize: 11 }}>
        <AlertTriangle size={12} />
        Unknown
      </span>
    );
  }

  const normalized = status.toLowerCase();
  if (normalized === 'up' || normalized === 'healthy') {
    return (
      <span className="badge-success" style={{ display: 'inline-flex', alignItems: 'center', gap: 4, fontSize: 11 }}>
        <CheckCircle size={12} />
        {status}
      </span>
    );
  }
  if (normalized === 'down' || normalized === 'unhealthy') {
    return (
      <span className="badge-danger" style={{ display: 'inline-flex', alignItems: 'center', gap: 4, fontSize: 11 }}>
        <XCircle size={12} />
        {status}
      </span>
    );
  }

  return (
    <span className="badge-warning" style={{ display: 'inline-flex', alignItems: 'center', gap: 4, fontSize: 11 }}>
      <AlertTriangle size={12} />
      {status}
    </span>
  );
}

export default function Architecture() {
  const [selectedNode, setSelectedNode] = useState(null);
  const { data: health } = useSystemHealth();
  const { data: metrics } = useRawPrometheusMetrics();

  const selected = selectedNode ? NODES.find((n) => n.id === selectedNode) : null;

  return (
    <div style={{ display: 'flex', gap: 16, height: '100%', minHeight: 0 }}>
      {/* Left: Node Grid */}
      <div style={{ flex: 3, minWidth: 0, overflow: 'auto' }}>
        <div style={{ marginBottom: 16 }}>
          <h2 style={{ margin: 0, fontSize: 18, fontWeight: 600 }}>System Topology</h2>
          <p style={{ margin: '4px 0 0', fontSize: 12, opacity: 0.6 }}>
            {NODES.length} components · Click a node to inspect
          </p>
        </div>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
            gap: 10,
          }}
        >
          {NODES.map((node) => {
            const Icon = node.icon;
            const isSelected = selectedNode === node.id;
            const catColor = CATEGORY_COLORS[node.category] || '#888';
            const healthStatus = getHealthStatus(node.id, health);

            return (
              <div
                key={node.id}
                className="glass-card"
                onClick={() => setSelectedNode(node.id)}
                style={{
                  padding: '12px 14px',
                  cursor: 'pointer',
                  border: isSelected ? `1.5px solid ${catColor}` : '1.5px solid transparent',
                  transition: 'border-color 0.15s ease',
                  position: 'relative',
                }}
              >
                <div
                  style={{
                    fontSize: 10,
                    textTransform: 'uppercase',
                    fontFamily: 'monospace',
                    letterSpacing: '0.05em',
                    color: catColor,
                    marginBottom: 8,
                    fontWeight: 600,
                  }}
                >
                  {node.category}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <Icon size={18} style={{ color: catColor, flexShrink: 0 }} />
                  <span style={{ fontSize: 13, fontWeight: 500 }}>{node.name}</span>
                </div>
                {healthStatus && (
                  <div style={{ marginTop: 8 }}>
                    <StatusBadge status={healthStatus} />
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Right: Detail Panel */}
      <div
        className="glass-card"
        style={{
          flex: 1,
          minWidth: 240,
          padding: 20,
          overflow: 'auto',
          alignSelf: 'flex-start',
          position: 'sticky',
          top: 0,
        }}
      >
        {!selected ? (
          <div className="empty-state" style={{ textAlign: 'center', padding: '40px 12px' }}>
            <ChevronRight size={32} style={{ opacity: 0.3, marginBottom: 8 }} />
            <div style={{ fontSize: 13, opacity: 0.5 }}>Select a node to view details</div>
          </div>
        ) : (
          <div>
            <div
              style={{
                fontSize: 10,
                textTransform: 'uppercase',
                fontFamily: 'monospace',
                letterSpacing: '0.05em',
                color: CATEGORY_COLORS[selected.category] || '#888',
                marginBottom: 6,
                fontWeight: 600,
              }}
            >
              {selected.category}
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 14 }}>
              <selected.icon size={22} style={{ color: CATEGORY_COLORS[selected.category] || '#888' }} />
              <h3 style={{ margin: 0, fontSize: 16, fontWeight: 600 }}>{selected.name}</h3>
            </div>

            <p style={{ fontSize: 12, lineHeight: 1.6, opacity: 0.75, margin: '0 0 18px' }}>
              {selected.description}
            </p>

            <div
              style={{
                borderTop: '1px solid rgba(255,255,255,0.08)',
                paddingTop: 14,
              }}
            >
              <div
                style={{
                  fontSize: 10,
                  textTransform: 'uppercase',
                  fontFamily: 'monospace',
                  letterSpacing: '0.05em',
                  opacity: 0.5,
                  marginBottom: 8,
                }}
              >
                Health Status
              </div>

              {(() => {
                const status = getHealthStatus(selected.id, health);
                if (status) {
                  return <StatusBadge status={status} />;
                }
                return (
                  <span style={{ fontSize: 12, opacity: 0.4 }}>
                    No health endpoint for this component
                  </span>
                );
              })()}
            </div>

            <div
              style={{
                borderTop: '1px solid rgba(255,255,255,0.08)',
                paddingTop: 14,
                marginTop: 14,
              }}
            >
              <div
                style={{
                  fontSize: 10,
                  textTransform: 'uppercase',
                  fontFamily: 'monospace',
                  letterSpacing: '0.05em',
                  opacity: 0.5,
                  marginBottom: 8,
                }}
              >
                Node ID
              </div>
              <code style={{ fontSize: 12, opacity: 0.7 }}>{selected.id}</code>
            </div>

            {metrics && (
              <div
                style={{
                  borderTop: '1px solid rgba(255,255,255,0.08)',
                  paddingTop: 14,
                  marginTop: 14,
                }}
              >
                <div
                  style={{
                    fontSize: 10,
                    textTransform: 'uppercase',
                    fontFamily: 'monospace',
                    letterSpacing: '0.05em',
                    opacity: 0.5,
                    marginBottom: 8,
                  }}
                >
                  Metrics Feed
                </div>
                <span className="badge-success" style={{ fontSize: 11 }}>
                  Prometheus Connected
                </span>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
