import React from 'react';
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
  const { teams } = useIncidents();

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
          <h1 className="text-2xl font-extrabold text-slate-800 tracking-wide flex items-center gap-2">
            Emergency Response Teams & Field Units
            <span className="text-xs px-2.5 py-0.5 rounded-full bg-blue-50 text-blue-600 border border-blue-200 font-mono font-bold">
              {teams.length} Units Active
            </span>
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Command directory of specialized first responders, water rescue boats, and medical triage teams
          </p>
        </div>
      </div>

      {/* Teams Grid */}
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
