/**
 * Incident Service Abstraction Layer
 * Serves simulated test data during UI development.
 * Designed to cleanly plug into Supabase Client (@supabase/supabase-js) in Phase 2.
 */

import { EmergencyReport, RescueUpdate, MeshPeerNode, RescueTeam } from '../types';
import {
  INITIAL_MOCK_REPORTS,
  INITIAL_MOCK_UPDATES,
  INITIAL_MOCK_NODES,
  INITIAL_MOCK_TEAMS
} from '../utils/mockData';

export const incidentService = {
  async getReports(): Promise<EmergencyReport[]> {
    // Simulated network latency for realistic UI state testing
    await new Promise((r) => setTimeout(r, 150));
    return [...INITIAL_MOCK_REPORTS];
  },

  async getUpdates(): Promise<RescueUpdate[]> {
    await new Promise((r) => setTimeout(r, 100));
    return [...INITIAL_MOCK_UPDATES];
  },

  async getMeshNodes(): Promise<MeshPeerNode[]> {
    await new Promise((r) => setTimeout(r, 100));
    return [...INITIAL_MOCK_NODES];
  },

  async getTeams(): Promise<RescueTeam[]> {
    await new Promise((r) => setTimeout(r, 100));
    return [...INITIAL_MOCK_TEAMS];
  }
};
