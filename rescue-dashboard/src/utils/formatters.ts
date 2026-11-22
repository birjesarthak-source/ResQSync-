import { UrgencyLevel, ReportStatus, IncidentType } from '../types';

export const formatDate = (timestamp: number): string => {
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).format(new Date(timestamp));
};

export const formatRelativeTime = (timestamp: number): string => {
  const seconds = Math.floor((Date.now() - timestamp) / 1000);
  if (seconds < 60) return `${Math.max(1, seconds)}s ago`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
};

export const getUrgencyColor = (urgency: UrgencyLevel) => {
  switch (urgency) {
    case 'CRITICAL':
      return {
        bg: 'bg-red-500/10',
        border: 'border-red-500/40',
        text: 'text-red-400',
        badge: 'bg-red-500 text-white',
        dot: 'bg-red-500'
      };
    case 'HIGH':
      return {
        bg: 'bg-orange-500/10',
        border: 'border-orange-500/40',
        text: 'text-orange-400',
        badge: 'bg-orange-500 text-white',
        dot: 'bg-orange-500'
      };
    case 'MEDIUM':
      return {
        bg: 'bg-yellow-500/10',
        border: 'border-yellow-500/40',
        text: 'text-yellow-400',
        badge: 'bg-yellow-500 text-black',
        dot: 'bg-yellow-500'
      };
    case 'LOW':
    default:
      return {
        bg: 'bg-blue-500/10',
        border: 'border-blue-500/40',
        text: 'text-blue-400',
        badge: 'bg-blue-500 text-white',
        dot: 'bg-blue-500'
      };
  }
};

export const getStatusColor = (status: ReportStatus) => {
  switch (status) {
    case 'NEW':
      return {
        badge: 'bg-red-500/20 text-red-300 border border-red-500/30',
        label: 'New Unassigned'
      };
    case 'ASSIGNED':
      return {
        badge: 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/30',
        label: 'Team Dispatched'
      };
    case 'IN_PROGRESS':
      return {
        badge: 'bg-amber-500/20 text-amber-300 border border-amber-500/30',
        label: 'Rescue In Progress'
      };
    case 'RESOLVED':
      return {
        badge: 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30',
        label: 'Safely Resolved'
      };
  }
};

export const getIncidentTypeLabel = (type: IncidentType): string => {
  switch (type) {
    case 'FLOOD': return 'Flood Emergency';
    case 'FIRE': return 'Structural Fire';
    case 'EARTHQUAKE': return 'Seismic Tremor';
    case 'LANDSLIDE': return 'Landslide Blockage';
    case 'BUILDING_COLLAPSE': return 'Building Collapse';
    case 'MEDICAL_EMERGENCY': return 'Critical Medical';
    case 'OTHER': default: return 'Emergency Incident';
  }
};
