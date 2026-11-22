import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  AlertTriangle,
  MapPin,
  Radio,
  Network,
  Users,
  Settings,
  ShieldAlert,
  ChevronLeft,
  ChevronRight,
  X
} from 'lucide-react';

interface SidebarProps {
  isCollapsed: boolean;
  onToggleCollapse: () => void;
  isMobileOpen: boolean;
  onCloseMobile: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  isCollapsed,
  onToggleCollapse,
  isMobileOpen,
  onCloseMobile
}) => {
  const navItems = [
    { name: 'Dashboard Overview', path: '/', icon: LayoutDashboard },
    { name: 'Emergency Reports', path: '/reports', icon: AlertTriangle, badge: '6' },
    { name: 'Incident Map', path: '/map', icon: MapPin },
    { name: 'Rescue Updates', path: '/updates', icon: Radio },
    { name: 'Network Monitoring', path: '/network', icon: Network },
    { name: 'Team Management', path: '/teams', icon: Users },
    { name: 'Settings', path: '/settings', icon: Settings },
  ];

  return (
    <>
      {/* Mobile Backdrop */}
      {isMobileOpen && (
        <div
          onClick={onCloseMobile}
          className="fixed inset-0 z-40 bg-black/70 backdrop-blur-sm lg:hidden transition-opacity"
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed lg:static top-0 bottom-0 left-0 z-50 flex flex-col bg-navy-900 border-r border-navy-700 transition-all duration-300 ease-in-out ${
          isCollapsed ? 'w-20' : 'w-64'
        } ${isMobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}`}
      >
        {/* Brand Header */}
        <div className="h-16 flex items-center justify-between px-4 border-b border-navy-700 bg-navy-950/50">
          <div className="flex items-center gap-3 overflow-hidden">
            <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-red-600 to-red-700 flex items-center justify-center font-bold text-white shadow-lg shadow-red-900/40 shrink-0">
              <ShieldAlert className="w-6 h-6 text-white" />
            </div>
            {!isCollapsed && (
              <div className="leading-tight">
                <span className="font-extrabold text-white text-base tracking-wide flex items-center gap-1.5">
                  ResQSync <span className="text-cyan text-xs font-semibold px-1.5 py-0.5 rounded bg-cyan/10 border border-cyan/30">AI</span>
                </span>
                <span className="text-[10px] uppercase font-mono tracking-wider text-slate-400 block">
                  Rescue Command Deck
                </span>
              </div>
            )}
          </div>

          {/* Mobile close button */}
          <button
            onClick={onCloseMobile}
            className="p-1.5 rounded-md text-slate-400 hover:text-white hover:bg-navy-800 lg:hidden"
            aria-label="Close menu"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation Items */}
        <nav className="flex-1 py-4 px-3 space-y-1 overflow-y-auto">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={onCloseMobile}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all group relative ${
                    isActive
                      ? 'bg-cyan/15 text-cyan border border-cyan/40 shadow-sm shadow-cyan/10'
                      : 'text-slate-300 hover:text-white hover:bg-navy-800/80 border border-transparent'
                  }`
                }
              >
                <Icon className="w-5 h-5 shrink-0 transition-transform group-hover:scale-110" />
                {!isCollapsed && (
                  <span className="truncate flex-1 tracking-tight">{item.name}</span>
                )}
                {!isCollapsed && item.badge && (
                  <span className="px-2 py-0.5 text-[11px] font-bold rounded-full bg-red-500/20 text-red-400 border border-red-500/30">
                    {item.badge}
                  </span>
                )}
                {isCollapsed && (
                  <div className="absolute left-full ml-2 px-2 py-1 bg-navy-800 text-white text-xs rounded shadow-lg whitespace-nowrap opacity-0 pointer-events-none group-hover:opacity-100 transition-opacity z-50">
                    {item.name}
                  </div>
                )}
              </NavLink>
            );
          })}
        </nav>

        {/* System Status Footnote */}
        <div className="p-3 border-t border-navy-700 bg-navy-950/40">
          {!isCollapsed ? (
            <div className="bg-navy-850 p-2.5 rounded-lg border border-navy-700/60">
              <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
                <span>Mesh Node</span>
                <span className="flex items-center gap-1.5 text-emerald-400 font-mono text-[11px]">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                  GATEWAY
                </span>
              </div>
              <p className="text-[11px] text-slate-400 leading-snug">
                Store-and-forward mesh active. 6 peer relays online.
              </p>
            </div>
          ) : (
            <div className="flex justify-center">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" title="Gateway Online" />
            </div>
          )}

          {/* Desktop collapse toggle */}
          <button
            onClick={onToggleCollapse}
            className="hidden lg:flex w-full mt-2 items-center justify-center p-2 rounded-md text-slate-400 hover:text-white hover:bg-navy-800 text-xs gap-1.5 transition-colors"
          >
            {isCollapsed ? <ChevronRight className="w-4 h-4" /> : <><ChevronLeft className="w-4 h-4" /> Collapse</>}
          </button>
        </div>
      </aside>
    </>
  );
};
