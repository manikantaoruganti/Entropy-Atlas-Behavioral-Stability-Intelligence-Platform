import React, { useState } from 'react';
import Card from '../components/Card';
import Table from '../components/Table';
import Button from '../components/Button';
import Modal from '../components/Modal';
import {
  useRiskAlerts,
  useRiskEvidence,
  useRiskDecisions,
  useRiskAudit,
  useRiskReplay,
} from '../api/hooks';
import {
  Search,
  User,
  Activity,
  Brain,
  GitFork,
  Atom,
  Scale,
  RefreshCcw,
  WifiOff,
  ChevronRight,
  ExternalLink,
} from 'lucide-react';

/* ── Formatters ── */
const fmt = (v, d = 2) => (v == null || isNaN(v)) ? '—' : Number(v).toFixed(d);
const fmtTs = (ts) => {
  if (!ts) return '—';
  try { return new Date(ts).toLocaleString(); } catch { return '—'; }
};
const fmtTime = (ts) => {
  if (!ts) return '—';
  try { return new Date(ts).toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' }); }
  catch { return '—'; }
};

const severityClass = (level) => {
  if (!level) return 'severity-indicator severity-low';
  const l = String(level).toUpperCase();
  if (l === 'CRITICAL') return 'severity-indicator severity-critical';
  if (l === 'HIGH') return 'severity-indicator severity-high';
  if (l === 'MEDIUM') return 'severity-indicator severity-medium';
  return 'severity-indicator severity-low';
};

/* ── Loading Skeleton ── */
const LoadingSkeleton = () => (
  <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }} role="status" aria-label="Loading investigation data">
    <div className="skeleton-card">
      <div className="skeleton skeleton-line w-1/3" />
      {Array.from({ length: 8 }).map((_, i) => <div key={i} className="skeleton skeleton-table-row" />)}
    </div>
  </div>
);

/* ── Error Banner ── */
const ErrorBanner = ({ message }) => (
  <div className="error-banner" role="alert">
    <WifiOff className="error-banner__icon" style={{ width: 16, height: 16, color: 'var(--accent-amber)' }} />
    <div>
      <div className="error-banner__title">API Unavailable</div>
      <div className="error-banner__desc">{message}</div>
    </div>
  </div>
);

/* ── Detail Pair ── */
const DetailPair = ({ label, value, icon: Icon, valueColor }) => (
  <div className="detail-pair">
    <div className="detail-pair__label">
      {Icon && <Icon style={{ width: 13, height: 13 }} />}
      {label}
    </div>
    <div className="detail-pair__value" style={valueColor ? { color: valueColor } : undefined}>
      {value ?? '—'}
    </div>
  </div>
);

/* ════════════════════════════════════════════
   Risk Investigation
   ════════════════════════════════════════════ */
