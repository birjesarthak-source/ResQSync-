import React from 'react';
import { UrgencyLevel, ReportStatus } from '../types';
import { getUrgencyColor, getStatusColor } from '../utils/formatters';

export const UrgencyBadge: React.FC<{ urgency: UrgencyLevel }> = ({ urgency }) => {
  const color = getUrgencyColor(urgency);
  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-bold uppercase tracking-wider ${color.badge}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${urgency === 'CRITICAL' ? 'animate-ping' : ''} bg-current`} />
      {urgency}
    </span>
  );
};

export const StatusBadge: React.FC<{ status: ReportStatus }> = ({ status }) => {
  const config = getStatusColor(status);
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold tracking-wide ${config.badge}`}>
      {config.label}
    </span>
  );
};
