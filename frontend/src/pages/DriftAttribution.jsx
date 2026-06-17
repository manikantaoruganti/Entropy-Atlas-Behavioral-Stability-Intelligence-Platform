import React from 'react';
import Card from '../components/Card';
import Table from '../components/Table';
import { useAnalyticsDrift } from '../api/hooks';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from '../components/ChartContainer';
import { GitFork, Zap } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const DriftAttribution = () => {
  const navigate = useNavigate();
  const { data: driftData, isLoading, isError } = useAnalyticsDrift();

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center h-96 text-text-secondary">
        <Zap className="animate-pulse w-8 h-8 text-accent-cyan mb-4" /> 
        <span className="font-mono text-xs tracking-wider">RESOLVING SHAP CONTRIBUTION MATRICES...</span>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="flex items-center justify-center h-96 text-accent-red">
        Telemetry stream disconnected. Check backend services.
      </div>
    );
  }

  const list = driftData || [];

  const columns = [
    { key: 'timestamp', header: 'Detected At', render: (row) => new Date(row.timestamp).toLocaleString() },
    { 
      key: 'entityId', 
      header: 'Entity ID', 
      render: (row) => (
        <span className="font-mono text-accent-cyan cursor-pointer hover:underline" onClick={() => navigate(`/entities/${row.entityId}`)}>
          {row.entityId}
        </span>
      ) 
    },
    { key: 'explanationSummary', header: 'Drift Attribution Summary' },
    {
      key: 'dimensionContributions',
      header: 'Dimension Breakdown',
      render: (row) => (
        <div className="flex flex-wrap gap-1">
          {Object.entries(row.dimensionContributions || {}).map(([dim, val]) => (
            <span key={dim} className="px-2 py-0.5 text-[10px] rounded bg-accent-red-dim text-accent-red border border-accent-red/20 font-mono">
              {dim}: {val.toFixed(1)}
            </span>
          ))}
        </div>
      )
    }
  ];

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
            <GitFork className="w-6 h-6 text-accent-amber" /> Drift Attribution Studio
          </h1>
          <p className="text-text-secondary mt-1">Isolating root causes of behavioral drift by partitioning contributions into discrete dimensions.</p>
        </div>
      </div>

      <Card title="Drift Explanations Matrix Log">
        <Table
          data={list}
          columns={columns}
          emptyMessage="No recent behavioral drift explanations found in the database."
        />
      </Card>
    </div>
  );
};

export default DriftAttribution;
