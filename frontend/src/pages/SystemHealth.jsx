import React, { useMemo } from 'react';
import Card from '../components/Card';
import {
  useSystemHealth,
  useSystemResources,
  useSystemDependencies,
  useSystemStatus
} from '../api/hooks';
import { ShieldCheck, Cpu, HardDrive, Server, Database, Activity, Radio, Boxes } from 'lucide-react';

const formatMB = (bytes) => {
  if (bytes == null || isNaN(bytes)) return '—';
  return `${(bytes / (1024 * 1024)).toFixed(0)} MB`;
};

const SystemHealth = () => {
  const { data: health, isLoading: loadHealth } = useSystemHealth();
  const { data: resources, isLoading: loadResources } = useSystemResources();
  const { data: dependencies, isLoading: loadDeps } = useSystemDependencies();
  const { data: status, isLoading: loadStatus } = useSystemStatus();

  const loading = loadHealth || loadResources || loadDeps || loadStatus;

  const depItems = useMemo(() => {
    if (loading) return [];
    return [
      {
        name: 'Application API Core',
        icon: Server,
        state: health?.status || 'UNKNOWN',
        detail: `Aggregated health endpoint — version ${status?.version || '—'}`
      },
      {
        name: 'PostgreSQL',
        icon: Database,
        state: dependencies?.postgresql || 'UNKNOWN',
        detail: 'Primary relational store — active connection pool'
      },
      {
        name: 'Redis',
        icon: Boxes,
        state: dependencies?.redis || 'UNKNOWN',
        detail: 'Volatility cache layer — in-memory key-value store'
      },
      {
        name: 'Kafka',
        icon: Radio,
        state: dependencies?.kafka || 'UNKNOWN',
        detail: 'Event stream broker — partition listeners'
      }
    ];
  }, [health, dependencies, status, loading]);

  if (loading) {
    return (
      <div className="loading-state">
        <Activity className="animate-spin w-8 h-8 text-accent-cyan mb-4" />
        <span className="font-mono text-xs tracking-wider uppercase">Resolving infrastructure probes…</span>
      </div>
    );
  }

  const heapAllocatedMB = resources?.memoryTotal != null
    ? (resources.memoryTotal / (1024 * 1024)).toFixed(0)
    : '—';

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
            <ShieldCheck className="w-6 h-6 text-accent-green" />
            Infrastructure Status
          </h1>
          <p className="text-text-secondary text-xs mt-1 font-mono">
            Platform resources, engine metadata, and dependency health checks
          </p>
        </div>
      </div>

      {/* KPI Strip */}
      <div className="kpi-strip">
        <div className="kpi-item">
          <span className="kpi-label">CPU Cores</span>
          <span className="kpi-value">
            <Cpu className="w-3.5 h-3.5 text-accent-cyan inline mr-1" />
            {resources?.cpuCount ?? '—'}
          </span>
        </div>
        <div className="kpi-item">
          <span className="kpi-label">Heap Allocated</span>
          <span className="kpi-value">
            <HardDrive className="w-3.5 h-3.5 text-accent-purple inline mr-1" />
            {heapAllocatedMB} MB
          </span>
        </div>
        <div className="kpi-item">
          <span className="kpi-label">Release Version</span>
          <span className="kpi-value font-mono">{status?.version ?? '—'}</span>
        </div>
        <div className="kpi-item">
          <span className="kpi-label">Operational State</span>
          <span className="kpi-value">
            <span className={`status-dot ${status?.operationalState === 'RUNNING' ? 'status-green' : 'status-red'}`} />
            {status?.operationalState ?? '—'}
          </span>
        </div>
      </div>

      {/* Two-column layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Left column — 1/3 */}
        <div className="lg:col-span-1 space-y-4">
          {/* Compute Resources */}
          <Card title="Compute Resources" delay={0.05}>
            <div className="space-y-3">
              <div className="data-row">
                <span className="data-row-label">CPU Count</span>
                <span className="data-row-value font-mono">{resources?.cpuCount ?? '—'} cores</span>
              </div>
              <div className="data-row">
                <span className="data-row-label">Memory Total</span>
                <span className="data-row-value font-mono">{formatMB(resources?.memoryTotal)}</span>
              </div>
              <div className="data-row" style={{ border: 'none' }}>
                <span className="data-row-label">Memory Max</span>
                <span className="data-row-value font-mono">{formatMB(resources?.memoryMax)}</span>
              </div>
            </div>
          </Card>

          {/* Engine Properties */}
          <Card title="Engine Properties" delay={0.1}>
            <div className="space-y-3">
              <div className="data-row">
                <span className="data-row-label">Version</span>
                <span className="data-row-value font-mono">{status?.version ?? '—'}</span>
              </div>
              <div className="data-row" style={{ border: 'none' }}>
                <span className="data-row-label">Operational State</span>
                <span className={`badge ${status?.operationalState === 'RUNNING' ? 'badge-green' : 'badge-red'}`}>
                  {status?.operationalState ?? '—'}
                </span>
              </div>
            </div>
          </Card>
        </div>

        {/* Right column — 2/3 */}
        <div className="lg:col-span-2">
          <Card
            title="Dependencies Health Matrix"
            description="Live connection checks from Spring Boot Actuator health aggregates"
            delay={0.15}
          >
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mt-2">
              {depItems.map((item, idx) => {
                const isUp = item.state === 'UP';
                const Icon = item.icon;
                return (
                  <div
                    key={idx}
                    className="p-4 bg-glass-bg border border-glass-border rounded hover:bg-glass-bg-hover transition-colors"
                  >
                    <div className="flex justify-between items-center mb-2">
                      <div className="flex items-center gap-2">
                        <Icon className={`w-4 h-4 ${isUp ? 'text-accent-green' : 'text-accent-red'}`} />
                        <span className="font-bold text-sm text-text-primary">{item.name}</span>
                      </div>
                      <span className={`badge ${isUp ? 'badge-green' : 'badge-red'}`}>{item.state}</span>
                    </div>
                    <p className="text-[10px] text-text-secondary font-mono leading-relaxed">{item.detail}</p>
                  </div>
                );
              })}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default SystemHealth;
