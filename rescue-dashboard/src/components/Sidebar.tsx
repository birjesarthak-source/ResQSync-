import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  AlertTriangle,
  MapPin,
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
    { name: 'Overview', path: '/', icon: LayoutDashboard },
    { name: 'Emergency Reports', path: '/reports', icon: AlertTriangle },
    { name: 'Live Map', path: '/map', icon: MapPin },
    { name: 'Rescue Teams', path: '/teams', icon: Users },
    { name: 'Settings', path: '/settings', icon: Settings },
  ];

  return (
    <>
      {/* Mobile Backdrop */}
      {isMobileOpen && (
        <div
          onClick={onCloseMobile}
          className="fixed inset-0 z-40 bg-slate-900/40 backdrop-blur-sm lg:hidden transition-opacity"
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed lg:static top-0 bottom-0 left-0 z-50 flex flex-col bg-white border-r border-slate-200/80 transition-all duration-300 ease-in-out ${
          isCollapsed ? 'w-20' : 'w-64'
        } ${isMobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}`}
      >
        {/* Brand Header */}
        <div className="h-20 flex items-center justify-between px-5 border-b border-slate-100">
          <div className="flex items-center gap-3 overflow-hidden">
            <div className="w-11 h-11 rounded-xl bg-blue-600 flex items-center justify-center font-bold text-white shadow-md shadow-blue-600/20 shrink-0">
              <ShieldAlert className="w-6 h-6 text-white" />
            </div>
            {!isCollapsed && (
              <div className="leading-tight">
                <span className="font-extrabold text-slate-800 text-base tracking-wide flex items-center gap-1.5">
                  ResQSync <span className="text-blue-600 text-xs font-bold px-1.5 py-0.5 rounded bg-blue-50 border border-blue-200">AI</span>
                </span>
                <span className="text-xs text-slate-500 block mt-1">
                  Rescue command
                </span>
              </div>
            )}
          </div>

          {/* Mobile close button */}
          <button
            onClick={onCloseMobile}
            className="p-1.5 rounded-md text-slate-500 hover:text-slate-800 hover:bg-slate-100 lg:hidden"
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
                      ? 'bg-blue-50 text-blue-600 font-bold border border-blue-200 shadow-sm'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100 border border-transparent'
                  }`
                }
              >
                <Icon className="w-5 h-5 shrink-0 transition-transform group-hover:scale-110" />
                {!isCollapsed && (
                  <span className="truncate flex-1 tracking-tight">{item.name}</span>
                )}
                {isCollapsed && (
                  <div className="absolute left-full ml-2 px-2 py-1 bg-slate-800 text-white text-xs rounded shadow-lg whitespace-nowrap opacity-0 pointer-events-none group-hover:opacity-100 transition-opacity z-50">
                    {item.name}
                  </div>
                )}
              </NavLink>
            );
          })}
        </nav>

        {/* System Status Footnote */}
        <div className="p-3 border-t border-slate-200 bg-slate-50/50">
          {!isCollapsed ? (
            <div className="bg-white p-2.5 rounded-lg border border-slate-200 shadow-xs">
              <div className="flex items-center justify-between text-xs text-slate-500 mb-1">
                <span>Mesh Node</span>
                <span className="flex items-center gap-1.5 text-emerald-600 font-mono text-[11px] font-bold">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                  GATEWAY
                </span>
              </div>
              <p className="text-[11px] text-slate-500 leading-snug">
                Store-and-forward mesh active. Syncing automatically with cloud.
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
            className="hidden lg:flex w-full mt-2 items-center justify-center p-2 rounded-md text-slate-500 hover:text-slate-800 hover:bg-slate-100 text-xs gap-1.5 transition-colors"
          >
            {isCollapsed ? <ChevronRight className="w-4 h-4" /> : <><ChevronLeft className="w-4 h-4" /> Collapse</>}
          </button>
        </div>
      </aside>
    </>
  );
};
