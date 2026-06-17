import React, { useState, useMemo, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Card from '../components/Card';
import Button from '../components/Button';
import Table from '../components/Table';
import Modal from '../components/Modal';
import {
  useEntity,
  useEntityStabilityTimeline,
  useEntityBehaviorTimeline,
  useDriftExplanations,
  useReplayEntity,
  useEntityBehaviorDna,
  useEntityEntropyEvolution,
  useEntityVolatility,
  useDriftReport,
  useRebuildEntity
} from '../api/hooks';
import ChartContainer, {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
} from '../components/ChartContainer';
import {
  Atom,
  ArrowLeft,
  RefreshCcw,
  Database,
  TrendingUp,
  Gauge,
  Layers,
  Activity,
  Fingerprint,
  ShieldAlert,
  Zap,
  Clock,
  MapPin,
  Timer,
  FileText,
  AlertTriangle,
  ChevronRight,
} from 'lucide-react';

const TABS = [
  { key: 'overview', label: 'Overview', icon: Layers },
  { key: 'evolution', label: 'Entropy Evolution', icon: TrendingUp },
  { key: 'timeline', label: 'Event Timeline', icon: Clock },
  { key: 'drift', label: 'Drift Analysis', icon: ShieldAlert },
];

const getStabilityColor = (score) => {
  if (score == null) return 'text-text-muted';
  if (score > 70) return 'text-accent-green';
  if (score >= 40) return 'text-accent-amber';
  return 'text-accent-red';
};

const getStabilityBg = (score) => {
  if (score == null) return 'rgba(100,116,139,0.15)';
  if (score > 70) return 'rgba(34,197,94,0.1)';
  if (score >= 40) return 'rgba(245,158,11,0.1)';
  return 'rgba(239,68,68,0.1)';
};

const getStabilityBorder = (score) => {
  if (score == null) return 'var(--border-subtle)';
  if (score > 70) return 'var(--accent-green)';
  if (score >= 40) return 'var(--accent-amber)';
  return 'var(--accent-red)';
};

const getVolatilityBadge = (state) => {
  if (!state) return 'badge-gray';
  const s = state.toUpperCase();
  if (s === 'STABLE' || s === 'LOW') return 'badge-green';
  if (s === 'MODERATE' || s === 'MEDIUM') return 'badge-amber';
  return 'badge-red';
};

const formatTimestamp = (ts) => {
  if (!ts) return '—';
  const d = new Date(ts);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) + ' ' + d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false });
};

