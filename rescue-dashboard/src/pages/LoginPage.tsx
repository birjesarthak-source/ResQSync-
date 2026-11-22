import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldAlert, LogIn, Lock, Mail, Radio, UserCheck, KeyRound, AlertCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const LoginPage: React.FC = () => {
  const { loginWithPassword, loginDemo, authError } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [localError, setLocalError] = useState('');

  const handleSupabaseLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLocalError('');
    if (!email || !password) {
      setLocalError('Please enter both email and password.');
      return;
    }

    setIsSubmitting(true);
    const result = await loginWithPassword(email, password);
    setIsSubmitting(false);

    if (result.success) {
      navigate('/');
    } else {
      setLocalError(result.error || 'Failed to authenticate with Supabase');
    }
  };

  const handleQuickDemoLogin = (role: 'admin' | 'coordinator' | 'field_responder') => {
    loginDemo(role);
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

        {/* Security / RLS Notice */}
        <div className="mb-6 p-3 rounded-xl bg-navy-950 border border-navy-800 text-xs text-slate-300 flex items-start gap-2.5">
          <Lock className="w-4 h-4 text-cyan shrink-0 mt-0.5" />
          <div>
            <strong className="font-semibold text-cyan">RLS Protected Access:</strong>
            <p className="mt-0.5 text-slate-400 leading-tight">
              Sign in with your Supabase account to access live disaster records governed by Row Level Security.
            </p>
          </div>
        </div>

        {(localError || authError) && (
          <div className="mb-4 p-3 rounded-lg bg-red-500/15 border border-red-500/40 text-red-400 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{localError || authError}</span>
          </div>
        )}

        {/* Supabase Password Login Form */}
        <form onSubmit={handleSupabaseLogin} className="space-y-4">
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
                placeholder="coordinator@rescue.org"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
              Password
            </label>
            <div className="relative">
              <KeyRound className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full bg-navy-950 border border-navy-700 rounded-lg pl-9 pr-3 py-2 text-sm text-white focus:outline-none focus:border-cyan transition-colors"
                placeholder="••••••••"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full mt-2 py-2.5 px-4 rounded-lg bg-cyan hover:bg-cyan-glow disabled:opacity-50 text-navy-950 font-bold text-sm flex items-center justify-center gap-2 shadow-lg shadow-cyan/20 transition-all hover:translate-y-[-1px]"
          >
            <LogIn className="w-4 h-4" />
            {isSubmitting ? 'Authenticating...' : 'Sign In to Supabase Command'}
          </button>
        </form>

        {/* Quick Demo Session Option */}
        <div className="mt-6 pt-5 border-t border-navy-800">
          <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 block mb-2 text-center">
            Or Access via Local Demo Session
          </span>
          <div className="grid grid-cols-2 gap-2 text-xs">
            <button
              type="button"
              onClick={() => handleQuickDemoLogin('coordinator')}
              className="p-2.5 rounded-lg bg-navy-950 hover:bg-navy-800 text-left border border-navy-800 hover:border-cyan/40 text-slate-200 transition-colors"
            >
              <div className="font-bold text-cyan text-[11px]">Rescue Coordinator</div>
              <div className="text-[10px] text-slate-400">Sector Dispatcher</div>
            </button>
            <button
              type="button"
              onClick={() => handleQuickDemoLogin('admin')}
              className="p-2.5 rounded-lg bg-navy-950 hover:bg-navy-800 text-left border border-navy-800 hover:border-cyan/40 text-slate-200 transition-colors"
            >
              <div className="font-bold text-cyan text-[11px]">System Admin</div>
              <div className="text-[10px] text-slate-400">Full Oversight</div>
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
