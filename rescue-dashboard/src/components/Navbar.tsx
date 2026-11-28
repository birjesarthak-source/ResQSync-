import React from 'react';
import { Menu, RefreshCw, LogOut, Radio } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useIncidents } from '../context/IncidentContext';
import { NotificationPanel } from './NotificationPanel';

interface NavbarProps {
  onOpenMobileMenu: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onOpenMobileMenu }) => {
  const { user, logout } = useAuth();
  const { reports, refetchReports, isLoadingReports } = useIncidents();

  const criticalCount = reports.filter((r) => r.urgency === 'CRITICAL' && r.status !== 'RESOLVED').length;
  const activeCount = reports.filter((r) => r.status !== 'RESOLVED').length;

  return (
    <header className="h-20 bg-white/95 backdrop-blur border-b border-slate-200/80 px-4 lg:px-6 flex items-center justify-between sticky top-0 z-30">
      {/* Left: Mobile Toggle & Section Name */}
      <div className="flex items-center gap-3">
        <button
          onClick={onOpenMobileMenu}
          className="p-2 rounded-lg text-slate-600 hover:text-slate-900 hover:bg-slate-100 lg:hidden"
          aria-label="Open mobile menu"
        >
          <Menu className="w-5 h-5" />
        </button>

        <div className="hidden sm:flex items-center gap-2 text-xs">
          <span className="w-2 h-2 rounded-full bg-emerald-500" />
          <span className="text-emerald-700 font-semibold">System connected</span>
        </div>
      </div>

      {/* Center: Live Alert Counters */}
      <div className="flex items-center gap-2 sm:gap-4">
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-blue-50 border border-blue-200">
          <Radio className="w-4 h-4 text-blue-600" />
          <div className="text-xs">
            <span className="text-slate-500 hidden sm:inline">Active Incidents: </span>
            <span className="text-blue-600 font-bold font-mono">{activeCount}</span>
          </div>
        </div>

        <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-red-50 border border-red-200">
          <span className="w-2 h-2 rounded-full bg-red-600 animate-pulse" />
          <div className="text-xs">
            <span className="text-red-700 hidden sm:inline">Critical: </span>
            <span className="text-red-600 font-extrabold font-mono">{criticalCount}</span>
          </div>
        </div>

        {/* Live Refresh Button */}
        <button
          onClick={() => refetchReports()}
          disabled={isLoadingReports}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-blue-50 hover:bg-blue-100 border border-blue-200 text-blue-600 text-xs font-semibold transition-colors disabled:opacity-50"
          title="Sync live records from Supabase"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoadingReports ? 'animate-spin' : ''}`} />
          <span className="hidden md:inline">Live Sync</span>
        </button>
      </div>

      {/* Right: Notifications & Operator Profile & Logout */}
      <div className="flex items-center gap-3">
        <NotificationPanel />

        {user ? (
          <div className="flex items-center gap-3">
            <div className="text-right hidden md:block leading-tight">
              <div className="text-xs font-bold text-slate-800 flex items-center justify-end gap-1.5">
                {user.name}
                <span className="text-[10px] px-1.5 py-0.2 bg-slate-100 text-slate-600 rounded border border-slate-200">
                  {user.callsign}
                </span>
              </div>
              <div className="text-[11px] text-blue-600 font-mono">{user.role} • {user.sector}</div>
            </div>

            <button
              onClick={logout}
              className="p-2 rounded-lg text-slate-500 hover:text-red-600 hover:bg-red-50 border border-transparent hover:border-red-200 transition-colors"
              title="End Session"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        ) : (
          <span className="text-xs text-slate-500">Guest Operator</span>
        )}
      </div>
    </header>
  );
};
