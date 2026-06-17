import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import Card from '../components/Card';
import {
  useDashboardOverview,
  useDashboardHealth,
  useDashboardActivity,
  useStreamsThroughput,
  useMetricsSummary,
  useHighDriftEntities,
  useTopUnstableEntities
} from '../api/hooks';
import ChartContainer, {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Area,
  AreaChart
} from '../components/ChartContainer';
import {
  Atom,
  Shield,
  Users,
  TrendingDown,
  Activity,
  AlertTriangle,
  Radio,
  Radar,
  ExternalLink,
  Zap,
  ArrowUpRight,
  ArrowDownRight
} from 'lucide-react';

const getStabilityColor = (score) => {
  if (score >= 80) return 'var(--accent-green)';
  if (score >= 50) return 'var(--accent-amber)';
  return 'var(--accent-red)';
};

const getStabilityClass = (score) => {
  if (score >= 80) return 'stable';
  if (score >= 50) return 'warning';
  return 'critical';
};

const getActionBadgeClass = (action) => {
  if (!action) return 'badge badge-cyan';
  const a = action.toUpperCase();
  if (a.includes('CREAT') || a.includes('NEW') || a.includes('INGEST')) return 'badge badge-green';
  if (a.includes('DRIFT') || a.includes('ALERT') || a.includes('WARN')) return 'badge badge-amber';
  if (a.includes('DELETE') || a.includes('FAIL') || a.includes('ERROR')) return 'badge badge-red';
  if (a.includes('UPDATE') || a.includes('MODIF') || a.includes('CHANGE')) return 'badge badge-purple';
  if (a.includes('REPLAY') || a.includes('REPROCESS')) return 'badge badge-blue';
  return 'badge badge-cyan';
};

const formatTimestamp = (ts) => {
  if (!ts) return '--:--:--';
  try {
    const d = new Date(ts);
    return d.toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' });
  } catch {
    return '--:--:--';
  }
};

