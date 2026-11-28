export type IncidentType =
  | 'FLOOD'
  | 'FIRE'
  | 'EARTHQUAKE'
  | 'LANDSLIDE'
  | 'BUILDING_COLLAPSE'
  | 'MEDICAL_EMERGENCY'
  | 'OTHER';

export type UrgencyLevel = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export type ReportStatus = 'NEW' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED';

export type VerificationStatus =
  | 'UNVERIFIED'
  | 'CORROBORATED'
  | 'HUMAN_REVIEWED'
  | 'DISPUTED';

export interface EmergencyReport {
  reportId: string;
  reporterDeviceId: string;
  incidentType: IncidentType;
  description: string;
  latitude: number | null;
  longitude: number | null;
  urgency: UrgencyLevel;
  peopleAffected: number;
  verificationStatus: VerificationStatus;
  status: ReportStatus;
  createdAt: number;
  expiresAt?: number;
  syncedAt?: string;
  aiSummary?: string;
  aiActions?: string[];
  assignedTeamId?: string;
  assignedTeamName?: string;
  isSimulatedMock?: boolean;
  voiceBase64?: string;
}

export interface NetworkMessage {
  messageId: string;
  sourceDeviceId: string;
  destinationDeviceId?: string;
  hopCount: number;
  payload?: string;
  createdAt: number;
  syncedAt?: string;
}

export interface RescueUpdate {
  updateId: string;
  reportId: string;
  teamId: string;
  teamName: string;
  author: string;
  message: string;
  timestamp: number;
  urgency: UrgencyLevel;
  statusChange?: ReportStatus;
  isSimulatedMock?: boolean;
}

export interface MeshPeerNode {
  nodeId: string;
  deviceName: string;
  hopDistance: number;
  isGateway: boolean;
  batteryLevel: number;
  signalStrength: number; // dBm e.g. -65
  pendingMessagesCount: number;
  lastSeen: number;
  status: 'ONLINE' | 'RELAYING' | 'OFFLINE';
  isSimulatedMock?: boolean;
}

export interface RescueTeam {
  teamId: string;
  teamName: string;
  callsign: string;
  leaderName: string;
  contactRadio: string;
  membersCount: number;
  specialization: 'Search & Rescue' | 'Medical Triage' | 'Water Rescue' | 'Heavy Debris' | 'Drone Recon';
  status: 'AVAILABLE' | 'DISPATCHED' | 'ON_SITE' | 'STANDBY';
  currentSector: string;
  assignedReportIds: string[];
  isSimulatedMock?: boolean;
}

export interface User {
  id: string;
  name: string;
  callsign: string;
  role: 'admin' | 'coordinator' | 'field_responder';
  sector: string;
  email: string;
}
