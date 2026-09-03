import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Entities from './pages/Entities';
import EntityProfile from './pages/EntityProfile';
import EntropyExplorer from './pages/EntropyExplorer';
import DriftAttribution from './pages/DriftAttribution';
import StabilityTimeline from './pages/StabilityTimeline';
import ReplayCenter from './pages/ReplayCenter';
import StreamAnalytics from './pages/StreamAnalytics';
import MetricsCenter from './pages/MetricsCenter';
import SystemHealth from './pages/SystemHealth';
import Architecture from './pages/Architecture';
import EventIngestion from './pages/EventIngestion';
import RiskCommandCenter from './pages/RiskCommandCenter';
import RiskInvestigation from './pages/RiskInvestigation';
import RiskEvaluation from './pages/RiskEvaluation';
import PaymentRiskSimulator from './pages/PaymentRiskSimulator';
import RiskAudit from './pages/RiskAudit';
import { Toaster } from 'react-hot-toast';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30000,
      refetchOnWindowFocus: false,
      retry: 2,
      refetchInterval: false,
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <Layout>
          <Routes>
            <Route path="/" element={<RiskCommandCenter />} />
            <Route path="/stability-dashboard" element={<Dashboard />} />
            <Route path="/entities" element={<Entities />} />
            <Route path="/entities/:id" element={<EntityProfile />} />
            <Route path="/entropy-explorer" element={<EntropyExplorer />} />
            <Route path="/drift-attribution" element={<DriftAttribution />} />
            <Route path="/stability-timeline" element={<StabilityTimeline />} />
            <Route path="/replay-center" element={<ReplayCenter />} />
            <Route path="/stream-analytics" element={<StreamAnalytics />} />
            <Route path="/metrics-center" element={<MetricsCenter />} />
            <Route path="/system-health" element={<SystemHealth />} />
            <Route path="/architecture" element={<Architecture />} />
            <Route path="/event-ingestion" element={<EventIngestion />} />
            <Route path="/risk-command-center" element={<RiskCommandCenter />} />
            <Route path="/risk-investigation" element={<RiskInvestigation />} />
            <Route path="/risk-evaluation" element={<RiskEvaluation />} />
            <Route path="/risk-simulator" element={<PaymentRiskSimulator />} />
            <Route path="/risk-audit" element={<RiskAudit />} />
          </Routes>
        </Layout>
      </Router>
      <Toaster 
        position="bottom-right" 
        reverseOrder={false}
        toastOptions={{
          style: {
            background: 'var(--bg-elevated)',
            color: 'var(--text-primary)',
            border: '1px solid var(--border-emphasis)',
            fontSize: '13px',
          }
        }}
      />
    </QueryClientProvider>
  );
}
