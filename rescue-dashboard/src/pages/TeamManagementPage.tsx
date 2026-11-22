import React from 'react';
import {
  Users,
  Radio,
  MapPin,
  ShieldCheck,
  LifeBuoy,
  HeartPulse,
  Truck,
  Eye,
  CheckCircle2
} from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { RescueTeam } from '../types';

export const TeamManagementPage: React.FC = () => {
  const { teams } = useIncidents();

  const getSpecializationIcon = (spec: RescueTeam['specialization']) => {
    switch (spec) {
      case 'Water Rescue': return <LifeBuoy className="w-4 h-4 text-cyan" />;
      case 'Medical Triage': return <HeartPulse className="w-4 h-4 text-rose-400" />;
      case 'Heavy Debris': return <Truck className="w-4 h-4 text-amber-400" />;
      case 'Drone Recon': return <Eye className="w-4 h-4 text-purple-400" />;
      default: return <ShieldCheck className="w-4 h-4 text-emerald-400" />;
    }
  };

  const getStatusBadge = (status: RescueTeam['status']) => {
    switch (status) {
      case 'AVAILABLE':
        return <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">Available</span>;
      case 'DISPATCHED':
        return <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-cyan/20 text-cyan border border-cyan/30">En Route</span>;
      case 'ON_SITE':
        return <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-amber-500/20 text-amber-400 border border-amber-500/30">On Scene</span>;
      default:
        return <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-slate-500/20 text-slate-400 border border-slate-500/30">Standby</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-navy-800">
        <div>
          <h1 className="text-2xl font-extrabold text-white tracking-wide flex items-center gap-2">
            Emergency Response Teams & Field Units
            <span className="text-xs px-2.5 py-0.5 rounded-full bg-cyan/15 text-cyan border border-cyan/30 font-mono font-bold">
              {teams.length} Units Active
            </span>
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Command directory of specialized first responders, water rescue boats, and medical triage teams
          </p>
        </div>
      </div>

      {/* Teams Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {teams.map((team) => (
          <div
            key={team.teamId}
            className="p-5 rounded-2xl bg-navy-900 border border-navy-700 shadow-xl space-y-4"
          >
            {/* Top row */}
            <div className="flex items-start justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-navy-950 border border-navy-800 flex items-center justify-center">
                  {getSpecializationIcon(team.specialization)}
                </div>
                <div>
                  <h3 className="font-bold text-white text-sm tracking-wide">{team.teamName}</h3>
                  <span className="text-xs text-cyan font-mono font-bold">Callsign: {team.callsign}</span>
                </div>
              </div>

              {getStatusBadge(team.status)}
            </div>

            {/* Details */}
            <div className="grid grid-cols-2 gap-2 text-xs p-3 rounded-xl bg-navy-950 border border-navy-800">
              <div>
                <span className="text-[10px] text-slate-400 uppercase block">Leader</span>
                <span className="font-semibold text-slate-200">{team.leaderName}</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 uppercase block">Crew Size</span>
                <span className="font-mono font-bold text-slate-200">{team.membersCount} Rescuers</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 uppercase block">Radio Frequency</span>
                <span className="font-mono text-cyan">{team.contactRadio}</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 uppercase block">Sector</span>
                <span className="text-slate-300">{team.currentSector}</span>
              </div>
            </div>

            {/* Assigned Incidents */}
            <div className="pt-2 border-t border-navy-800 flex items-center justify-between text-xs">
              <span className="text-slate-400">
                Assigned Incidents: <strong className="text-white font-mono">{team.assignedReportIds.length}</strong>
              </span>
              {team.assignedReportIds.length > 0 && (
                <span className="text-[11px] font-mono text-amber-400">
                  {team.assignedReportIds.join(', ')}
                </span>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
