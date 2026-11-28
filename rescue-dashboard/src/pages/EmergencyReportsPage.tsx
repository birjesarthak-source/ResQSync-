import React, { useState } from 'react';
import {
  Search,
  AlertTriangle,
  MapPin,
  Flame,
  Droplets,
  Building,
  HeartPulse,
  RefreshCw,
  Eye,
  Volume2
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
    isDemoMode,
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
      case 'FLOOD': return <Droplets className="w-4 h-4 text-blue-600" />;
      case 'FIRE': return <Flame className="w-4 h-4 text-red-600" />;
      case 'BUILDING_COLLAPSE': return <Building className="w-4 h-4 text-amber-600" />;
      case 'MEDICAL_EMERGENCY': return <HeartPulse className="w-4 h-4 text-rose-600" />;
      default: return <AlertTriangle className="w-4 h-4 text-yellow-600" />;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-800 tracking-wide flex items-center gap-2">
            Emergency Incident Reports
            <span className="text-xs px-2.5 py-0.5 rounded-full bg-blue-50 text-blue-600 border border-blue-200 font-mono font-bold">
              {filteredReports.length} {isDemoMode ? 'Simulated' : 'Live'}
            </span>
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Synchronized emergency records from offline mesh gateways & mobile devices
          </p>
        </div>

        <button
          onClick={() => refetchReports()}
          disabled={isLoadingReports}
          className="px-3.5 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs flex items-center gap-2 shadow-xs transition-colors self-start sm:self-auto"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoadingReports ? 'animate-spin' : ''}`} />
          <span>Refresh Database Feed</span>
        </button>
      </div>

      {/* Filters Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-col md:flex-row gap-3">
        {/* Search */}
        <div className="flex-1 relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search report ID, description, or device..."
            className="w-full bg-slate-50 border border-slate-200 rounded-lg pl-9 pr-3 py-2 text-xs text-slate-800 placeholder-slate-400 focus:outline-none focus:border-blue-600"
          />
        </div>

        {/* Urgency Filter */}
        <select
          value={urgencyFilter}
          onChange={(e) => setUrgencyFilter(e.target.value)}
          className="bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-xs text-slate-700 focus:outline-none focus:border-blue-600"
        >
          <option value="ALL">All Urgency Levels</option>
          <option value="CRITICAL">Critical Only</option>
          <option value="HIGH">High Only</option>
          <option value="MEDIUM">Medium Only</option>
          <option value="LOW">Low Only</option>
        </select>

        {/* Type Filter */}
        <select
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value)}
          className="bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-xs text-slate-700 focus:outline-none focus:border-blue-600"
        >
          <option value="ALL">All Incident Types</option>
          <option value="FLOOD">Flood</option>
          <option value="FIRE">Fire</option>
          <option value="EARTHQUAKE">Earthquake</option>
          <option value="LANDSLIDE">Landslide</option>
          <option value="BUILDING_COLLAPSE">Building Collapse</option>
          <option value="MEDICAL_EMERGENCY">Medical Emergency</option>
        </select>

        {/* Status Filter */}
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-xs text-slate-700 focus:outline-none focus:border-blue-600"
        >
          <option value="ALL">All Report Statuses</option>
          <option value="NEW">New / Unassigned</option>
          <option value="ASSIGNED">Unit Dispatched</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="RESOLVED">Resolved</option>
        </select>
      </div>

      {/* Reports Table / List */}
      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-xs">
        {reportsError && (
          <div className="p-4 bg-red-50 border-b border-red-200 text-xs text-red-700">
            <strong>Error loading reports:</strong> {reportsError}
          </div>
        )}

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-[11px] font-bold uppercase tracking-wider text-slate-500">
                <th className="p-3.5">Type & ID</th>
                <th className="p-3.5">Situation Description</th>
                <th className="p-3.5">Urgency</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5">GPS Location</th>
                <th className="p-3.5">Victims</th>
                <th className="p-3.5">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-xs">
              {filteredReports.length === 0 ? (
                <tr>
                  <td colSpan={7} className="p-8 text-center text-slate-400">
                    No emergency reports match the selected filters.
                  </td>
                </tr>
              ) : (
                filteredReports.map((report) => (
                  <tr key={report.reportId} className="hover:bg-slate-50/80 transition-colors">
                    <td className="p-3.5 font-medium text-slate-800">
                      <div className="flex items-center gap-2">
                        {getIncidentIcon(report.incidentType)}
                        <span>{getIncidentTypeLabel(report.incidentType)}</span>
                        {report.voiceBase64 && (
                          <span className="px-1.5 py-0.5 rounded bg-blue-100 text-blue-700 text-[10px] font-bold flex items-center gap-0.5" title="Voice SOS Memo Attached">
                            <Volume2 className="w-3 h-3 text-blue-600" />
                            <span>Audio</span>
                          </span>
                        )}
                      </div>
                      <span className="text-[10px] text-slate-400 font-mono block mt-0.5">{report.reportId}</span>
                    </td>

                    <td className="p-3.5 max-w-xs text-slate-700">
                      <p className="line-clamp-2">{report.description}</p>
                      {report.aiSummary && (
                        <p className="text-[10px] text-blue-600 font-medium mt-1">🤖 {report.aiSummary}</p>
                      )}
                    </td>

                    <td className="p-3.5">
                      <UrgencyBadge urgency={report.urgency} />
                    </td>

                    <td className="p-3.5">
                      <StatusBadge status={report.status} />
                    </td>

                    <td className="p-3.5 font-mono text-[11px] text-slate-600">
                      <div className="flex items-center gap-1">
                        <MapPin className="w-3 h-3 text-blue-600 shrink-0" />
                        <span>
                          {report.latitude !== null ? report.latitude.toFixed(4) : 'N/A'}, {report.longitude !== null ? report.longitude.toFixed(4) : 'N/A'}
                        </span>
                      </div>
                    </td>

                    <td className="p-3.5 font-mono font-bold text-red-600">
                      {report.peopleAffected}
                    </td>

                    <td className="p-3.5">
                      <button
                        onClick={() => setSelectedReport(report)}
                        className="px-2.5 py-1 rounded bg-blue-50 hover:bg-blue-100 text-blue-600 border border-blue-200 font-semibold text-[11px] flex items-center gap-1 transition-colors"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        <span>Details</span>
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {selectedReport && (
        <IncidentDetailsModal
          report={selectedReport}
          onClose={() => setSelectedReport(null)}
        />
      )}
    </div>
  );
};
