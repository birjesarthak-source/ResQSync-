import React, { useState } from 'react';
import {
  Settings,
  Volume2,
  Radio,
  Database,
  Bot,
  RefreshCw,
  CheckCircle2,
  AlertTriangle,
  Play,
  ShieldCheck,
  XCircle,
  HelpCircle
} from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import {
  validateSupabaseConfig,
  testSupabaseConnection,
  ConnectionTestResult
} from '../lib/supabaseClient';

export const SettingsPage: React.FC = () => {
  const { resetToInitialMock } = useIncidents();
  const [audioAlerts, setAudioAlerts] = useState(true);
  const [pollingInterval, setPollingInterval] = useState('15');
  const [mapStyle, setMapStyle] = useState('tactical');
  const [aiAnalysisMode, setAiAnalysisMode] = useState('hybrid');
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
    resetToInitialMock();
    setResetMessage('Simulated data reset to initial default state.');
    setTimeout(() => setResetMessage(''), 3000);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="pb-2 border-b border-navy-800">
        <h1 className="text-2xl font-extrabold text-white tracking-wide flex items-center gap-2">
          Dashboard System & Dispatch Settings
          <Settings className="w-5 h-5 text-cyan" />
        </h1>
        <p className="text-xs text-slate-400 mt-0.5">
          Configure interface alerts, mesh polling frequencies, and backend connector policies
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Operational Preferences */}
        <div className="bg-navy-900 border border-navy-700 rounded-2xl p-5 shadow-2xl space-y-5">
          <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
            <Radio className="w-4 h-4 text-cyan" />
            Dispatch Console Preferences
          </h2>

          <div className="space-y-4 text-xs">
            {/* Audio Alerts */}
            <div className="flex items-center justify-between p-3 rounded-xl bg-navy-950 border border-navy-800">
              <div className="space-y-0.5">
                <span className="font-bold text-slate-200 flex items-center gap-1.5">
                  <Volume2 className="w-4 h-4 text-cyan" />
                  Critical Incident Audible Beacon
                </span>
                <span className="text-[11px] text-slate-400 block">
                  Sound klaxon audio tone whenever a CRITICAL emergency arrives
                </span>
              </div>
              <input
                type="checkbox"
                checked={audioAlerts}
                onChange={(e) => setAudioAlerts(e.target.checked)}
                className="w-4 h-4 accent-cyan cursor-pointer"
              />
            </div>

            {/* Mesh Polling */}
            <div className="p-3 rounded-xl bg-navy-950 border border-navy-800 space-y-1.5">
              <span className="font-bold text-slate-200 block">
                Mesh Beacon Refresh Rate
              </span>
              <span className="text-[11px] text-slate-400 block mb-2">
                Interval to probe local store-and-forward gateway uplink
              </span>
              <select
                value={pollingInterval}
                onChange={(e) => setPollingInterval(e.target.value)}
                className="w-full bg-navy-900 border border-navy-700 rounded-lg p-2 text-xs text-white focus:outline-none focus:border-cyan"
              >
                <option value="10">10 Seconds (High Alert / Emergency Surge)</option>
                <option value="15">15 Seconds (Recommended Standard)</option>
                <option value="30">30 Seconds (Battery Conservative)</option>
                <option value="60">60 Seconds (Low Power Standby)</option>
              </select>
            </div>

            {/* Map Theme */}
            <div className="p-3 rounded-xl bg-navy-950 border border-navy-800 space-y-1.5">
              <span className="font-bold text-slate-200 block">
                Tactical Geolocation Map Display Mode
              </span>
              <select
                value={mapStyle}
                onChange={(e) => setMapStyle(e.target.value)}
                className="w-full bg-navy-900 border border-navy-700 rounded-lg p-2 text-xs text-white focus:outline-none focus:border-cyan"
              >
                <option value="tactical">Tactical Navy Grid (Optimized for Low-Light Command Centers)</option>
                <option value="high-contrast">High Contrast Emergency Red/Cyan</option>
                <option value="topographic">Topographic Elevation Contours</option>
              </select>
            </div>

            {/* AI Engine */}
            <div className="p-3 rounded-xl bg-navy-950 border border-navy-800 space-y-1.5">
              <span className="font-bold text-slate-200 flex items-center gap-1.5">
                <Bot className="w-4 h-4 text-cyan" />
                Gemini AI Triage Engine Mode
              </span>
              <select
                value={aiAnalysisMode}
                onChange={(e) => setAiAnalysisMode(e.target.value)}
                className="w-full bg-navy-900 border border-navy-700 rounded-lg p-2 text-xs text-white focus:outline-none focus:border-cyan"
              >
                <option value="hybrid">Hybrid (On-Device Nano + Cloud Synthesis)</option>
                <option value="local-only">Local Offline Triage Only (Zero Bandwidth)</option>
                <option value="cloud-first">Deep Cloud Synthesis (High Bandwidth)</option>
              </select>
            </div>
          </div>
        </div>

        {/* Supabase Shared Backend Status & Testing Tools */}
        <div className="bg-navy-900 border border-navy-700 rounded-2xl p-5 shadow-2xl space-y-5">
          <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
            <Database className="w-4 h-4 text-cyan" />
            Supabase Cloud Backend Integration
          </h2>

          <div className="p-4 rounded-xl bg-navy-950 border border-navy-800 space-y-3 text-xs">
            {/* Config Status */}
            <div className="flex items-center justify-between">
              <span className="text-slate-400">Client Configuration (.env.local):</span>
              <span
                className={`px-2 py-0.5 rounded font-mono font-bold text-[11px] border ${
                  configStatus.isValid
                    ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                    : 'bg-red-500/20 text-red-400 border-red-500/30'
                }`}
              >
                {configStatus.isValid ? 'DETECTED & LOADED' : 'MISSING / INCOMPLETE'}
              </span>
            </div>

            <div className="space-y-1 text-slate-300 font-mono text-[11px]">
              <div>Target Table: <span className="text-cyan">public.emergency_reports</span></div>
              <div>Telemetry Table: <span className="text-cyan">public.network_messages</span></div>
              <div>Schema Script: <span className="text-slate-400">documentation/supabase-schema.sql</span></div>
            </div>

            {/* Connection Test Action */}
            <div className="pt-2 border-t border-navy-800 flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <span className="text-slate-300 font-semibold text-xs">Safe Read Probe Test:</span>
                <button
                  onClick={handleTestConnection}
                  disabled={isTesting || !configStatus.isValid}
                  className="px-3 py-1.5 rounded-lg bg-cyan hover:bg-cyan-glow disabled:opacity-50 text-navy-950 font-bold text-xs flex items-center gap-1.5 transition-colors"
                >
                  {isTesting ? (
                    <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                  ) : (
                    <Play className="w-3.5 h-3.5" />
                  )}
                  {isTesting ? 'Testing Read Access...' : 'Test Supabase Connection'}
                </button>
              </div>

              {/* Test Diagnostics Output */}
              {testResult && (
                <div
                  className={`mt-2 p-3 rounded-lg border text-xs leading-relaxed space-y-1.5 ${
                    testResult.isConnected && testResult.isTableFound
                      ? 'bg-emerald-950/40 border-emerald-500/40 text-emerald-300'
                      : testResult.isConnected && !testResult.isTableFound
                      ? 'bg-amber-950/40 border-amber-500/40 text-amber-300'
                      : 'bg-red-950/40 border-red-500/40 text-red-300'
                  }`}
                >
                  <div className="flex items-center gap-2 font-bold">
                    {testResult.isConnected && testResult.isTableFound ? (
                      <>
                        <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0" />
                        <span>Database Read Verified</span>
                      </>
                    ) : testResult.isConnected && !testResult.isTableFound ? (
                      <>
                        <HelpCircle className="w-4 h-4 text-amber-400 shrink-0" />
                        <span>Endpoint Connected • Schema & RLS Setup Needed</span>
                      </>
                    ) : (
                      <>
                        <XCircle className="w-4 h-4 text-red-400 shrink-0" />
                        <span>Connection Test Failed</span>
                      </>
                    )}
                  </div>

                  <p className="text-[11px] text-slate-200">
                    {testResult.diagnosticDetails}
                  </p>

                  <div className="pt-1 text-[10px] font-mono text-slate-400 flex justify-between">
                    <span>Table Checked: {testResult.tableChecked}</span>
                    <span>Tested at: {new Date(testResult.checkedAt).toLocaleTimeString()}</span>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Reset Mock State Action */}
          <div className="p-4 rounded-xl bg-navy-950 border border-navy-800 space-y-2">
            <span className="text-xs font-bold text-white block">Simulation Test Data Controls</span>
            <p className="text-[11px] text-slate-400">
              Reset all simulated reports, dispatch updates, and team states back to default seed records.
            </p>
            {resetMessage && (
              <div className="p-2 rounded bg-emerald-500/20 text-emerald-300 text-xs flex items-center gap-1.5">
                <CheckCircle2 className="w-3.5 h-3.5" />
                {resetMessage}
              </div>
            )}
            <button
              onClick={handleReset}
              className="px-3.5 py-2 rounded-lg bg-navy-800 hover:bg-navy-700 text-slate-200 border border-navy-700 text-xs font-semibold flex items-center gap-2 transition-colors"
            >
              <RefreshCw className="w-3.5 h-3.5 text-cyan" />
              Reset All Mock Data
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
