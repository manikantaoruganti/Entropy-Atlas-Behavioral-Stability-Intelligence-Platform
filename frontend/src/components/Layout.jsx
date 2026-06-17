import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useActuatorHealth } from '../api/hooks';
import {
  Users, Activity, GitFork, Clock, RefreshCcw, BarChart2,
  HeartPulse, LayoutDashboard, Menu, Atom, Zap, Cloud,
  ChevronRight, ChevronDown, Send
} from 'lucide-react';

const NavItem = ({ to, icon: Icon, label, isSidebarOpen }) => {
  const location = useLocation();
  const isActive = location.pathname === to || 
    (to !== '/' && location.pathname.startsWith(to));

  return (
    <Link to={to} className={`sidebar-link ${isActive ? 'active' : ''}`}>
      <Icon className="sidebar-link-icon" />
      {isSidebarOpen && <span className="whitespace-nowrap">{label}</span>}
    </Link>
  );
};

const SectionToggle = ({ label, isOpen, onToggle, isSidebarOpen }) => {
  if (!isSidebarOpen) return null;
  return (
    <div className="sidebar-section-label" onClick={onToggle}>
      <span>{label}</span>
      {isOpen ? <ChevronDown className="w-3 h-3" /> : <ChevronRight className="w-3 h-3" />}
    </div>
  );
};

const Sidebar = ({ isSidebarOpen, toggleSidebar }) => {
  const navigate = useNavigate();
  const [sections, setSections] = useState({
    intelligence: true,
    operations: true,
    observability: true,
    system: true,
  });

  const { data: healthData } = useActuatorHealth();
  const overallStatus = healthData?.status || 'UNKNOWN';
  const isHealthy = overallStatus === 'UP';

  const toggle = (key) => setSections(prev => ({ ...prev, [key]: !prev[key] }));

  return (
    <aside className="sidebar" style={{ width: isSidebarOpen ? 'var(--sidebar-width)' : 'var(--sidebar-collapsed)' }}>
      <div className="sidebar-header">
        <div className="sidebar-logo" onClick={() => navigate('/')}>
          <Atom />
        </div>
        {isSidebarOpen && (
          <div className="sidebar-brand" onClick={() => navigate('/')}>
            <span className="sidebar-brand-name">Entropy Atlas</span>
            <span className="sidebar-brand-tag">Behavioral Intelligence</span>
          </div>
        )}
      </div>

      <nav className="sidebar-nav">
        {/* Core Intelligence */}
        <div>
          <SectionToggle label="Intelligence" isOpen={sections.intelligence} onToggle={() => toggle('intelligence')} isSidebarOpen={isSidebarOpen} />
          {(!isSidebarOpen || sections.intelligence) && (
            <div className="flex flex-col gap-1">
              <NavItem to="/" icon={LayoutDashboard} label="Command Center" isSidebarOpen={isSidebarOpen} />
              <NavItem to="/entities" icon={Users} label="Entity Registry" isSidebarOpen={isSidebarOpen} />
              <NavItem to="/entropy-explorer" icon={Atom} label="Entropy Analysis" isSidebarOpen={isSidebarOpen} />
              <NavItem to="/drift-attribution" icon={GitFork} label="Drift Attribution" isSidebarOpen={isSidebarOpen} />
              <NavItem to="/stability-timeline" icon={Clock} label="Stability History" isSidebarOpen={isSidebarOpen} />
            </div>
          )}
        </div>

        {/* Operations */}
        <div className="mt-4">
          <SectionToggle label="Operations" isOpen={sections.operations} onToggle={() => toggle('operations')} isSidebarOpen={isSidebarOpen} />
          {(!isSidebarOpen || sections.operations) && (
            <div className="flex flex-col gap-1">
              <NavItem to="/replay-center" icon={RefreshCcw} label="Replay Engine" isSidebarOpen={isSidebarOpen} />
              <NavItem to="/event-ingestion" icon={Send} label="Event Ingestion" isSidebarOpen={isSidebarOpen} />
            </div>
          )}
        </div>

        {/* Observability */}
        <div className="mt-4">
          <SectionToggle label="Observability" isOpen={sections.observability} onToggle={() => toggle('observability')} isSidebarOpen={isSidebarOpen} />
          {(!isSidebarOpen || sections.observability) && (
            <div className="flex flex-col gap-1">
              <NavItem to="/stream-analytics" icon={Zap} label="Stream Processing" isSidebarOpen={isSidebarOpen} />
              <NavItem to="/metrics-center" icon={BarChart2} label="Platform Metrics" isSidebarOpen={isSidebarOpen} />
            </div>
          )}
        </div>

        {/* System */}
        <div className="mt-4">
          <SectionToggle label="System" isOpen={sections.system} onToggle={() => toggle('system')} isSidebarOpen={isSidebarOpen} />
          {(!isSidebarOpen || sections.system) && (
            <div className="flex flex-col gap-1">
              <NavItem to="/system-health" icon={HeartPulse} label="Infrastructure" isSidebarOpen={isSidebarOpen} />
              <NavItem to="/architecture" icon={Cloud} label="Architecture" isSidebarOpen={isSidebarOpen} />
            </div>
          )}
        </div>
      </nav>

      <div className="sidebar-footer">
        <div className="sidebar-status" onClick={toggleSidebar}>
          {isSidebarOpen ? (
            <div className="flex items-center gap-2 justify-between w-full">
              <div className="flex items-center gap-2 cursor-pointer" onClick={(e) => { e.stopPropagation(); navigate('/system-health'); }}>
                <span className={`status-dot ${isHealthy ? 'stable' : 'critical'}`}></span>
                <span className="text-xs text-secondary">{isHealthy ? 'All Systems Operational' : 'Degraded'}</span>
              </div>
              <Menu className="w-4 h-4 text-muted cursor-pointer" />
            </div>
          ) : (
            <Menu className="w-4 h-4 text-muted cursor-pointer mx-auto" />
          )}
        </div>
      </div>
    </aside>
  );
};

