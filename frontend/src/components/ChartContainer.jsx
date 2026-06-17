import React from 'react';
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, AreaChart, Area, BarChart, Bar, Cell } from 'recharts';

const ChartContainer = ({ title, description, children, className = '', animate = true, delay = 0 }) => {
  const animationClass = animate ? 'animate-slide-up' : '';
  const style = animate && delay ? { animationDelay: `${delay}s` } : {};

  return (
    <div className={`glass-card ${animationClass} ${className}`} style={style}>
      <div className="glass-card-header">
        <div>
          <h3 className="glass-card-title">{title}</h3>
          {description && <p className="text-xs text-text-muted mt-1">{description}</p>}
        </div>
      </div>
      <div className="glass-card-body p-4">
        <div className="h-72 w-full">
          {children}
        </div>
      </div>
    </div>
  );
};

export default ChartContainer;

// Re-export Recharts components
export { ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, AreaChart, Area, BarChart, Bar, Cell };
