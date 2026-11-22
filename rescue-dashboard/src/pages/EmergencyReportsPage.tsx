import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Search,
  Filter,
  AlertTriangle,
  Users,
  MapPin,
  Bot,
  Flame,
  Droplets,
  Building,
  HeartPulse,
  Eye,
  RefreshCw,
  Lock,
  Radio,
  CheckCircle2,
  Database
} from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { UrgencyBadge, StatusBadge } from '../components/StatusBadge';
import { IncidentDetailsModal } from '../components/IncidentDetailsModal';
import { formatDate, getIncidentTypeLabel } from '../utils/formatters';
import { EmergencyReport, IncidentType } from '../types';

export const EmergencyReportsPage: React.FC = () => {
  const {
    reports,
    isLoadingReports,
    reportsError,
    isReportsRlsDenied,
    isRealtimeConnected,
    isDemoMode,
    toggleDemoMode,
    refetchReports
  } = useIncidents();

  const [selectedReport, setSelectedReport] = useState<EmergencyReport | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [urgencyFilter, setUrgencyFilter] = useState<string>('ALL');
  const [typeFilter, setTypeFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const filteredReports = reports.filter((report) => {
    if (urgencyFilter !== 'ALL' && report.urgency !== urgencyFilter) return false;
    if (typeFilter !== 'ALL' && report.incidentType !== typeFilter) return false;
    if (statusFilter !== 'ALL' && report.status !== statusFilter) return false;
    if (searchTerm) {
      const query = searchTerm.toLowerCase();
      const matchDesc = report.description.toLowerCase().includes(query);
      const matchId = report.reportId.toLowerCase().includes(query);
      const matchReporter = report.reporterDeviceId.toLowerCase().includes(query);
      if (!matchDesc && !matchId && !matchReporter) return false;
    }
    return true;
  });

  const getIncidentIcon = (type: IncidentType) => {
    switch (type) {
      case 'FLOOD': return <Droplets className="w-4 h-4 text-cyan" />;
      case 'FIRE': return <Flame className="w-4 h-4 text-red-500" />;
      case 'BUILDING_COLLAPSE': return <Building className="w-4 h-4 text-amber-500" />;
      case 'MEDICAL_EMERGENCY': return <HeartPulse className="w-4 h-4 text-rose-500" />;
      default: return <AlertTriangle className="w-4 h-4 text-yellow-500" />;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-navy-800">
        <div>
          <h1 className="text-2xl font-extrabold text-white tracking-wide flex items-center gap-2">
            Emergency Incident Reports
            <span className="text-xs px-2.5 py-0.5 rounded-full bg-cyan/15 text-cyan border border-cyan/30 font-mono font-bold">
              {filteredReports.length} {isDemoMode ? 'Simulated' : 'Live'}
            </span>
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Connected to Supabase table: <span className="font-mono text-cyan">public.emergency_reports</span>
          </p>
        </div>

        <div className="flex items-center gap-3">
          {/* Realtime Status Indicator */}
          <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-navy-900 border border-navy-700 text-xs font-mono">
            <span className={`w-2 h-2 rounded-full ${isRealtimeConnected ? 'bg-emerald-400 animate-pulse' : 'bg-amber-400'}`} />
            <span className={isRealtimeConnected ? 'text-emerald-300' : 'text-amber-300'}>
              {isRealtimeConnected ? 'Realtime: Listening' : 'Connecting Realtime...'}
            </span>
          </div>

          <button
            onClick={() => refetchReports()}
            disabled={isLoadingReports}
            className="p-2 rounded-lg bg-navy-900 hover:bg-navy-800 border border-navy-700 text-slate-300 hover:text-white transition-colors"
            title="Refresh database records"
          >
            <RefreshCw className={`w-4 h-4 ${isLoadingReports ? 'animate-spin text-cyan' : ''}`} />
          </button>
        </div>
      </div>

      {/* RLS Security Error Notice */}
      {isReportsRlsDenied && (
        <div className="p-4 rounded-xl bg-amber-950/40 border border-amber-500/40 text-amber-200 text-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-lg">
          <div className="flex items-center gap-2.5">
            <Lock className="w-5 h-5 text-amber-400 shrink-0" />
            <div>
              <strong className="font-semibold text-amber-100 block">
                Row Level Security (RLS) Active
              </strong>
              <p className="text-amber-200/90 text-[11px] leading-snug">
                Anonymous read access is blocked to protect sensitive casualty coordinates. Please log in with an authorized rescue operator account to read live database rows.
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2 shrink-0">
            <Link
              to="/login"
              className="px-3 py-1.5 rounded-lg bg-cyan hover:bg-cyan-glow text-navy-950 font-bold text-xs transition-colors"
            >
              Sign In to Supabase
            </Link>
            <button
              onClick={toggleDemoMode}
              className="px-3 py-1.5 rounded-lg bg-navy-800 hover:bg-navy-700 text-slate-200 border border-navy-700 text-xs font-medium"
            >
              {isDemoMode ? 'Exit Demo Preview' : 'Preview Demo Data'}
            </button>
          </div>
        </div>
      )}

      {/* General Error Notice */}
      {reportsError && !isReportsRlsDenied && (
        <div className="p-4 rounded-xl bg-red-950/40 border border-red-500/40 text-red-200 text-xs flex items-center justify-between">
          <div className="flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-red-400" />
            <span>Database query error: {reportsError}</span>
          </div>
          <button
            onClick={() => refetchReports()}
            className="px-3 py-1 rounded bg-red-500/20 hover:bg-red-500/30 text-red-200 text-xs font-semibold"
          >
            Retry
          </button>
        </div>
      )}

      {/* Filter and Search Controls */}
      <div className="bg-navy-900 p-4 rounded-xl border border-navy-700 shadow-lg space-y-3">
        <div className="flex flex-col md:flex-row items-center gap-3">
          {/* Search bar */}
          <div className="relative flex-1 w-full">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search by keywords, ID, device, or symptoms..."
              className="w-full bg-navy-950 border border-navy-700 rounded-lg pl-9 pr-3 py-2 text-xs text-white focus:outline-none focus:border-cyan"
            />
          </div>

          {/* Quick Urgency filter */}
          <div className="flex items-center gap-2 w-full md:w-auto">
            <select
              value={urgencyFilter}
              onChange={(e) => setUrgencyFilter(e.target.value)}
              className="bg-navy-950 border border-navy-700 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-cyan flex-1 md:flex-initial"
            >
              <option value="ALL">All Urgencies</option>
              <option value="CRITICAL">Critical Only</option>
              <option value="HIGH">High Only</option>
              <option value="MEDIUM">Medium Only</option>
              <option value="LOW">Low Only</option>
            </select>

            <select
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
              className="bg-navy-950 border border-navy-700 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-cyan flex-1 md:flex-initial"
            >
              <option value="ALL">All Incident Types</option>
              <option value="FLOOD">Flood</option>
              <option value="BUILDING_COLLAPSE">Building Collapse</option>
              <option value="MEDICAL_EMERGENCY">Medical Emergency</option>
              <option value="LANDSLIDE">Landslide</option>
              <option value="FIRE">Structural Fire</option>
              <option value="OTHER">Other Hazards</option>
            </select>

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="bg-navy-950 border border-navy-700 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-cyan flex-1 md:flex-initial"
            >
              <option value="ALL">All Statuses</option>
              <option value="NEW">New Unassigned</option>
              <option value="ASSIGNED">Assigned</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="RESOLVED">Resolved</option>
            </select>
          </div>
        </div>
      </div>

      {/* Reports Table / State Handlers */}
      <div className="bg-navy-900 rounded-xl border border-navy-700 overflow-hidden shadow-xl">
        {/* Loading State Skeleton */}
        {isLoadingReports ? (
          <div className="p-8 space-y-4">
            <div className="flex items-center justify-center gap-2 text-cyan text-xs font-mono">
              <RefreshCw className="w-4 h-4 animate-spin" />
              <span>Querying public.emergency_reports from Supabase...</span>
            </div>
            <div className="space-y-3 pt-2">
              {[1, 2, 3].map((i) => (
                <div key={i} className="h-12 bg-navy-950/60 rounded-lg border border-navy-800 animate-pulse" />
              ))}
            </div>
          </div>
        ) : filteredReports.length === 0 ? (
          /* Empty State */
          <div className="py-12 px-4 text-center space-y-3">
            <div className="w-12 h-12 rounded-2xl bg-navy-950 border border-navy-800 flex items-center justify-center mx-auto text-cyan">
              <Database className="w-6 h-6" />
            </div>
            <div className="space-y-1">
              <h3 className="text-sm font-bold text-white">No Emergency Reports Recorded</h3>
              <p className="text-xs text-slate-400 max-w-md mx-auto leading-relaxed">
                Table <span className="font-mono text-cyan">public.emergency_reports</span> currently contains 0 records. As field devices sync offline disaster reports or as reports are received, they will appear here in real time.
              </p>
            </div>
            <div className="pt-2">
              <button
                onClick={toggleDemoMode}
                className="px-3.5 py-1.5 rounded-lg bg-navy-800 hover:bg-navy-700 text-cyan text-xs font-semibold border border-navy-700"
              >
                {isDemoMode ? 'Disable Demo Data' : 'Preview with Simulated Mock Data'}
              </button>
            </div>
          </div>
        ) : (
          /* Populated Table */
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-navy-950/80 border-b border-navy-700 text-slate-400 uppercase tracking-wider font-semibold">
                <tr>
                  <th className="py-3 px-4">Incident & ID</th>
                  <th className="py-3 px-4">Urgency</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Description</th>
                  <th className="py-3 px-4">Victims</th>
                  <th className="py-3 px-4">Reported Time</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-800">
                {filteredReports.map((report) => (
                  <tr
                    key={report.reportId}
                    className="hover:bg-navy-800/40 transition-colors cursor-pointer group"
                    onClick={() => setSelectedReport(report)}
                  >
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-2">
                        {getIncidentIcon(report.incidentType)}
                        <div>
                          <div className="font-bold text-white group-hover:text-cyan transition-colors">
                            {getIncidentTypeLabel(report.incidentType)}
                          </div>
                          <div className="text-[10px] text-slate-400 font-mono">
                            {report.reportId}
                          </div>
                        </div>
                      </div>
                    </td>

                    <td className="py-3.5 px-4">
                      <UrgencyBadge urgency={report.urgency} />
                    </td>

                    <td className="py-3.5 px-4">
                      <StatusBadge status={report.status} />
                    </td>

                    <td className="py-3.5 px-4 max-w-xs">
                      <p className="text-slate-300 truncate">{report.description}</p>
                      {report.aiSummary && (
                        <p className="text-[10px] text-cyan truncate flex items-center gap-1 mt-0.5">
                          <Bot className="w-2.5 h-2.5 shrink-0" />
                          {report.aiSummary}
                        </p>
                      )}
                    </td>

                    <td className="py-3.5 px-4">
                      <span className="font-mono font-bold text-slate-200">
                        {report.peopleAffected > 0 ? (
                          <span className="text-red-400 font-bold">{report.peopleAffected}</span>
                        ) : (
                          '0'
                        )}
                      </span>
                    </td>

                    <td className="py-3.5 px-4 font-mono text-slate-400 whitespace-nowrap">
                      {formatDate(report.createdAt)}
                    </td>

                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedReport(report);
                        }}
                        className="p-1.5 rounded-lg bg-navy-800 hover:bg-cyan hover:text-navy-950 text-slate-300 border border-navy-700 transition-colors inline-flex items-center gap-1 font-semibold text-[11px]"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        <span>Inspect</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Details Modal */}
      <IncidentDetailsModal report={selectedReport} onClose={() => setSelectedReport(null)} />
    </div>
  );
};
