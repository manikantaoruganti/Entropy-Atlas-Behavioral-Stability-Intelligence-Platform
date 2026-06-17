import React, { useMemo } from 'react';
import Card from '../components/Card';
import { Clock, Zap, TrendingUp, TrendingDown, Activity, ShieldCheck, AlertTriangle, BarChart3 } from 'lucide-react';
import { useEntities } from '../api/hooks';
import { useQuery } from '@tanstack/react-query';
import axiosInstance from '../api/axiosInstance';
import { ResponsiveContainer, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from '../components/ChartContainer';

const ReferenceLine = ({ y, stroke, strokeDasharray, label, viewBox }) => {
  // Lightweight reference line rendered inside AreaChart via customized SVG
  return null;
};

const StabilityTimeline = () => {
  const { data: entitiesData, isLoading: entitiesLoading } = useEntities(0, 100);
  const entities = entitiesData?.content || [];

  const { data: timelineData, isLoading: timelineLoading, isError } = useQuery({
    queryKey: ['globalStabilityTimeline', entities.map(e => e.id).join(',')],
    queryFn: async () => {
      if (entities.length === 0) return [];

      const allSnapshots = await Promise.all(
        entities.map(async (entity) => {
          try {
            const res = await axiosInstance.get(`/api/v1/entities/${entity.id}/stability`);
            return Array.isArray(res.data) ? res.data : [];
          } catch {
            return [];
          }
        })
      );

      const grouped = {};
      allSnapshots.flat().forEach(snap => {
        if (!snap || snap.behavioralStabilityScore == null) return;
        const ts = snap.timestamp || snap.date || snap.createdAt;
        if (!ts) return;
        const dateStr = new Date(ts).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
        const sortKey = new Date(ts).toISOString().slice(0, 10);
        if (!grouped[sortKey]) {
          grouped[sortKey] = { total: 0, count: 0, min: 100, max: 0, label: dateStr };
        }
        const score = Number(snap.behavioralStabilityScore);
        grouped[sortKey].total += score;
        grouped[sortKey].count++;
        grouped[sortKey].min = Math.min(grouped[sortKey].min, score);
        grouped[sortKey].max = Math.max(grouped[sortKey].max, score);
      });

      return Object.entries(grouped)
        .map(([sortKey, meta]) => ({
          date: meta.label,
          sortKey,
          systemStability: Math.round((meta.total / meta.count) * 100) / 100,
          min: Math.round(meta.min * 100) / 100,
          max: Math.round(meta.max * 100) / 100,
          samples: meta.count,
          baseline: 80,
        }))
        .sort((a, b) => a.sortKey.localeCompare(b.sortKey));
    },
    enabled: entities.length > 0,
    staleTime: 30000,
    refetchInterval: 60000,
  });

  const stats = useMemo(() => {
    if (!timelineData || timelineData.length === 0) {
      return { current: null, avg: null, min: null, max: null, trend: null, totalSamples: 0, daysTracked: 0, belowBaseline: 0 };
    }
    const scores = timelineData.map(d => d.systemStability);
    const current = scores[scores.length - 1];
    const avg = Math.round((scores.reduce((a, b) => a + b, 0) / scores.length) * 100) / 100;
    const min = Math.min(...scores);
    const max = Math.max(...scores);
    const totalSamples = timelineData.reduce((acc, d) => acc + d.samples, 0);
    const belowBaseline = timelineData.filter(d => d.systemStability < 80).length;

    let trend = null;
    if (scores.length >= 2) {
      const recent = scores.slice(-3);
      const earlier = scores.slice(0, 3);
      const recentAvg = recent.reduce((a, b) => a + b, 0) / recent.length;
      const earlierAvg = earlier.reduce((a, b) => a + b, 0) / earlier.length;
      trend = Math.round((recentAvg - earlierAvg) * 100) / 100;
    }

    return { current, avg, min, max, trend, totalSamples, daysTracked: timelineData.length, belowBaseline };
  }, [timelineData]);

  const isLoading = entitiesLoading || timelineLoading;

  if (isLoading) {
    return (
      <div className="loading-state" style={{ minHeight: '60vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        <Zap className="animate-pulse" style={{ width: 32, height: 32, color: 'var(--accent-cyan)', marginBottom: 16 }} />
        <span className="font-mono" style={{ fontSize: '0.75rem', letterSpacing: '0.1em', color: 'var(--text-secondary)' }}>
          AGGREGATING MACRO HISTORICAL STABILITY DATA...
        </span>
        <span className="font-mono" style={{ fontSize: '0.65rem', color: 'var(--text-muted)', marginTop: 4 }}>
          Fetching snapshots from {entities.length || '...'} entities
        </span>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="empty-state" style={{ minHeight: '60vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        <AlertTriangle style={{ width: 32, height: 32, color: 'var(--accent-red)', marginBottom: 12 }} />
        <span style={{ color: 'var(--accent-red)', fontFamily: 'monospace', fontSize: '0.8rem' }}>
          TELEMETRY FAILURE — Historical baseline disrupted
        </span>
      </div>
    );
  }

  const hasData = timelineData && timelineData.length > 0;

  const CustomTooltip = ({ active, payload, label }) => {
    if (!active || !payload || !payload.length) return null;
    const d = payload[0]?.payload;
    if (!d) return null;
    return (
      <div style={{
        backgroundColor: 'var(--bg-elevated)',
        border: '1px solid var(--border-emphasis)',
        borderRadius: 8,
        padding: '12px 16px',
        fontFamily: 'monospace',
        fontSize: '0.75rem',
      }}>
        <div style={{ color: 'var(--text-primary)', fontWeight: 600, marginBottom: 6 }}>{d.date}</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '4px 16px' }}>
          <span style={{ color: 'var(--text-muted)' }}>Stability:</span>
          <span style={{ color: 'var(--accent-cyan)', fontWeight: 600 }}>{d.systemStability}%</span>
          <span style={{ color: 'var(--text-muted)' }}>Min:</span>
          <span style={{ color: 'var(--text-secondary)' }}>{d.min}%</span>
          <span style={{ color: 'var(--text-muted)' }}>Max:</span>
          <span style={{ color: 'var(--text-secondary)' }}>{d.max}%</span>
          <span style={{ color: 'var(--text-muted)' }}>Samples:</span>
          <span style={{ color: 'var(--text-secondary)' }}>{d.samples}</span>
          <span style={{ color: 'var(--text-muted)' }}>Baseline:</span>
          <span style={{ color: d.systemStability >= 80 ? 'var(--accent-green)' : 'var(--accent-red)' }}>
            {d.systemStability >= 80 ? '✓ ABOVE' : '✗ BELOW'}
          </span>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
        <div>
          <h1 className="text-2xl font-bold" style={{ color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: 8 }}>
            <Clock style={{ width: 24, height: 24, color: 'var(--accent-cyan)' }} />
            Global Stability Timeline
          </h1>
          <p style={{ color: 'var(--text-secondary)', marginTop: 4, fontSize: '0.85rem' }}>
            Macro-level system-wide behavioral stability over time — aggregated from {entities.length} tracked entities
          </p>
        </div>
        {hasData && (
          <div style={{
            display: 'flex', alignItems: 'center', gap: 8,
            padding: '6px 14px', borderRadius: 6,
            backgroundColor: stats.current >= 80 ? 'rgba(34,197,94,0.1)' : 'rgba(239,68,68,0.1)',
            border: `1px solid ${stats.current >= 80 ? 'rgba(34,197,94,0.25)' : 'rgba(239,68,68,0.25)'}`,
          }}>
            <span className="status-dot" style={{ backgroundColor: stats.current >= 80 ? 'var(--accent-green)' : 'var(--accent-red)' }} />
            <span style={{ fontFamily: 'monospace', fontSize: '0.75rem', color: stats.current >= 80 ? 'var(--accent-green)' : 'var(--accent-red)' }}>
              {stats.current >= 80 ? 'SYSTEM STABLE' : 'STABILITY DEGRADED'}
            </span>
          </div>
        )}
      </div>

      {/* KPI Strip */}
      {hasData && (
        <div className="kpi-strip">
          <div className="kpi-item">
            <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 4 }}>
              <ShieldCheck style={{ width: 14, height: 14, color: 'var(--accent-cyan)' }} />
              <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Current</span>
            </div>
            <div style={{ fontSize: '1.5rem', fontWeight: 700, color: stats.current >= 80 ? 'var(--accent-green)' : 'var(--accent-red)', fontFamily: 'monospace' }}>
              {stats.current}%
            </div>
          </div>

          <div className="kpi-item">
            <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 4 }}>
              <Activity style={{ width: 14, height: 14, color: 'var(--text-muted)' }} />
              <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Average</span>
            </div>
            <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-primary)', fontFamily: 'monospace' }}>
              {stats.avg}%
            </div>
          </div>

          <div className="kpi-item">
            <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 4 }}>
              <TrendingDown style={{ width: 14, height: 14, color: 'var(--accent-red)' }} />
              <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Min</span>
            </div>
            <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--accent-red)', fontFamily: 'monospace' }}>
              {stats.min}%
            </div>
          </div>

          <div className="kpi-item">
            <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 4 }}>
              <TrendingUp style={{ width: 14, height: 14, color: 'var(--accent-green)' }} />
              <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Max</span>
            </div>
            <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--accent-green)', fontFamily: 'monospace' }}>
              {stats.max}%
            </div>
          </div>

          <div className="kpi-item">
            <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 4 }}>
              {stats.trend !== null && stats.trend >= 0
                ? <TrendingUp style={{ width: 14, height: 14, color: 'var(--accent-green)' }} />
                : <TrendingDown style={{ width: 14, height: 14, color: 'var(--accent-red)' }} />
              }
              <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Trend</span>
            </div>
            <div style={{
              fontSize: '1.5rem', fontWeight: 700, fontFamily: 'monospace',
              color: stats.trend !== null ? (stats.trend >= 0 ? 'var(--accent-green)' : 'var(--accent-red)') : 'var(--text-muted)',
            }}>
              {stats.trend !== null ? `${stats.trend > 0 ? '+' : ''}${stats.trend}` : '—'}
            </div>
          </div>

          <div className="kpi-item">
            <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 4 }}>
              <BarChart3 style={{ width: 14, height: 14, color: 'var(--text-muted)' }} />
              <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Samples</span>
            </div>
            <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-primary)', fontFamily: 'monospace' }}>
              {stats.totalSamples.toLocaleString()}
            </div>
          </div>
        </div>
      )}

      {/* Secondary stats row */}
      {hasData && (
        <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
          <div className="glass-card" style={{ padding: '10px 16px', display: 'flex', alignItems: 'center', gap: 8, flex: 1, minWidth: 180 }}>
            <Clock style={{ width: 14, height: 14, color: 'var(--accent-cyan)' }} />
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Days tracked:</span>
            <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-primary)', fontFamily: 'monospace' }}>{stats.daysTracked}</span>
          </div>
          <div className="glass-card" style={{ padding: '10px 16px', display: 'flex', alignItems: 'center', gap: 8, flex: 1, minWidth: 180 }}>
            <Activity style={{ width: 14, height: 14, color: 'var(--text-muted)' }} />
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Entities contributing:</span>
            <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-primary)', fontFamily: 'monospace' }}>{entities.length}</span>
          </div>
          <div className="glass-card" style={{ padding: '10px 16px', display: 'flex', alignItems: 'center', gap: 8, flex: 1, minWidth: 180 }}>
            <AlertTriangle style={{ width: 14, height: 14, color: stats.belowBaseline > 0 ? 'var(--accent-red)' : 'var(--accent-green)' }} />
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Days below baseline:</span>
            <span style={{
              fontSize: '0.8rem', fontWeight: 600, fontFamily: 'monospace',
              color: stats.belowBaseline > 0 ? 'var(--accent-red)' : 'var(--accent-green)',
            }}>
              {stats.belowBaseline}
            </span>
          </div>
        </div>
      )}

      {/* Main Chart */}
      <Card title="System-Wide Stability Trend" description="Averaged behavioral stability score across all monitored entities, per date">
        <div style={{ height: 500, width: '100%' }}>
          {hasData ? (
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={timelineData} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="gradStability" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="var(--accent-cyan)" stopOpacity={0.35} />
                    <stop offset="50%" stopColor="var(--accent-cyan)" stopOpacity={0.1} />
                    <stop offset="95%" stopColor="var(--accent-cyan)" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" vertical={false} />
                <XAxis
                  dataKey="date"
                  stroke="#64748b"
                  tick={{ fill: '#64748b', fontSize: 11, fontFamily: 'monospace' }}
                  tickLine={false}
                  axisLine={false}
                  interval="preserveStartEnd"
                />
                <YAxis
                  stroke="#64748b"
                  domain={[0, 100]}
                  tick={{ fill: '#64748b', fontSize: 11, fontFamily: 'monospace' }}
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={v => `${v}%`}
                />
                <Tooltip content={<CustomTooltip />} />
                <Legend
                  wrapperStyle={{ fontSize: '0.7rem', fontFamily: 'monospace', color: 'var(--text-muted)' }}
                />
                {/* Baseline rendered as a flat Area at y=80 */}
                <Area
                  type="stepAfter"
                  dataKey="baseline"
                  name="Target Baseline (80%)"
                  stroke="var(--accent-green)"
                  fill="none"
                  strokeWidth={1.5}
                  strokeDasharray="6 4"
                  dot={false}
                  activeDot={false}
                  isAnimationActive={false}
                />
                {/* Primary stability area */}
                <Area
                  type="monotone"
                  dataKey="systemStability"
                  name="Global Stability Score"
                  stroke="var(--accent-cyan)"
                  fillOpacity={1}
                  fill="url(#gradStability)"
                  strokeWidth={2.5}
                  dot={{ r: 3, fill: 'var(--accent-cyan)', stroke: 'var(--bg-primary)', strokeWidth: 2 }}
                  activeDot={{ r: 5, fill: 'var(--accent-cyan)', stroke: 'var(--bg-primary)', strokeWidth: 2 }}
                />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty-state" style={{
              display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
              height: '100%', gap: 12,
            }}>
              <Activity style={{ width: 40, height: 40, color: 'var(--text-muted)', opacity: 0.4 }} />
              <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem', textAlign: 'center', maxWidth: 400 }}>
                No historical stability snapshots found. Ingest behavioral events to generate system-wide timeline trends.
              </span>
              <span style={{ color: 'var(--text-muted)', fontSize: '0.7rem', fontFamily: 'monospace', opacity: 0.6 }}>
                {entities.length} entities registered · 0 snapshots collected
              </span>
            </div>
          )}
        </div>
      </Card>
    </div>
  );
};

export default StabilityTimeline;
