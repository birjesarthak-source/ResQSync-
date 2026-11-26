import React from 'react';
import { BarChart2, ShieldAlert, Users, TrendingUp, CheckCircle2, Clock } from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { MetricCard } from '../components/MetricCard';

export const AnalyticsPage: React.FC = () => {
  const { reports, teams } = useIncidents();

  const totalReports = reports.length;
  const resolvedCount = reports.filter((r) => r.status === 'RESOLVED').length;
  const resolutionRate = totalReports > 0 ? Math.round((resolvedCount / totalReports) * 100) : 0;
  const totalVictims = reports.reduce((acc, r) => acc + r.peopleAffected, 0);

  // Group by incident type
  const typeCounts: Record<string, number> = {};
  reports.forEach((r) => {
    typeCounts[r.incidentType] = (typeCounts[r.incidentType] || 0) + 1;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-800 tracking-wide flex items-center gap-2">
            Disaster Response Analytics
            <BarChart2 className="w-5 h-5 text-blue-600" />
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Tactical operational analytics and emergency resolution velocity metrics
          </p>
        </div>
      </div>

      {/* Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Total Incidents"
          value={totalReports}
          subtitle="Lifetime logged SOS alerts"
          icon={ShieldAlert}
          variant="blue"
        />
        <MetricCard
          title="Victim Count"
          value={totalVictims}
          subtitle="Reported people at risk"
          icon={Users}
          variant="amber"
        />
        <MetricCard
          title="Resolution Rate"
          value={`${resolutionRate}%`}
          subtitle={`${resolvedCount} resolved incidents`}
          icon={CheckCircle2}
          variant="green"
        />
        <MetricCard
          title="Avg Dispatch Time"
          value="4.2m"
          subtitle="From SOS to team dispatch"
          icon={Clock}
          variant="default"
        />
      </div>

      {/* Category Distribution Card */}
      <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-xs space-y-4">
        <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wider pb-2 border-b border-slate-100 flex items-center justify-between">
          <span>Incident Distribution by Disaster Category</span>
          <TrendingUp className="w-4 h-4 text-blue-600" />
        </h2>

        <div className="space-y-3">
          {Object.entries(typeCounts).map(([type, count]) => {
            const percentage = totalReports > 0 ? Math.round((count / totalReports) * 100) : 0;
            return (
              <div key={type} className="space-y-1">
                <div className="flex justify-between text-xs font-semibold text-slate-700">
                  <span>{type.replace('_', ' ')}</span>
                  <span>{count} incidents ({percentage}%)</span>
                </div>
                <div className="w-full bg-slate-100 h-2.5 rounded-full overflow-hidden">
                  <div
                    className="bg-blue-600 h-full rounded-full transition-all duration-500"
                    style={{ width: `${percentage}%` }}
                  />
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
