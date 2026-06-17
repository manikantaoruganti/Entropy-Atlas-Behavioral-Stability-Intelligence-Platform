export const formatTimestamp = (timestamp) => {
  if (!timestamp) return 'N/A';
  const date = new Date(timestamp);
  return date.toLocaleString();
};

export const getStatusColorClass = (status) => {
  if (!status) return 'text-text-muted';
  switch (status.toUpperCase()) {
    case 'UP':
    case 'COMPLETED':
    case 'STABLE':
    case 'DECREASING':
      return 'text-accent-green';
    case 'WARNING':
    case 'PENDING':
      return 'text-accent-amber';
    case 'DOWN':
    case 'FAILED':
    case 'INCREASING':
      return 'text-accent-red';
    default:
      return 'text-text-secondary';
  }
};

export const getStatusBgClass = (status) => {
  if (!status) return 'bg-bg-surface';
  switch (status.toUpperCase()) {
    case 'UP':
    case 'COMPLETED':
    case 'STABLE':
    case 'DECREASING':
      return 'badge-green';
    case 'WARNING':
    case 'PENDING':
      return 'badge-amber';
    case 'DOWN':
    case 'FAILED':
    case 'INCREASING':
      return 'badge-red';
    default:
      return 'badge';
  }
};
