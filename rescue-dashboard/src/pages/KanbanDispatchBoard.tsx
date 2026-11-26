import React from 'react';
import { LayoutGrid, ArrowRight, CheckCircle2, ShieldAlert, Clock } from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { UrgencyBadge } from '../components/StatusBadge';
import { ReportStatus } from '../types';

export const KanbanDispatchBoard: React.FC = () => {
  const { reports, updateReportStatus } = useIncidents();

  const columns: { title: string; status: ReportStatus; color: string }[] = [
    { title: 'New (Unassigned)', status: 'NEW', color: 'border-red-500 bg-red-50/30' },
    { title: 'Assigned (En Route)', status: 'ASSIGNED', color: 'border-blue-500 bg-blue-50/30' },
    { title: 'In Progress (On-Site)', status: 'IN_PROGRESS', color: 'border-amber-500 bg-amber-50/30' },
    { title: 'Resolved', status: 'RESOLVED', color: 'border-emerald-500 bg-emerald-50/30' }
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-800 tracking-wide flex items-center gap-2">
            Tactical Dispatch Kanban Board
            <LayoutGrid className="w-5 h-5 text-blue-600" />
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            One-click incident status progression across field rescue stages
          </p>
        </div>
      </div>

      {/* Kanban Board Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {columns.map((col) => {
          const colReports = reports.filter((r) => r.status === col.status);
          return (
            <div key={col.status} className={`rounded-2xl border-t-4 ${col.color} bg-white border border-slate-200 p-4 shadow-xs space-y-3`}>
              <div className="flex justify-between items-center pb-2 border-b border-slate-100 font-bold text-slate-800 text-xs uppercase tracking-wider">
                <span>{col.title}</span>
                <span className="px-2 py-0.5 bg-slate-100 text-slate-600 rounded-full text-[11px] font-mono">
                  {colReports.length}
                </span>
              </div>

              <div className="space-y-2 min-h-[400px]">
                {colReports.length === 0 ? (
                  <p className="text-xs text-slate-400 text-center py-8">No incidents in stage.</p>
                ) : (
                  colReports.map((report) => (
                    <div key={report.reportId} className="p-3 bg-slate-50 border border-slate-200 rounded-xl space-y-2 text-xs">
                      <div className="flex items-center justify-between">
                        <span className="font-bold text-slate-800">{report.incidentType.replace('_', ' ')}</span>
                        <UrgencyBadge urgency={report.urgency} />
                      </div>
                      <p className="text-slate-600 text-[11px] line-clamp-2">{report.description}</p>

                      {/* Action transition button */}
                      {col.status === 'NEW' && (
                        <button
                          onClick={() => updateReportStatus(report.reportId, 'ASSIGNED')}
                          className="w-full py-1.5 bg-blue-600 hover:bg-blue-700 text-white font-bold text-[11px] rounded-lg flex items-center justify-center gap-1 transition-colors"
                        >
                          <span>Dispatch Unit</span>
                          <ArrowRight className="w-3 h-3" />
                        </button>
                      )}
                      {col.status === 'ASSIGNED' && (
                        <button
                          onClick={() => updateReportStatus(report.reportId, 'IN_PROGRESS')}
                          className="w-full py-1.5 bg-amber-600 hover:bg-amber-700 text-white font-bold text-[11px] rounded-lg flex items-center justify-center gap-1 transition-colors"
                        >
                          <span>Mark On-Site</span>
                          <ArrowRight className="w-3 h-3" />
                        </button>
                      )}
                      {col.status === 'IN_PROGRESS' && (
                        <button
                          onClick={() => updateReportStatus(report.reportId, 'RESOLVED')}
                          className="w-full py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-[11px] rounded-lg flex items-center justify-center gap-1 transition-colors"
                        >
                          <CheckCircle2 className="w-3 h-3" />
                          <span>Mark Resolved</span>
                        </button>
                      )}
                    </div>
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
