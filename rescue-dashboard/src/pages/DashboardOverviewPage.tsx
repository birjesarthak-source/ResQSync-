import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  AlertTriangle,
  Users,
  Radio,
  CheckCircle2,
  Bot,
  MapPin,
  ArrowRight,
  ShieldAlert,
  Flame,
  Activity
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
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-navy-800">
        <div>
          <h1 className="text-2xl font-extrabold text-white tracking-wide flex items-center gap-2">
            Disaster Command Overview
            <span className="w-2.5 h-2.5 rounded-full bg-cyan animate-pulse" />
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Real-time emergency aggregation from offline smartphone mesh networks
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link
            to="/reports"
            className="px-3.5 py-1.5 rounded-lg bg-cyan hover:bg-cyan-glow text-navy-950 font-bold text-xs flex items-center gap-1.5 shadow-sm shadow-cyan/20 transition-colors"
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
          variant="cyan"
          trend={{ label: '+2 past hour', positive: false }}
        />
        <MetricCard
          title="Critical Alarms"
          value={criticalCount}
          subtitle="Immediate dispatch needed"
          icon={ShieldAlert}
          variant="red"
          trend={{ label: 'High priority', positive: false }}
        />
        <MetricCard
          title="Victims At Risk"
          value={totalVictims}
          subtitle="Across active zones"
          icon={Users}
          variant="amber"
          trend={{ label: 'Reported in field' }}
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
        {/* Left 2 Columns: Urgent Action Queue & AI Incident Digest */}
        <div className="lg:col-span-2 space-y-6">
          {/* Urgent Incident Queue */}
          <div className="bg-navy-900 rounded-xl border border-navy-700 overflow-hidden shadow-xl">
            <div className="p-4 border-b border-navy-700 bg-navy-950/60 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Flame className="w-4 h-4 text-red-400" />
                <h2 className="text-sm font-bold text-white uppercase tracking-wider">
                  Priority Emergency Dispatch Queue
                </h2>
              </div>
              <span className="text-xs text-slate-400 font-mono">
                Showing {Math.min(4, activeIncidents.length)} of {activeIncidents.length}
              </span>
            </div>

            <div className="divide-y divide-navy-800">
              {activeIncidents.length === 0 ? (
                <div className="p-8 text-center text-slate-400 space-y-1.5">
                  <p className="text-xs font-semibold text-white">No Active Emergency Reports</p>
                  <p className="text-[11px] text-slate-400 max-w-sm mx-auto leading-relaxed">
                    Table public.emergency_reports contains 0 active records. Realtime listener is streaming.
                  </p>
                </div>
              ) : (
                activeIncidents.slice(0, 4).map((report) => (
                  <div
                    key={report.reportId}
                    className="p-4 hover:bg-navy-800/40 transition-colors flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                  >
                    <div className="space-y-1.5 flex-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="text-sm font-bold text-white">
                          {getIncidentTypeLabel(report.incidentType)}
                        </span>
                        <UrgencyBadge urgency={report.urgency} />
                        <StatusBadge status={report.status} />
                      </div>

                      <p className="text-xs text-slate-300 line-clamp-2 leading-relaxed">
                        {report.description}
                      </p>

                      <div className="flex flex-wrap items-center gap-4 text-[11px] text-slate-400 font-mono pt-1">
                        <span className="flex items-center gap-1">
                          <Users className="w-3 h-3 text-red-400" />
                          {report.peopleAffected} trapped
                        </span>
                        <span className="flex items-center gap-1">
                          <MapPin className="w-3 h-3 text-cyan" />
                          {report.latitude.toFixed(3)}, {report.longitude.toFixed(3)}
                        </span>
                        <span>{formatDate(report.createdAt)}</span>
                      </div>
                    </div>

                    <button
                      onClick={() => setModalReport(report)}
                      className="px-3 py-1.5 rounded-lg bg-navy-800 hover:bg-navy-700 border border-navy-600 text-xs font-semibold text-cyan hover:text-cyan-glow shrink-0 transition-colors"
                    >
                      Dossier & Dispatch
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Gemini AI Emergency Digest Banner */}
          <div className="p-5 rounded-xl bg-gradient-to-r from-navy-900 via-navy-850 to-navy-900 border border-cyan/40 shadow-xl relative overflow-hidden">
            <div className="flex items-start gap-4">
              <div className="p-3 rounded-xl bg-cyan/15 border border-cyan/30 text-cyan shrink-0">
                <Bot className="w-6 h-6" />
              </div>
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <h3 className="text-sm font-bold text-white tracking-wide">
                    GEMINI AI SITUATIONAL INTELLIGENCE DIGEST
                  </h3>
                  <span className="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-cyan/20 text-cyan border border-cyan/30">
                    EDGE HYBRID EVALUATION
                  </span>
                </div>
                <p className="text-xs text-slate-200 leading-relaxed">
                  Multiple critical reports concentrated around Sector 4 (Riverside Market) indicate water levels exceeding 4.5 feet with rising flood current. Building collapse in Sector 2 requires hydraulic shoring. Recommend prioritizing boat deployment to Market Square while re-routing logistics past hillside road blockage.
                </p>
                <div className="pt-1 flex flex-wrap gap-2 text-[11px]">
                  <span className="px-2 py-0.5 rounded bg-red-500/20 text-red-300 border border-red-500/30">
                    Primary: Water Extraction (Sector 4)
                  </span>
                  <span className="px-2 py-0.5 rounded bg-orange-500/20 text-orange-300 border border-orange-500/30">
                    Secondary: Structural Shoring (Sector 2)
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right 1 Column: Mini Tactical Map & Dispatch Activity */}
        <div className="space-y-6">
          {/* Mini Incident Map Preview */}
          <div className="bg-navy-900 rounded-xl border border-navy-700 p-4 shadow-xl">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-xs font-bold text-white uppercase tracking-wider flex items-center gap-2">
                <MapPin className="w-4 h-4 text-cyan" />
                Live Incident Map
              </h2>
              <Link to="/map" className="text-xs text-cyan hover:underline font-semibold">
                Interactive View →
              </Link>
            </div>

            {/* Tactical Grid Visualization Box */}
            <div className="h-48 rounded-lg bg-navy-950 border border-navy-800 relative overflow-hidden flex items-center justify-center p-3">
              {/* Radar Grid Lines */}
              <div className="absolute inset-0 bg-[radial-gradient(#00c5d2_1px,transparent_1px)] [background-size:16px_16px] opacity-20" />
              <div className="absolute w-32 h-32 rounded-full border border-cyan/20 animate-ping opacity-25" />
              <div className="absolute w-44 h-44 rounded-full border border-cyan/30" />

              {/* Sample Plotted Pins */}
              {reports.slice(0, 4).map((r, idx) => (
                <div
                  key={r.reportId}
                  className="absolute cursor-pointer group"
                  style={{
                    top: `${25 + idx * 18}%`,
                    left: `${20 + idx * 20}%`
                  }}
                  onClick={() => setModalReport(r)}
                >
                  <div className={`w-3.5 h-3.5 rounded-full flex items-center justify-center ${
                    r.urgency === 'CRITICAL' ? 'bg-red-500 animate-pulse' : 'bg-cyan'
                  } shadow-md shadow-black`}>
                    <span className="w-1.5 h-1.5 rounded-full bg-white" />
                  </div>
                  <div className="absolute bottom-full mb-1 left-1/2 -translate-x-1/2 px-1.5 py-0.5 rounded bg-black/90 text-[10px] text-white whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity">
                    {r.incidentType}
                  </div>
                </div>
              ))}

              <div className="absolute bottom-2 left-2 text-[10px] font-mono text-slate-400 bg-navy-900/80 px-2 py-0.5 rounded border border-navy-700">
                Center: 18.5204° N, 73.8567° E
              </div>
            </div>
          </div>

          {/* Recent Radio Logs Feed */}
          <div className="bg-navy-900 rounded-xl border border-navy-700 p-4 shadow-xl">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-xs font-bold text-white uppercase tracking-wider flex items-center gap-2">
                <Radio className="w-4 h-4 text-emerald-400" />
                Recent Field Dispatches
              </h2>
              <Link to="/updates" className="text-xs text-cyan hover:underline font-semibold">
                View All →
              </Link>
            </div>

            <div className="space-y-3">
              {updates.slice(0, 3).map((upd) => (
                <div key={upd.updateId} className="p-2.5 rounded-lg bg-navy-950 border border-navy-800 text-xs">
                  <div className="flex items-center justify-between mb-1">
                    <span className="font-bold text-cyan text-[11px]">{upd.teamName}</span>
                    <span className="text-[10px] text-slate-400">{formatDate(upd.timestamp)}</span>
                  </div>
                  <p className="text-slate-300 leading-snug">{upd.message}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Incident Details Modal */}
      <IncidentDetailsModal report={modalReport} onClose={() => setModalReport(null)} />
    </div>
  );
};
