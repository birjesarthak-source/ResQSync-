import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  AlertTriangle,
  Users,
  Radio,
  ShieldAlert,
  Flame,
  ArrowRight,
  MapPin
} from 'lucide-react';
import { MetricCard } from '../components/MetricCard';
import { UrgencyBadge, StatusBadge } from '../components/StatusBadge';
import { IncidentDetailsModal } from '../components/IncidentDetailsModal';
import { useIncidents } from '../context/IncidentContext';
import { formatDate, getIncidentTypeLabel } from '../utils/formatters';
import { EmergencyReport } from '../types';

export const DashboardOverviewPage: React.FC = () => {
  const { reports, updates, nodes, teams } = useIncidents();
  const [modalReport, setModalReport] = useState<EmergencyReport | null>(null);

  const activeIncidents = reports.filter((r) => r.status !== 'RESOLVED');
  const criticalCount = reports.filter((r) => r.urgency === 'CRITICAL' && r.status !== 'RESOLVED').length;
  const totalVictims = reports.reduce((acc, r) => acc + (r.status !== 'RESOLVED' ? r.peopleAffected : 0), 0);
  const activeTeams = teams.filter((t) => t.status === 'DISPATCHED' || t.status === 'ON_SITE').length;

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-800 tracking-wide flex items-center gap-2">
            Disaster Command Overview
            <span className="w-2.5 h-2.5 rounded-full bg-blue-600 animate-pulse" />
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Real-time emergency report aggregation from offline smartphone mesh networks
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link
            to="/reports"
            className="px-3.5 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs flex items-center gap-1.5 shadow-xs transition-colors"
          >
            <span>All Emergency Reports</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>
      </div>

      {/* Key Operational Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Active Emergencies"
          value={activeIncidents.length}
          subtitle="Awaiting resolution"
          icon={AlertTriangle}
          variant="blue"
          trend={{ label: 'Active in field' }}
        />
        <MetricCard
          title="Critical Alarms"
          value={criticalCount}
          subtitle="Immediate dispatch needed"
          icon={ShieldAlert}
          variant="red"
          trend={{ label: 'High priority' }}
        />
        <MetricCard
          title="Victims At Risk"
          value={totalVictims}
          subtitle="Across active zones"
          icon={Users}
          variant="amber"
          trend={{ label: 'Reported count' }}
        />
        <MetricCard
          title="Field Teams Deployed"
          value={`${activeTeams} / ${teams.length}`}
          subtitle="Operational units active"
          icon={Radio}
          variant="green"
          trend={{ label: 'Units responding', positive: true }}
        />
      </div>

      {/* Main Two-Column Tactical Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Columns: Urgent Action Queue */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-xs">
            <div className="p-4 border-b border-slate-200 bg-slate-50/80 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Flame className="w-4 h-4 text-red-600" />
                <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wider">Urgent Incident Queue</h2>
              </div>
              <span className="text-xs text-slate-500 font-mono">Sorted by urgency & timestamp</span>
            </div>

            <div className="divide-y divide-slate-100">
              {reports.length === 0 ? (
                <div className="p-8 text-center text-slate-400 text-xs">
                  No active emergency reports in queue.
                </div>
              ) : (
                reports.slice(0, 5).map((report) => (
                  <div
                    key={report.reportId}
                    onClick={() => setModalReport(report)}
                    className="p-4 hover:bg-slate-50 transition-colors cursor-pointer flex flex-col sm:flex-row sm:items-center justify-between gap-3"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <UrgencyBadge urgency={report.urgency} />
                        <span className="font-bold text-slate-800 text-sm">{getIncidentTypeLabel(report.incidentType)}</span>
                        <StatusBadge status={report.status} />
                      </div>
                      <p className="text-xs text-slate-600 line-clamp-2">{report.description}</p>
                      {report.aiSummary && (
                        <p className="text-[11px] text-blue-600 font-medium">🤖 AI: {report.aiSummary}</p>
                      )}
                    </div>

                    <div className="text-right shrink-0 text-xs text-slate-500 font-mono space-y-0.5">
                      <div className="flex items-center justify-end gap-1 text-slate-700">
                        <MapPin className="w-3 h-3 text-blue-600" />
                        <span>{report.latitude.toFixed(4)}, {report.longitude.toFixed(4)}</span>
                      </div>
                      <div>{formatDate(report.createdAt)}</div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        {/* Right 1 Column: Recent Broadcast Updates */}
        <div className="space-y-6">
          <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-xs p-4">
            <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wider mb-3 pb-2 border-b border-slate-100 flex items-center justify-between">
              <span>Rescue Updates</span>
              <Radio className="w-4 h-4 text-blue-600" />
            </h2>

            <div className="space-y-3">
              {updates.length === 0 ? (
                <p className="text-xs text-slate-400 text-center py-4">No broadcast updates available.</p>
              ) : (
                updates.slice(0, 4).map((upd) => (
                  <div key={upd.updateId} className="p-3 bg-slate-50 rounded-lg border border-slate-200 text-xs space-y-1">
                    <div className="flex justify-between items-center font-bold text-slate-800">
                      <span>{upd.teamName}</span>
                      <span className="text-[10px] text-slate-400 font-mono">{formatDate(upd.timestamp)}</span>
                    </div>
                    <p className="text-slate-600 leading-snug">{upd.message}</p>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Modal for detailed report view */}
      {modalReport && (
        <IncidentDetailsModal
          report={modalReport}
          onClose={() => setModalReport(null)}
        />
      )}
    </div>
  );
};
