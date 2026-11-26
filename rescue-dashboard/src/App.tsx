import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { IncidentProvider } from './context/IncidentContext';
import { DashboardLayout } from './layouts/DashboardLayout';

import { LoginPage } from './pages/LoginPage';
import { DashboardOverviewPage } from './pages/DashboardOverviewPage';
import { EmergencyReportsPage } from './pages/EmergencyReportsPage';
import { IncidentMapPage } from './pages/IncidentMapPage';
import { KanbanDispatchBoard } from './pages/KanbanDispatchBoard';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { RescueUpdatesPage } from './pages/RescueUpdatesPage';
import { NetworkMonitoringPage } from './pages/NetworkMonitoringPage';
import { TeamManagementPage } from './pages/TeamManagementPage';
import { SettingsPage } from './pages/SettingsPage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <IncidentProvider>
          <Routes>
            {/* Public Access */}
            <Route path="/login" element={<LoginPage />} />

            {/* Protected Dashboard Command Console */}
            <Route path="/" element={<DashboardLayout />}>
              <Route index element={<DashboardOverviewPage />} />
              <Route path="reports" element={<EmergencyReportsPage />} />
              <Route path="map" element={<IncidentMapPage />} />
              <Route path="dispatch" element={<KanbanDispatchBoard />} />
              <Route path="analytics" element={<AnalyticsPage />} />
              <Route path="updates" element={<RescueUpdatesPage />} />
              <Route path="network" element={<NetworkMonitoringPage />} />
              <Route path="teams" element={<TeamManagementPage />} />
              <Route path="settings" element={<SettingsPage />} />
            </Route>

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </IncidentProvider>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
