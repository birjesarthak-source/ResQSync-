import React, { useState } from 'react';
import {
  Users,
  ShieldCheck,
  LifeBuoy,
  HeartPulse,
  Truck,
  Eye
} from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { RescueTeam } from '../types';

export const TeamManagementPage: React.FC = () => {
  const { teams, reports, assignTeamToReport } = useIncidents();
  const [selectedTeamId, setSelectedTeamId] = useState('');
  const [selectedReportId, setSelectedReportId] = useState('');

  const getSpecializationIcon = (spec: RescueTeam['specialization']) => {
    switch (spec) {
      case 'Water Rescue': return <LifeBuoy className="w-4 h-4 text-blue-600" />;
      case 'Medical Triage': return <HeartPulse className="w-4 h-4 text-rose-600" />;
      case 'Heavy Debris': return <Truck className="w-4 h-4 text-amber-600" />;
      case 'Drone Recon': return <Eye className="w-4 h-4 text-purple-600" />;
      default: return <ShieldCheck className="w-4 h-4 text-emerald-600" />;
    }
  };

  const getStatusBadge = (status: RescueTeam['status']) => {
    switch (status) {
      case 'AVAILABLE':
        return <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-100 text-emerald-800 border border-emerald-200">Available</span>;
      case 'DISPATCHED':
        return <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-blue-100 text-blue-800 border border-blue-200">En Route</span>;
      case 'ON_SITE':
        return <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-amber-100 text-amber-800 border border-amber-200">On Scene</span>;
      default:
        return <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-slate-100 text-slate-700 border border-slate-200">Standby</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
          Rescue teams
            <span className="text-xs px-2.5 py-0.5 rounded-full bg-blue-50 text-blue-600 border border-blue-200 font-mono font-bold">
              {teams.length} Units Active
            </span>
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Availability and active assignments for field response units
          </p>
        </div>
      </div>

      {/* Teams Grid */}
      <div className="surface-card p-4 flex flex-col md:flex-row gap-3 md:items-end">
        <div className="flex-1">
          <label className="block text-xs font-semibold text-slate-600 mb-1.5">Emergency report</label>
          <select
            value={selectedReportId}
            onChange={(event) => setSelectedReportId(event.target.value)}
            className="w-full h-11 rounded-xl border border-slate-200 bg-slate-50 px-3 text-sm text-slate-800"
          >
            <option value="">Select an emergency</option>
            {reports.filter((report) => report.status !== 'RESOLVED').map((report) => (
              <option key={report.reportId} value={report.reportId}>
                {report.reportId} - {report.incidentType.replace('_', ' ')}
              </option>
            ))}
          </select>
        </div>
        <div className="flex-1">
          <label className="block text-xs font-semibold text-slate-600 mb-1.5">Rescue team</label>
          <select
            value={selectedTeamId}
            onChange={(event) => setSelectedTeamId(event.target.value)}
            className="w-full h-11 rounded-xl border border-slate-200 bg-slate-50 px-3 text-sm text-slate-800"
          >
            <option value="">Select a team</option>
            {teams.map((team) => (
              <option key={team.teamId} value={team.teamId}>{team.teamName}</option>
            ))}
          </select>
        </div>
        <button
          type="button"
          disabled={!selectedReportId || !selectedTeamId}
          onClick={() => {
            assignTeamToReport(selectedReportId, selectedTeamId);
            setSelectedReportId('');
            setSelectedTeamId('');
          }}
          className="h-11 rounded-xl bg-blue-600 px-5 text-sm font-bold text-white disabled:opacity-40"
        >
          Assign team
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {teams.map((team) => (
          <div key={team.teamId} className="bg-white border border-slate-200 rounded-xl p-5 shadow-xs space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                {getSpecializationIcon(team.specialization)}
                <span className="font-bold text-slate-800 text-base">{team.teamName}</span>
                <span className="text-xs text-slate-400 font-mono">({team.callsign})</span>
              </div>
              {getStatusBadge(team.status)}
            </div>

            <div className="grid grid-cols-2 gap-2 text-xs bg-slate-50 p-3 rounded-lg border border-slate-100 text-slate-600 font-mono">
              <div>Leader: <span className="font-bold text-slate-800">{team.leaderName}</span></div>
              <div>Members: <span className="font-bold text-slate-800">{team.membersCount} Crew</span></div>
              <div>Radio: <span className="font-bold text-blue-600">{team.contactRadio}</span></div>
              <div>Sector: <span className="font-bold text-slate-800">{team.currentSector}</span></div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
