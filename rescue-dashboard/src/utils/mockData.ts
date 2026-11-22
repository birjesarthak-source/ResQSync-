/**
 * ============================================================================
 * RESQSYNC AI - SIMULATED TEST DATA FOR UI DEVELOPMENT ONLY
 * ============================================================================
 * IMPORTANT: The following records are strictly synthetic test mocks created
 * to develop and verify UI layout, responsive components, and user flows.
 * They DO NOT represent live or real emergency operations.
 * ============================================================================
 */

import { EmergencyReport, RescueUpdate, MeshPeerNode, RescueTeam } from '../types';

export const SIMULATION_DISCLAIMER =
  'UI DEVELOPMENT MODE: Displaying synthetic test records. Supabase real-time sync is not activated.';

export const INITIAL_MOCK_REPORTS: EmergencyReport[] = [
  {
    reportId: 'SIM-REP-001',
    reporterDeviceId: 'mesh-node-alpha-44',
    incidentType: 'FLOOD',
    description: 'Flash flooding near Riverside Market Square. Water levels above 4.5 feet and rising. 6 citizens trapped on grocery store rooftop.',
    latitude: 18.5204,
    longitude: 73.8567,
    urgency: 'CRITICAL',
    peopleAffected: 6,
    verificationStatus: 'CORROBORATED',
    status: 'IN_PROGRESS',
    createdAt: Date.now() - 12 * 60 * 1000,
    aiSummary: 'High-velocity water inundation. Immediate inflatable boat or air evacuation requested. Strong current makes wading unsafe.',
    aiActions: [
      'Dispatch Water Rescue Unit 1 with Zodiac inflatable craft.',
      'Alert riverside flood defense barrage.',
      'Broadcast shelter advisory to Sector 4.'
    ],
    assignedTeamId: 'TEAM-WATER-01',
    assignedTeamName: 'AquaRescue Sector 4',
    isSimulatedMock: true
  },
  {
    reportId: 'SIM-REP-002',
    reporterDeviceId: 'mesh-node-beta-12',
    incidentType: 'BUILDING_COLLAPSE',
    description: 'Corner wall of three-story apartment building collapsed following secondary seismic tremor. 3 people trapped under porch debris.',
    latitude: 18.5312,
    longitude: 73.8445,
    urgency: 'CRITICAL',
    peopleAffected: 3,
    verificationStatus: 'HUMAN_REVIEWED',
    status: 'ASSIGNED',
    createdAt: Date.now() - 34 * 60 * 1000,
    aiSummary: 'Structural collapse with confined space victims. Acoustic detection indicates conscious survivors. Hydraulic cutters and heavy shoring needed.',
    aiActions: [
      'Deploy Heavy Debris Unit 2 with hydraulic rescue tools.',
      'Request on-site emergency triage surgeon.',
      'Isolate local gas main line.'
    ],
    assignedTeamId: 'TEAM-HEAVY-02',
    assignedTeamName: 'Urban Search & Rescue B',
    isSimulatedMock: true
  },
  {
    reportId: 'SIM-REP-003',
    reporterDeviceId: 'mesh-node-gamma-88',
    incidentType: 'MEDICAL_EMERGENCY',
    description: 'Senior citizen community center lost generator backup. 2 oxygen-dependent patients in respiratory distress.',
    latitude: 18.5109,
    longitude: 73.8621,
    urgency: 'HIGH',
    peopleAffected: 2,
    verificationStatus: 'CORROBORATED',
    status: 'NEW',
    createdAt: Date.now() - 52 * 60 * 1000,
    aiSummary: 'Critical power outage affecting life-support equipment. Portable oxygen tanks and auxiliary lithium power packs required immediately.',
    aiActions: [
      'Dispatch Rapid Medical Unit with 4x Type-E O2 cylinders.',
      'Coordinate portable battery inverter from Central Depot.'
    ],
    isSimulatedMock: true
  },
  {
    reportId: 'SIM-REP-004',
    reporterDeviceId: 'mesh-node-delta-07',
    incidentType: 'LANDSLIDE',
    description: 'Mud and boulder slide blocked arterial access road to hillside clinic. Ambulances currently rerouted.',
    latitude: 18.5428,
    longitude: 73.8715,
    urgency: 'HIGH',
    peopleAffected: 0,
    verificationStatus: 'CORROBORATED',
    status: 'ASSIGNED',
    createdAt: Date.now() - 75 * 60 * 1000,
    aiSummary: 'Logistics impediment. No human casualties reported at blockage, but impedes medical transport to East Sector hospital.',
    aiActions: [
      'Send earthmoving machinery to clear lane 1.',
      'Issue alternate route alerts to emergency vehicles.'
    ],
    assignedTeamId: 'TEAM-DEBRIS-01',
    assignedTeamName: 'Civic Engineers Rapid Response',
    isSimulatedMock: true
  },
  {
    reportId: 'SIM-REP-005',
    reporterDeviceId: 'mesh-node-epsilon-99',
    incidentType: 'FIRE',
    description: 'Transformer explosion sparked localized rooftop fire on industrial storage warehouse. Chemical containers nearby.',
    latitude: 18.5021,
    longitude: 73.8329,
    urgency: 'HIGH',
    peopleAffected: 4,
    verificationStatus: 'UNVERIFIED',
    status: 'NEW',
    createdAt: Date.now() - 110 * 60 * 1000,
    aiSummary: 'Potential hazardous chemical escalation. Fire team with foam suppressants recommended.',
    aiActions: [
      'Dispatch Fire Station 3 with Hazmat foam tender.',
      'Evacuate downwind perimeter 300 meters.'
    ],
    isSimulatedMock: true
  },
  {
    reportId: 'SIM-REP-006',
    reporterDeviceId: 'mesh-node-zeta-33',
    incidentType: 'OTHER',
    description: 'Downed high-voltage electric line across school access road. Area cordoned off with warning tape.',
    latitude: 18.4988,
    longitude: 73.8512,
    urgency: 'LOW',
    peopleAffected: 0,
    verificationStatus: 'HUMAN_REVIEWED',
    status: 'RESOLVED',
    createdAt: Date.now() - 180 * 60 * 1000,
    aiSummary: 'Electrical hazard isolated. Power utility has de-energized grid line segment. No active casualties.',
    aiActions: [
      'Line grounded and certified safe by electricity board.'
    ],
    assignedTeamId: 'TEAM-SAFETY-01',
    assignedTeamName: 'Perimeter Safety Unit',
    isSimulatedMock: true
  }
];

