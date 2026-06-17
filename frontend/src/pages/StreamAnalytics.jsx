import React, { useMemo } from 'react';
import Card from '../components/Card';
import {
  useStreamsThroughput,
  useStreamsLag,
  useStreamsTopics,
  useStreamsPartitions
} from '../api/hooks';
import { Activity, Radio, Server, Hash, Layers, AlertTriangle } from 'lucide-react';

const StreamAnalytics = () => {
  const { data: throughput, isLoading: loadThroughput } = useStreamsThroughput();
  const { data: lag, isLoading: loadLag } = useStreamsLag();
  const { data: topics, isLoading: loadTopics } = useStreamsTopics();
  const { data: partitions, isLoading: loadPartitions } = useStreamsPartitions();

  const loading = loadThroughput || loadLag || loadTopics || loadPartitions;

  const topicsList = useMemo(() => {
    if (!topics) return [];
    return Array.isArray(topics) ? topics : [];
  }, [topics]);

  const partitionEntries = useMemo(() => {
    if (!partitions) return [];
    if (typeof partitions === 'object' && !Array.isArray(partitions)) {
      return Object.entries(partitions);
    }
    return [];
  }, [partitions]);

  const totalPartitions = useMemo(() => {
    return partitionEntries.reduce((sum, [, count]) => sum + (typeof count === 'number' ? count : 0), 0);
  }, [partitionEntries]);

  if (loading) {
    return (
      <div className="loading-state">
        <Activity className="animate-spin w-6 h-6 text-accent-cyan" />
        <span className="font-mono text-xs tracking-wider text-text-secondary mt-3">
          RESOLVING BROKER TOPOLOGY...
        </span>
      </div>
    );
  }

  const currentEps = throughput?.currentThroughputEps ?? 0;
  const totalLag = lag?.totalLag ?? 0;
  const totalProcessed = throughput?.totalEventsIngested ?? 0;

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-text-primary flex items-center gap-2">
            <Radio className="w-5 h-5 text-accent-cyan" />
            Pipeline Intelligence
          </h1>
          <p className="text-xs text-text-muted mt-0.5 font-mono">
            Kafka streams · throughput · partitions · consumer offsets
          </p>
        </div>
        <div className="flex items-center gap-2">
          <span className="status-dot status-healthy" />
          <span className="text-xs font-mono text-text-secondary">CONNECTED</span>
        </div>
      </div>

      {/* KPI Strip */}
      <div className="kpi-strip">
        <div className="kpi-item">
          <div className="flex items-center gap-1.5 mb-1">
            <Activity className="w-3 h-3 text-accent-cyan" />
            <span className="text-[10px] font-mono text-text-muted uppercase tracking-wider">Throughput</span>
          </div>
          <span className="text-xl font-bold font-mono text-text-primary">
            {currentEps.toFixed(1)}
          </span>
          <span className="text-[10px] text-text-muted font-mono ml-1">eps</span>
        </div>

        <div className="kpi-item">
          <div className="flex items-center gap-1.5 mb-1">
            <Server className="w-3 h-3 text-accent-purple" />
            <span className="text-[10px] font-mono text-text-muted uppercase tracking-wider">Consumer Lag</span>
          </div>
          <span className={`text-xl font-bold font-mono ${totalLag > 1000 ? 'text-accent-red' : totalLag > 100 ? 'text-accent-amber' : 'text-text-primary'}`}>
            {totalLag.toLocaleString()}
          </span>
          <span className="text-[10px] text-text-muted font-mono ml-1">msg</span>
        </div>

        <div className="kpi-item">
          <div className="flex items-center gap-1.5 mb-1">
            <Layers className="w-3 h-3 text-accent-green" />
            <span className="text-[10px] font-mono text-text-muted uppercase tracking-wider">Processed</span>
          </div>
          <span className="text-xl font-bold font-mono text-text-primary">
            {totalProcessed.toLocaleString()}
          </span>
          <span className="text-[10px] text-text-muted font-mono ml-1">total</span>
        </div>

        <div className="kpi-item">
          <div className="flex items-center gap-1.5 mb-1">
            <Hash className="w-3 h-3 text-accent-cyan" />
            <span className="text-[10px] font-mono text-text-muted uppercase tracking-wider">Topics</span>
          </div>
          <span className="text-xl font-bold font-mono text-text-primary">
            {topicsList.length}
          </span>
          <span className="text-[10px] text-text-muted font-mono ml-1">active</span>
        </div>

        <div className="kpi-item">
          <div className="flex items-center gap-1.5 mb-1">
            <Layers className="w-3 h-3 text-accent-purple" />
            <span className="text-[10px] font-mono text-text-muted uppercase tracking-wider">Partitions</span>
          </div>
          <span className="text-xl font-bold font-mono text-text-primary">
            {totalPartitions}
          </span>
          <span className="text-[10px] text-text-muted font-mono ml-1">total</span>
        </div>
      </div>

      {/* Two-Column Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Left: Active Topics */}
        <Card
          title="Active Topics"
          description="Subscribed Kafka topic schemas"
        >
          {topicsList.length === 0 ? (
            <div className="empty-state">
              <AlertTriangle className="w-5 h-5 text-text-muted" />
              <span className="text-xs text-text-muted font-mono mt-2">No active topics discovered</span>
            </div>
          ) : (
            <div className="space-y-1">
              {topicsList.map((topic, idx) => (
                <div
                  key={typeof topic === 'string' ? topic : idx}
                  className="data-row"
                >
                  <div className="flex items-center gap-2 min-w-0">
                    <span className="status-dot status-healthy flex-shrink-0" />
                    <span className="font-mono text-xs text-text-primary truncate">
                      {typeof topic === 'string' ? topic : topic?.name || `topic-${idx}`}
                    </span>
                  </div>
                  <span className="badge badge-purple text-[10px] flex-shrink-0">ACTIVE</span>
                </div>
              ))}
            </div>
          )}
        </Card>

        {/* Right: Partition Layout */}
        <Card
          title="Partition Layout"
          description="Per-topic partition allocation"
        >
          {partitionEntries.length === 0 ? (
            <div className="empty-state">
              <AlertTriangle className="w-5 h-5 text-text-muted" />
              <span className="text-xs text-text-muted font-mono mt-2">No partition data available</span>
            </div>
          ) : (
            <div className="space-y-1">
              {partitionEntries.map(([topic, partitionCount]) => (
                <div key={topic} className="data-row">
                  <div className="flex items-center gap-2 min-w-0">
                    <Hash className="w-3 h-3 text-text-muted flex-shrink-0" />
                    <span className="font-mono text-xs text-text-primary truncate">{topic}</span>
                  </div>
                  <div className="flex items-center gap-2 flex-shrink-0">
                    <div className="flex gap-px">
                      {Array.from({ length: Math.min(partitionCount, 12) }).map((_, i) => (
                        <div
                          key={i}
                          className="w-1.5 h-3 rounded-sm"
                          style={{ backgroundColor: 'var(--accent-cyan)', opacity: 0.4 + (0.6 * (i + 1) / Math.min(partitionCount, 12)) }}
                        />
                      ))}
                      {partitionCount > 12 && (
                        <span className="text-[9px] text-text-muted font-mono ml-1">+{partitionCount - 12}</span>
                      )}
                    </div>
                    <span className="font-mono text-xs text-accent-cyan font-bold w-8 text-right">
                      {partitionCount}
                    </span>
                  </div>
                </div>
              ))}
              <div className="flex justify-end pt-2 border-t border-glass-border mt-2">
                <span className="text-[10px] font-mono text-text-muted">
                  TOTAL: <span className="text-text-primary font-bold">{totalPartitions}</span> partitions across <span className="text-text-primary font-bold">{partitionEntries.length}</span> topics
                </span>
              </div>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
};

export default StreamAnalytics;