const RiskInvestigation = () => {
  const [selectedAlert, setSelectedAlert] = useState(null);
  const [detailOpen, setDetailOpen] = useState(false);

  const selectedAlertId = selectedAlert?.id || selectedAlert?.paymentRiskId;

  const { data: alerts, isLoading: loadAlerts, error: errAlerts } = useRiskAlerts();
  const { data: evidence, isLoading: loadEvidence, error: errEvidence } = useRiskEvidence(selectedAlertId);
  const { data: decisions } = useRiskDecisions();
  const { data: audit } = useRiskAudit(selectedAlertId);
  const replayMutation = useRiskReplay();

  const loading = loadAlerts || (detailOpen && loadEvidence);
  const hasError = errAlerts || (detailOpen && errEvidence);

  if (loading && !detailOpen) return <LoadingSkeleton />;

  const alertList = Array.isArray(alerts) ? alerts : [];
  const decisionList = Array.isArray(decisions) ? decisions : [];
  const auditList = Array.isArray(audit) ? audit : [];

  const handleSelect = (alert) => {
    setSelectedAlert(alert);
    setDetailOpen(true);
  };

  const handleReplay = () => {
    if (!selectedAlert) return;
    replayMutation.mutate({
      entityId: selectedAlert.entityId || selectedAlert.entity,
      correlationId: selectedAlert.correlationId || selectedAlert.id,
    });
  };

  const matchedEvidence = evidence ? [evidence] : [];
  const matchedDecision = decisionList.find(d =>
    (d.paymentRiskId === selectedAlertId) || (d.correlationId === selectedAlert?.correlationId) || (d.entityId === selectedAlert?.entityId)
  );
  const matchedAudit = auditList;

  const alertColumns = [
    { header: 'Entity', key: 'entityId', render: (r) => <span className="font-mono text-xs">{r.entityId || r.entity || '—'}</span> },
    { header: 'Severity', key: 'riskLevel', render: (r) => {
      const level = r.riskLevel || r.severity || '';
      return <span className={severityClass(level)}><span className="severity-dot" />{level || '—'}</span>;
    }},
    { header: 'Score', key: 'riskScore', render: (r) => <span className="font-mono text-sm">{fmt(r.riskScore)}</span> },
    { header: 'Action', key: 'action', render: (r) => <span className="text-xs" style={{ color: 'var(--text-secondary)' }}>{r.action || '—'}</span> },
    { header: 'Time', key: 'timestamp', render: (r) => <span className="text-xs font-mono" style={{ color: 'var(--text-dim)' }}>{fmtTime(r.timestamp || r.createdAt)}</span> },
    { header: '', key: 'investigate', render: (r) => (
      <button
        className="btn btn-ghost btn-sm"
        onClick={(e) => { e.stopPropagation(); handleSelect(r); }}
        aria-label={`Investigate ${r.entityId || 'entity'}`}
      >
        <ExternalLink style={{ width: 12, height: 12 }} />
      </button>
    )},
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }} role="main" aria-label="Risk Investigation">
      {hasError && <ErrorBanner message="Some risk investigation APIs are not responding." />}

      {/* ALERTS TABLE */}
      <Card title="Risk Alerts" description="Select an alert to investigate" animate>
        <Table
          data={alertList.slice(0, 50)}
          columns={alertColumns}
          onRowClick={handleSelect}
          emptyMessage="No risk alerts available for investigation."
        />
      </Card>

      {/* INVESTIGATION MODAL */}
      <Modal isOpen={detailOpen} onClose={() => setDetailOpen(false)} title="Risk Investigation" className="modal--wide">
        {selectedAlert ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>

            {/* ── Entity + Risk Assessment side-by-side on desktop ── */}
            <div className="investigation-grid">
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                {/* Entity Details */}
                <Card title="Entity Details" animate={false}>
                  <DetailPair label="Entity ID" value={selectedAlert.entityId || selectedAlert.entity} icon={User} />
                  <DetailPair label="Amount" value={selectedAlert.amount != null ? `${selectedAlert.currency || ''} ${fmt(selectedAlert.amount)}` : undefined} icon={Activity} />
                  <DetailPair label="Device ID" value={selectedAlert.deviceId} icon={Activity} />
                  <DetailPair label="Location" value={selectedAlert.location} icon={Activity} />
                  <DetailPair label="Resource" value={selectedAlert.resource} icon={Activity} />
                </Card>

                {/* Behavioral Evidence */}
                <Card title="Behavioral Evidence" animate={false}>
                  <DetailPair label="Entropy Contribution" value={fmt(selectedAlert.entropyContribution)} icon={Atom} valueColor="var(--accent-cyan)" />
                  <DetailPair label="Drift Contribution" value={fmt(selectedAlert.driftContribution)} icon={GitFork} valueColor="var(--accent-amber)" />
                  <DetailPair label="Baseline Behavior" value={selectedAlert.baselineBehavior || '—'} icon={Activity} />
                  <DetailPair label="Current Behavior" value={selectedAlert.currentBehavior || '—'} icon={Activity} />
                  {matchedEvidence.length > 0 && (
                    <div style={{ marginTop: 'var(--space-3)' }}>
                      <div className="text-xs font-medium mb-2" style={{ color: 'var(--text-secondary)' }}>Evidence Items</div>
                      {matchedEvidence.map((ev, i) => (
                        <div key={i} style={{ padding: '6px 8px', marginBottom: 4, borderRadius: 'var(--radius-sm)', background: 'var(--glass-bg)', border: '1px solid var(--border-subtle)', fontSize: 'var(--text-xs)' }}>
                          <span className="font-mono" style={{ color: 'var(--accent-cyan)' }}>{ev.type || ev.evidenceType || 'evidence'}</span>
                          <span style={{ color: 'var(--text-muted)' }}> — {ev.description || ev.value || JSON.stringify(ev)}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </Card>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                {/* Risk Assessment */}
                <Card title="Risk Assessment" animate={false}>
                  <DetailPair label="Risk Score" value={fmt(selectedAlert.riskScore)} icon={Activity} valueColor="var(--accent-red)" />
                  <DetailPair label="Risk Level" value={selectedAlert.riskLevel || selectedAlert.severity} icon={Activity} valueColor="var(--accent-amber)" />
                  <DetailPair label="AI Confidence" value={selectedAlert.aiConfidence != null ? `${fmt(selectedAlert.aiConfidence * 100, 1)}%` : undefined} icon={Brain} valueColor="var(--accent-purple)" />
                </Card>

                {/* Decision */}
                <Card title="Decision" animate={false}>
                  <DetailPair label="AI Explanation" value={selectedAlert.aiExplanation || selectedAlert.explanation} icon={Brain} />
                  <DetailPair label="Recommendation" value={selectedAlert.recommendedDecision || selectedAlert.recommendation} icon={Scale} />
                  <DetailPair label="Policy Decision" value={matchedDecision?.decision || matchedDecision?.action || selectedAlert.policyDecision} icon={Scale} valueColor="var(--accent-green)" />
                  <DetailPair label="Policy Applied" value={matchedDecision?.policyApplied || matchedDecision?.policy} icon={Scale} />
                </Card>
              </div>
            </div>

            {/* ── Audit Timeline ── */}
            <Card title="Audit Timeline" animate={false}>
              {matchedAudit.length > 0 ? (
                <div className="risk-timeline">
                  {matchedAudit.map((a, i) => {
                    const action = String(a.actionType || a.action || a.event || a.type || '').toUpperCase();
                    const dotClass = action.includes('BLOCK') ? 'risk-timeline__dot--red'
                      : action.includes('ALLOW') ? 'risk-timeline__dot--green'
                      : action.includes('REVIEW') ? 'risk-timeline__dot--amber' : '';
                    return (
                      <div key={i} className="risk-timeline__item">
                        <div className={`risk-timeline__dot ${dotClass}`} />
                        <div className="risk-timeline__content">{a.details || a.actionType || a.action || a.event || a.type || '—'}</div>
                        <div className="risk-timeline__time">{fmtTs(a.timestamp || a.createdAt)}</div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className="empty-state" style={{ padding: 'var(--space-6)' }}>
                  <div className="empty-state-title">No Audit Records</div>
                  <div className="empty-state-desc">No audit trail found for this entity or correlation ID.</div>
                </div>
              )}
            </Card>

            {/* ── Actions ── */}
            <div className="flex gap-3">
              <Button icon={RefreshCcw} variant="primary" onClick={handleReplay} loading={replayMutation.isPending}>
                Replay Event
              </Button>
              <Button variant="ghost" onClick={() => setDetailOpen(false)}>Close</Button>
            </div>
          </div>
        ) : (
          <div className="empty-state" style={{ padding: 'var(--space-8)' }}>
            <div className="empty-state-title">No Alert Selected</div>
            <div className="empty-state-desc">Select an alert from the table to begin investigation.</div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default RiskInvestigation;
