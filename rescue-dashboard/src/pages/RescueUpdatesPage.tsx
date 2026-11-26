import React, { useState } from 'react';
import { Radio, Send, Filter } from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { formatDate } from '../utils/formatters';

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
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-800 tracking-wide flex items-center gap-2">
            Field Rescue & Dispatch Updates
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-600 animate-pulse" />
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Real-time chronological timeline of rescue transmissions and field situational logs
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-slate-500" />
          <select
            value={teamFilter}
            onChange={(e) => setTeamFilter(e.target.value)}
            className="bg-white border border-slate-200 rounded-lg px-3 py-1.5 text-xs text-slate-800 focus:outline-none focus:border-blue-600"
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
        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-xs space-y-4 h-fit">
          <div className="flex items-center gap-2 text-blue-600 font-bold text-sm">
            <Radio className="w-4 h-4" />
            <h2>ISSUE RESCUE BROADCAST UPDATE</h2>
          </div>

          <form onSubmit={handleBroadcast} className="space-y-3">
            <div>
              <label className="text-[11px] font-semibold text-slate-500 uppercase block mb-1">
                Target Emergency Report
              </label>
              <select
                value={selectedReportId}
                onChange={(e) => setSelectedReportId(e.target.value)}
                className="w-full bg-slate-50 border border-slate-200 rounded-lg p-2 text-xs text-slate-800 focus:outline-none focus:border-blue-600"
              >
                {reports.map((r) => (
                  <option key={r.reportId} value={r.reportId}>
                    {r.reportId} ({r.incidentType.replace('_', ' ')})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-[11px] font-semibold text-slate-500 uppercase block mb-1">
                Authoring Rescue Unit
              </label>
              <select
                value={selectedTeamId}
                onChange={(e) => setSelectedTeamId(e.target.value)}
                className="w-full bg-slate-50 border border-slate-200 rounded-lg p-2 text-xs text-slate-800 focus:outline-none focus:border-blue-600"
              >
                {teams.map((t) => (
                  <option key={t.teamId} value={t.teamId}>
                    {t.teamName} ({t.leaderName})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-[11px] font-semibold text-slate-500 uppercase block mb-1">
                Broadcast Log Message
              </label>
              <textarea
                value={updateMessage}
                onChange={(e) => setUpdateMessage(e.target.value)}
                placeholder="Enter field update instructions or status message..."
                rows={4}
                className="w-full bg-slate-50 border border-slate-200 rounded-lg p-2.5 text-xs text-slate-800 placeholder-slate-400 focus:outline-none focus:border-blue-600"
              />
            </div>

            <button
              type="submit"
              disabled={!updateMessage.trim()}
              className="w-full py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-xs disabled:opacity-40 transition-colors"
            >
              <Send className="w-3.5 h-3.5" />
              <span>Broadcast Update to Mesh</span>
            </button>
          </form>
        </div>

        {/* Right Column: Timeline Log Feed */}
        <div className="lg:col-span-2 bg-white border border-slate-200 rounded-2xl p-5 shadow-xs space-y-4">
          <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wider pb-2 border-b border-slate-100">
            Chronological Field Transmission Timeline
          </h2>

          <div className="space-y-3">
            {filteredUpdates.length === 0 ? (
              <p className="text-xs text-slate-400 text-center py-8">
                No dispatch updates logged. Use the form on the left to issue broadcasts.
              </p>
            ) : (
              filteredUpdates.map((upd) => (
                <div key={upd.updateId} className="p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-blue-600 text-xs">{upd.teamName}</span>
                    <span className="text-[10px] text-slate-400 font-mono">{formatDate(upd.timestamp)}</span>
                  </div>
                  <p className="text-xs text-slate-700 leading-relaxed">{upd.message}</p>
                  <div className="text-[10px] text-slate-500 font-mono pt-1 border-t border-slate-200">
                    Author: {upd.author} • Target Report: {upd.reportId}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
