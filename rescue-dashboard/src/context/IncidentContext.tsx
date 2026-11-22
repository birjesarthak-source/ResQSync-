import React, { createContext, useContext, useState } from 'react';
import { EmergencyReport, RescueUpdate, MeshPeerNode, RescueTeam, ReportStatus } from '../types';
import {
  INITIAL_MOCK_REPORTS,
  INITIAL_MOCK_UPDATES,
  INITIAL_MOCK_NODES,
  INITIAL_MOCK_TEAMS
} from '../utils/mockData';

interface IncidentContextType {
  reports: EmergencyReport[];
  updates: RescueUpdate[];
  nodes: MeshPeerNode[];
  teams: RescueTeam[];
  selectedReportId: string | null;
  setSelectedReportId: (id: string | null) => void;
  updateReportStatus: (reportId: string, status: ReportStatus) => void;
  assignTeamToReport: (reportId: string, teamId: string) => void;
  addRescueUpdate: (reportId: string, teamId: string, message: string) => void;
  addSimulatedIncident: () => void;
  resetToInitialMock: () => void;
}

const IncidentContext = createContext<IncidentContextType | undefined>(undefined);

export const IncidentProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [reports, setReports] = useState<EmergencyReport[]>(INITIAL_MOCK_REPORTS);
  const [updates, setUpdates] = useState<RescueUpdate[]>(INITIAL_MOCK_UPDATES);
  const [nodes] = useState<MeshPeerNode[]>(INITIAL_MOCK_NODES);
  const [teams, setTeams] = useState<RescueTeam[]>(INITIAL_MOCK_TEAMS);
  const [selectedReportId, setSelectedReportId] = useState<string | null>(INITIAL_MOCK_REPORTS[0].reportId);

  const updateReportStatus = (reportId: string, status: ReportStatus) => {
    setReports((prev) =>
      prev.map((r) => (r.reportId === reportId ? { ...r, status } : r))
    );
  };

  const assignTeamToReport = (reportId: string, teamId: string) => {
    const team = teams.find((t) => t.teamId === teamId);
    if (!team) return;

    setReports((prev) =>
      prev.map((r) =>
        r.reportId === reportId
          ? {
              ...r,
              status: 'ASSIGNED',
              assignedTeamId: teamId,
              assignedTeamName: team.teamName
            }
          : r
      )
    );

    setTeams((prev) =>
      prev.map((t) =>
        t.teamId === teamId
          ? {
              ...t,
              status: 'DISPATCHED',
              assignedReportIds: [...t.assignedReportIds, reportId]
            }
          : t
      )
    );

    // Create an update log entry
    const newUpdate: RescueUpdate = {
      updateId: `UPD-${Date.now()}`,
      reportId,
      teamId,
      teamName: team.teamName,
      author: team.leaderName,
      message: `${team.teamName} (${team.callsign}) dispatched to incident ${reportId}.`,
      timestamp: Date.now(),
      urgency: 'HIGH',
      statusChange: 'ASSIGNED',
      isSimulatedMock: true
    };
    setUpdates((prev) => [newUpdate, ...prev]);
  };

  const addRescueUpdate = (reportId: string, teamId: string, message: string) => {
    const team = teams.find((t) => t.teamId === teamId);
    const newUpdate: RescueUpdate = {
      updateId: `UPD-${Date.now()}`,
      reportId,
      teamId,
      teamName: team?.teamName || 'Command HQ',
      author: 'Dispatch Desk',
      message,
      timestamp: Date.now(),
      urgency: 'MEDIUM',
      isSimulatedMock: true
    };
    setUpdates((prev) => [newUpdate, ...prev]);
  };

  const addSimulatedIncident = () => {
    const sampleId = `SIM-REP-00${reports.length + 1}`;
    const newReport: EmergencyReport = {
      reportId: sampleId,
      reporterDeviceId: `mesh-node-test-${Math.floor(Math.random() * 90 + 10)}`,
      incidentType: 'MEDICAL_EMERGENCY',
      description: 'Simulated trial report generated from offline smartphone mesh via BLE relay.',
      latitude: 18.525 + (Math.random() - 0.5) * 0.03,
      longitude: 73.85 + (Math.random() - 0.5) * 0.03,
      urgency: 'HIGH',
      peopleAffected: 2,
      verificationStatus: 'CORROBORATED',
      status: 'NEW',
      createdAt: Date.now(),
      aiSummary: 'Simulated test beacon received. Urgency assessed as HIGH.',
      aiActions: ['Acknowledge beacon reception', 'Queue for next available paramedic unit'],
      isSimulatedMock: true
    };
    setReports((prev) => [newReport, ...prev]);
    setSelectedReportId(sampleId);
  };

  const resetToInitialMock = () => {
    setReports(INITIAL_MOCK_REPORTS);
    setUpdates(INITIAL_MOCK_UPDATES);
    setTeams(INITIAL_MOCK_TEAMS);
    setSelectedReportId(INITIAL_MOCK_REPORTS[0].reportId);
  };

  return (
    <IncidentContext.Provider
      value={{
        reports,
        updates,
        nodes,
        teams,
        selectedReportId,
        setSelectedReportId,
        updateReportStatus,
        assignTeamToReport,
        addRescueUpdate,
        addSimulatedIncident,
        resetToInitialMock
      }}
    >
      {children}
    </IncidentContext.Provider>
  );
};

export const useIncidents = (): IncidentContextType => {
  const context = useContext(IncidentContext);
  if (!context) {
    throw new Error('useIncidents must be used within an IncidentProvider');
  }
  return context;
};
