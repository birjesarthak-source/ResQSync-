import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import {
  EmergencyReport,
  NetworkMessage,
  RescueUpdate,
  MeshPeerNode,
  RescueTeam,
  ReportStatus
} from '../types';
import {
  incidentService,
  deriveNodesFromMessages
} from '../services/incidentService';
import {
  INITIAL_MOCK_REPORTS,
  INITIAL_MOCK_UPDATES,
  INITIAL_MOCK_NODES,
  INITIAL_MOCK_TEAMS
} from '../utils/mockData';
import { useAuth } from './AuthContext';

interface IncidentContextType {
  reports: EmergencyReport[];
  networkMessages: NetworkMessage[];
  updates: RescueUpdate[];
  nodes: MeshPeerNode[];
  teams: RescueTeam[];
  selectedReportId: string | null;
  setSelectedReportId: (id: string | null) => void;

  // Loading and Error States
  isLoadingReports: boolean;
  reportsError: string | null;
  isReportsRlsDenied: boolean;
  isLoadingMessages: boolean;
  messagesError: string | null;
  isRealtimeConnected: boolean;

  // Demo Mode controls (separated from production data flow)
  isDemoMode: boolean;
  isSupabaseAuth: boolean;
  toggleDemoMode: () => void;

  // Data Refresh actions
  refetchReports: () => Promise<void>;
  refetchMessages: () => Promise<void>;
  updateReportStatus: (reportId: string, status: ReportStatus) => Promise<void>;
  updateReportCoordinates: (
    reportId: string,
    latitude: number,
    longitude: number
  ) => Promise<{ error: string | null }>;
  assignTeamToReport: (reportId: string, teamId: string) => void;
  addRescueUpdate: (reportId: string, teamId: string, message: string) => void;
}

const IncidentContext = createContext<IncidentContextType | undefined>(undefined);

export const IncidentProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isSupabaseAuth } = useAuth();

  // Production data states (empty by default; populated from Supabase)
  const [reports, setReports] = useState<EmergencyReport[]>([]);
  const [networkMessages, setNetworkMessages] = useState<NetworkMessage[]>([]);
  const [derivedNodes, setDerivedNodes] = useState<MeshPeerNode[]>([]);
  const [updates, setUpdates] = useState<RescueUpdate[]>([]);
  const [teams, setTeams] = useState<RescueTeam[]>(INITIAL_MOCK_TEAMS);
  const [selectedReportId, setSelectedReportId] = useState<string | null>(null);

  // Status flags
  const [isLoadingReports, setIsLoadingReports] = useState(true);
  const [reportsError, setReportsError] = useState<string | null>(null);
  const [isReportsRlsDenied, setIsReportsRlsDenied] = useState(false);
  const [isLoadingMessages, setIsLoadingMessages] = useState(true);
  const [messagesError, setMessagesError] = useState<string | null>(null);
  const [isRealtimeConnected, setIsRealtimeConnected] = useState(false);

  // Optional Demo Mode state (isolated from production pipeline)
  const [isDemoMode, setIsDemoMode] = useState(false);

  const fetchReports = useCallback(async () => {
    setIsLoadingReports(true);
    setReportsError(null);
    setIsReportsRlsDenied(false);

    const result = await incidentService.fetchEmergencyReports();
    if (result.error) {
      setReportsError(result.error);
      setIsReportsRlsDenied(result.isRlsDenied);
    } else {
      setReports(result.data);
      if (result.data.length > 0 && !selectedReportId) {
        setSelectedReportId(result.data[0].reportId);
      }
    }
    setIsLoadingReports(false);
  }, [selectedReportId]);

  const fetchMessages = useCallback(async () => {
    setIsLoadingMessages(true);
    setMessagesError(null);

    const result = await incidentService.fetchNetworkMessages();
    if (result.error) {
      setMessagesError(result.error);
    } else {
      setNetworkMessages(result.data);
      setDerivedNodes(deriveNodesFromMessages(result.data));
    }
    setIsLoadingMessages(false);
  }, []);

  // Fetch production data on mount & whenever auth state changes
  useEffect(() => {
    fetchReports();
    fetchMessages();
  }, [fetchReports, fetchMessages, isSupabaseAuth]);

  // Subscribe to Supabase Realtime for live emergency report inserts and updates
  useEffect(() => {
    const unsubscribe = incidentService.subscribeToEmergencyReports(
      (newReport) => {
        setReports((prev) => {
          // Avoid duplicate insertion
          const exists = prev.some((r) => r.reportId === newReport.reportId);
          if (exists) return prev;
          return [newReport, ...prev];
        });
        setIsRealtimeConnected(true);
      },
      (updatedReport) => {
        setReports((prev) =>
          prev.map((r) => (r.reportId === updatedReport.reportId ? updatedReport : r))
        );
        setIsRealtimeConnected(true);
      }
    );

    setIsRealtimeConnected(true);
    return () => {
      unsubscribe();
    };
  }, []);

  const updateReportStatus = async (reportId: string, status: ReportStatus) => {
    // Optimistic local update
    setReports((prev) =>
      prev.map((r) => (r.reportId === reportId ? { ...r, status } : r))
    );
  };

  const updateReportCoordinates = async (
    reportId: string,
    latitude: number,
    longitude: number
  ): Promise<{ error: string | null }> => {
    if (!isSupabaseAuth || isDemoMode) {
      return { error: 'Sign in with a Supabase responder account to save report coordinates.' };
    }

    const result = await incidentService.updateReportCoordinates(reportId, latitude, longitude);
    if (!result.error) {
      setReports((prev) =>
        prev.map((report) =>
          report.reportId === reportId
            ? { ...report, latitude, longitude }
            : report
        )
      );
    }
    return result;
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

    const newUpdate: RescueUpdate = {
      updateId: `UPD-${Date.now()}`,
      reportId,
      teamId,
      teamName: team.teamName,
      author: team.leaderName,
      message: `${team.teamName} (${team.callsign}) assigned to report ${reportId}.`,
      timestamp: Date.now(),
      urgency: 'HIGH',
      statusChange: 'ASSIGNED'
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
      urgency: 'MEDIUM'
    };
    setUpdates((prev) => [newUpdate, ...prev]);
  };

  const toggleDemoMode = () => {
    setIsDemoMode((prev) => !prev);
  };

  // When demo mode is toggled ON, serve simulated test data; otherwise serve LIVE Supabase data
  const effectiveReports = isDemoMode ? INITIAL_MOCK_REPORTS : reports;
  const effectiveNodes = isDemoMode ? INITIAL_MOCK_NODES : derivedNodes;
  const effectiveUpdates = isDemoMode ? INITIAL_MOCK_UPDATES : updates;

  return (
    <IncidentContext.Provider
      value={{
        reports: effectiveReports,
        networkMessages,
        updates: effectiveUpdates,
        nodes: effectiveNodes,
        teams,
        selectedReportId: selectedReportId || (effectiveReports[0]?.reportId ?? null),
        setSelectedReportId,

        isLoadingReports: isDemoMode ? false : isLoadingReports,
        reportsError: isDemoMode ? null : reportsError,
        isReportsRlsDenied: isDemoMode ? false : isReportsRlsDenied,
        isLoadingMessages: isDemoMode ? false : isLoadingMessages,
        messagesError: isDemoMode ? null : messagesError,
        isRealtimeConnected,

        isDemoMode,
        isSupabaseAuth,
        toggleDemoMode,

        refetchReports: fetchReports,
        refetchMessages: fetchMessages,
        updateReportStatus,
        updateReportCoordinates,
        assignTeamToReport,
        addRescueUpdate
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