export const INITIAL_MOCK_UPDATES: RescueUpdate[] = [
  {
    updateId: 'UPD-101',
    reportId: 'SIM-REP-001',
    teamId: 'TEAM-WATER-01',
    teamName: 'AquaRescue Sector 4',
    author: 'Capt. R. Deshmukh',
    message: 'Zodiac boat deployed into Market Square floodwaters. First 3 victims boarded; securing remaining 3.',
    timestamp: Date.now() - 5 * 60 * 1000,
    urgency: 'CRITICAL',
    statusChange: 'IN_PROGRESS',
    isSimulatedMock: true
  },
  {
    updateId: 'UPD-102',
    reportId: 'SIM-REP-002',
    teamId: 'TEAM-HEAVY-02',
    teamName: 'Urban Search & Rescue B',
    author: 'Lt. S. Nair',
    message: 'On scene at residential block. Structural shoring in place. Hydraulic jacks clearing entrance.',
    timestamp: Date.now() - 18 * 60 * 1000,
    urgency: 'CRITICAL',
    statusChange: 'IN_PROGRESS',
    isSimulatedMock: true
  },
  {
    updateId: 'UPD-103',
    reportId: 'SIM-REP-004',
    teamId: 'TEAM-DEBRIS-01',
    teamName: 'Civic Engineers Rapid Response',
    author: 'Eng. K. Rao',
    message: 'Bulldozer operator arrived at hillside road. Estimated 30 minutes to open single lane for emergency transit.',
    timestamp: Date.now() - 32 * 60 * 1000,
    urgency: 'MEDIUM',
    statusChange: 'ASSIGNED',
    isSimulatedMock: true
  },
  {
    updateId: 'UPD-104',
    reportId: 'SIM-REP-006',
    teamId: 'TEAM-SAFETY-01',
    teamName: 'Perimeter Safety Unit',
    author: 'Officer M. Kulkarni',
    message: 'Grid segment verified de-energized. Hazard resolved; roadway re-opened.',
    timestamp: Date.now() - 95 * 60 * 1000,
    urgency: 'LOW',
    statusChange: 'RESOLVED',
    isSimulatedMock: true
  }
];