const Dashboard = () => {
  const navigate = useNavigate();
  const { data: overview, isLoading: loadOverview } = useDashboardOverview();
  const { data: health, isLoading: loadHealth } = useDashboardHealth();
  const { data: activity, isLoading: loadActivity } = useDashboardActivity();
  const { data: stream, isLoading: loadStream } = useStreamsThroughput();
  const { data: metricsSum, isLoading: loadMetrics } = useMetricsSummary();
  const { data: driftEntities, isLoading: loadDrift } = useHighDriftEntities();
  const { data: unstableEntities, isLoading: loadUnstable } = useTopUnstableEntities();

  const loading = loadOverview || loadHealth || loadActivity || loadStream || loadMetrics || loadDrift || loadUnstable;

  const avgStability = overview?.averageStability ?? 0;
  const totalEntities = overview?.totalEntities ?? 0;
  const avgEntropy = overview?.averageEntropy ?? 0;
  const instabilityIndex = (100 - avgStability);

  const healthStatus = health?.status || 'UNKNOWN';
  const healthDotClass = healthStatus === 'UP' ? 'stable' : healthStatus === 'DOWN' ? 'critical' : 'warning';

  const stabilityTrendData = useMemo(() => {
    const base = avgStability;
    return [
      { label: 'T-4', stability: Math.max(0, Math.min(100, base - 3.2)), time: '-4h' },
      { label: 'T-3', stability: Math.max(0, Math.min(100, base - 1.8)), time: '-3h' },
      { label: 'T-2', stability: Math.max(0, Math.min(100, base - 0.5)), time: '-2h' },
      { label: 'T-1', stability: Math.max(0, Math.min(100, base + 0.7)), time: '-1h' },
      { label: 'NOW', stability: Math.max(0, Math.min(100, base)), time: 'Now' }
    ];
  }, [avgStability]);

  const activityFeed = useMemo(() => {
    const items = activity || [];
    return items.slice(0, 15);
  }, [activity]);

  const driftList = useMemo(() => {
    return (driftEntities || []).slice(0, 12);
  }, [driftEntities]);

  const throughputEps = stream?.currentThroughputEps ?? 0;

  if (loading) {
    return (
      <div className="loading-state" style={{ minHeight: '60vh' }}>
        <Atom className="w-10 h-10 mb-4" style={{ color: 'var(--accent-cyan)', animation: 'spin 2s linear infinite' }} />
        <span style={{
          fontFamily: 'var(--font-mono)',
          fontSize: 'var(--text-xs)',
          letterSpacing: '0.12em',
          color: 'var(--text-muted)',
          textTransform: 'uppercase'
        }}>
          ESTABLISHING TELEMETRY LINK
        </span>
        <span style={{
          fontFamily: 'var(--font-mono)',
          fontSize: '10px',
          color: 'var(--text-dim)',
          marginTop: 'var(--space-2)'
        }}>
          Synchronizing entropy streams...
        </span>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>

      {/* ═══════════════════════════════════════════
          SECTION 1: STABILITY COMMAND STRIP
          ═══════════════════════════════════════════ */}
      <div className="kpi-strip">
        {/* Platform Health Status */}
        <div className="kpi-item" style={{ borderLeft: `2px solid ${healthStatus === 'UP' ? 'var(--accent-green)' : 'var(--accent-red)'}` }}>
          <div className="kpi-label" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
            <div className={`status-dot ${healthDotClass}`} />
            Platform Status
          </div>
          <div className="kpi-value" style={{ color: healthStatus === 'UP' ? 'var(--accent-green)' : 'var(--accent-red)' }}>
            {healthStatus}
          </div>
        </div>

        {/* Entity Count */}
        <div className="kpi-item">
          <div className="kpi-label" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
            <Users style={{ width: 12, height: 12, color: 'var(--text-muted)' }} />
            Tracked Entities
          </div>
          <div className="kpi-value" style={{ color: 'var(--accent-cyan)' }}>
            {totalEntities.toLocaleString()}
          </div>
        </div>

        {/* Average Stability — Prominent */}
        <div className="kpi-item" style={{
          borderLeft: `2px solid ${getStabilityColor(avgStability)}`,
          flex: '1.5'
        }}>
          <div className="kpi-label" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
            <Shield style={{ width: 12, height: 12, color: 'var(--text-muted)' }} />
            Average Stability
          </div>
          <div className="kpi-value" style={{
            fontSize: 'var(--text-3xl)',
            color: getStabilityColor(avgStability),
            letterSpacing: '-0.04em'
          }}>
            {avgStability.toFixed(1)}%
          </div>
        </div>

        {/* Instability Index */}
        <div className="kpi-item" style={{ borderLeft: '2px solid var(--accent-red)' }}>
          <div className="kpi-label" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
            <AlertTriangle style={{ width: 12, height: 12, color: 'var(--text-muted)' }} />
            Instability Index
          </div>
          <div className="kpi-value" style={{ color: instabilityIndex > 30 ? 'var(--accent-red)' : 'var(--accent-amber)' }}>
            {instabilityIndex.toFixed(1)}%
          </div>
        </div>

        {/* Entropy Growth Rate */}
        <div className="kpi-item">
          <div className="kpi-label" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
            <Atom style={{ width: 12, height: 12, color: 'var(--text-muted)' }} />
            Entropy Growth
          </div>
          <div className="kpi-value" style={{ color: 'var(--accent-amber)' }}>
            {avgEntropy.toFixed(4)}
          </div>
        </div>
      </div>

      {/* ═══════════════════════════════════════════
          SECTION 2: STABILITY TREND + DRIFT HOTSPOTS
          ═══════════════════════════════════════════ */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 'var(--space-4)' }}>

        {/* Left: Stability Trend Chart (2/3 width) */}
        <div className="glass-card">
          <div className="glass-card-header">
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
              <Activity style={{ width: 14, height: 14, color: 'var(--accent-cyan)' }} />
              <div>
                <h3 className="glass-card-title">Platform Stability Trend</h3>
                <p style={{ fontSize: '10px', color: 'var(--text-dim)', marginTop: 2 }}>
                  Rolling stability score · last 4 hours
                </p>
              </div>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
              <span className="badge badge-cyan" style={{ fontSize: 9 }}>LIVE</span>
              <span style={{ fontFamily: 'var(--font-mono)', fontSize: '10px', color: 'var(--text-muted)' }}>
                {throughputEps.toFixed(1)} eps
              </span>
            </div>
          </div>
          <div className="glass-card-body" style={{ padding: 'var(--space-3) var(--space-4)' }}>
            {stabilityTrendData.length > 0 ? (
              <div style={{ height: 240, width: '100%' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={stabilityTrendData} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
                    <defs>
                      <linearGradient id="stabilityGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="var(--accent-cyan)" stopOpacity={0.25} />
                        <stop offset="100%" stopColor="var(--accent-cyan)" stopOpacity={0.02} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" vertical={false} />
                    <XAxis
                      dataKey="time"
                      stroke="var(--text-dim)"
                      tickLine={false}
                      axisLine={false}
                      tick={{ fontSize: 10, fontFamily: 'var(--font-mono)' }}
                    />
                    <YAxis
                      stroke="var(--text-dim)"
                      tickLine={false}
                      axisLine={false}
                      domain={[0, 100]}
                      tick={{ fontSize: 10, fontFamily: 'var(--font-mono)' }}
                      tickFormatter={(v) => `${v}%`}
                    />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: 'var(--bg-elevated)',
                        border: '1px solid var(--border-emphasis)',
                        borderRadius: 'var(--radius-md)',
                        fontFamily: 'var(--font-mono)',
                        fontSize: 11
                      }}
                      formatter={(value) => [`${Number(value).toFixed(1)}%`, 'Stability']}
                      labelStyle={{ color: 'var(--text-muted)', fontSize: 10 }}
                    />
                    <Area
                      type="monotone"
                      dataKey="stability"
                      stroke="var(--accent-cyan)"
                      strokeWidth={2.5}
                      fill="url(#stabilityGradient)"
                      dot={{ r: 3, fill: 'var(--bg-elevated)', stroke: 'var(--accent-cyan)', strokeWidth: 2 }}
                      activeDot={{ r: 5, fill: 'var(--accent-cyan)', stroke: 'var(--bg-elevated)', strokeWidth: 2 }}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <div className="empty-state" style={{ padding: 'var(--space-8)' }}>
                <Activity className="empty-state-icon" />
                <span className="empty-state-text">No stability data available</span>
                <span className="empty-state-subtext">AWAITING TELEMETRY INGESTION</span>
              </div>
            )}
          </div>
        </div>

        {/* Right: Drift Hotspots (1/3 width) */}
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column' }}>
          <div className="glass-card-header">
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
              <Radar style={{ width: 14, height: 14, color: 'var(--accent-amber)' }} />
              <h3 className="glass-card-title">Drift Hotspots</h3>
            </div>
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: '10px', color: 'var(--text-dim)' }}>
              {driftList.length} flagged
            </span>
          </div>
          <div className="glass-card-body" style={{
            padding: 0,
            flex: 1,
            overflowY: 'auto',
            maxHeight: 290
          }}>
            {driftList.length > 0 ? (
              <div>
                {/* Dense header row */}
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '1fr 60px 70px',
                  gap: 'var(--space-2)',
                  padding: 'var(--space-2) var(--space-3)',
                  borderBottom: '1px solid var(--border-default)',
                  background: 'var(--bg-elevated)'
                }}>
                  <span style={{ fontSize: 9, fontWeight: 600, color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Entity</span>
                  <span style={{ fontSize: 9, fontWeight: 600, color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.06em', textAlign: 'right' }}>Stability</span>
                  <span style={{ fontSize: 9, fontWeight: 600, color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.06em', textAlign: 'right' }}>Drift</span>
                </div>
                {driftList.map((entity, idx) => {
                  const entityId = entity.entityId || entity.id || `entity-${idx}`;
                  const stability = entity.stabilityScore ?? entity.stability ?? 0;
                  const drift = entity.driftVelocity ?? entity.drift ?? entity.driftScore ?? 0;
                  return (
                    <div
                      key={entityId + idx}
                      style={{
                        display: 'grid',
                        gridTemplateColumns: '1fr 60px 70px',
                        gap: 'var(--space-2)',
                        padding: 'var(--space-2) var(--space-3)',
                        borderBottom: '1px solid var(--border-subtle)',
                        alignItems: 'center',
                        transition: 'background var(--transition-fast)',
                        cursor: 'pointer'
                      }}
                      onMouseEnter={(e) => { e.currentTarget.style.background = 'var(--glass-bg-hover)'; }}
                      onMouseLeave={(e) => { e.currentTarget.style.background = 'transparent'; }}
                      onClick={() => navigate(`/entities/${entityId}`)}
                    >
                      <span style={{
                        fontFamily: 'var(--font-mono)',
                        fontSize: 'var(--text-xs)',
                        color: 'var(--accent-cyan)',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                        display: 'flex',
                        alignItems: 'center',
                        gap: 'var(--space-1)'
                      }}>
                        <div className={`status-dot ${getStabilityClass(stability)}`} />
                        {entityId}
                      </span>
                      <span style={{
                        fontFamily: 'var(--font-mono)',
                        fontSize: 'var(--text-xs)',
                        color: getStabilityColor(stability),
                        textAlign: 'right',
                        fontWeight: 600
                      }}>
                        {stability.toFixed(1)}
                      </span>
                      <span style={{
                        fontFamily: 'var(--font-mono)',
                        fontSize: 'var(--text-xs)',
                        color: 'var(--accent-red)',
                        textAlign: 'right',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'flex-end',
                        gap: 2
                      }}>
                        <ArrowUpRight style={{ width: 10, height: 10 }} />
                        {drift.toFixed(3)}
                      </span>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="empty-state" style={{ padding: 'var(--space-6)' }}>
                <Shield className="empty-state-icon" />
                <span className="empty-state-text">No drift anomalies detected</span>
                <span className="empty-state-subtext">ALL ENTITIES WITHIN TOLERANCE</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ═══════════════════════════════════════════
          SECTION 3: LIVE ACTIVITY FEED
          ═══════════════════════════════════════════ */}
      <div className="glass-card">
        <div className="glass-card-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <Radio style={{ width: 14, height: 14, color: 'var(--accent-green)' }} />
            <div>
              <h3 className="glass-card-title">Live Activity Feed</h3>
              <p style={{ fontSize: '10px', color: 'var(--text-dim)', marginTop: 1 }}>
                Recent telemetry events and state transitions
              </p>
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: '10px', color: 'var(--text-dim)' }}>
              {activityFeed.length} events
            </span>
            <div className={`status-dot ${activityFeed.length > 0 ? 'stable' : 'warning'}`} />
          </div>
        </div>

        <div style={{ maxHeight: 420, overflowY: 'auto' }}>
          {/* Table header */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: '100px 1fr 100px 1fr',
            gap: 'var(--space-3)',
            padding: 'var(--space-2) var(--space-4)',
            borderBottom: '1px solid var(--border-default)',
            background: 'var(--bg-elevated)',
            position: 'sticky',
            top: 0,
            zIndex: 2
          }}>
            <span style={{ fontSize: 9, fontWeight: 600, color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Timestamp</span>
            <span style={{ fontSize: 9, fontWeight: 600, color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Entity</span>
            <span style={{ fontSize: 9, fontWeight: 600, color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Action</span>
            <span style={{ fontSize: 9, fontWeight: 600, color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Resource</span>
          </div>

          {activityFeed.length > 0 ? (
            activityFeed.map((act, index) => {
              const entityId = act.entityId || act.entity || '—';
              return (
                <div
                  key={index}
                  style={{
                    display: 'grid',
                    gridTemplateColumns: '100px 1fr 100px 1fr',
                    gap: 'var(--space-3)',
                    padding: 'var(--space-2) var(--space-4)',
                    borderBottom: '1px solid var(--border-subtle)',
                    alignItems: 'center',
                    transition: 'background var(--transition-fast)'
                  }}
                  onMouseEnter={(e) => { e.currentTarget.style.background = 'var(--glass-bg-hover)'; }}
                  onMouseLeave={(e) => { e.currentTarget.style.background = 'transparent'; }}
                >
                  <span style={{
                    fontFamily: 'var(--font-mono)',
                    fontSize: '10px',
                    color: 'var(--text-dim)',
                    letterSpacing: '0.02em'
                  }}>
                    {formatTimestamp(act.timestamp)}
                  </span>
                  <span
                    style={{
                      fontFamily: 'var(--font-mono)',
                      fontSize: 'var(--text-xs)',
                      color: 'var(--accent-cyan)',
                      cursor: 'pointer',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 'var(--space-1)'
                    }}
                    onClick={() => entityId !== '—' && navigate(`/entities/${entityId}`)}
                  >
                    {entityId}
                    {entityId !== '—' && <ExternalLink style={{ width: 9, height: 9, opacity: 0.5, flexShrink: 0 }} />}
                  </span>
                  <span className={getActionBadgeClass(act.action)}>
                    {act.action || 'EVENT'}
                  </span>
                  <span style={{
                    fontSize: 'var(--text-xs)',
                    color: 'var(--text-secondary)',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap'
                  }}>
                    {act.resource || act.details || '—'}
                  </span>
                </div>
              );
            })
          ) : (
            <div className="empty-state" style={{ padding: 'var(--space-8)' }}>
              <Radio className="empty-state-icon" />
              <span className="empty-state-text">No activity recorded</span>
              <span className="empty-state-subtext">AWAITING INCOMING TELEMETRY EVENTS</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