const Layout = ({ children }) => {
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);
  const location = useLocation();

  const getPageMeta = (pathname) => {
    const routes = {
      '/': { title: 'Command Center', subtitle: 'Platform stability overview' },
      '/entities': { title: 'Entity Registry', subtitle: 'Behavioral entity directory' },
      '/entropy-explorer': { title: 'Entropy Analysis', subtitle: 'Multi-dimensional entropy decomposition' },
      '/drift-attribution': { title: 'Drift Attribution', subtitle: 'Root cause analysis' },
      '/stability-timeline': { title: 'Stability History', subtitle: 'Historical behavioral tracking' },
      '/replay-center': { title: 'Replay Engine', subtitle: 'Forensic state reconstruction' },
      '/stream-analytics': { title: 'Stream Processing', subtitle: 'Kafka pipeline intelligence' },
      '/metrics-center': { title: 'Platform Metrics', subtitle: 'Operational observability' },
      '/system-health': { title: 'Infrastructure', subtitle: 'System health and dependencies' },
      '/architecture': { title: 'Architecture', subtitle: 'System topology explorer' },
      '/event-ingestion': { title: 'Event Ingestion', subtitle: 'Transmit behavioral telemetry events' },
    };
    if (pathname.startsWith('/entities/')) return { title: 'Entity Investigation', subtitle: 'Behavioral profile analysis' };
    return routes[pathname] || { title: 'Entropy Atlas', subtitle: '' };
  };

  const { title, subtitle } = getPageMeta(location.pathname);

  return (
    <div className="app-layout">
      <Sidebar isSidebarOpen={isSidebarOpen} toggleSidebar={() => setIsSidebarOpen(!isSidebarOpen)} />
      <main className="main-content" style={{ marginLeft: isSidebarOpen ? 'var(--sidebar-width)' : 'var(--sidebar-collapsed)' }}>
        <header className="page-header">
          <div className="page-header-inner">
            <div className="page-title-section">
              <h1 className="page-title">{title}</h1>
              <p className="page-subtitle">{subtitle}</p>
            </div>
            <div className="page-actions">
              <div className="flex items-center gap-2 text-xs text-muted">
                <span className="status-dot stable"></span>
                <span className="font-mono">Live</span>
              </div>
            </div>
          </div>
        </header>
        <div className="page-content">
          {children}
        </div>
      </main>
    </div>
  );
};

export default Layout;
