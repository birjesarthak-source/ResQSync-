import React, { useState } from 'react';
import {
  Settings,
  RefreshCw,
  CheckCircle2,
  AlertTriangle,
  XCircle,
  Database
} from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import {
  validateSupabaseConfig,
  testSupabaseConnection,
  ConnectionTestResult
} from '../lib/supabaseClient';

export const SettingsPage: React.FC = () => {
  const { toggleDemoMode, isDemoMode, refetchReports } = useIncidents();
  const [audioAlerts, setAudioAlerts] = useState(true);
  const [resetMessage, setResetMessage] = useState('');

  // Supabase Connection Test state
  const configStatus = validateSupabaseConfig();
  const [testResult, setTestResult] = useState<ConnectionTestResult | null>(null);
  const [isTesting, setIsTesting] = useState(false);

  const handleTestConnection = async () => {
    setIsTesting(true);
    try {
      const result = await testSupabaseConnection();
      setTestResult(result);
    } finally {
      setIsTesting(false);
    }
  };

  const handleReset = () => {
    refetchReports();
    setResetMessage('Live records refreshed from Supabase.');
    setTimeout(() => setResetMessage(''), 3000);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="pb-2 border-b border-slate-200">
        <h1 className="text-2xl font-extrabold text-slate-800 tracking-wide flex items-center gap-2">
          Dashboard System & Dispatch Settings
          <Settings className="w-5 h-5 text-blue-600" />
        </h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Configure real-time WebSocket channels, audio alarms, and database connections
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Connection Diagnostics Card */}
        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-xs space-y-4">
          <div className="flex items-center gap-2 text-blue-600 font-bold text-sm">
            <Database className="w-4 h-4" />
            <h2>SUPABASE DATABASE CONNECTION PROBE</h2>
          </div>

          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 text-xs space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-slate-500">Config Status:</span>
              <span className={`font-bold font-mono ${configStatus.isValid ? 'text-emerald-600' : 'text-amber-600'}`}>
                {configStatus.isValid ? 'Valid .env.local' : 'Missing Variables'}
              </span>
            </div>
            {configStatus.missingVars.length > 0 && (
              <div className="text-[11px] text-amber-600 font-mono">
                Missing: {configStatus.missingVars.join(', ')}
              </div>
            )}
          </div>

          <button
            onClick={handleTestConnection}
            disabled={isTesting}
            className="w-full py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-xs disabled:opacity-50 transition-colors"
          >
            <RefreshCw className={`w-4 h-4 ${isTesting ? 'animate-spin' : ''}`} />
            <span>Test Database Connection</span>
          </button>

          {testResult && (
            <div className={`p-3 rounded-xl border text-xs space-y-1.5 ${
              testResult.isConnected && testResult.isTableFound
                ? 'bg-emerald-50 border-emerald-200 text-emerald-800'
                : 'bg-amber-50 border-amber-200 text-amber-800'
            }`}>
              <div className="flex items-center gap-1.5 font-bold">
                {testResult.isConnected && testResult.isTableFound ? (
                  <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                ) : (
                  <AlertTriangle className="w-4 h-4 text-amber-600" />
                )}
                <span>{testResult.isConnected && testResult.isTableFound ? 'Connected to Supabase' : 'Diagnostics Result'}</span>
              </div>
              <p className="text-[11px] leading-snug">{testResult.diagnosticDetails}</p>
            </div>
          )}
        </div>

        {/* Operating Mode Card */}
        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-xs space-y-4">
          <div className="flex items-center gap-2 text-slate-800 font-bold text-sm">
            <Settings className="w-4 h-4 text-blue-600" />
            <h2>OPERATING MODE & PREFERENCES</h2>
          </div>

          <div className="p-4 bg-slate-50 rounded-xl border border-slate-200 space-y-3">
            <div className="flex items-center justify-between">
              <div>
                <span className="font-bold text-slate-800 text-xs block">Demo Preview Mode</span>
                <span className="text-[11px] text-slate-500 block">Switch between live feed & test data</span>
              </div>
              <button
                onClick={toggleDemoMode}
                className={`px-3 py-1 rounded-lg text-xs font-bold transition-colors ${
                  isDemoMode ? 'bg-amber-500 text-white' : 'bg-slate-200 text-slate-700'
                }`}
              >
                {isDemoMode ? 'DEMO MODE ON' : 'LIVE MODE'}
              </button>
            </div>

            <div className="flex items-center justify-between border-t border-slate-200 pt-3">
              <div>
                <span className="font-bold text-slate-800 text-xs block">Audio Alert Sounds</span>
                <span className="text-[11px] text-slate-500 block">Play sound chime for incoming reports</span>
              </div>
              <button
                onClick={() => setAudioAlerts((prev) => !prev)}
                className={`px-3 py-1 rounded-lg text-xs font-bold transition-colors ${
                  audioAlerts ? 'bg-emerald-600 text-white' : 'bg-slate-200 text-slate-700'
                }`}
              >
                {audioAlerts ? 'ENABLED' : 'MUTED'}
              </button>
            </div>
          </div>

          <button
            onClick={handleReset}
            className="w-full py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-xs transition-colors"
          >
            Force Sync Live Feed
          </button>

          {resetMessage && (
            <p className="text-xs text-center text-emerald-600 font-semibold">{resetMessage}</p>
          )}
        </div>
      </div>
    </div>
  );
};
