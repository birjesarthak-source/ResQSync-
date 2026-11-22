import React, { useState } from 'react';
import { X, MapPin, Users, Bot, Radio, CheckCircle, ShieldAlert } from 'lucide-react';
import { EmergencyReport, ReportStatus } from '../types';
import { UrgencyBadge, StatusBadge } from './StatusBadge';
import { formatDate, getIncidentTypeLabel } from '../utils/formatters';
import { useIncidents } from '../context/IncidentContext';

interface IncidentDetailsModalProps {
  report: EmergencyReport | null;
  onClose: () => void;
}

export const IncidentDetailsModal: React.FC<IncidentDetailsModalProps> = ({ report, onClose }) => {
  const { teams, updateReportStatus, assignTeamToReport } = useIncidents();
  const [selectedTeamId, setSelectedTeamId] = useState<string>('');

  if (!report) return null;

  const handleStatusChange = (status: ReportStatus) => {
    updateReportStatus(report.reportId, status);
  };

  const handleAssignTeam = () => {
    if (!selectedTeamId) return;
    assignTeamToReport(report.reportId, selectedTeamId);
    setSelectedTeamId('');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-navy-900 border border-navy-700 w-full max-w-2xl rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="p-5 border-b border-navy-700 bg-navy-950/80 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-red-500/10 border border-red-500/30 flex items-center justify-center text-red-400">
              <ShieldAlert className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-lg font-bold text-white tracking-wide">
                  {getIncidentTypeLabel(report.incidentType)}
                </h3>
                <UrgencyBadge urgency={report.urgency} />
                <StatusBadge status={report.status} />
              </div>
              <span className="text-xs text-slate-400 font-mono">
                ID: {report.reportId} • Logged: {formatDate(report.createdAt)}
              </span>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-2 rounded-lg text-slate-400 hover:text-white hover:bg-navy-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 space-y-6 overflow-y-auto">
          {/* Situation Description */}
          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-cyan mb-2">
              Citizen Field Dispatch Description
            </h4>
            <div className="p-4 rounded-xl bg-navy-950 border border-navy-800 text-slate-200 text-sm leading-relaxed">
              {report.description}
            </div>
          </div>

          {/* Quick Metrics Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div className="p-3 rounded-lg bg-navy-800/60 border border-navy-700 flex items-center gap-3">
              <MapPin className="w-5 h-5 text-cyan shrink-0" />
              <div className="overflow-hidden">
                <span className="text-[11px] text-slate-400 uppercase font-semibold block">Coordinates</span>
                <span className="text-xs font-mono font-bold text-slate-200 truncate block">
                  {report.latitude.toFixed(4)}, {report.longitude.toFixed(4)}
                </span>
              </div>
            </div>

            <div className="p-3 rounded-lg bg-navy-800/60 border border-navy-700 flex items-center gap-3">
              <Users className="w-5 h-5 text-red-400 shrink-0" />
              <div>
                <span className="text-[11px] text-slate-400 uppercase font-semibold block">Victims At Risk</span>
                <span className="text-xs font-mono font-bold text-red-400">
                  {report.peopleAffected} Persons
                </span>
              </div>
            </div>

            <div className="p-3 rounded-lg bg-navy-800/60 border border-navy-700 flex items-center gap-3">
              <Radio className="w-5 h-5 text-emerald-400 shrink-0" />
              <div className="overflow-hidden">
                <span className="text-[11px] text-slate-400 uppercase font-semibold block">Reporter Node</span>
                <span className="text-xs font-mono font-bold text-emerald-300 truncate block">
                  {report.reporterDeviceId}
                </span>
              </div>
            </div>
          </div>

          {/* Gemini AI Edge Triage Analysis */}
          <div className="p-4 rounded-xl bg-gradient-to-br from-cyan/10 to-navy-950 border border-cyan/30">
            <div className="flex items-center gap-2 mb-2 text-cyan font-semibold text-xs tracking-wide">
              <Bot className="w-4 h-4 text-cyan" />
              <span>GEMINI AI TRIAGE DIGEST (ON-DEVICE HYBRID ANALYSIS)</span>
            </div>
            <p className="text-xs text-slate-200 mb-3 leading-relaxed">
              {report.aiSummary}
            </p>
            {report.aiActions && report.aiActions.length > 0 && (
              <div>
                <span className="text-[11px] font-semibold text-slate-400 uppercase block mb-1.5">
                  Recommended Dispatch Protocols:
                </span>
                <ul className="space-y-1">
                  {report.aiActions.map((action, i) => (
                    <li key={i} className="text-xs text-slate-300 flex items-start gap-2">
                      <span className="text-cyan font-bold">•</span>
                      <span>{action}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>

          {/* Assigned Team Info & Reassignment */}
          <div className="p-4 rounded-xl bg-navy-950 border border-navy-800">
            <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
              Assigned Field Rescue Team
            </h4>
            {report.assignedTeamName ? (
              <div className="flex items-center justify-between">
                <div>
                  <span className="text-sm font-bold text-emerald-400">{report.assignedTeamName}</span>
                  <span className="text-xs text-slate-400 block">Unit status actively working sector</span>
                </div>
                <button
                  onClick={() => handleStatusChange('RESOLVED')}
                  className="px-3 py-1.5 rounded-lg bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-300 border border-emerald-500/40 text-xs font-semibold flex items-center gap-1.5 transition-colors"
                >
                  <CheckCircle className="w-4 h-4" />
                  Mark Resolved
                </button>
              </div>
            ) : (
              <div className="flex flex-col sm:flex-row gap-2">
                <select
                  value={selectedTeamId}
                  onChange={(e) => setSelectedTeamId(e.target.value)}
                  className="flex-1 bg-navy-800 border border-navy-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan"
                >
                  <option value="">-- Select Available Rescue Unit --</option>
                  {teams
                    .filter((t) => t.status === 'AVAILABLE')
                    .map((t) => (
                      <option key={t.teamId} value={t.teamId}>
                        {t.teamName} ({t.specialization} • {t.membersCount} crew)
                      </option>
                    ))}
                </select>
                <button
                  onClick={handleAssignTeam}
                  disabled={!selectedTeamId}
                  className="px-4 py-2 rounded-lg bg-cyan hover:bg-cyan-glow text-navy-950 font-bold text-xs disabled:opacity-40 transition-colors"
                >
                  Dispatch Unit
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Footer actions */}
        <div className="p-4 border-t border-navy-700 bg-navy-950/60 flex items-center justify-between">
          <span className="text-[11px] text-amber-400 font-mono">
            ⚠ Simulation mode: changes persist in memory
          </span>
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-lg bg-navy-800 hover:bg-navy-700 text-slate-200 text-xs font-semibold"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
