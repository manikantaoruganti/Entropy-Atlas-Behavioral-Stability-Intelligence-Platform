import React from 'react';
import { ChevronDown, ChevronUp, Loader2, Database } from 'lucide-react';

const Table = ({
  data,
  columns,
  onRowClick,
  className = '',
  loading = false,
  emptyMessage = "No data available.",
  sortBy,
  sortDir,
  onSortChange,
}) => {
  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-text-secondary glass-card">
        <Loader2 className="animate-spin w-8 h-8 text-accent-cyan mb-4" />
        <span className="text-sm">Loading data streams...</span>
      </div>
    );
  }

  if (!data || data.length === 0) {
    return (
      <div className="empty-state glass-card">
        <div className="empty-state-icon">
          <Database className="w-8 h-8 opacity-50" />
        </div>
        <h3 className="empty-state-title">No Records Found</h3>
        <p className="empty-state-desc">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className={`overflow-x-auto glass-card ${className}`}>
      <table className="data-table">
        <thead>
          <tr>
            {columns.map((column, index) => (
              <th key={index}>
                {column.sortable && onSortChange ? (
                  <div
                    onClick={() => onSortChange(column.key)}
                    className="flex items-center cursor-pointer hover:text-text-primary transition-colors select-none"
                  >
                    {column.header}
                    {sortBy === column.key ? (
                      sortDir === 'asc' ? <ChevronUp className="w-3 h-3 ml-1 text-accent-cyan" /> : <ChevronDown className="w-3 h-3 ml-1 text-accent-cyan" />
                    ) : (
                      <ChevronDown className="w-3 h-3 ml-1 opacity-0 hover:opacity-100 transition-opacity" />
                    )}
                  </div>
                ) : (
                  column.header
                )}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((item, rowIndex) => (
            <tr
              key={rowIndex}
              className={onRowClick ? 'cursor-pointer' : ''}
              onClick={() => onRowClick && onRowClick(item)}
            >
              {columns.map((column, colIndex) => (
                <td key={colIndex}>
                  {column.render ? column.render(item) : item[column.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Table;
