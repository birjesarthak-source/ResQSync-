import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldAlert, LogIn, Lock, Mail, Radio, UserCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('arjun.varma@resqsync.internal');
  const [callsign, setCallsign] = useState('Apex-Lead');
  const [role, setRole] = useState<'COMMANDER' | 'DISPATCHER' | 'FIELD_LEAD'>('COMMANDER');
  const [error, setError] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !email.includes('@')) {
      setError('Please provide a valid operator email address');
      return;
    }
    const success = login(email, callsign, role);
    if (success) {
      navigate('/');
    } else {
      setError('Invalid operator credentials');
    }
  };

  const handleQuickLogin = (presetEmail: string, presetCallsign: string, presetRole: any) => {
    setEmail(presetEmail);
    setCallsign(presetCallsign);
    setRole(presetRole);
    login(presetEmail, presetCallsign, presetRole);
    navigate('/');
  };

  return (
    <div className="min-h-screen w-screen flex flex-col justify-center items-center bg-navy-950 p-4 relative overflow-hidden font-sans">
      {/* Background Tactical Grid Pattern */}
      <div className="absolute inset-0 bg-[radial-gradient(#14314e_1px,transparent_1px)] [background-size:24px_24px] opacity-40 pointer-events-none" />

      {/* Login Card */}
      <div className="w-full max-w-md bg-navy-900 border border-navy-700 rounded-2xl shadow-2xl p-6 sm:p-8 z-10 relative">
        {/* Brand Header */}
        <div className="text-center mb-6">
          <div className="inline-flex w-14 h-14 rounded-2xl bg-gradient-to-br from-red-600 to-red-700 items-center justify-center text-white shadow-xl shadow-red-900/50 mb-3">
            <ShieldAlert className="w-8 h-8" />
          </div>
          <h1 className="text-2xl font-black tracking-wide text-white flex items-center justify-center gap-2">
            ResQSync <span className="text-cyan text-sm font-semibold px-2 py-0.5 rounded bg-cyan/10 border border-cyan/30">AI</span>
          </h1>
          <p className="text-xs uppercase font-mono tracking-widest text-slate-400 mt-1">
            Rescue Command & Dispatch System
          </p>
        </div>

        {/* Development Notice */}
        <div className="mb-6 p-3 rounded-xl bg-amber-500/10 border border-amber-500/30 text-xs text-amber-300 flex items-start gap-2.5">
          <Radio className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
          <div>
            <strong className="font-semibold text-amber-200">UI Development Mode:</strong>
            <p className="mt-0.5 text-amber-300/90 leading-tight">
              Select an operator profile below or log in to access the test command deck.
            </p>
          </div>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-500/15 border border-red-500/40 text-red-400 text-xs">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
              Operator Email
            </label>
            <div className="relative">
              <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full bg-navy-950 border border-navy-700 rounded-lg pl-9 pr-3 py-2 text-sm text-white focus:outline-none focus:border-cyan transition-colors"
                placeholder="operator@resqsync.internal"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
              Radio Callsign
            </label>
            <div className="relative">
              <Radio className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="text"
                value={callsign}
                onChange={(e) => setCallsign(e.target.value)}
                required
                className="w-full bg-navy-950 border border-navy-700 rounded-lg pl-9 pr-3 py-2 text-sm text-white focus:outline-none focus:border-cyan transition-colors"
                placeholder="e.g. Apex-Lead"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
              Duty Role
            </label>
            <select
              value={role}
              onChange={(e) => setRole(e.target.value as any)}
              className="w-full bg-navy-950 border border-navy-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan transition-colors"
            >
              <option value="COMMANDER">Incident Commander (Full Oversight)</option>
              <option value="DISPATCHER">Sector Dispatcher (Resource Logistics)</option>
              <option value="FIELD_LEAD">Field Team Lead (On-Site Coordinator)</option>
            </select>
          </div>

          <button
            type="submit"
            className="w-full mt-2 py-2.5 px-4 rounded-lg bg-cyan hover:bg-cyan-glow text-navy-950 font-bold text-sm flex items-center justify-center gap-2 shadow-lg shadow-cyan/20 transition-all hover:translate-y-[-1px]"
          >
            <LogIn className="w-4 h-4" />
            Enter Command Dashboard
          </button>
        </form>

        {/* Quick Login Presets for testing */}
        <div className="mt-6 pt-5 border-t border-navy-800">
          <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 block mb-2 text-center">
            Quick-Select Test Operators
          </span>
          <div className="grid grid-cols-2 gap-2 text-xs">
            <button
              onClick={() => handleQuickLogin('arjun.varma@resqsync.internal', 'Apex-Lead', 'COMMANDER')}
              className="p-2 rounded-lg bg-navy-800 hover:bg-navy-700 text-left border border-navy-700 text-slate-200 transition-colors"
            >
              <div className="font-bold text-cyan text-[11px]">Capt. Varma</div>
              <div className="text-[10px] text-slate-400">Commander HQ</div>
            </button>
            <button
              onClick={() => handleQuickLogin('priya.sharma@resqsync.internal', 'River-Watch', 'DISPATCHER')}
              className="p-2 rounded-lg bg-navy-800 hover:bg-navy-700 text-left border border-navy-700 text-slate-200 transition-colors"
            >
              <div className="font-bold text-cyan text-[11px]">Priya Sharma</div>
              <div className="text-[10px] text-slate-400">Sector 4 Dispatch</div>
            </button>
          </div>
        </div>
      </div>

      <p className="text-center text-xs text-slate-400 mt-6 z-10">
        ResQSync AI • Offline Mesh Emergency Management Platform
      </p>
    </div>
  );
};
