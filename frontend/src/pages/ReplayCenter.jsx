import React, { useState, useMemo, useCallback } from 'react';
import Card from '../components/Card';
import Table from '../components/Table';
import Button from '../components/Button';
import Input from '../components/Input';
import {
  useReplayStatistics,
  useReplayHistory,
  useReplayConsistency,
  useReplayEntity,
  useReplayReports
} from '../api/hooks';
import {
  RefreshCcw,
  Play,
  ShieldCheck,
  AlertTriangle,
  CheckCircle2,
  XCircle,
  Clock,
  Hash,
  FileSearch,
  Loader2
} from 'lucide-react';

const ReplayCenter = () => {
  const [targetEntityId, setTargetEntityId] = useState('');
  const { data: stats, isLoading: loadStats } = useReplayStatistics();
  const { data: history, isLoading: loadHistory } = useReplayHistory();
  const { data: consistency, isLoading: loadConsistency } = useReplayConsistency();
  const { mutate: runReplay, isPending: isReplaying } = useReplayEntity();
  const { data: replayReports, isLoading: loadReports } = useReplayReports(targetEntityId);

  const loading = loadStats || loadHistory || loadConsistency;

  const handleReplayTrigger = useCallback(() => {
    const trimmed = targetEntityId.trim();
    if (!trimmed) return;
    runReplay(trimmed);
    setTargetEntityId('');
  }, [targetEntityId, runReplay]);

  const handleKeyDown = useCallback((e) => {
    if (e.key === 'Enter') {
      handleReplayTrigger();
    }
  }, [handleReplayTrigger]);

  const historyRows = useMemo(() => {
    if (!history) return [];
    return Array.isArray(history) ? history : [];
  }, [history]);

  const columns = useMemo(() => [
    {
      key: 'id',
      header: 'Report ID',
      render: (row) => (
        <span className="font-mono text-xs" style={{ color: 'var(--text-muted)', letterSpacing: '-0.02em' }}>
          {row.id ?? '—'}
        </span>
      )
    },
    {
      key: 'entityId',
      header: 'Entity ID',
      render: (row) => (
        <span
          className="font-mono text-xs"
          style={{ color: 'var(--accent-cyan)', cursor: 'pointer' }}
          title={`Investigate ${row.entityId}`}
          onClick={(e) => {
            e.stopPropagation();
            setTargetEntityId(row.entityId || '');
          }}
        >
          {row.entityId ?? '—'}
        </span>
      )
    },
    {
      key: 'reportGeneratedAt',
      header: 'Generated At',
      render: (row) => {
        if (!row.reportGeneratedAt) return <span className="text-text-muted">—</span>;
        const d = new Date(row.reportGeneratedAt);
        return (
          <span className="font-mono text-xs" style={{ color: 'var(--text-secondary)' }}>
            {d.toLocaleDateString('en-US', { month: 'short', day: '2-digit', year: 'numeric' })}{' '}
            <span style={{ color: 'var(--text-muted)' }}>
              {d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false })}
            </span>
          </span>
        );
      }
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => {
        const status = row.status || 'UNKNOWN';
        const isCompleted = status === 'COMPLETED';
        const isFailed = status === 'FAILED';
        return (
          <span className={`badge ${isCompleted ? 'badge-green' : isFailed ? 'badge-red' : 'badge-amber'}`}>
            <span
              className="status-dot"
              style={{
                width: '5px',
                height: '5px',
                borderRadius: '50%',
                background: isCompleted ? 'var(--accent-green)' : isFailed ? 'var(--accent-red)' : 'var(--accent-amber)',
                display: 'inline-block'
              }}
            />
            {status}
          </span>
        );
      }
    },
    {
      key: 'comparisonSummary',
      header: 'Audit Summary',
      render: (row) => (
        <span className="text-xs" style={{ color: 'var(--text-secondary)', maxWidth: '280px', display: 'inline-block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {row.comparisonSummary || '—'}
        </span>
      )
    }
  ], []);

  const consistencyVerified = consistency?.verified ?? false;
  const discrepancyCount = consistency?.discrepanciesObserved ?? 0;

  if (loading) {
    return (
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
            <RefreshCcw className="w-6 h-6 text-accent-purple" />
            Replay Engine
          </h1>
          <p className="text-text-secondary text-xs mt-1">
            Forensic behavioral reconstruction and write-consistency verification
          </p>
        </div>

        {/* Loading state */}
        <div className="flex flex-col items-center justify-center" style={{ minHeight: '400px' }}>
          <Loader2 className="animate-spin w-8 h-8 text-accent-cyan mb-4" />
          <span className="font-mono text-xs tracking-wider text-text-muted" style={{ letterSpacing: '0.12em' }}>
            INITIALIZING REPLAY TELEMETRY...
          </span>
          <span className="text-xs text-text-muted mt-2" style={{ opacity: 0.5 }}>
            Harvesting consistency state and historical replay manifests
          </span>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* ── Header ── */}
      <div>
        <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
          <RefreshCcw className="w-6 h-6 text-accent-purple" />
          Replay Engine
        </h1>
        <p className="text-text-secondary text-xs mt-1">
          Forensic behavioral reconstruction &mdash; rebuild entity timelines, verify write consistency, and audit state divergence across replay epochs.
        </p>
      </div>

      {/* ── KPI Strip ── */}
      <div className="kpi-strip">
        <div className="kpi-item">
          <div className="kpi-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Hash className="w-3 h-3" style={{ color: 'var(--accent-cyan)' }} />
            Total Replays
          </div>
          <div className="kpi-value" style={{ color: 'var(--text-primary)' }}>
            {stats?.totalReplays ?? 0}
          </div>
        </div>

        <div className="kpi-item">
          <div className="kpi-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <CheckCircle2 className="w-3 h-3" style={{ color: 'var(--accent-green)' }} />
            Completed
          </div>
          <div className="kpi-value" style={{ color: 'var(--accent-green)' }}>
            {stats?.completedCount ?? 0}
          </div>
        </div>

        <div className="kpi-item">
          <div className="kpi-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <XCircle className="w-3 h-3" style={{ color: 'var(--accent-red)' }} />
            Failed
          </div>
          <div className="kpi-value" style={{ color: 'var(--accent-red)' }}>
            {stats?.failedCount ?? 0}
          </div>
        </div>

        <div className="kpi-item">
          <div className="kpi-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <ShieldCheck className="w-3 h-3" style={{ color: consistencyVerified ? 'var(--accent-green)' : 'var(--accent-red)' }} />
            Consistency
          </div>
          <div className="kpi-value">
            <span className={`badge ${consistencyVerified ? 'badge-green' : 'badge-red'}`} style={{ fontSize: '11px' }}>
              {consistencyVerified ? 'VERIFIED' : 'UNVERIFIED'}
            </span>
          </div>
        </div>
      </div>

      {/* ── Two-Column Layout ── */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* ── Left Column: Investigation Panel (1/3) ── */}
        <div className="lg:col-span-1 space-y-4">

          {/* Investigation Panel */}
          <div className="investigation-panel">
            <div className="investigation-header">
              <div className="investigation-title">
                <FileSearch className="w-4 h-4" style={{ color: 'var(--accent-purple)' }} />
                Forensic Replay
              </div>
              <span className="badge badge-purple" style={{ fontSize: '9px' }}>INVESTIGATION</span>
            </div>
            <div className="investigation-body">
              <div className="space-y-4">
                <Input
                  label="Target Entity ID"
                  value={targetEntityId}
                  onChange={(e) => setTargetEntityId(e.target.value)}
                  onKeyDown={handleKeyDown}
                  placeholder="e.g. user-9f3a-x7b2"
                  className="font-mono"
                />

                <Button
                  variant="primary"
                  className="w-full justify-center"
                  icon={isReplaying ? Loader2 : Play}
                  onClick={handleReplayTrigger}
                  disabled={isReplaying || !targetEntityId.trim()}
                  loading={isReplaying}
                >
                  {isReplaying ? 'Rebuilding Timeline...' : 'Execute Replay'}
                </Button>

                <div style={{ padding: '8px 0 0', borderTop: '1px solid var(--border-subtle)' }}>
                  <div className="text-xs text-text-muted" style={{ lineHeight: 1.6 }}>
                    Reconstructs the full behavioral timeline for a single entity. Replays all ingested events through the entropy pipeline and compares results against stored state.
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Write Consistency Checker */}
          <div className="investigation-panel">
            <div className="investigation-header">
              <div className="investigation-title">
                <ShieldCheck className="w-4 h-4" style={{ color: consistencyVerified ? 'var(--accent-green)' : 'var(--accent-red)' }} />
                Write Consistency
              </div>
              <span
                className="status-dot"
                style={{
                  width: '7px',
                  height: '7px',
                  borderRadius: '50%',
                  background: consistencyVerified ? 'var(--accent-green)' : 'var(--accent-red)',
                  boxShadow: consistencyVerified
                    ? '0 0 8px var(--accent-green-glow)'
                    : '0 0 8px var(--accent-red-glow)',
                  display: 'inline-block'
                }}
              />
            </div>
            <div className="investigation-body">
              <div className="data-row">
                <span className="data-row-label">Integrity Status</span>
                <span className={`badge ${consistencyVerified ? 'badge-green' : 'badge-red'}`}>
                  {consistencyVerified ? (
                    <><CheckCircle2 className="w-3 h-3" /> VERIFIED</>
                  ) : (
                    <><AlertTriangle className="w-3 h-3" /> FAILED</>
                  )}
                </span>
              </div>
              <div className="data-row">
                <span className="data-row-label">State Discrepancies</span>
                <span className="data-row-value" style={{ color: discrepancyCount > 0 ? 'var(--accent-red)' : 'var(--accent-green)' }}>
                  {discrepancyCount}
                </span>
              </div>
              <div className="data-row">
                <span className="data-row-label">Last Check</span>
                <span className="data-row-value" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                  <Clock className="w-3 h-3" style={{ display: 'inline', verticalAlign: 'middle', marginRight: '4px' }} />
                  {consistency?.lastCheckedAt
                    ? new Date(consistency.lastCheckedAt).toLocaleString()
                    : 'N/A'
                  }
                </span>
              </div>
            </div>
          </div>

          {/* Quick Stats */}
          <Card title="Replay Statistics" description="Aggregate replay pipeline metrics">
            <div className="data-row">
              <span className="data-row-label">Total Processed</span>
              <span className="data-row-value">{stats?.totalReplays ?? 0}</span>
            </div>
            <div className="data-row">
              <span className="data-row-label">Success Rate</span>
              <span className="data-row-value" style={{ color: 'var(--accent-green)' }}>
                {stats?.totalReplays
                  ? `${(((stats?.completedCount || 0) / stats.totalReplays) * 100).toFixed(1)}%`
                  : '—'
                }
              </span>
            </div>
            <div className="data-row">
              <span className="data-row-label">Failure Rate</span>
              <span className="data-row-value" style={{ color: (stats?.failedCount || 0) > 0 ? 'var(--accent-red)' : 'var(--text-muted)' }}>
                {stats?.totalReplays
                  ? `${(((stats?.failedCount || 0) / stats.totalReplays) * 100).toFixed(1)}%`
                  : '—'
                }
              </span>
            </div>
          </Card>

          {/* Dynamic Entity Replay Reports */}
          {targetEntityId && (
            <Card title={`Reports: ${targetEntityId}`} description="Replay runs for selected entity">
              {loadReports ? (
                <div className="py-4 text-center font-mono text-[10px] text-text-muted">LOADING REPORTS...</div>
              ) : replayReports && replayReports.length > 0 ? (
                <div className="space-y-2 max-h-48 overflow-y-auto">
                  {replayReports.map((rep) => (
                    <div key={rep.id} className="p-2 rounded border border-border-subtle bg-glass-bg">
                      <div className="flex justify-between text-[10px] font-mono text-text-secondary">
                        <span>Report #{rep.id?.slice(0, 8) || '—'}</span>
                        <span className={rep.status === 'COMPLETED' ? 'text-accent-green' : 'text-accent-red'}>
                          {rep.status}
                        </span>
                      </div>
                      <p className="text-[11px] text-text-primary mt-1 font-sans leading-tight">
                        {rep.auditSummary || rep.comparisonSummary || 'Audit run complete.'}
                      </p>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="py-4 text-center text-[11px] text-text-muted">No specific reports found.</div>
              )}
            </Card>
          )}
        </div>

        {/* ── Right Column: Replay History (2/3) ── */}
        <div className="lg:col-span-2">
          <Card title="Replay History" description="Chronological audit trail of all forensic replay operations">
            <Table
              data={historyRows}
              columns={columns}
              emptyMessage="No replay records found. Execute a forensic replay to begin building the audit trail."
            />
          </Card>
        </div>
      </div>
    </div>
  );
};

export default ReplayCenter;
