import React, { useState } from 'react';
import {
  MapPin,
  ShieldAlert,
  Users,
  Compass,
  Layers,
  Crosshair,
  Bot,
  CheckCircle2,
  AlertTriangle
} from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { UrgencyBadge, StatusBadge } from '../components/StatusBadge';
import { IncidentDetailsModal } from '../components/IncidentDetailsModal';
import { EmergencyReport } from '../types';
import { getIncidentTypeLabel } from '../utils/formatters';

export const IncidentMapPage: React.FC = () => {
  const { reports, selectedReportId, setSelectedReportId } = useIncidents();
  const [selectedReportForModal, setSelectedReportForModal] = useState<EmergencyReport | null>(null);
  const [filterUrgency, setFilterUrgency] = useState<string>('ALL');

  const filteredReports = reports.filter((r) => {
    if (filterUrgency !== 'ALL' && r.urgency !== filterUrgency) return false;
    return true;
  });

  const activeReport = reports.find((r) => r.reportId === selectedReportId) || filteredReports[0] || null;

  // Normalized coordinate projection for demo canvas
  // Base origin: 18.5204 N, 73.8567 E
  const calculatePlotPosition = (lat: number, lon: number) => {
    const latOffset = (lat - 18.5204) * 2000;
    const lonOffset = (lon - 73.8567) * 2000;
    const top = Math.min(85, Math.max(15, 50 - latOffset));
    const left = Math.min(85, Math.max(15, 50 + lonOffset));
    return { top: `${top}%`, left: `${left}%` };
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-navy-800">
        <div>
          <h1 className="text-2xl font-extrabold text-white tracking-wide flex items-center gap-2">
            Incident Geolocation & Tactical Map
            <span className="w-2.5 h-2.5 rounded-full bg-cyan animate-pulse" />
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Geographic dispersion of emergency reports mapped from peer GPS fixes
          </p>
        </div>

        <div className="flex items-center gap-3">
          <select
            value={filterUrgency}
            onChange={(e) => setFilterUrgency(e.target.value)}
            className="bg-navy-900 border border-navy-700 rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-cyan"
          >
            <option value="ALL">Show All Urgencies</option>
            <option value="CRITICAL">Critical Pins Only</option>
            <option value="HIGH">High Urgency Pins</option>
            <option value="MEDIUM">Medium Urgency Pins</option>
          </select>
        </div>
      </div>

      {/* Main Map Interface Area */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-[640px]">
        {/* Map Canvas (Left 2 columns) */}
        <div className="lg:col-span-2 bg-navy-900 border border-navy-700 rounded-2xl relative overflow-hidden shadow-2xl flex flex-col">
          {/* Tactical Overlay Bar */}
          <div className="p-3 bg-navy-950/80 border-b border-navy-700 flex items-center justify-between z-10">
            <div className="flex items-center gap-3 text-xs font-mono text-cyan">
              <Compass className="w-4 h-4 text-cyan" />
              <span>RADAR GRID: SECTORS 1-4 (PUNE EMERGENCY DISPATCH ZONE)</span>
            </div>
            <div className="flex items-center gap-2 text-xs text-slate-400 font-mono">
              <Crosshair className="w-3.5 h-3.5 text-slate-400" />
              <span>DATUM: WGS84</span>
            </div>
          </div>

          {/* Interactive Map Visualizer */}
          <div className="flex-1 relative bg-navy-950 flex items-center justify-center overflow-hidden">
            {/* Grid Lines Pattern */}
            <div className="absolute inset-0 bg-[linear-gradient(to_right,#0d2238_1px,transparent_1px),linear-gradient(to_bottom,#0d2238_1px,transparent_1px)] bg-[size:40px_40px] opacity-70" />

            {/* Radar Circular Scan Lines */}
            <div className="absolute w-[360px] h-[360px] rounded-full border border-cyan/20 pointer-events-none" />
            <div className="absolute w-[540px] h-[540px] rounded-full border border-cyan/15 pointer-events-none" />
            <div className="absolute w-[720px] h-[720px] rounded-full border border-cyan/10 pointer-events-none" />
            <div className="absolute w-2 h-2 rounded-full bg-cyan/60 pointer-events-none" />

            {/* Sector Zones Overlay Labels */}
            <span className="absolute top-6 left-6 text-[10px] font-mono tracking-widest text-slate-400 uppercase">
              Sector 4 (Riverside Basin)
            </span>
            <span className="absolute top-6 right-6 text-[10px] font-mono tracking-widest text-slate-400 uppercase">
              Sector 3 (East Highlands)
            </span>
            <span className="absolute bottom-6 left-6 text-[10px] font-mono tracking-widest text-slate-400 uppercase">
              Sector 2 (Central Urban)
            </span>
            <span className="absolute bottom-6 right-6 text-[10px] font-mono tracking-widest text-slate-400 uppercase">
              Sector 1 (Command Deck)
            </span>

            {/* Plotted Emergency Pins */}
            {filteredReports.map((report) => {
              const pos = calculatePlotPosition(report.latitude, report.longitude);
              const isSelected = activeReport?.reportId === report.reportId;
              const isCritical = report.urgency === 'CRITICAL';

              return (
                <div
                  key={report.reportId}
                  style={pos}
                  onClick={() => setSelectedReportId(report.reportId)}
                  className="absolute -translate-x-1/2 -translate-y-1/2 cursor-pointer group z-20"
                >
                  {/* Radar pulse for critical alarms */}
                  {isCritical && (
                    <span className="absolute -inset-2 rounded-full bg-red-500/30 animate-ping" />
                  )}

                  <div
                    className={`relative w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs shadow-xl transition-transform hover:scale-125 ${
                      isSelected
                        ? 'ring-4 ring-cyan bg-white text-navy-950 scale-110'
                        : isCritical
                        ? 'bg-red-500 text-white shadow-red-900/50'
                        : report.urgency === 'HIGH'
                        ? 'bg-orange-500 text-white'
                        : 'bg-cyan text-navy-950'
                    }`}
                  >
                    <MapPin className="w-4 h-4" />
                  </div>

                  {/* Hover Tag */}
                  <div className="absolute bottom-full mb-1.5 left-1/2 -translate-x-1/2 px-2 py-1 rounded bg-navy-900/95 border border-cyan/40 text-white text-[10px] whitespace-nowrap shadow-xl opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
                    <span className="font-bold text-cyan">{report.incidentType}: </span>
                    <span>{report.peopleAffected} victims</span>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Map Footer Bar */}
          <div className="p-3 bg-navy-950/90 border-t border-navy-700 flex items-center justify-between text-xs text-slate-400">
            <span>Showing {filteredReports.length} Active Incident Pins</span>
            <span className="font-mono text-cyan">GPS Status: Calibrated (Cellular Tower Assisted)</span>
          </div>
        </div>

        {/* Selected Incident Inspector Panel (Right 1 column) */}
        <div className="bg-navy-900 border border-navy-700 rounded-2xl p-5 shadow-2xl flex flex-col justify-between overflow-y-auto">
          {activeReport ? (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-mono text-slate-400 uppercase">Selected Incident</span>
                <UrgencyBadge urgency={activeReport.urgency} />
              </div>

              <div>
                <h3 className="text-lg font-bold text-white tracking-wide">
                  {getIncidentTypeLabel(activeReport.incidentType)}
                </h3>
                <span className="text-xs font-mono text-cyan">ID: {activeReport.reportId}</span>
              </div>

              <div className="p-3.5 rounded-xl bg-navy-950 border border-navy-800 text-xs text-slate-200 leading-relaxed">
                {activeReport.description}
              </div>

              <div className="grid grid-cols-2 gap-2 text-xs">
                <div className="p-2.5 rounded-lg bg-navy-800/60 border border-navy-700">
                  <span className="text-[10px] text-slate-400 uppercase block">Casualties</span>
                  <span className="text-sm font-mono font-bold text-red-400">
                    {activeReport.peopleAffected} Trapped
                  </span>
                </div>
                <div className="p-2.5 rounded-lg bg-navy-800/60 border border-navy-700">
                  <span className="text-[10px] text-slate-400 uppercase block">Coordinates</span>
                  <span className="text-xs font-mono font-bold text-slate-200 truncate block">
                    {activeReport.latitude.toFixed(4)}, {activeReport.longitude.toFixed(4)}
                  </span>
                </div>
              </div>

              {/* AI Dispatch summary */}
              {activeReport.aiSummary && (
                <div className="p-3 rounded-lg bg-cyan/10 border border-cyan/30 text-xs text-slate-200 space-y-1">
                  <div className="flex items-center gap-1.5 text-cyan font-bold text-[11px]">
                    <Bot className="w-3.5 h-3.5" />
                    <span>AI ACTION PROTOCOL</span>
                  </div>
                  <p className="text-[11px] text-slate-300 leading-relaxed">{activeReport.aiSummary}</p>
                </div>
              )}

              <button
                onClick={() => setSelectedReportForModal(activeReport)}
                className="w-full py-2.5 rounded-lg bg-cyan hover:bg-cyan-glow text-navy-950 font-bold text-xs shadow-lg shadow-cyan/20 transition-all"
              >
                Open Full Tactical Dossier
              </button>
            </div>
          ) : (
            <div className="h-full flex flex-col items-center justify-center text-center p-6 text-slate-400">
              <MapPin className="w-8 h-8 text-cyan mb-2" />
              <p className="text-sm font-semibold text-white">No Incident Selected</p>
              <p className="text-xs mt-1">Click any pin on the tactical grid to inspect field telemetry.</p>
            </div>
          )}
        </div>
      </div>

      {/* Details Modal */}
      <IncidentDetailsModal report={selectedReportForModal} onClose={() => setSelectedReportForModal(null)} />
    </div>
  );
};