const EntityProfile = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('overview');
  const [isEventModalOpen, setIsEventModalOpen] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);

  const { data: entity, isLoading: loadEntity } = useEntity(id || '');
  const { data: stabilityTimeline, isLoading: loadStability } = useEntityStabilityTimeline(id || '');
  const { data: behaviorTimeline, isLoading: loadBehavior } = useEntityBehaviorTimeline(id || '');
  const { data: driftExplanations, isLoading: loadDrift } = useDriftExplanations(id || '');
  const { data: dna, isLoading: loadDna } = useEntityBehaviorDna(id || '');
  const { data: entropyEvolution, isLoading: loadEntropy } = useEntityEntropyEvolution(id || '');
  const { data: volatility, isLoading: loadVolatility } = useEntityVolatility(id || '');
  const { mutate: triggerReplay, isPending: isReplaying } = useReplayEntity();
  const { data: driftReport, isLoading: loadDriftReport } = useDriftReport(id || '');
  const { mutate: rebuildEntity, isPending: isRebuilding } = useRebuildEntity();

  const loading = loadEntity || loadStability || loadBehavior || loadDrift || loadDna || loadEntropy || loadVolatility || loadDriftReport;

  const latestStability = useMemo(() => {
    if (!stabilityTimeline || stabilityTimeline.length === 0) return null;
    return stabilityTimeline[0];
  }, [stabilityTimeline]);

  const stabilityScore = useMemo(() => {
    return latestStability?.behavioralStabilityScore ?? null;
  }, [latestStability]);

  const driftVelocity = useMemo(() => {
    return latestStability?.driftVelocity ?? null;
  }, [latestStability]);

  const entropyChartData = useMemo(() => {
    if (!entropyEvolution || entropyEvolution.length === 0) return [];
    return [...entropyEvolution].reverse();
  }, [entropyEvolution]);

  const handleForceReplay = useCallback(() => {
    if (entity?.id) triggerReplay(entity.id);
  }, [entity, triggerReplay]);

  const handleRowClick = useCallback((row) => {
    setSelectedEvent(row);
    setIsEventModalOpen(true);
  }, []);

  const handleCloseModal = useCallback(() => {
    setIsEventModalOpen(false);
    setSelectedEvent(null);
  }, []);

  const timelineColumns = useMemo(() => [
    {
      key: 'timestamp',
      header: 'Timestamp',
      render: (e) => (
        <span className="font-mono text-xs text-text-secondary">{formatTimestamp(e.timestamp)}</span>
      ),
    },
    {
      key: 'action',
      header: 'Action',
      render: (e) => <span className="badge badge-cyan">{e.action}</span>,
    },
    {
      key: 'resource',
      header: 'Resource',
      render: (e) => <span className="font-mono text-xs text-accent-purple">{e.resource}</span>,
    },
    {
      key: 'location',
      header: 'Location',
      render: (e) => <span className="text-xs text-text-secondary">{e.location || '—'}</span>,
    },
    {
      key: 'latency',
      header: 'Latency',
      render: (e) => (
        <span className="font-mono text-xs text-text-primary">{e.latency != null ? `${e.latency}ms` : '—'}</span>
      ),
    },
  ], []);

  /* ──────── LOADING STATE ──────── */
  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center h-96 text-text-secondary">
        <div className="relative">
          <Atom className="animate-spin w-10 h-10 text-accent-cyan opacity-40" />
          <div className="absolute inset-0 animate-ping">
            <Atom className="w-10 h-10 text-accent-cyan opacity-10" />
          </div>
        </div>
        <span className="font-mono text-xs tracking-widest uppercase mt-6 text-text-muted">
          RECONSTRUCTING BEHAVIORAL PROFILE...
        </span>
        <div className="flex gap-1 mt-3">
          {[0, 1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className="w-1.5 h-1.5 rounded-full bg-accent-cyan opacity-30"
              style={{ animation: `pulse 1.4s ease-in-out ${i * 0.15}s infinite` }}
            />
          ))}
        </div>
      </div>
    );
  }

  /* ──────── EMPTY / NOT FOUND STATE ──────── */
  if (!entity) {
    return (
      <div className="flex flex-col items-center justify-center h-96">
        <div className="w-16 h-16 rounded-xl bg-accent-red/10 border border-accent-red/30 flex items-center justify-center mb-4">
          <AlertTriangle className="w-8 h-8 text-accent-red opacity-60" />
        </div>
        <h2 className="text-lg font-semibold text-text-primary mb-1">Entity Not Found</h2>
        <p className="text-sm text-text-muted font-mono mb-6">No behavioral profile for <span className="text-accent-cyan">{id}</span></p>
        <Button variant="secondary" icon={ArrowLeft} onClick={() => navigate('/entities')}>
          Back to Entity Directory
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-5">
      {/* ══════════ HEADER ══════════ */}
      <div className="glass-card p-0 animate-scale-in overflow-hidden">
        {/* Top colored accent strip */}
        <div
          className="h-1"
          style={{ background: `linear-gradient(90deg, ${getStabilityBorder(stabilityScore)}, transparent)` }}
        />
        <div className="p-6">
          <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-5">
            {/* Left: Identity block */}
            <div className="flex items-start gap-4">
              {/* Score circle */}
              <div
                className="w-16 h-16 rounded-xl flex flex-col items-center justify-center shrink-0"
                style={{
                  background: getStabilityBg(stabilityScore),
                  border: `1.5px solid ${getStabilityBorder(stabilityScore)}`,
                }}
              >
                <span className={`text-xl font-bold font-mono leading-none ${getStabilityColor(stabilityScore)}`}>
                  {stabilityScore != null ? stabilityScore.toFixed(0) : '—'}
                </span>
                <span className="text-[9px] uppercase tracking-widest text-text-muted mt-0.5">score</span>
              </div>

              <div className="min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <h1 className="text-xl font-bold text-accent-cyan font-mono tracking-tight truncate">
                    {entity.id}
                  </h1>
                  {(dna?.entityType || entity.entityType) && (
                    <span className="badge badge-purple text-[10px]">
                      {dna?.entityType || entity.entityType}
                    </span>
                  )}
                </div>

                <div className="flex items-center gap-4 mt-2 flex-wrap">
                  {/* Volatility state */}
                  <div className="flex items-center gap-1.5">
                    <Activity className="w-3.5 h-3.5 text-text-muted" />
                    <span className={`badge ${getVolatilityBadge(volatility?.currentVolatility)} text-[10px]`}>
                      {volatility?.currentVolatility || 'UNKNOWN'}
                    </span>
                  </div>

                  {/* Drift velocity */}
                  <div className="flex items-center gap-1.5">
                    <Gauge className="w-3.5 h-3.5 text-text-muted" />
                    <span className="font-mono text-xs text-text-secondary">
                      drift: <span className="text-accent-amber">{driftVelocity != null ? driftVelocity.toFixed(4) : '—'}</span>
                    </span>
                  </div>

                  {/* Complexity */}
                  {dna?.complexityFactor != null && (
                    <div className="flex items-center gap-1.5">
                      <Fingerprint className="w-3.5 h-3.5 text-text-muted" />
                      <span className="font-mono text-xs text-text-secondary">
                        cx: <span className="text-accent-purple">{dna.complexityFactor.toFixed(2)}</span>
                      </span>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Right: Actions */}
            <div className="flex gap-2 shrink-0">
              <Button variant="secondary" icon={ArrowLeft} onClick={() => navigate('/entities')}>
                Entities
              </Button>
              <Button
                variant="outline"
                icon={Atom}
                onClick={() => id && rebuildEntity(id)}
                disabled={isRebuilding}
                loading={isRebuilding}
              >
                {isRebuilding ? 'Rebuilding…' : 'Rebuild State'}
              </Button>
              <Button
                variant="primary"
                icon={RefreshCcw}
                onClick={handleForceReplay}
                disabled={isReplaying}
                loading={isReplaying}
              >
                {isReplaying ? 'Replaying…' : 'Force Replay'}
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* ══════════ KPI STRIP ══════════ */}
      <div className="kpi-strip animate-slide-up">
        <div className="kpi-item">
          <span className="kpi-label">Stability Score</span>
          <span className={`kpi-value ${getStabilityColor(stabilityScore)}`}>
            {stabilityScore != null ? stabilityScore.toFixed(1) + '%' : '—'}
          </span>
        </div>
        <div className="kpi-item">
          <span className="kpi-label">Entropy Growth</span>
          <span className="kpi-value text-accent-purple">
            {entropyEvolution && entropyEvolution.length > 0
              ? (entropyEvolution[0].entropyGrowth ?? 0).toFixed(4)
              : '—'}
          </span>
        </div>
        <div className="kpi-item">
          <span className="kpi-label">Drift Velocity</span>
          <span className="kpi-value text-accent-amber">
            {driftVelocity != null ? driftVelocity.toFixed(4) : '—'}
          </span>
        </div>
        <div className="kpi-item">
          <span className="kpi-label">Complexity Factor</span>
          <span className="kpi-value text-accent-cyan">
            {dna?.complexityFactor != null ? dna.complexityFactor.toFixed(2) : '—'}
          </span>
        </div>
        <div className="kpi-item">
          <span className="kpi-label">Records Analyzed</span>
          <span className="kpi-value text-text-primary">
            {volatility?.recordsAnalyzed != null ? volatility.recordsAnalyzed.toLocaleString() : '—'}
          </span>
        </div>
      </div>

      {/* ══════════ TABS ══════════ */}
      <div className="tabs">
        {TABS.map((tab) => {
          const Icon = tab.icon;
          return (
            <button
              key={tab.key}
              className={`tab ${activeTab === tab.key ? 'active' : ''}`}
              onClick={() => setActiveTab(tab.key)}
            >
              <Icon className="w-3.5 h-3.5" />
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* ══════════ TAB CONTENT ══════════ */}

      {/* ─── OVERVIEW TAB ─── */}
      {activeTab === 'overview' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5 animate-slide-up">
          {/* Behavior DNA */}
          <Card title="Behavior DNA Fingerprint" description="Structural complexity and type classification">
            <div className="space-y-0">
              <div className="data-row">
                <div className="flex items-center gap-2">
                  <Fingerprint className="w-3.5 h-3.5 text-accent-purple" />
                  <span className="text-text-secondary text-xs">Complexity Factor</span>
                </div>
                <span className="font-mono text-sm font-semibold text-text-primary">
                  {dna?.complexityFactor != null ? dna.complexityFactor.toFixed(4) : '—'}
                </span>
              </div>
              <div className="data-row">
                <div className="flex items-center gap-2">
                  <Database className="w-3.5 h-3.5 text-accent-cyan" />
                  <span className="text-text-secondary text-xs">Entity Type</span>
                </div>
                <span className="badge badge-purple">{dna?.entityType || entity.entityType || '—'}</span>
              </div>
              <div className="data-row">
                <div className="flex items-center gap-2">
                  <Activity className="w-3.5 h-3.5 text-accent-green" />
                  <span className="text-text-secondary text-xs">Volatility State</span>
                </div>
                <span className={`badge ${getVolatilityBadge(volatility?.currentVolatility)}`}>
                  {volatility?.currentVolatility || 'UNKNOWN'}
                </span>
              </div>
              <div className="data-row">
                <div className="flex items-center gap-2">
                  <Layers className="w-3.5 h-3.5 text-accent-amber" />
                  <span className="text-text-secondary text-xs">Records Analyzed</span>
                </div>
                <span className="font-mono text-sm text-text-primary">
                  {volatility?.recordsAnalyzed != null ? volatility.recordsAnalyzed.toLocaleString() : '—'}
                </span>
              </div>
              {volatility?.volatilityScore != null && (
                <div className="data-row">
                  <div className="flex items-center gap-2">
                    <Zap className="w-3.5 h-3.5 text-accent-red" />
                    <span className="text-text-secondary text-xs">Volatility Score</span>
                  </div>
                  <span className="font-mono text-sm font-semibold text-accent-amber">
                    {volatility.volatilityScore.toFixed(4)}
                  </span>
                </div>
              )}
            </div>
          </Card>

          {/* Latest Stability Snapshot */}
          <Card title="Stability Snapshot" description="Latest behavioral stability measurement">
            {latestStability ? (
              <div className="space-y-0">
                <div className="data-row">
                  <div className="flex items-center gap-2">
                    <Gauge className="w-3.5 h-3.5 text-accent-green" />
                    <span className="text-text-secondary text-xs">Behavioral Stability</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span
                      className="w-2 h-2 rounded-full"
                      style={{ backgroundColor: getStabilityBorder(stabilityScore) }}
                    />
                    <span className={`text-lg font-bold font-mono ${getStabilityColor(stabilityScore)}`}>
                      {stabilityScore != null ? stabilityScore.toFixed(1) + '%' : '—'}
                    </span>
                  </div>
                </div>
                <div className="data-row">
                  <div className="flex items-center gap-2">
                    <TrendingUp className="w-3.5 h-3.5 text-accent-amber" />
                    <span className="text-text-secondary text-xs">Drift Velocity</span>
                  </div>
                  <span className="font-mono text-sm font-semibold text-accent-amber">
                    {driftVelocity != null ? driftVelocity.toFixed(6) : '—'}
                  </span>
                </div>
                {latestStability.entropyGrowth != null && (
                  <div className="data-row">
                    <div className="flex items-center gap-2">
                      <Atom className="w-3.5 h-3.5 text-accent-purple" />
                      <span className="text-text-secondary text-xs">Entropy Growth</span>
                    </div>
                    <span className="font-mono text-sm text-accent-purple">
                      {latestStability.entropyGrowth.toFixed(6)}
                    </span>
                  </div>
                )}
                <div className="data-row">
                  <div className="flex items-center gap-2">
                    <Clock className="w-3.5 h-3.5 text-text-muted" />
                    <span className="text-text-secondary text-xs">Measured At</span>
                  </div>
                  <span className="font-mono text-xs text-text-secondary">
                    {formatTimestamp(latestStability.timestamp)}
                  </span>
                </div>
              </div>
            ) : (
              <div className="empty-state">
                <div className="empty-state-icon">
                  <Gauge className="w-8 h-8 opacity-40" />
                </div>
                <h3 className="empty-state-title">No Stability Data</h3>
                <p className="empty-state-desc">No stability measurements have been recorded for this entity yet.</p>
              </div>
            )}
          </Card>
        </div>
      )}

      {/* ─── ENTROPY EVOLUTION TAB ─── */}
      {activeTab === 'evolution' && (
        <div className="animate-slide-up">
          {entropyChartData.length > 0 ? (
            <ChartContainer title="Entropy Evolution" description="Entropy growth trajectory over time">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={entropyChartData} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
                  <defs>
                    <linearGradient id="entropyGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="var(--accent-purple)" stopOpacity={0.4} />
                      <stop offset="50%" stopColor="var(--accent-purple)" stopOpacity={0.15} />
                      <stop offset="100%" stopColor="var(--accent-purple)" stopOpacity={0.02} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" vertical={false} />
                  <XAxis
                    dataKey="timestamp"
                    tickFormatter={(t) => {
                      const d = new Date(t);
                      return d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
                    }}
                    stroke="#475569"
                    tick={{ fontSize: 10 }}
                    axisLine={{ stroke: 'rgba(255,255,255,0.06)' }}
                  />
                  <YAxis
                    stroke="#475569"
                    tick={{ fontSize: 10 }}
                    axisLine={{ stroke: 'rgba(255,255,255,0.06)' }}
                    tickFormatter={(v) => v.toFixed(2)}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: 'var(--bg-elevated)',
                      border: '1px solid var(--border-emphasis)',
                      borderRadius: '8px',
                      fontSize: '12px',
                      fontFamily: 'monospace',
                    }}
                    labelFormatter={(label) => formatTimestamp(label)}
                    formatter={(value) => [value?.toFixed(6), 'Entropy Growth']}
                  />
                  <Area
                    type="monotone"
                    dataKey="entropyGrowth"
                    stroke="var(--accent-purple)"
                    fill="url(#entropyGradient)"
                    strokeWidth={2}
                    dot={false}
                    activeDot={{ r: 4, stroke: 'var(--accent-purple)', strokeWidth: 2, fill: 'var(--bg-elevated)' }}
                  />
                </AreaChart>
              </ResponsiveContainer>
            </ChartContainer>
          ) : (
            <Card>
              <div className="empty-state">
                <div className="empty-state-icon">
                  <TrendingUp className="w-8 h-8 opacity-40" />
                </div>
                <h3 className="empty-state-title">No Entropy Data</h3>
                <p className="empty-state-desc">
                  Entropy evolution data will appear after the system processes behavioral events for this entity.
                </p>
              </div>
            </Card>
          )}
        </div>
      )}

      {/* ─── EVENT TIMELINE TAB ─── */}
      {activeTab === 'timeline' && (
        <div className="animate-slide-up">
          <Card title="Behavioral Event Timeline" description="Chronological audit of all entity actions">
            <Table
              data={behaviorTimeline || []}
              columns={timelineColumns}
              onRowClick={handleRowClick}
              emptyMessage="No behavioral events recorded for this entity."
            />
          </Card>
        </div>
      )}

      {/* ─── DRIFT ANALYSIS TAB ─── */}
      {activeTab === 'drift' && (
        <div className="animate-slide-up space-y-5">
          {driftReport && (
            <Card title="Comprehensive Drift Report" description="Deep diagnostics of entity drift matrix state (administrative metadata)">
              <div className="space-y-0">
                <div className="data-row">
                  <span className="text-text-secondary text-xs">Entity Target ID</span>
                  <span className="font-mono text-xs text-accent-cyan">{driftReport.entityId || id}</span>
                </div>
                <div className="data-row">
                  <span className="text-text-secondary text-xs">Report Generated At</span>
                  <span className="font-mono text-xs text-text-primary">{driftReport.timestamp ? new Date(driftReport.timestamp).toLocaleString() : 'Just now'}</span>
                </div>
                <div className="data-row">
                  <span className="text-text-secondary text-xs">Drift Metrics Status</span>
                  <span className={`badge ${driftReport.driftDetected ? 'badge-red' : 'badge-green'}`}>
                    {driftReport.driftDetected ? 'DRIFT ACTIVE' : 'STEADY BASELINE'}
                  </span>
                </div>
                {driftReport.overallDriftIndex != null && (
                  <div className="data-row">
                    <span className="text-text-secondary text-xs">Overall Drift Index</span>
                    <span className="font-mono text-sm text-accent-amber font-semibold">{driftReport.overallDriftIndex.toFixed(6)}</span>
                  </div>
                )}
              </div>
            </Card>
          )}

          <Card title="Drift Explanations" description="AI-generated explanations of behavioral drift dimensions">
            {driftExplanations && driftExplanations.length > 0 ? (
              <div className="space-y-0">
                {driftExplanations.map((explanation, index) => (
                  <div
                    key={index}
                    className="p-4 border-b border-border-subtle last:border-b-0 hover:bg-glass-bg-hover transition-colors"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div className="min-w-0 flex-1">
                        {/* Timestamp + index */}
                        <div className="flex items-center gap-2 mb-2">
                          <span className="font-mono text-[10px] text-text-muted bg-glass-bg px-1.5 py-0.5 rounded">
                            #{index + 1}
                          </span>
                          <Clock className="w-3 h-3 text-text-muted" />
                          <span className="font-mono text-xs text-text-secondary">
                            {formatTimestamp(explanation.timestamp)}
                          </span>
                        </div>

                        {/* Explanation summary */}
                        <p className="text-sm text-text-primary leading-relaxed mb-3">
                          {explanation.explanationSummary || 'No explanation summary available.'}
                        </p>

                        {/* Dimension contributions */}
                        {explanation.dimensionContributions && (
                          <div className="flex flex-wrap gap-1.5">
                            {Object.entries(explanation.dimensionContributions).map(([dimension, contribution]) => (
                              <span
                                key={dimension}
                                className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-mono border"
                                style={{
                                  borderColor: 'var(--border-subtle)',
                                  background: 'var(--glass-bg)',
                                  color: 'var(--text-secondary)',
                                }}
                              >
                                <span className="text-accent-cyan">{dimension}</span>
                                <span className="text-text-muted">:</span>
                                <span className="text-accent-amber font-semibold">
                                  {typeof contribution === 'number' ? contribution.toFixed(3) : contribution}
                                </span>
                              </span>
                            ))}
                          </div>
                        )}

                        {/* Handle array-based contributions */}
                        {Array.isArray(explanation.dimensionContributions) && (
                          <div className="flex flex-wrap gap-1.5">
                            {explanation.dimensionContributions.map((item, i) => (
                              <span
                                key={i}
                                className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-mono border"
                                style={{
                                  borderColor: 'var(--border-subtle)',
                                  background: 'var(--glass-bg)',
                                  color: 'var(--text-secondary)',
                                }}
                              >
                                <span className="text-accent-cyan">{item.dimension || item.name || `dim-${i}`}</span>
                                <span className="text-text-muted">:</span>
                                <span className="text-accent-amber font-semibold">
                                  {typeof item.contribution === 'number' ? item.contribution.toFixed(3) : item.value || item.contribution}
                                </span>
                              </span>
                            ))}
                          </div>
                        )}
                      </div>

                      <ChevronRight className="w-4 h-4 text-text-muted shrink-0 mt-1" />
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="empty-state">
                <div className="empty-state-icon">
                  <ShieldAlert className="w-8 h-8 opacity-40" />
                </div>
                <h3 className="empty-state-title">No Drift Explanations</h3>
                <p className="empty-state-desc">
                  Drift explanations will be generated when the system detects significant behavioral deviations.
                </p>
              </div>
            )}
          </Card>
        </div>
      )}

      {/* ══════════ EVENT DETAIL MODAL ══════════ */}
      <Modal isOpen={isEventModalOpen} onClose={handleCloseModal} title="Event Investigation">
        {selectedEvent && (
          <div className="space-y-5">
            {/* Event identity */}
            <div className="flex items-center gap-3 pb-4 border-b border-border-subtle">
              <div className="w-10 h-10 rounded-lg bg-accent-cyan/10 border border-accent-cyan/30 flex items-center justify-center">
                <FileText className="w-5 h-5 text-accent-cyan" />
              </div>
              <div>
                <span className="badge badge-cyan text-xs">{selectedEvent.action}</span>
                <p className="text-xs text-text-muted mt-1 font-mono">{formatTimestamp(selectedEvent.timestamp)}</p>
              </div>
            </div>

            {/* Detail grid */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <span className="text-[10px] uppercase tracking-widest text-text-muted block mb-1.5">Action</span>
                <span className="badge badge-cyan">{selectedEvent.action}</span>
              </div>
              <div>
                <span className="text-[10px] uppercase tracking-widest text-text-muted block mb-1.5">Latency</span>
                <div className="flex items-center gap-1.5">
                  <Timer className="w-3.5 h-3.5 text-accent-amber" />
                  <span className="font-mono text-sm text-text-primary font-semibold">
                    {selectedEvent.latency != null ? `${selectedEvent.latency} ms` : '—'}
                  </span>
                </div>
              </div>
              <div>
                <span className="text-[10px] uppercase tracking-widest text-text-muted block mb-1.5">Location</span>
                <div className="flex items-center gap-1.5">
                  <MapPin className="w-3.5 h-3.5 text-accent-green" />
                  <span className="text-sm text-text-primary">{selectedEvent.location || '—'}</span>
                </div>
              </div>
              <div>
                <span className="text-[10px] uppercase tracking-widest text-text-muted block mb-1.5">Timestamp</span>
                <span className="font-mono text-xs text-text-secondary">{formatTimestamp(selectedEvent.timestamp)}</span>
              </div>
              <div className="col-span-2">
                <span className="text-[10px] uppercase tracking-widest text-text-muted block mb-1.5">Resource Path</span>
                <div className="p-2.5 rounded-md bg-glass-bg border border-border-subtle">
                  <span className="font-mono text-xs text-accent-purple break-all">{selectedEvent.resource || '—'}</span>
                </div>
              </div>
            </div>

            {/* Raw data dump */}
            {selectedEvent && (
              <div>
                <span className="text-[10px] uppercase tracking-widest text-text-muted block mb-1.5">Raw Event Data</span>
                <pre className="p-3 rounded-md bg-glass-bg border border-border-subtle text-xs font-mono text-text-secondary overflow-auto max-h-40">
                  {JSON.stringify(selectedEvent, null, 2)}
                </pre>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default EntityProfile;
