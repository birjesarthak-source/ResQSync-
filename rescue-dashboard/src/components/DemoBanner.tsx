import React from 'react';
import { AlertCircle, Database, RotateCcw } from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { validateSupabaseConfig } from '../lib/supabaseClient';

export const DemoBanner: React.FC = () => {
  const { resetToInitialMock } = useIncidents();
  const config = validateSupabaseConfig();

  return (
    <div className="bg-amber-500/10 border-b border-amber-500/30 px-4 py-2 flex flex-wrap items-center justify-between gap-2 text-xs text-amber-300">
      <div className="flex items-center gap-2 font-medium">
        <AlertCircle className="w-4 h-4 text-amber-400 shrink-0" />
        <span>
          <strong className="font-semibold text-amber-200">UI DEVELOPMENT MODE:</strong> All records below are simulated test fixtures for interface testing. Do not use for real emergency dispatch.
        </span>
      </div>

      <div className="flex items-center gap-3">
        <span className="flex items-center gap-1.5 text-amber-400 font-mono text-[11px]">
          <Database className="w-3.5 h-3.5" />
          {config.isValid ? 'Supabase: Credentials Detected' : 'Supabase: Credentials Unset'}
        </span>
        <button
          onClick={resetToInitialMock}
          className="flex items-center gap-1 px-2 py-0.5 rounded bg-amber-500/20 hover:bg-amber-500/30 border border-amber-500/40 text-amber-200 text-[11px] font-semibold transition-colors"
          title="Reset simulated data to initial state"
        >
          <RotateCcw className="w-3 h-3" />
          Reset Mock Data
        </button>
      </div>
    </div>
  );
};
