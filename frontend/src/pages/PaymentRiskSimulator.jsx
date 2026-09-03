import React, { useState } from 'react';
import Card from '../components/Card';
import Button from '../components/Button';
import { usePaymentScenarioSimulation } from '../api/hooks';
import {
  Play,
  FlaskConical,
  CheckCircle2,
  XCircle,
  Radio,
  WifiOff,
} from 'lucide-react';

const SCENARIOS = [
  { value: 'NORMAL_PAYMENT_TRAFFIC', label: 'Normal Payment Traffic', desc: 'Baseline transactional activity with low overall risk profile.' },
  { value: 'VELOCITY_SPIKE', label: 'Velocity Spike', desc: 'Rapid surge in transaction frequency from a single entity within a short window.' },
  { value: 'GEO_DRIFT', label: 'Geo Drift', desc: 'Transactions occurring across physically impossible distances in rapid succession.' },
  { value: 'DEVICE_DRIFT', label: 'Device Drift', desc: 'Frequent shifts in device fingerprint for a single merchant/payment profile.' },
  { value: 'AMOUNT_ANOMALY', label: 'Amount Anomaly', desc: 'Transaction value significantly exceeding historical entity baseline averages.' },
  { value: 'PAYMENT_METHOD_SHIFT', label: 'Payment Method Shift', desc: 'Sudden change in preferred payment type, suggesting card testing or account takeover.' },
  { value: 'FAILURE_CLUSTER', label: 'Failure Cluster', desc: 'High density of payment authentication/authorization failures in a short span.' },
  { value: 'COORDINATED_PAYMENT_ABUSE', label: 'Coordinated Payment Abuse', desc: 'Multi-merchant, multi-user campaign utilizing shared device/location features.' },
  { value: 'AI_SERVICE_FAILURE', label: 'AI Service Failure', desc: 'Simulates primary AI verification model downtime and fallback execution.' },
  { value: 'POLICY_BLOCK', label: 'Policy Block', desc: 'Triggers strict deterministic rule matches to bypass machine learning layers.' },
];

const fmtTs = (ts) => {
  if (!ts) return '—';
  try { return new Date(ts).toLocaleString(); } catch { return '—'; }
};

