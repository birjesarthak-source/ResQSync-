import React, { createContext, useContext, useState, useEffect } from 'react';
import { supabase } from '../lib/supabaseClient';
import { User } from '../types';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isSupabaseAuth: boolean;
  isLoading: boolean;
  authError: string | null;
  loginWithPassword: (email: string, password: string) => Promise<{ success: boolean; error?: string }>;
  signUpWithPassword: (email: string, password: string, callsign: string, role: User['role']) => Promise<{ success: boolean; error?: string }>;
  loginDemo: (role?: User['role']) => void;
  logout: () => Promise<void>;
}

const DEFAULT_DEMO_USER: User = {
  id: 'usr-coordinator-01',
  name: 'Commander Arjun Varma',
  callsign: 'Apex-Lead',
  role: 'coordinator',
  sector: 'Command HQ (Sector 1)',
  email: 'arjun.varma@resqsync.internal'
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isSupabaseAuth, setIsSupabaseAuth] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [authError, setAuthError] = useState<string | null>(null);

  // Sync Supabase Auth session on mount
  useEffect(() => {
    let mounted = true;

    async function initAuth() {
      if (!supabase) {
        // Fallback to local storage demo session if Supabase client not available
        const saved = localStorage.getItem('resqsync_demo_auth');
        if (saved && mounted) {
          try {
            setUser(JSON.parse(saved));
          } catch {
            setUser(DEFAULT_DEMO_USER);
          }
        } else if (mounted) {
          setUser(DEFAULT_DEMO_USER);
        }
        setIsLoading(false);
        return;
      }

      try {
        const { data: { session } } = await supabase.auth.getSession();
        if (session && session.user && mounted) {
          const metadata = session.user.user_metadata || {};
          setUser({
            id: session.user.id,
            name: metadata.name || session.user.email?.split('@')[0].toUpperCase() || 'OPERATOR',
            callsign: metadata.callsign || 'Tactical-01',
            role: (metadata.role as User['role']) || 'coordinator',
            sector: metadata.sector || 'Sector 1 Command Deck',
            email: session.user.email || 'operator@resqsync.internal'
          });
          setIsSupabaseAuth(true);
        } else if (mounted) {
          // Check for saved local demo session
          const saved = localStorage.getItem('resqsync_demo_auth');
          if (saved) {
            try {
              setUser(JSON.parse(saved));
              setIsSupabaseAuth(false);
            } catch {
              setUser(DEFAULT_DEMO_USER);
            }
          } else {
            setUser(DEFAULT_DEMO_USER);
          }
        }
      } catch (err: any) {
        console.warn('[Auth] Failed to restore Supabase session:', err?.message);
      } finally {
        if (mounted) setIsLoading(false);
      }
    }

    initAuth();

    // Listen to real-time auth changes from Supabase
    if (supabase) {
      const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
        if (session?.user) {
          const metadata = session.user.user_metadata || {};
          setUser({
            id: session.user.id,
            name: metadata.name || session.user.email?.split('@')[0].toUpperCase() || 'OPERATOR',
            callsign: metadata.callsign || 'Tactical-01',
            role: (metadata.role as User['role']) || 'coordinator',
            sector: metadata.sector || 'Sector 1 Command Deck',
            email: session.user.email || 'operator@resqsync.internal'
          });
          setIsSupabaseAuth(true);
        } else {
          setIsSupabaseAuth(false);
        }
      });

      return () => {
        mounted = false;
        subscription.unsubscribe();
      };
    }

    return () => {
      mounted = false;
    };
  }, []);

  const loginWithPassword = async (email: string, password: string): Promise<{ success: boolean; error?: string }> => {
    setAuthError(null);
    if (!supabase) {
      return { success: false, error: 'Supabase client is not initialised.' };
    }

    try {
      const { data, error } = await supabase.auth.signInWithPassword({
        email,
        password
      });

      if (error) {
        setAuthError(error.message);
        return { success: false, error: error.message };
      }

      if (data.user) {
        const metadata = data.user.user_metadata || {};
        setUser({
          id: data.user.id,
          name: metadata.name || email.split('@')[0].toUpperCase(),
          callsign: metadata.callsign || 'Tactical-01',
          role: (metadata.role as User['role']) || 'coordinator',
          sector: metadata.sector || 'Sector 1 Command Deck',
          email
        });
        setIsSupabaseAuth(true);
        localStorage.removeItem('resqsync_demo_auth');
        return { success: true };
      }

      return { success: false, error: 'Unknown authentication response' };
    } catch (err: any) {
      setAuthError(err?.message || 'Authentication failed');
      return { success: false, error: err?.message };
    }
  };

  const signUpWithPassword = async (
    email: string,
    password: string,
    callsign: string,
    role: User['role']
  ): Promise<{ success: boolean; error?: string }> => {
    setAuthError(null);
    if (!supabase) {
      return { success: false, error: 'Supabase client is not initialised.' };
    }

    try {
      const { data, error } = await supabase.auth.signUp({
        email,
        password,
        options: {
          data: {
            callsign,
            role,
            name: email.split('@')[0].toUpperCase(),
            sector: 'Sector 1'
          }
        }
      });

      if (error) {
        setAuthError(error.message);
        return { success: false, error: error.message };
      }

      if (data.user) {
        setUser({
          id: data.user.id,
          name: email.split('@')[0].toUpperCase(),
          callsign,
          role,
          sector: 'Sector 1',
          email
        });
        setIsSupabaseAuth(true);
        return { success: true };
      }

      return { success: false, error: 'Sign up did not return user' };
    } catch (err: any) {
      setAuthError(err?.message || 'Sign up exception');
      return { success: false, error: err?.message };
    }
  };

  const loginDemo = (role: User['role'] = 'coordinator') => {
    const demoUser: User = {
      ...DEFAULT_DEMO_USER,
      role
    };
    setUser(demoUser);
    setIsSupabaseAuth(false);
    localStorage.setItem('resqsync_demo_auth', JSON.stringify(demoUser));
  };

  const logout = async () => {
    if (supabase && isSupabaseAuth) {
      await supabase.auth.signOut();
    }
    setUser(null);
    setIsSupabaseAuth(false);
    localStorage.removeItem('resqsync_demo_auth');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: Boolean(user),
        isSupabaseAuth,
        isLoading,
        authError,
        loginWithPassword,
        signUpWithPassword,
        loginDemo,
        logout
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
