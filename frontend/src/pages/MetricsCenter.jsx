import React, { useMemo } from 'react';
import Card from '../components/Card';
import {
  useMetricsJvm,
  useMetricsKafka,
  useMetricsDatabase,
  useMetricsCache
} from '../api/hooks';
import { BarChart2, Cpu, Database, HardDrive, Server, Activity, Layers } from 'lucide-react';

const formatMB = (bytes) => {
  if (bytes == null || isNaN(bytes)) return '—';
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

const formatMBShort = (bytes) => {
  if (bytes == null || isNaN(bytes)) return '—';
  return `${(bytes / (1024 * 1024)).toFixed(0)}MB`;
};

const MetricsCenter = () => {
  const { data: jvm, isLoading: loadJvm } = useMetricsJvm();
  const { data: kafka, isLoading: loadKafka } = useMetricsKafka();
  const { data: database, isLoading: loadDb } = useMetricsDatabase();
  const { data: cache, isLoading: loadCache } = useMetricsCache();

  const loading = loadJvm || loadKafka || loadDb || loadCache;

  const kpis = useMemo(() => [
    {
      label: 'JVM Heap Allocated',
      value: jvm ? formatMBShort(jvm.totalMemoryBytes) : '—',
      icon: HardDrive,
      accent: 'cyan'
    },
    {
      label: 'DB Pool Active',
      value: database?.poolConnectionsActive ?? '—',
      icon: Database,
      accent: 'purple'
    },
    {
      label: 'Cache Hit Rate',
      value: cache?.hitRate != null ? `${cache.hitRate}%` : '—',
      icon: Layers,
      accent: 'green'
    },
    {
      label: 'CPU Cores',
      value: jvm?.availableProcessors ?? '—',
      icon: Cpu,
      accent: 'amber'
    }
  ], [jvm, database, cache]);

  const clusterStatusBadge = useMemo(() => {
    const status = kafka?.clusterStatus;
    if (!status) return 'badge badge-amber';
    const s = status.toLowerCase();
    if (s === 'connected' || s === 'up' || s === 'online' || s === 'healthy') return 'badge badge-green';
    if (s === 'degraded' || s === 'warning') return 'badge badge-amber';
    return 'badge badge-red';
  }, [kafka?.clusterStatus]);

  const dbStatusBadge = useMemo(() => {
    const status = database?.status;
    if (!status) return 'badge badge-amber';
    const s = status.toLowerCase();
    if (s === 'up' || s === 'healthy' || s === 'connected' || s === 'ok') return 'badge badge-green';
    if (s === 'degraded' || s === 'warning') return 'badge badge-amber';
    return 'badge badge-red';
  }, [database?.status]);

  const underRepColor = useMemo(() => {
    const val = kafka?.underReplicatedPartitions;
    if (val == null) return 'data-row-value';
    return val > 0 ? 'data-row-value' : 'data-row-value';
  }, [kafka?.underReplicatedPartitions]);

  if (loading) {
    return (
      <div className="loading-state">
        <div className="loading-spinner" />
        <span className="loading-text">Resolving runtime metric registries…</span>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
      {/* Page Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h1 style={{
            fontSize: 'var(--text-2xl)',
            fontWeight: 700,
            color: 'var(--text-primary)',
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--space-2)',
            letterSpacing: '-0.02em'
          }}>
            <BarChart2 style={{ width: 20, height: 20, color: 'var(--accent-cyan)' }} />
            Platform Observability
          </h1>
          <p style={{
            fontSize: 'var(--text-xs)',
            color: 'var(--text-muted)',
            marginTop: 'var(--space-1)',
            fontFamily: 'var(--font-mono)',
            letterSpacing: '0.02em'
          }}>
            JVM runtime · Kafka cluster · Database pool · Cache subsystem
          </p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
          <div className="status-dot stable" />
          <span style={{ fontSize: '10px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            All systems nominal
          </span>
        </div>
      </div>

      {/* KPI Strip */}
      <div className="kpi-strip">
        {kpis.map((kpi) => {
          const Icon = kpi.icon;
          return (
            <div className="kpi-item" key={kpi.label}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 'var(--space-2)' }}>
                <span className="kpi-label">{kpi.label}</span>
                <div className="metric-icon" style={{
                  width: 24,
                  height: 24,
                  background: `var(--accent-${kpi.accent}-dim)`,
                  color: `var(--accent-${kpi.accent})`
                }}>
                  <Icon style={{ width: 12, height: 12 }} />
                </div>
              </div>
              <span className="kpi-value">{kpi.value}</span>
            </div>
          );
        })}
      </div>

      {/* Three-Column Detail Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 'var(--space-4)' }}>

        {/* Column 1: JVM Runtime */}
        <Card title="JVM Runtime" description="HotSpot memory allocation">
          <div>
            <div className="data-row">
              <span className="data-row-label">Max Memory</span>
              <span className="data-row-value">{formatMB(jvm?.maxMemoryBytes)}</span>
            </div>
            <div className="data-row">
              <span className="data-row-label">Free Memory</span>
              <span className="data-row-value">{formatMB(jvm?.freeMemoryBytes)}</span>
            </div>
            <div className="data-row">
              <span className="data-row-label">Total Allocated</span>
              <span className="data-row-value">{formatMB(jvm?.totalMemoryBytes)}</span>
            </div>
            <div className="data-row">
              <span className="data-row-label">Available Processors</span>
              <span className="data-row-value">{jvm?.availableProcessors ?? '—'} cores</span>
            </div>
            <div className="data-row">
              <span className="data-row-label">Heap Utilization</span>
              <span className="data-row-value">
                {jvm?.totalMemoryBytes && jvm?.freeMemoryBytes
                  ? `${(((jvm.totalMemoryBytes - jvm.freeMemoryBytes) / jvm.totalMemoryBytes) * 100).toFixed(1)}%`
                  : '—'}
              </span>
            </div>
          </div>
        </Card>

        {/* Column 2: Kafka Broker */}
        <Card title="Kafka Cluster" description="Broker connectivity and replication">
          <div>
            <div className="data-row">
              <span className="data-row-label">Cluster Status</span>
              <span className={clusterStatusBadge}>
                {kafka?.clusterStatus ?? '—'}
              </span>
            </div>
            <div className="data-row">
              <span className="data-row-label">Active Brokers</span>
              <span className="data-row-value">{kafka?.activeBrokers ?? '—'}</span>
            </div>
            <div className="data-row">
              <span className="data-row-label">Under-Replicated Partitions</span>
              <span className="data-row-value" style={{
                color: kafka?.underReplicatedPartitions > 0 ? 'var(--accent-red)' : 'var(--text-primary)',
                fontWeight: kafka?.underReplicatedPartitions > 0 ? 700 : 500
              }}>
                {kafka?.underReplicatedPartitions ?? '—'}
              </span>
            </div>
            <div className="data-row">
              <span className="data-row-label">Replication Health</span>
              <span className={kafka?.underReplicatedPartitions === 0 ? 'badge badge-green' : 'badge badge-red'}>
                {kafka?.underReplicatedPartitions === 0 ? 'HEALTHY' : 'DEGRADED'}
              </span>
            </div>
          </div>
        </Card>

        {/* Column 3: Database */}
        <Card title="Database Pool" description="Connection pool and driver diagnostics">
          <div>
            <div className="data-row">
              <span className="data-row-label">Driver</span>
              <span className="data-row-value" style={{ fontSize: 'var(--text-xs)' }}>
                {database?.driverName ?? '—'}
              </span>
            </div>
            <div className="data-row">
              <span className="data-row-label">Active Connections</span>
              <span className="data-row-value">{database?.poolConnectionsActive ?? '—'}</span>
            </div>
            <div className="data-row">
              <span className="data-row-label">Connection Health</span>
              <span className={dbStatusBadge}>
                {database?.status ?? '—'}
              </span>
            </div>
            <div className="data-row">
              <span className="data-row-label">Pool Utilization</span>
              <span className="data-row-value">
                {database?.poolConnectionsActive != null
                  ? `${database.poolConnectionsActive} active`
                  : '—'}
              </span>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default MetricsCenter;
