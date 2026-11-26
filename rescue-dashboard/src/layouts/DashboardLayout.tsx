import React, { useState } from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { Sidebar } from '../components/Sidebar';
import { Navbar } from '../components/Navbar';
import { DemoBanner } from '../components/DemoBanner';
import { useAuth } from '../context/AuthContext';

export const DashboardLayout: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  // If unauthenticated, redirect to login page
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-slate-50 font-sans text-slate-800">
      {/* Sidebar */}
      <Sidebar
        isCollapsed={isSidebarCollapsed}
        onToggleCollapse={() => setIsSidebarCollapsed((prev) => !prev)}
        isMobileOpen={isMobileMenuOpen}
        onCloseMobile={() => setIsMobileMenuOpen(false)}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-full min-w-0 overflow-hidden">
        {/* Top Navbar */}
        <Navbar onOpenMobileMenu={() => setIsMobileMenuOpen(true)} />

        {/* Global Demo Banner */}
        <DemoBanner />

        {/* Scrollable Page Body */}
        <main className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8 bg-slate-50">
          <div className="max-w-7xl mx-auto">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};
