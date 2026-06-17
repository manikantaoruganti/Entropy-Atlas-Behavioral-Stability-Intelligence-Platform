import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosInstance from './axiosInstance';
import toast from 'react-hot-toast';

// --- Ingestion API ---
export const useIngestEvent = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (eventData) =>
      axiosInstance.post('/api/v1/events', eventData).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dashboardOverview'] });
      queryClient.invalidateQueries({ queryKey: ['dashboardActivity'] });
      toast.success('Event ingested successfully!');
    },
    onError: (error) => {
      toast.error(`Failed to ingest event: ${error.message}`);
    },
  });
};

// --- Dashboard APIs ---
export const useDashboardOverview = () => {
  return useQuery({
    queryKey: ['dashboardOverview'],
    queryFn: () => axiosInstance.get('/api/v1/dashboard/overview').then(res => res.data)
  });
};

export const useDashboardHealth = () => {
  return useQuery({
    queryKey: ['dashboardHealth'],
    queryFn: () => axiosInstance.get('/api/v1/dashboard/health').then(res => res.data)
  });
};

export const useDashboardActivity = () => {
  return useQuery({
    queryKey: ['dashboardActivity'],
    queryFn: () => axiosInstance.get('/api/v1/dashboard/activity').then(res => res.data)
  });
};

// --- Analytics APIs ---
export const useAnalyticsEntropy = () => {
  return useQuery({
    queryKey: ['analyticsEntropy'],
    queryFn: () => axiosInstance.get('/api/v1/analytics/entropy').then(res => res.data)
  });
};

export const useAnalyticsDrift = () => {
  return useQuery({
    queryKey: ['analyticsDrift'],
    queryFn: () => axiosInstance.get('/api/v1/analytics/drift').then(res => res.data)
  });
};

export const useAnalyticsVolatility = () => {
  return useQuery({
    queryKey: ['analyticsVolatility'],
    queryFn: () => axiosInstance.get('/api/v1/analytics/volatility').then(res => res.data)
  });
};

export const useAnalyticsTrends = () => {
  return useQuery({
    queryKey: ['analyticsTrends'],
    queryFn: () => axiosInstance.get('/api/v1/analytics/trends').then(res => res.data)
  });
};

export const useAnalyticsDistribution = () => {
  return useQuery({
    queryKey: ['analyticsDistribution'],
    queryFn: () => axiosInstance.get('/api/v1/analytics/distribution').then(res => res.data)
  });
};

// --- Entity Intelligence APIs ---
export const useTopStableEntities = () => {
  return useQuery({
    queryKey: ['topStableEntities'],
    queryFn: () => axiosInstance.get('/api/v1/entities/top-stable').then(res => res.data)
  });
};

export const useTopUnstableEntities = () => {
  return useQuery({
    queryKey: ['topUnstableEntities'],
    queryFn: () => axiosInstance.get('/api/v1/entities/top-unstable').then(res => res.data)
  });
};

export const useHighDriftEntities = () => {
  return useQuery({
    queryKey: ['highDriftEntities'],
    queryFn: () => axiosInstance.get('/api/v1/entities/high-drift').then(res => res.data)
  });
};

export const useEntityBehaviorDna = (entityId) => {
  return useQuery({
    queryKey: ['entityBehaviorDna', entityId],
    queryFn: () => axiosInstance.get(`/api/v1/entities/${entityId}/behavior-dna`).then(res => res.data),
    enabled: !!entityId
  });
};

export const useEntityEntropyEvolution = (entityId) => {
  return useQuery({
    queryKey: ['entityEntropyEvolution', entityId],
    queryFn: () => axiosInstance.get(`/api/v1/entities/${entityId}/entropy-evolution`).then(res => res.data),
    enabled: !!entityId
  });
};

export const useEntityVolatility = (entityId) => {
  return useQuery({
    queryKey: ['entityVolatility', entityId],
    queryFn: () => axiosInstance.get(`/api/v1/entities/${entityId}/volatility`).then(res => res.data),
    enabled: !!entityId
  });
};

