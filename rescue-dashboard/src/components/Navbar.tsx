import React from 'react';
import { Menu, RefreshCw, LogOut, Radio } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useIncidents } from '../context/IncidentContext';

interface NavbarProps {
  onOpenMobileMenu: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onOpenMobileMenu }) => {
  const { user, logout } = useAuth();
  const { reports, refetchReports, isLoadingReports } = useIncidents();

  const criticalCount = reports.filter((r) => r.urgency === 'CRITICAL' && r.status !== 'RESOLVED').length;
  const activeCount = reports.filter((r) => r.status !== 'RESOLVED').length;

  return (
    <header className="h-16 bg-navy-900 border-b border-navy-700 px-4 lg:px-6 flex items-center justify-between sticky top-0 z-30">
      {/* Left: Mobile Toggle & Section Name */}
      <div className="flex items-center gap-3">
        <button
          onClick={onOpenMobileMenu}
          className="p-2 rounded-lg text-slate-400 hover:text-white hover:bg-navy-800 lg:hidden"
          aria-label="Open mobile menu"
        >
          <Menu className="w-5 h-5" />
        </button>

        <div className="hidden sm:flex items-center gap-2 text-xs font-mono">
          <span className="w-2 h-2 rounded-full bg-cyan animate-ping" />
          <span className="text-cyan font-bold tracking-wider">OFF-GRID MESH PROTOCOL: ACTIVE</span>
        </div>
      </div>

      {/* Center: Live Alert Counters */}
      <div className="flex items-center gap-2 sm:gap-4">
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-navy-800 border border-navy-700">
          <Radio className="w-4 h-4 text-cyan" />
          <div className="text-xs">
            <span className="text-slate-400 hidden sm:inline">Active Incidents: </span>
            <span className="text-cyan font-bold font-mono">{activeCount}</span>
          </div>
        </div>

        <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-red-950/60 border border-red-600/50 shadow-sm shadow-red-950">
          <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />
          <div className="text-xs">
            <span className="text-red-300 hidden sm:inline">Critical: </span>
            <span className="text-red-400 font-extrabold font-mono">{criticalCount}</span>
          </div>
        </div>

        {/* Live Refresh Button */}
        <button
          onClick={() => refetchReports()}
          disabled={isLoadingReports}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-cyan/10 hover:bg-cyan/20 border border-cyan/40 text-cyan text-xs font-semibold transition-colors disabled:opacity-50"
          title="Sync live records from Supabase"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoadingReports ? 'animate-spin' : ''}`} />
          <span className="hidden md:inline">Live Sync</span>
        </button>
      </div>

      {/* Right: Operator Profile & Logout */}
      <div className="flex items-center gap-3">
        {user ? (
          <div className="flex items-center gap-3">
            <div className="text-right hidden md:block leading-tight">
              <div className="text-xs font-bold text-slate-100 flex items-center justify-end gap-1.5">
                {user.name}
                <span className="text-[10px] px-1.5 py-0.2 bg-navy-700 text-slate-300 rounded border border-navy-600">
                  {user.callsign}
                </span>
              </div>
              <div className="text-[11px] text-cyan font-mono">{user.role} • {user.sector}</div>
            </div>

            <button
              onClick={logout}
              className="p-2 rounded-lg text-slate-400 hover:text-red-400 hover:bg-navy-800 border border-transparent hover:border-red-900/40 transition-colors"
              title="End Session"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        ) : (
          <span className="text-xs text-slate-400">Guest Operator</span>
        )}
      </div>
    </header>
  );
};
