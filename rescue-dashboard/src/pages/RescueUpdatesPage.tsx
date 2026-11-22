import React, { useState } from 'react';
import {
  Radio,
  Send,
  Clock,
  Users,
  ShieldAlert,
  AlertTriangle,
  CheckCircle2,
  Filter
} from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { formatDate } from '../utils/formatters';
import { UrgencyBadge } from '../components/StatusBadge';

export const RescueUpdatesPage: React.FC = () => {
  const { updates, teams, reports, addRescueUpdate } = useIncidents();
  const [selectedReportId, setSelectedReportId] = useState(reports[0]?.reportId || '');
  const [selectedTeamId, setSelectedTeamId] = useState(teams[0]?.teamId || '');
  const [updateMessage, setUpdateMessage] = useState('');
  const [teamFilter, setTeamFilter] = useState('ALL');

  const handleBroadcast = (e: React.FormEvent) => {
    e.preventDefault();
    if (!updateMessage.trim()) return;
    addRescueUpdate(selectedReportId, selectedTeamId, updateMessage.trim());
    setUpdateMessage('');
  };

  const filteredUpdates = updates.filter((upd) => {
    if (teamFilter !== 'ALL' && upd.teamId !== teamFilter) return false;
    return true;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-navy-800">
        <div>
          <h1 className="text-2xl font-extrabold text-white tracking-wide flex items-center gap-2">
            Field Rescue & Dispatch Updates
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse" />
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Real-time chronological timeline of rescue transmissions and field situational logs
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-slate-400" />
          <select
            value={teamFilter}
            onChange={(e) => setTeamFilter(e.target.value)}
            className="bg-navy-900 border border-navy-700 rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-cyan"
          >
            <option value="ALL">All Rescue Teams</option>
            {teams.map((t) => (
              <option key={t.teamId} value={t.teamId}>
                {t.teamName} ({t.callsign})
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Broadcast Dispatcher Form */}
        <div className="bg-navy-900 border border-navy-700 rounded-2xl p-5 shadow-2xl space-y-4 h-fit">
          <div className="flex items-center gap-2 text-cyan font-bold text-sm">
            <Radio className="w-4 h-4" />
            <span>TRANSMIT FIELD LOG ENTRY</span>
          </div>
          <p className="text-xs text-slate-400 leading-snug">
            Log dispatch instructions, casualty evacuation reports, or resource milestones.
          </p>

          <form onSubmit={handleBroadcast} className="space-y-3 pt-2">
            <div>
              <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-400 mb-1">
                Target Emergency Report
              </label>
              <select
                value={selectedReportId}
                onChange={(e) => setSelectedReportId(e.target.value)}
                className="w-full bg-navy-950 border border-navy-700 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-cyan"
              >
                {reports.map((r) => (
                  <option key={r.reportId} value={r.reportId}>
                    {r.reportId} - {r.incidentType} ({r.peopleAffected} at risk)
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-400 mb-1">
                Reporting Unit / Author
              </label>
              <select
                value={selectedTeamId}
                onChange={(e) => setSelectedTeamId(e.target.value)}
                className="w-full bg-navy-950 border border-navy-700 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-cyan"
              >
                {teams.map((t) => (
                  <option key={t.teamId} value={t.teamId}>
                    {t.teamName} • Callsign: {t.callsign}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-400 mb-1">
                Transmission Message
              </label>
              <textarea
                value={updateMessage}
                onChange={(e) => setUpdateMessage(e.target.value)}
                rows={4}
                required
                placeholder="e.g. Paramedic triage unit arrived at grocery roof. Evacuating elderly casualties first..."
                className="w-full bg-navy-950 border border-navy-700 rounded-lg p-3 text-xs text-white focus:outline-none focus:border-cyan resize-none"
              />
            </div>

            <button
              type="submit"
              className="w-full py-2.5 rounded-lg bg-cyan hover:bg-cyan-glow text-navy-950 font-bold text-xs flex items-center justify-center gap-2 shadow-lg shadow-cyan/20 transition-all"
            >
              <Send className="w-3.5 h-3.5" />
              Broadcast to Command Feed
            </button>
          </form>
        </div>

        {/* Right 2 Columns: Chronological Radio Logs Timeline */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-navy-900 border border-navy-700 rounded-2xl p-5 shadow-2xl">
            <h2 className="text-sm font-bold text-white uppercase tracking-wider mb-4 flex items-center gap-2">
              <Clock className="w-4 h-4 text-cyan" />
              Chronological Transmission Log ({filteredUpdates.length})
            </h2>

            <div className="relative pl-6 space-y-6 before:absolute before:left-2 before:top-2 before:bottom-2 before:w-0.5 before:bg-navy-700">
              {filteredUpdates.map((upd) => (
                <div key={upd.updateId} className="relative group">
                  {/* Timeline Indicator Dot */}
                  <span className="absolute -left-6 top-1 w-2.5 h-2.5 rounded-full bg-cyan ring-4 ring-navy-900 shadow-sm shadow-cyan/50" />

                  <div className="p-4 rounded-xl bg-navy-950 border border-navy-800 hover:border-cyan/30 transition-all">
                    <div className="flex flex-wrap items-center justify-between gap-2 mb-2">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-white text-xs tracking-wide">
                          {upd.teamName}
                        </span>
                        <span className="text-[11px] text-slate-400 font-mono">
                          by {upd.author}
                        </span>
                      </div>
                      <div className="flex items-center gap-2">
                        <UrgencyBadge urgency={upd.urgency} />
                        <span className="text-[11px] text-slate-400 font-mono">
                          {formatDate(upd.timestamp)}
                        </span>
                      </div>
                    </div>

                    <p className="text-xs text-slate-200 leading-relaxed">{upd.message}</p>

                    <div className="mt-2.5 pt-2 border-t border-navy-800/80 flex items-center justify-between text-[11px] text-slate-400 font-mono">
                      <span>Related Incident: {upd.reportId}</span>
                      {upd.statusChange && (
                        <span className="text-cyan font-bold">
                          Status updated: {upd.statusChange}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
