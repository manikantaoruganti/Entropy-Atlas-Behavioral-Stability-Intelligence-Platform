import React, { useState, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Table from '../components/Table';
import Input from '../components/Input';
import {
  useEntities,
  useTopStableEntities,
  useTopUnstableEntities,
  useHighDriftEntities
} from '../api/hooks';
import { Search, ShieldCheck, AlertTriangle, GitBranch, Activity, Layers } from 'lucide-react';

const TABS = [
  { key: 'all', label: 'All Entities', icon: Layers },
  { key: 'stable', label: 'Most Stable', icon: ShieldCheck },
  { key: 'unstable', label: 'High Instability', icon: AlertTriangle },
  { key: 'drift', label: 'Active Drift', icon: GitBranch },
];

const Entities = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [currentTab, setCurrentTab] = useState('all');

  const { data: allData, isLoading: loadAll } = useEntities(0, 100);
  const { data: stableData, isLoading: loadStable } = useTopStableEntities();
  const { data: unstableData, isLoading: loadUnstable } = useTopUnstableEntities();
  const { data: driftData, isLoading: loadDrift } = useHighDriftEntities();

  const isLoading = useMemo(() => {
    if (currentTab === 'all') return loadAll;
    if (currentTab === 'stable') return loadStable;
    if (currentTab === 'unstable') return loadUnstable;
    if (currentTab === 'drift') return loadDrift;
    return false;
  }, [currentTab, loadAll, loadStable, loadUnstable, loadDrift]);

  const sourceList = useMemo(() => {
    if (currentTab === 'all') return allData?.content || [];
    if (currentTab === 'stable') return stableData || [];
    if (currentTab === 'unstable') return unstableData || [];
    if (currentTab === 'drift') return driftData || [];
    return [];
  }, [currentTab, allData, stableData, unstableData, driftData]);

  const filtered = useMemo(() => {
    if (!searchTerm.trim()) return sourceList;
    const term = searchTerm.toLowerCase();
    return sourceList.filter(
      (e) =>
        (e.id && e.id.toLowerCase().includes(term)) ||
        (e.entityType && e.entityType.toLowerCase().includes(term))
    );
  }, [sourceList, searchTerm]);

  const handleRowClick = useCallback(
    (row) => navigate(`/entities/${row.id}`),
    [navigate]
  );

  const handleSearchChange = useCallback((e) => {
    setSearchTerm(e.target.value);
  }, []);

  const tabCounts = useMemo(() => ({
    all: allData?.content?.length ?? null,
    stable: stableData?.length ?? null,
    unstable: unstableData?.length ?? null,
    drift: driftData?.length ?? null,
  }), [allData, stableData, unstableData, driftData]);

  const columns = useMemo(() => [
    {
      key: 'id',
      header: 'Entity ID',
      render: (row) => (
        <span
          className="font-mono text-accent-cyan cursor-pointer hover:underline text-xs"
          onClick={(e) => {
            e.stopPropagation();
            navigate(`/entities/${row.id}`);
          }}
        >
          {row.id}
        </span>
      ),
    },
    {
      key: 'entityType',
      header: 'Type',
      render: (row) => (
        <span className="badge badge-purple">{row.entityType || '—'}</span>
      ),
    },
    {
      key: 'stabilityScore',
      header: 'Stability Score',
      render: (row) => {
        const score = row.stabilityScore;
        if (score === null || score === undefined)
          return <span className="font-mono text-text-muted text-xs">—</span>;
        const color =
          score < 40
            ? 'text-accent-red'
            : score < 70
            ? 'text-accent-amber'
            : 'text-accent-green';
        return (
          <span className={`font-mono font-bold text-xs ${color}`}>
            {score.toFixed(1)}%
          </span>
        );
      },
    },
    {
      key: 'driftVelocity',
      header: 'Drift Velocity',
      render: (row) => {
        const vel = row.driftVelocity;
        if (vel === null || vel === undefined)
          return <span className="font-mono text-text-muted text-xs">—</span>;
        return (
          <span
            className={`font-mono text-xs ${
              vel > 1.0 ? 'text-accent-red font-bold' : 'text-text-secondary'
            }`}
          >
            {vel.toFixed(4)}
          </span>
        );
      },
    },
    {
      key: 'entropyGrowth',
      header: 'Entropy Growth',
      render: (row) => {
        const growth = row.entropyGrowth;
        if (growth === null || growth === undefined)
          return <span className="font-mono text-text-muted text-xs">—</span>;
        return (
          <span className="font-mono text-xs text-text-secondary">
            {growth.toFixed(4)}
          </span>
        );
      },
    },
  ], [navigate]);

  return (
    <div className="space-y-4">
      {/* Tab bar */}
      <div className="tabs">
        {TABS.map(({ key, label, icon: Icon }) => (
          <button
            key={key}
            className={`tab ${currentTab === key ? 'active' : ''}`}
            onClick={() => setCurrentTab(key)}
          >
            <Icon
              className={`w-3.5 h-3.5 mr-1.5 inline ${
                key === 'stable'
                  ? 'text-accent-green'
                  : key === 'unstable'
                  ? 'text-accent-red'
                  : key === 'drift'
                  ? 'text-accent-amber'
                  : 'text-accent-cyan'
              }`}
            />
            {label}
            {tabCounts[key] !== null && (
              <span className="ml-1.5 text-text-muted font-mono text-xs">
                ({tabCounts[key]})
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Search input */}
      <div className="max-w-md">
        <Input
          placeholder="Filter by entity ID or type…"
          value={searchTerm}
          onChange={handleSearchChange}
        />
      </div>

      {/* Loading state */}
      {isLoading ? (
        <div className="loading-state">
          <Activity className="animate-pulse w-8 h-8 text-accent-cyan mb-4" />
          <span className="font-mono text-xs tracking-wider text-text-secondary">
            RESOLVING ENTITY REGISTRY…
          </span>
        </div>
      ) : (
        <Table
          data={filtered}
          columns={columns}
          onRowClick={handleRowClick}
          emptyMessage="No entities match the current filter criteria."
        />
      )}
    </div>
  );
};

export default Entities;