export const INITIAL_MOCK_NODES: MeshPeerNode[] = [
  {
    nodeId: 'NODE-GW-01',
    deviceName: 'Command HQ Gateway Phone (Pixel 8)',
    hopDistance: 0,
    isGateway: true,
    batteryLevel: 94,
    signalStrength: -48,
    pendingMessagesCount: 0,
    lastSeen: Date.now() - 2000,
    status: 'ONLINE',
    isSimulatedMock: true
  },
  {
    nodeId: 'NODE-RELAY-12',
    deviceName: 'Sector 4 Shelter Relay (Galaxy S22)',
    hopDistance: 1,
    isGateway: false,
    batteryLevel: 78,
    signalStrength: -62,
    pendingMessagesCount: 2,
    lastSeen: Date.now() - 15000,
    status: 'RELAYING',
    isSimulatedMock: true
  },
  {
    nodeId: 'NODE-RELAY-44',
    deviceName: 'Market Square Watch Post (OnePlus 11)',
    hopDistance: 2,
    isGateway: false,
    batteryLevel: 55,
    signalStrength: -74,
    pendingMessagesCount: 4,
    lastSeen: Date.now() - 28000,
    status: 'RELAYING',
    isSimulatedMock: true
  },
  {
    nodeId: 'NODE-EDGE-88',
    deviceName: 'Clinic Hill Edge Node (Redmi Note 12)',
    hopDistance: 3,
    isGateway: false,
    batteryLevel: 42,
    signalStrength: -82,
    pendingMessagesCount: 1,
    lastSeen: Date.now() - 45000,
    status: 'ONLINE',
    isSimulatedMock: true
  },
  {
    nodeId: 'NODE-RELAY-07',
    deviceName: 'Hillside Bypass Node (Moto G84)',
    hopDistance: 2,
    isGateway: false,
    batteryLevel: 68,
    signalStrength: -70,
    pendingMessagesCount: 0,
    lastSeen: Date.now() - 32000,
    status: 'ONLINE',
    isSimulatedMock: true
  },
  {
    nodeId: 'NODE-EDGE-99',
    deviceName: 'Industrial Sector Drone Hub',
    hopDistance: 3,
    isGateway: false,
    batteryLevel: 89,
    signalStrength: -76,
    pendingMessagesCount: 3,
    lastSeen: Date.now() - 18000,
    status: 'RELAYING',
    isSimulatedMock: true
  }
];

export const INITIAL_MOCK_TEAMS: RescueTeam[] = [
  {
    teamId: 'TEAM-WATER-01',
    teamName: 'AquaRescue Sector 4',
    callsign: 'Triton-1',
    leaderName: 'Capt. R. Deshmukh',
    contactRadio: 'CH-04 (156.200 MHz)',
    membersCount: 6,
    specialization: 'Water Rescue',
    status: 'DISPATCHED',
    currentSector: 'Sector 4 (Riverside)',
    assignedReportIds: ['SIM-REP-001'],
    isSimulatedMock: true
  },
  {
    teamId: 'TEAM-HEAVY-02',
    teamName: 'Urban Search & Rescue B',
    callsign: 'Vanguard-2',
    leaderName: 'Lt. S. Nair',
    contactRadio: 'CH-02 (154.600 MHz)',
    membersCount: 8,
    specialization: 'Heavy Debris',
    status: 'ON_SITE',
    currentSector: 'Sector 2 (Central West)',
    assignedReportIds: ['SIM-REP-002'],
    isSimulatedMock: true
  },
  {
    teamId: 'TEAM-MED-03',
    teamName: 'Mobile Paramedic Unit 3',
    callsign: 'Lifeline-3',
    leaderName: 'Dr. Anita Joshi',
    contactRadio: 'CH-07 (155.340 MHz)',
    membersCount: 4,
    specialization: 'Medical Triage',
    status: 'AVAILABLE',
    currentSector: 'Base Camp Hospital',
    assignedReportIds: [],
    isSimulatedMock: true
  },
  {
    teamId: 'TEAM-DRONE-01',
    teamName: 'Aerial Recon & Thermal Scan',
    callsign: 'Falcon-Eye',
    leaderName: 'Operator V. Mehta',
    contactRadio: 'CH-09 (151.625 MHz)',
    membersCount: 3,
    specialization: 'Drone Recon',
    status: 'AVAILABLE',
    currentSector: 'Sector 1 Command Deck',
    assignedReportIds: [],
    isSimulatedMock: true
  }
];
