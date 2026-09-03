import React from 'react';
import Card from '../components/Card';
import Table from '../components/Table';
import { useRiskAudit, useRawPrometheusMetrics } from '../api/hooks';
import {
  FileText,
  ShieldAlert,
  Clock,
  WifiOff,
  AlertTriangle,
  Brain,
  ShieldCheck,
  Ban,
} from 'lucide-react';

/* ── Formatters ── */
const fmtTs = (ts) => {
  if (!ts) return '—';
  try { return new Date(ts).toLocaleString(); } catch { return '—'; }
};
const fmtInt = (v) => (v == null || isNaN(v)) ? '—' : Number(v).toLocaleString();

/* ── Severity Helper ── */
const severityClass = (level) => {
  if (!level) return 'severity-indicator severity-low';
  const l = String(level).toUpperCase();
  if (l === 'CRITICAL') return 'severity-indicator severity-critical';
  if (l === 'HIGH') return 'severity-indicator severity-high';
  if (l === 'MEDIUM') return 'severity-indicator severity-medium';
  return 'severity-indicator severity-low';
};

/* ── Loading Skeleton ── */
const SkeletonKPIs = () => (
  <div className="risk-grid risk-grid--kpis" role="status" aria-label="Loading audit summaries">
    {Array.from({ length: 3 }).map((_, i) => (
      <div key={i} className="skeleton skeleton-kpi" />
    ))}
  </div>
);

const SkeletonTable = () => (
  <div className="skeleton-card" role="status" aria-label="Loading data">
    <div className="skeleton skeleton-line w-1/4" style={{ marginBottom: 16 }} />
    {Array.from({ length: 8 }).map((_, i) => (
      <div key={i} className="skeleton skeleton-table-row" />
    ))}
  </div>
);

/* ── Error Banner ── */
const ErrorBanner = ({ message }) => (
  <div className="error-banner error-banner--critical" role="alert">
    <WifiOff className="error-banner__icon" style={{ width: 16, height: 16, color: 'var(--accent-red)' }} />
    <div>
      <div className="error-banner__title">Audit API Unavailable</div>
      <div className="error-banner__desc">{message || 'The risk audit service is not responding. Please verify the backend is running.'}</div>
    </div>
  </div>
);

/* ── KPI Component ── */
const AuditKPI = ({ label, value, color = 'cyan', icon: Icon }) => (
  <div className={`risk-kpi risk-kpi--${color}`} tabIndex={0} aria-label={`${label}: ${value}`}>
    <div className="risk-kpi__label">
      {Icon && <Icon style={{ width: 12, height: 12 }} />}
      {label}
    </div>
    <div className="risk-kpi__value" style={color !== 'cyan' ? { color: `var(--accent-${color})` } : undefined}>
      {value}
    </div>
  </div>
);

/* ════════════════════════════════════════════
   Risk Audit
   ════════════════════════════════════════════ */
const RiskAudit = () => {
  const { data: audit, isLoading, error } = useRiskAudit();
  const { data: prom } = useRawPrometheusMetrics();

  const policyBlocksTotal = prom?.risk_policy_blocks_total ?? 0;
  const aiFallbackTotal = prom?.risk_ai_fallback_total ?? 0;

  if (isLoading) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
        <SkeletonKPIs />
        <SkeletonTable />
      </div>
    );
  }

  if (error) return <ErrorBanner />;

  const auditList = Array.isArray(audit) ? audit : [];

  const policyBlocks = auditList.filter(a => {
    const d = String(a.decision || a.action || a.type || '').toUpperCase();
    return d === 'BLOCK' || d === 'POLICY_BLOCK';
  });

  const aiFallbacks = auditList.filter(a => {
    const d = String(a.decision || a.action || a.type || '').toUpperCase();
    return d.includes('FALLBACK') || d.includes('AI_FALLBACK');
  });

  const columns = [
    { header: 'Correlation ID', key: 'correlationId', render: (r) => (
      <span className="font-mono text-xs">{r.correlationId || r.id || '—'}</span>
    )},
    { header: 'Entity', key: 'entityId', render: (r) => (
      <span className="font-mono text-xs" style={{ color: 'var(--accent-cyan)' }}>{r.entityId || r.entity || '—'}</span>
    )},
    { header: 'Decision', key: 'decision', render: (r) => {
      const d = String(r.decision || r.action || '').toUpperCase();
      const cls = d === 'BLOCK' ? 'severity-indicator severity-critical'
        : d === 'REVIEW' ? 'severity-indicator severity-high'
        : d.includes('FALLBACK') ? 'severity-indicator severity-medium'
        : d === 'ALLOW' ? 'severity-indicator severity-low'
        : 'severity-indicator';
      return <span className={cls}>{d || '—'}</span>;
    }},
    { header: 'Policy Applied', key: 'policyApplied', render: (r) => (
      <span className="text-xs font-mono">{r.policyApplied || r.policy || '—'}</span>
    )},
    { header: 'Risk Level', key: 'riskLevel', render: (r) => {
      const level = r.riskLevel || r.severity || '';
      return <span className={severityClass(level)}><span className="severity-dot" />{level || '—'}</span>;
    }},
    { header: 'Timestamp', key: 'timestamp', render: (r) => (
      <span className="text-xs font-mono" style={{ color: 'var(--text-dim)' }}>{fmtTs(r.timestamp || r.createdAt || r.decisionTime)}</span>
    )},
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }} role="main" aria-label="Risk Audit Trail">

      {/* SUMMARY KPIs */}
      <div className="risk-grid risk-grid--kpis" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
        <AuditKPI label="Total Records" value={fmtInt(auditList.length)} icon={FileText} color="cyan" />
        <AuditKPI label="Policy Blocks" value={fmtInt(policyBlocksTotal || policyBlocks.length)} icon={Ban} color="red" />
        <AuditKPI label="AI Fallbacks" value={fmtInt(aiFallbackTotal || aiFallbacks.length)} icon={Brain} color="purple" />
      </div>

      {/* DECISION HISTORY TABLE */}
      <Card title="Decision History" description="Complete audit trail of risk decisions" animate>
        <Table
          data={auditList}
          columns={columns}
          emptyMessage="No audit records available. Deploy simulator events to create records."
        />
      </Card>

      {/* POLICY BLOCKS SECTION */}
      {policyBlocks.length > 0 && (
        <Card title="Policy Blocks Only" description="Transactions blocked by policy rules" animate delay={0.1}>
          <Table
            data={policyBlocks}
            columns={columns}
            emptyMessage="No policy blocks recorded."
          />
        </Card>
      )}

      {/* AI FALLBACK EVENTS SECTION */}
      {aiFallbacks.length > 0 && (
        <Card title="AI Fallback Events Only" description="Decisions made without primary AI verification models" animate delay={0.2}>
          <Table
            data={aiFallbacks}
            columns={columns}
            emptyMessage="No AI fallback events recorded."
          />
        </Card>
      )}
    </div>
  );
};

export default RiskAudit;