// --- Replay Intelligence APIs ---
export const useReplayStatistics = () => {
  return useQuery({
    queryKey: ['replayStatistics'],
    queryFn: () => axiosInstance.get('/api/v1/replay/statistics').then(res => res.data)
  });
};

export const useReplayHistory = () => {
  return useQuery({
    queryKey: ['replayHistory'],
    queryFn: () => axiosInstance.get('/api/v1/replay/history').then(res => res.data)
  });
};

export const useReplayConsistency = () => {
  return useQuery({
    queryKey: ['replayConsistency'],
    queryFn: () => axiosInstance.get('/api/v1/replay/consistency').then(res => res.data)
  });
};

// --- Metrics APIs ---
export const useMetricsSummary = () => {
  return useQuery({
    queryKey: ['metricsSummary'],
    queryFn: () => axiosInstance.get('/api/v1/metrics/summary').then(res => res.data)
  });
};

export const useMetricsJvm = () => {
  return useQuery({
    queryKey: ['metricsJvm'],
    queryFn: () => axiosInstance.get('/api/v1/metrics/jvm').then(res => res.data)
  });
};

export const useMetricsKafka = () => {
  return useQuery({
    queryKey: ['metricsKafka'],
    queryFn: () => axiosInstance.get('/api/v1/metrics/kafka').then(res => res.data)
  });
};

export const useMetricsDatabase = () => {
  return useQuery({
    queryKey: ['metricsDatabase'],
    queryFn: () => axiosInstance.get('/api/v1/metrics/database').then(res => res.data)
  });
};

export const useMetricsCache = () => {
  return useQuery({
    queryKey: ['metricsCache'],
    queryFn: () => axiosInstance.get('/api/v1/metrics/cache').then(res => res.data)
  });
};

// --- System APIs ---
export const useSystemHealth = () => {
  return useQuery({
    queryKey: ['systemHealth'],
    queryFn: () => axiosInstance.get('/api/v1/system/health').then(res => res.data)
  });
};

export const useSystemResources = () => {
  return useQuery({
    queryKey: ['systemResources'],
    queryFn: () => axiosInstance.get('/api/v1/system/resources').then(res => res.data)
  });
};

export const useSystemDependencies = () => {
  return useQuery({
    queryKey: ['systemDependencies'],
    queryFn: () => axiosInstance.get('/api/v1/system/dependencies').then(res => res.data)
  });
};

export const useSystemStatus = () => {
  return useQuery({
    queryKey: ['systemStatus'],
    queryFn: () => axiosInstance.get('/api/v1/system/status').then(res => res.data)
  });
};

// --- Stream APIs ---
export const useStreamsThroughput = () => {
  return useQuery({
    queryKey: ['streamsThroughput'],
    queryFn: () => axiosInstance.get('/api/v1/streams/throughput').then(res => res.data)
  });
};

export const useStreamsLag = () => {
  return useQuery({
    queryKey: ['streamsLag'],
    queryFn: () => axiosInstance.get('/api/v1/streams/lag').then(res => res.data)
  });
};

export const useStreamsTopics = () => {
  return useQuery({
    queryKey: ['streamsTopics'],
    queryFn: () => axiosInstance.get('/api/v1/streams/topics').then(res => res.data)
  });
};

export const useStreamsPartitions = () => {
  return useQuery({
    queryKey: ['streamsPartitions'],
    queryFn: () => axiosInstance.get('/api/v1/streams/partitions').then(res => res.data)
  });
};

// --- Core Legacy mappings ---
export const useEntities = (page = 0, size = 10, sortBy = 'id', sortDir = 'asc') => {
  return useQuery({
    queryKey: ['entities', page, size, sortBy, sortDir],
    queryFn: () => axiosInstance.get(`/api/v1/entities?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`).then(res => res.data)
  });
};

