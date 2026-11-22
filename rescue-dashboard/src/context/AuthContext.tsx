import React, { createContext, useContext, useState, useEffect } from 'react';
import { User } from '../types';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, callsign?: string, role?: User['role']) => boolean;
  logout: () => void;
}

const DEFAULT_DEMO_USER: User = {
  id: 'usr-dispatch-01',
  name: 'Commander Arjun Varma',
  callsign: 'Apex-Lead',
  role: 'COMMANDER',
  sector: 'Command HQ (Sector 1)',
  email: 'arjun.varma@resqsync.internal'
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('resqsync_demo_auth');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        return DEFAULT_DEMO_USER;
      }
    }
    return DEFAULT_DEMO_USER; // Default logged in for fast development verification, but toggleable
  });

  useEffect(() => {
    if (user) {
      localStorage.setItem('resqsync_demo_auth', JSON.stringify(user));
    } else {
      localStorage.removeItem('resqsync_demo_auth');
    }
  }, [user]);

  const login = (email: string, callsign = 'Tactical-01', role: User['role'] = 'DISPATCHER'): boolean => {
    if (!email || !email.includes('@')) {
      return false;
    }
    const newUser: User = {
      id: `usr-${Date.now()}`,
      name: email.split('@')[0].replace('.', ' ').toUpperCase(),
      callsign,
      role,
      sector: 'Sector 1 Command Deck',
      email
    };
    setUser(newUser);
    return true;
  };

  const logout = () => {
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: Boolean(user),
        login,
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
