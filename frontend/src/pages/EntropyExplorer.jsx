import React, { useMemo } from 'react';
import Card from '../components/Card';
import {
  useAnalyticsEntropy,
  useAnalyticsVolatility,
  useAnalyticsDistribution,
} from '../api/hooks';
import ChartContainer, {
  ResponsiveContainer,
  BarChart,
  Bar,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
} from '../components/ChartContainer';
import { Atom, TrendingUp, Calculator, Users, BarChart3, Activity } from 'lucide-react';

const BUCKET_LABELS = ['0-20', '21-40', '41-60', '61-80', '81-100'];
const BUCKET_COLORS = ['#ef4444', '#f97316', '#eab308', '#22d3ee', '#10b981'];

const EntropyExplorer = () => {
  const { data: entropy, isLoading: loadEntropy } = useAnalyticsEntropy();
  const { data: volatility, isLoading: loadVolatility } = useAnalyticsVolatility();
  const { data: distribution, isLoading: loadDist } = useAnalyticsDistribution();

  const loading = loadEntropy || loadVolatility || loadDist;

  const chartData = useMemo(() => {
    const raw = distribution?.distribution || [];
    return BUCKET_LABELS.map((label, i) => ({
      range: label,
      count: raw[i] || 0,
      fill: BUCKET_COLORS[i],
    }));
  }, [distribution]);

  const totalEntities = useMemo(() => {
    return chartData.reduce((sum, b) => sum + b.count, 0);
  }, [chartData]);

  const avgGrowth = entropy?.averageEntropyGrowth ?? 0;
  const totalCalcs = entropy?.totalCalculations ?? 0;

  /* ── Loading state ─────────────────────────────────────────── */
  if (loading) {
    return (
      <div className="loading-state">
        <Atom className="animate-spin" size={32} style={{ color: 'var(--accent-cyan)', marginBottom: 12 }} />
        <span className="font-mono" style={{ fontSize: 11, letterSpacing: '0.1em', color: 'var(--text-secondary)' }}>
          DECOMPOSING SHANNON ENTROPY MANIFOLDS…
        </span>
      </div>
    );
  }

  /* ── Empty guard ───────────────────────────────────────────── */
  const hasEntropy = entropy && typeof entropy.averageEntropyGrowth !== 'undefined';
  const hasVolatility = volatility && volatility.volatilityTrends && Object.keys(volatility.volatilityTrends).length > 0;
  const hasDist = distribution && Array.isArray(distribution.distribution) && distribution.distribution.some((v) => v > 0);

  if (!hasEntropy && !hasVolatility && !hasDist) {
    return (
      <div className="empty-state">
        <Atom size={40} style={{ color: 'var(--text-muted)', marginBottom: 12 }} />
        <p style={{ color: 'var(--text-secondary)', fontSize: 13 }}>No entropy data available yet. Ingest behavioral events to begin analysis.</p>
      </div>
    );
  }

  /* ── Volatility trend entries ──────────────────────────────── */
  const volatilityEntries = volatility?.volatilityTrends ? Object.entries(volatility.volatilityTrends) : [];

  /* ── Render ────────────────────────────────────────────────── */
  return (
    <div className="space-y-6">
      {/* ── Header ────────────────────────────────────────────── */}
      <div style={{ marginBottom: 8 }}>
        <h1 className="text-2xl font-bold text-text-primary" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <Atom size={24} style={{ color: 'var(--accent-cyan)' }} />
          Entropy Analysis
        </h1>
        <p className="text-text-secondary" style={{ marginTop: 4, fontSize: 13 }}>
          Shannon complexity decomposition — multi-dimensional entropy profiling across behavioral partitions.
        </p>
      </div>

      {/* ── KPI Strip ─────────────────────────────────────────── */}
      <div className="kpi-strip">
        <div className="kpi-item">
          <TrendingUp size={16} style={{ color: 'var(--accent-cyan)' }} />
          <div>
            <div className="font-mono" style={{ fontSize: 18, fontWeight: 700, color: 'var(--text-primary)' }}>
              {avgGrowth.toFixed(4)}
            </div>
            <div style={{ fontSize: 11, color: 'var(--text-secondary)', letterSpacing: '0.04em' }}>
              Avg Entropy Growth
            </div>
          </div>
        </div>

        <div className="kpi-item">
          <Calculator size={16} style={{ color: 'var(--accent-purple)' }} />
          <div>
            <div className="font-mono" style={{ fontSize: 18, fontWeight: 700, color: 'var(--text-primary)' }}>
              {totalCalcs.toLocaleString()}
            </div>
            <div style={{ fontSize: 11, color: 'var(--text-secondary)', letterSpacing: '0.04em' }}>
              Total Calculations
            </div>
          </div>
        </div>

        <div className="kpi-item">
          <Users size={16} style={{ color: 'var(--accent-green)' }} />
          <div>
            <div className="font-mono" style={{ fontSize: 18, fontWeight: 700, color: 'var(--text-primary)' }}>
              {totalEntities.toLocaleString()}
            </div>
            <div style={{ fontSize: 11, color: 'var(--text-secondary)', letterSpacing: '0.04em' }}>
              Entity Count
            </div>
          </div>
        </div>
      </div>

      {/* ── Two-Column Grid ───────────────────────────────────── */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Left — Global Shannon Entropy Metrics */}
        <Card title="Global Shannon Entropy Metrics" description="Core entropy computations across the entire entity population.">
          <div style={{ padding: 16 }}>
            {hasEntropy ? (
              <div className="space-y-4">
                <div className="data-row">
                  <span className="text-text-secondary" style={{ fontSize: 12 }}>Average Entropy Growth</span>
                  <span className="font-mono" style={{ fontSize: 14, color: 'var(--accent-cyan)', fontWeight: 600 }}>
                    {avgGrowth.toFixed(6)}
                  </span>
                </div>
                <div className="data-row">
                  <span className="text-text-secondary" style={{ fontSize: 12 }}>Total Calculations</span>
                  <span className="font-mono" style={{ fontSize: 14, color: 'var(--text-primary)', fontWeight: 600 }}>
                    {totalCalcs.toLocaleString()}
                  </span>
                </div>
                <div className="data-row">
                  <span className="text-text-secondary" style={{ fontSize: 12 }}>Growth Signal</span>
                  <span style={{ fontSize: 12 }}>
                    {avgGrowth > 0.01 ? (
                      <span className="badge-danger">High Divergence</span>
                    ) : avgGrowth > 0 ? (
                      <span className="badge-warning">Low Drift</span>
                    ) : (
                      <span className="badge-success">Converged</span>
                    )}
                  </span>
                </div>
                <div className="data-row">
                  <span className="text-text-secondary" style={{ fontSize: 12 }}>Calc-to-Entity Ratio</span>
                  <span className="font-mono" style={{ fontSize: 14, color: 'var(--text-primary)', fontWeight: 600 }}>
                    {totalEntities > 0 ? (totalCalcs / totalEntities).toFixed(2) : '—'}
                  </span>
                </div>
              </div>
            ) : (
              <div className="empty-state" style={{ padding: '24px 0' }}>
                <BarChart3 size={28} style={{ color: 'var(--text-muted)', marginBottom: 8 }} />
                <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>Awaiting entropy data.</span>
              </div>
            )}
          </div>
        </Card>

        {/* Right — Volatility Trend Aggregates */}
        <Card title="Volatility Trend Aggregates" description="Entity distribution across observed volatility trend categories.">
          <div style={{ padding: 16 }}>
            {hasVolatility && volatilityEntries.length > 0 ? (
              <div className="space-y-2">
                {volatilityEntries.map(([trend, count]) => {
                  const trendLower = trend.toLowerCase();
                  const badgeClass = trendLower.includes('increas') || trendLower.includes('high')
                    ? 'badge-danger'
                    : trendLower.includes('decreas') || trendLower.includes('stable')
                    ? 'badge-success'
                    : 'badge-info';
                  return (
                    <div key={trend} className="data-row">
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <Activity size={13} style={{ color: 'var(--text-muted)' }} />
                        <span style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{trend}</span>
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <span className="font-mono" style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-primary)' }}>
                          {count}
                        </span>
                        <span className={badgeClass} style={{ fontSize: 10 }}>entities</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="empty-state" style={{ padding: '24px 0' }}>
                <Activity size={28} style={{ color: 'var(--text-muted)', marginBottom: 8 }} />
                <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>No volatility trends emitted yet.</span>
              </div>
            )}
          </div>
        </Card>
      </div>

      {/* ── Full-Width Distribution Chart ─────────────────────── */}
      <ChartContainer
        title="Entity Stability Distribution"
        description="Histogram of entity distribution across 5 stability score buckets (0–100)."
      >
        {hasDist ? (
          <div style={{ height: 280 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} barCategoryGap="18%">
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                <XAxis
                  dataKey="range"
                  stroke="#64748b"
                  tick={{ fontSize: 11, fill: '#94a3b8' }}
                  axisLine={{ stroke: 'rgba(255,255,255,0.1)' }}
                />
                <YAxis
                  stroke="#64748b"
                  tick={{ fontSize: 11, fill: '#94a3b8' }}
                  axisLine={{ stroke: 'rgba(255,255,255,0.1)' }}
                  allowDecimals={false}
                />
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'var(--bg-elevated)',
                    border: '1px solid var(--border-emphasis)',
                    borderRadius: 8,
                    fontSize: 12,
                  }}
                  labelStyle={{ color: 'var(--text-primary)', fontWeight: 600 }}
                  itemStyle={{ color: 'var(--text-secondary)' }}
                  formatter={(value) => [value, 'Entities']}
                  labelFormatter={(label) => `Stability: ${label}`}
                />
                <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                  {chartData.map((entry, idx) => (
                    <Cell key={idx} fill={entry.fill} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <div className="empty-state" style={{ padding: '40px 0' }}>
            <BarChart3 size={32} style={{ color: 'var(--text-muted)', marginBottom: 8 }} />
            <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>Distribution data is not available yet.</span>
          </div>
        )}
      </ChartContainer>
    </div>
  );
};

export default EntropyExplorer;