export const useEntity = (id) => {
  return useQuery({
    queryKey: ['entity', id],
    queryFn: () => axiosInstance.get(`/api/v1/entities/${id}`).then(res => res.data),
    enabled: !!id
  });
};

export const useEntityStabilityTimeline = (id) => {
  return useQuery({
    queryKey: ['entityStabilityTimeline', id],
    queryFn: () => axiosInstance.get(`/api/v1/entities/${id}/stability`).then(res => res.data),
    enabled: !!id
  });
};

export const useEntityBehaviorTimeline = (id) => {
  return useQuery({
    queryKey: ['entityBehaviorTimeline', id],
    queryFn: () => axiosInstance.get(`/api/v1/entities/${id}/timeline`).then(res => res.data),
    enabled: !!id
  });
};

export const useDriftExplanations = (id) => {
  return useQuery({
    queryKey: ['driftExplanations', id],
    queryFn: () => axiosInstance.get(`/api/v1/entities/${id}/explanations`).then(res => res.data),
    enabled: !!id
  });
};

export const useReplayEntity = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (entityId) =>
      axiosInstance.post(`/admin/replay/${entityId}`).then((res) => res.data),
    onSuccess: (_, entityId) => {
      queryClient.invalidateQueries({ queryKey: ['replayReports', entityId] });
      toast.success(`Replay for entity ${entityId} initiated!`);
    },
    onError: (error) => {
      toast.error(`Failed to initiate replay: ${error.message}`);
    },
  });
};

export const useReplayReports = (entityId) => {
  return useQuery({
    queryKey: ['replayReports', entityId],
    queryFn: () =>
      axiosInstance.get(`/admin/replay-reports/${entityId}`).then((res) => res.data),
    enabled: !!entityId,
  });
};

export const useDriftReport = (entityId) => {
  return useQuery({
    queryKey: ['driftReport', entityId],
    queryFn: () =>
      axiosInstance.get(`/admin/drift-report/${entityId}`).then((res) => res.data),
    enabled: !!entityId,
  });
};

export const useRebuildEntity = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (entityId) =>
      axiosInstance.post(`/admin/rebuild/${entityId}`).then((res) => res.data),
    onSuccess: (_, entityId) => {
      queryClient.invalidateQueries({ queryKey: ['entity', entityId] });
      queryClient.invalidateQueries({ queryKey: ['entityBehaviorDna', entityId] });
      queryClient.invalidateQueries({ queryKey: ['entityEntropyEvolution', entityId] });
      queryClient.invalidateQueries({ queryKey: ['entityVolatility', entityId] });
      toast.success(`Entity state rebuild initiated for ${entityId}!`);
    },
    onError: (error) => {
      toast.error(`Rebuild failed: ${error.message}`);
    },
  });
};

// --- Actuator / Prometheus Hooks ---
export const useRawPrometheusMetrics = () => {
  return useQuery({
    queryKey: ['rawPrometheus'],
    queryFn: () => axiosInstance.get('/actuator/prometheus', { responseType: 'text' }).then(res => {
      const text = typeof res.data === 'string' ? res.data : '';
      const metrics = {};
      text.split('\n').forEach(line => {
        if (line && !line.startsWith('#')) {
          const match = line.match(/^([a-zA-Z_:][a-zA-Z0-9_:]*)\s+([\d.eE+-]+)/);
          if (match) {
            metrics[match[1]] = parseFloat(match[2]);
          }
        }
      });
      return metrics;
    }),
    refetchInterval: 15000,
    retry: 1,
    staleTime: 10000,
  });
};

export const useActuatorHealth = () => {
  return useQuery({
    queryKey: ['actuatorHealth'],
    queryFn: () => axiosInstance.get('/actuator/health').then(res => res.data),
    refetchInterval: 30000,
    retry: 1,
    staleTime: 20000,
  });
};

export const usePrometheusMetric = (metricName) => {
  const { data: allMetrics } = useRawPrometheusMetrics();
  return allMetrics ? allMetrics[metricName] || 0 : 0;
};
