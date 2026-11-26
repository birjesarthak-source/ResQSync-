import React, { useState } from 'react';
import { MapPin, RefreshCw, AlertTriangle } from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { UrgencyBadge } from '../components/StatusBadge';
import { EmergencyReport } from '../types';

export const IncidentMapPage: React.FC = () => {
  const { reports, isLoadingReports, refetchReports } = useIncidents();
  const [selectedReport, setSelectedReport] = useState<EmergencyReport | null>(
    reports.length > 0 ? reports[0] : null
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-800 tracking-wide flex items-center gap-2">
            Disaster Incident Map
            <span className="text-xs px-2.5 py-0.5 rounded-full bg-blue-50 text-blue-600 border border-blue-200 font-mono font-bold">
              {reports.length} Map Pins
            </span>
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Geospatial visualization of field emergency reports relayed through offline mesh gateways
          </p>
        </div>

        <button
          onClick={() => refetchReports()}
          disabled={isLoadingReports}
          className="px-3.5 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs flex items-center gap-2 shadow-xs transition-colors self-start sm:self-auto"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoadingReports ? 'animate-spin' : ''}`} />
          <span>Sync Coordinates</span>
        </button>
      </div>

      {/* Map Layout Container */}
      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-xs grid grid-cols-1 lg:grid-cols-3 h-[600px]">
        {/* Left Sidebar Pin Selector */}
        <div className="border-r border-slate-200 flex flex-col h-full bg-slate-50/50">
          <div className="p-3 bg-slate-100 border-b border-slate-200 text-xs font-bold text-slate-700 uppercase tracking-wider">
            Incident Pin Queue ({reports.length})
          </div>

          <div className="flex-1 overflow-y-auto divide-y divide-slate-100 p-2 space-y-1">
            {reports.length === 0 ? (
              <div className="p-6 text-center text-slate-400 text-xs">
                No report coordinates available on map.
              </div>
            ) : (
              reports.map((report) => (
                <div
                  key={report.reportId}
                  onClick={() => setSelectedReport(report)}
                  className={`p-3 rounded-lg cursor-pointer transition-colors ${
                    selectedReport?.reportId === report.reportId
                      ? 'bg-blue-50 border border-blue-300'
                      : 'bg-white hover:bg-slate-100 border border-slate-200'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1">
                    <span className="font-bold text-slate-800 text-xs">{report.incidentType.replace('_', ' ')}</span>
                    <UrgencyBadge urgency={report.urgency} />
                  </div>
                  <p className="text-slate-600 text-xs line-clamp-1">{report.description}</p>
                  <div className="text-[10px] text-slate-400 font-mono mt-1 flex items-center gap-1">
                    <MapPin className="w-3 h-3 text-blue-600 shrink-0" />
                    <span>{report.latitude.toFixed(4)}, {report.longitude.toFixed(4)}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Interactive Map Visualizer Canvas */}
        <div className="lg:col-span-2 relative bg-slate-100 flex items-center justify-center overflow-hidden">
          <div
            className="absolute inset-0 opacity-20"
            style={{
              backgroundImage: 'radial-gradient(#2563eb 1px, transparent 1px)',
              backgroundSize: '24px 24px'
            }}
          />

          {selectedReport ? (
            <div className="bg-white border border-slate-200 rounded-2xl p-6 max-w-md shadow-xl z-10 space-y-4">
              <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                <div>
                  <h3 className="text-base font-bold text-slate-800">{selectedReport.incidentType.replace('_', ' ')}</h3>
                  <span className="text-xs text-slate-400 font-mono">ID: {selectedReport.reportId}</span>
                </div>
                <UrgencyBadge urgency={selectedReport.urgency} />
              </div>

              <p className="text-xs text-slate-700 leading-relaxed">{selectedReport.description}</p>

              <div className="p-3 bg-slate-50 rounded-lg border border-slate-200 text-xs space-y-1 font-mono text-slate-600">
                <div className="flex justify-between">
                  <span>Latitude:</span>
                  <span className="font-bold text-slate-800">{selectedReport.latitude}</span>
                </div>
                <div className="flex justify-between">
                  <span>Longitude:</span>
                  <span className="font-bold text-slate-800">{selectedReport.longitude}</span>
                </div>
                <div className="flex justify-between">
                  <span>Victims:</span>
                  <span className="font-bold text-red-600">{selectedReport.peopleAffected}</span>
                </div>
                <div className="flex justify-between">
                  <span>Reporter:</span>
                  <span className="font-bold text-emerald-700">{selectedReport.reporterDeviceId}</span>
                </div>
              </div>
            </div>
          ) : (
            <div className="text-center text-slate-400 p-8 z-10">
              <AlertTriangle className="w-12 h-12 text-slate-300 mx-auto mb-3" />
              <h3 className="text-sm font-bold text-slate-600">Select an Emergency Pin</h3>
              <p className="text-xs text-slate-400 mt-1">Tap any report on the left panel to inspect GPS coordinates.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