const PaymentRiskSimulator = () => {
  const [selectedScenario, setSelectedScenario] = useState(SCENARIOS[0].value);
  const [eventCountOverride, setEventCountOverride] = useState('');
  const [result, setResult] = useState(null);

  const mutation = usePaymentScenarioSimulation();

  const handleStart = () => {
    setResult(null);
    const request = { scenario: selectedScenario };
    if (eventCountOverride && !isNaN(parseInt(eventCountOverride, 10))) {
      request.eventCount = parseInt(eventCountOverride, 10);
    }
    mutation.mutate(request, {
      onSuccess: (data) => setResult(data),
    });
  };

  const isRunning = mutation.isPending;
  const hasError = mutation.isError;
  const errorMsg = mutation.error?.response?.data?.message || mutation.error?.message || 'Scenario simulation failed.';

  const handleKeyDown = (e, value) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      setSelectedScenario(value);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }} role="main" aria-label="Payment Risk Simulator">

      {/* SCENARIO CONFIGURATION */}
      <Card title="Scenario Configuration" description="Configure and execute local payment-risk simulations to evaluate model performance." animate>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          
          <div className="label">Select Risk Scenario</div>
          <div className="risk-grid risk-grid--2col" style={{ gap: 'var(--space-3)' }} role="radiogroup" aria-label="Scenario List">
            {SCENARIOS.map((s) => {
              const isSelected = selectedScenario === s.value;
              return (
                <div
                  key={s.value}
                  className={`scenario-card ${isSelected ? 'selected' : ''}`}
                  onClick={() => setSelectedScenario(s.value)}
                  onKeyDown={(e) => handleKeyDown(e, s.value)}
                  tabIndex={0}
                  role="radio"
                  aria-checked={isSelected}
                  aria-label={s.label}
                >
                  <div style={{ width: 14, height: 14, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <Radio style={{ width: 12, height: 12, color: isSelected ? 'var(--accent-cyan)' : 'var(--text-dim)' }} />
                  </div>
                  <div style={{ minWidth: 0 }}>
                    <div className="text-xs font-semibold" style={{ color: isSelected ? 'var(--text-primary)' : 'var(--text-secondary)' }}>{s.label}</div>
                    <div className="text-xs" style={{ color: 'var(--text-muted)', marginTop: 2, fontSize: '11px', lineHeight: 1.4 }}>{s.desc}</div>
                  </div>
                </div>
              );
            })}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)', maxWidth: 280, marginTop: 'var(--space-2)' }}>
            <label htmlFor="event-override" className="label">Event Count Override (optional)</label>
            <input
              id="event-override"
              type="number"
              className="input"
              placeholder="Use scenario default"
              value={eventCountOverride}
              onChange={(e) => setEventCountOverride(e.target.value)}
              min="1"
              max="100000"
              disabled={isRunning}
            />
          </div>

          <div style={{ marginTop: 'var(--space-2)' }}>
            <Button
              icon={Play}
              variant="primary"
              onClick={handleStart}
              loading={isRunning}
              disabled={isRunning}
            >
              {isRunning ? 'Executing Simulation...' : 'Execute Simulation'}
            </Button>
          </div>
        </div>
      </Card>

      {/* PROGRESS / RUNNING STATE */}
      {isRunning && (
        <div className="sim-progress" role="status">
          <div className="sim-progress__spinner" />
          <div>
            <div className="text-xs font-semibold" style={{ color: 'var(--text-primary)' }}>Simulation In Progress</div>
            <div className="text-xs" style={{ color: 'var(--text-secondary)' }}>
              Publishing synthetic payment events for <span className="font-mono text-accent-cyan">{selectedScenario}</span> to the ingestion pipeline...
            </div>
          </div>
        </div>
      )}

      {/* ERROR STATE */}
      {hasError && !isRunning && (
        <div className="error-banner error-banner--critical" role="alert">
          <WifiOff className="error-banner__icon" style={{ width: 16, height: 16, color: 'var(--accent-red)' }} />
          <div>
            <div className="error-banner__title">Simulation Failed</div>
            <div className="error-banner__desc">{errorMsg}</div>
          </div>
        </div>
      )}

      {/* RESULT */}
      {result && !isRunning && (
        <div className="sim-result animate-slide-up" role="region" aria-label="Simulation results">
          <div className="sim-result__header">
            <CheckCircle2 style={{ width: 16, height: 16, color: 'var(--accent-green)' }} />
            <span className="text-xs font-semibold" style={{ color: 'var(--accent-green)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>Execution Succeeded</span>
          </div>

          <div className="sim-result__grid">
            <div className="sim-result__cell">
              <div className="sim-result__cell-label">Scenario ID</div>
              <div className="sim-result__cell-value text-xs truncate" style={{ color: 'var(--text-secondary)' }}>{result.scenarioId || '—'}</div>
            </div>
            <div className="sim-result__cell">
              <div className="sim-result__cell-label">Type</div>
              <div className="sim-result__cell-value" style={{ color: 'var(--accent-cyan)' }}>{result.scenarioType || '—'}</div>
            </div>
            <div className="sim-result__cell">
              <div className="sim-result__cell-label">Generated Events</div>
              <div className="sim-result__cell-value" style={{ fontSize: 'var(--text-base)' }}>{result.eventCount != null ? result.eventCount.toLocaleString() : '—'}</div>
            </div>
            <div className="sim-result__cell">
              <div className="sim-result__cell-label">Expected Truth</div>
              <div className="sim-result__cell-value" style={{ color: result.expectedGroundTruth === 'RISK' ? 'var(--accent-red)' : 'var(--accent-green)' }}>
                {result.expectedGroundTruth || '—'}
              </div>
            </div>
            <div className="sim-result__cell">
              <div className="sim-result__cell-label">Executed At</div>
              <div className="sim-result__cell-value text-xs" style={{ color: 'var(--text-secondary)' }}>{fmtTs(result.startTime)}</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PaymentRiskSimulator;
