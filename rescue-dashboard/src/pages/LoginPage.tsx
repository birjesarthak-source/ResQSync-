import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldAlert, LogIn, Lock, Mail, KeyRound, AlertCircle } from 'lucide-react';
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
    <div className="min-h-screen w-screen flex flex-col justify-center items-center bg-slate-50 p-4 relative overflow-hidden font-sans text-slate-800">
      {/* Background Subtle Grid Pattern */}
      <div className="absolute inset-0 bg-[radial-gradient(#cbd5e1_1px,transparent_1px)] [background-size:24px_24px] opacity-40 pointer-events-none" />

      {/* Login Card */}
      <div className="w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 sm:p-8 z-10 relative">
        {/* Brand Header */}
        <div className="text-center mb-6">
          <div className="inline-flex w-14 h-14 rounded-2xl bg-blue-600 items-center justify-center text-white shadow-lg shadow-blue-500/30 mb-3">
            <ShieldAlert className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-2xl font-black tracking-wide text-slate-800 flex items-center justify-center gap-2">
            ResQSync <span className="text-blue-600 text-xs font-bold px-2 py-0.5 rounded bg-blue-50 border border-blue-200">AI</span>
          </h1>
          <p className="text-xs uppercase font-mono tracking-widest text-slate-500 mt-1">
            Rescue Command & Dispatch System
          </p>
        </div>

        {/* Security / RLS Notice */}
        <div className="mb-6 p-3 rounded-xl bg-blue-50 border border-blue-200 text-xs text-blue-900 flex items-start gap-2.5">
          <Lock className="w-4 h-4 text-blue-600 shrink-0 mt-0.5" />
          <div>
            <strong className="font-bold text-blue-800">RLS Protected Access:</strong>
            <p className="mt-0.5 text-blue-700 leading-tight">
              Sign in with your Supabase account to access live disaster records governed by Row Level Security.
            </p>
          </div>
        </div>

        {(localError || authError) && (
          <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{localError || authError}</span>
          </div>
        )}

        {/* Supabase Password Login Form */}
        <form onSubmit={handleSupabaseLogin} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5">
              Operator Email
            </label>
            <div className="relative">
              <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full bg-slate-50 border border-slate-200 rounded-lg pl-9 pr-3 py-2 text-sm text-slate-800 placeholder-slate-400 focus:outline-none focus:border-blue-600 transition-colors"
                placeholder="coordinator@rescue.org"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5">
              Password
            </label>
            <div className="relative">
              <KeyRound className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full bg-slate-50 border border-slate-200 rounded-lg pl-9 pr-3 py-2 text-sm text-slate-800 placeholder-slate-400 focus:outline-none focus:border-blue-600 transition-colors"
                placeholder="••••••••"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full mt-2 py-2.5 px-4 rounded-lg bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-bold text-sm flex items-center justify-center gap-2 shadow-md shadow-blue-500/20 transition-all hover:translate-y-[-1px]"
          >
            <LogIn className="w-4 h-4" />
            {isSubmitting ? 'Authenticating...' : 'Sign In to Supabase Command'}
          </button>
        </form>

        {/* Quick Demo Session Option */}
        <div className="mt-6 pt-5 border-t border-slate-200">
          <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-500 block mb-2 text-center">
            Or Access via Local Demo Session
          </span>
          <div className="grid grid-cols-2 gap-2 text-xs">
            <button
              type="button"
              onClick={() => handleQuickDemoLogin('coordinator')}
              className="p-2.5 rounded-lg bg-slate-50 hover:bg-slate-100 text-left border border-slate-200 text-slate-800 transition-colors"
            >
              <div className="font-bold text-blue-600 text-[11px]">Rescue Coordinator</div>
              <div className="text-[10px] text-slate-500">Sector Dispatcher</div>
            </button>
            <button
              type="button"
              onClick={() => handleQuickDemoLogin('admin')}
              className="p-2.5 rounded-lg bg-slate-50 hover:bg-slate-100 text-left border border-slate-200 text-slate-800 transition-colors"
            >
              <div className="font-bold text-blue-600 text-[11px]">System Admin</div>
              <div className="text-[10px] text-slate-500">Full Oversight</div>
            </button>
          </div>
        </div>
      </div>

      <p className="text-center text-xs text-slate-500 mt-6 z-10">
        ResQSync AI • Offline Mesh Emergency Management Platform
      </p>
    </div>
  );
};
