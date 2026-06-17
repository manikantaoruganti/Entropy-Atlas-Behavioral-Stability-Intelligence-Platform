export const ENTITY_TYPES = [
  'USER',
  'SERVICE',
  'DEVICE',
  'MERCHANT',
  'REGION',
  'API_CLIENT',
  'DATACENTER',
  'PARTNER',
];

export const BEHAVIOR_DIMENSIONS = [
  'Timing Entropy',
  'Location Entropy',
  'Resource Affinity Drift',
  'Interaction Diversity',
  'Velocity Drift',
  'Temporal Irregularity',
];

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
export const ADMIN_API_BASE_URL = import.meta.env.VITE_ADMIN_API_BASE_URL || 'http://localhost:8080/admin';
