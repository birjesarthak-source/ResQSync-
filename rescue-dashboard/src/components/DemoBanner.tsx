import React from 'react';
import { Database, Radio, Sparkles, ShieldCheck, Lock } from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { useAuth } from '../context/AuthContext';
import { validateSupabaseConfig } from '../lib/supabaseClient';

export const DemoBanner: React.FC = () => {
  const { isDemoMode, toggleDemoMode, isRealtimeConnected } = useIncidents();
  const { isSupabaseAuth, user } = useAuth();
  const config = validateSupabaseConfig();

  return (
    <div
      className={`border-b px-4 py-2 flex flex-wrap items-center justify-between gap-2 text-xs transition-colors ${
        isDemoMode
          ? 'bg-amber-500/10 border-amber-500/30 text-amber-300'
          : 'bg-navy-900 border-navy-800 text-slate-300'
      }`}
    >
      <div className="flex items-center gap-2 font-medium">
        {isDemoMode ? (
          <>
            <Sparkles className="w-4 h-4 text-amber-400 shrink-0" />
            <span>
              <strong className="font-semibold text-amber-200">DEMO PREVIEW MODE:</strong> Displaying simulated test fixtures. Live database sync is currently paused.
            </span>
          </>
        ) : (
          <>
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse shrink-0" />
            <span>
              <strong className="font-semibold text-white">LIVE PRODUCTION FEED:</strong> Streaming directly from <span className="font-mono text-cyan">public.emergency_reports</span>.
            </span>
          </>
        )}
      </div>

      <div className="flex items-center gap-3">
        {/* Realtime Status */}
        <span className="flex items-center gap-1.5 text-slate-400 font-mono text-[11px]">
          <Radio className={`w-3.5 h-3.5 ${isRealtimeConnected ? 'text-emerald-400' : 'text-slate-500'}`} />
          {isRealtimeConnected ? 'Realtime WebSocket: Connected' : 'Realtime: Standby'}
        </span>

        {/* Auth Status */}
        <span className="flex items-center gap-1.5 font-mono text-[11px]">
          {isSupabaseAuth ? (
            <span className="text-emerald-400 flex items-center gap-1">
              <ShieldCheck className="w-3.5 h-3.5" />
              Auth: {user?.role.toUpperCase()}
            </span>
          ) : (
            <span className="text-amber-400 flex items-center gap-1" title="Row Level Security restricts read access to authenticated accounts">
              <Lock className="w-3.5 h-3.5" />
              RLS: Auth Required
            </span>
          )}
        </span>

        {/* Demo Mode Toggle Button */}
        <button
          onClick={toggleDemoMode}
          className={`px-2.5 py-0.5 rounded text-[11px] font-semibold border transition-colors ${
            isDemoMode
              ? 'bg-cyan hover:bg-cyan-glow text-navy-950 border-cyan font-bold'
              : 'bg-navy-800 hover:bg-navy-700 text-slate-300 border-navy-700'
          }`}
        >
          {isDemoMode ? 'Switch to Live Feed' : 'Preview Demo Data'}
        </button>
      </div>
    </div>
  );
};
