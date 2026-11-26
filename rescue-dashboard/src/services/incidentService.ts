/**
 * ResQSync AI - Supabase Production Data Service
 * Connects directly to public.emergency_reports and public.network_messages.
 * Manages Supabase Realtime WebSocket subscriptions for disaster monitoring.
 */

import { supabase } from '../lib/supabaseClient';
import { EmergencyReport, NetworkMessage, MeshPeerNode, IncidentType, UrgencyLevel, VerificationStatus } from '../types';

/**
 * Maps a raw PostgreSQL row from 'public.emergency_reports' to the frontend EmergencyReport model.
 */
export const mapRowToEmergencyReport = (row: any): EmergencyReport => {
  const createdAtNum = typeof row.created_at === 'number'
    ? row.created_at
    : Number(row.created_at) || Date.now();

  const expiresAtNum = row.expires_at ? Number(row.expires_at) : undefined;

  return {
    reportId: row.report_id || `rep-${Date.now()}`,
    reporterDeviceId: row.reporter_device_id || 'unknown-device',
    incidentType: (row.incident_type as IncidentType) || 'OTHER',
    description: row.description || '',
    latitude: typeof row.latitude === 'number' ? row.latitude : 18.5204,
    longitude: typeof row.longitude === 'number' ? row.longitude : 73.8567,
    urgency: (row.urgency as UrgencyLevel) || 'MEDIUM',
    peopleAffected: typeof row.people_affected === 'number' ? row.people_affected : 0,
    verificationStatus: (row.verification_status as VerificationStatus) || 'UNVERIFIED',
    status: row.status || 'NEW',
    createdAt: createdAtNum,
    expiresAt: expiresAtNum,
    syncedAt: row.synced_at,
    aiSummary: row.ai_summary || undefined,
    aiActions: row.ai_actions || undefined,
    voiceBase64: row.voice_base64 || undefined,
    isSimulatedMock: false
  };
};

/**
 * Maps a raw PostgreSQL row from 'public.network_messages' to NetworkMessage.
 */
export const mapRowToNetworkMessage = (row: any): NetworkMessage => {
  const createdAtNum = typeof row.created_at === 'number'
    ? row.created_at
    : Number(row.created_at) || Date.now();

  return {
    messageId: row.message_id || `msg-${Date.now()}`,
    sourceDeviceId: row.source_device_id || 'unknown-peer',
    destinationDeviceId: row.destination_device_id || undefined,
    hopCount: typeof row.hop_count === 'number' ? row.hop_count : 0,
    payload: row.payload || undefined,
    createdAt: createdAtNum,
    syncedAt: row.synced_at
  };
};

/**
 * Derives a dynamic MeshPeerNode topology from incoming network messages.
 */
export const deriveNodesFromMessages = (messages: NetworkMessage[]): MeshPeerNode[] => {
  const nodeMap = new Map<string, MeshPeerNode>();

  messages.forEach((msg) => {
    const existing = nodeMap.get(msg.sourceDeviceId);
    if (!existing) {
      nodeMap.set(msg.sourceDeviceId, {
        nodeId: msg.sourceDeviceId,
        deviceName: msg.sourceDeviceId.includes('gw') || msg.hopCount === 0
          ? `Gateway Relay (${msg.sourceDeviceId})`
          : `Field Node (${msg.sourceDeviceId})`,
        hopDistance: msg.hopCount,
        isGateway: msg.hopCount === 0,
        batteryLevel: Math.max(30, 95 - msg.hopCount * 12),
        signalStrength: -50 - msg.hopCount * 14,
        pendingMessagesCount: 1,
        lastSeen: msg.createdAt,
        status: msg.hopCount === 0 ? 'ONLINE' : 'RELAYING',
        isSimulatedMock: false
      });
    } else {
      existing.pendingMessagesCount += 1;
      if (msg.createdAt > existing.lastSeen) {
        existing.lastSeen = msg.createdAt;
        existing.hopDistance = msg.hopCount;
      }
    }
  });

  return Array.from(nodeMap.values());
};

export interface FetchResult<T> {
  data: T[];
  error: string | null;
  isRlsDenied: boolean;
}

export const incidentService = {
  /**
   * Reads all emergency reports from public.emergency_reports ordered by created_at DESC.
   */
  async fetchEmergencyReports(): Promise<FetchResult<EmergencyReport>> {
    if (!supabase) {
      return {
        data: [],
        error: 'Supabase client is not initialised (check .env.local).',
        isRlsDenied: false
      };
    }

    try {
      const { data, error } = await supabase
        .from('emergency_reports')
        .select('*')
        .order('created_at', { ascending: false });

      if (error) {
        const isRls = error.code === '42501' || error.message.toLowerCase().includes('permission denied');
        return {
          data: [],
          error: error.message,
          isRlsDenied: isRls
        };
      }

      const reports = (data || []).map(mapRowToEmergencyReport);
      return {
        data: reports,
        error: null,
        isRlsDenied: false
      };
    } catch (err: any) {
      return {
        data: [],
        error: err?.message || 'Network exception during fetch',
        isRlsDenied: false
      };
    }
  },

  /**
   * Reads all mesh telemetry messages from public.network_messages ordered by created_at DESC.
   */
  async fetchNetworkMessages(): Promise<FetchResult<NetworkMessage>> {
    if (!supabase) {
      return {
        data: [],
        error: 'Supabase client is not initialised.',
        isRlsDenied: false
      };
    }

    try {
      const { data, error } = await supabase
        .from('network_messages')
        .select('*')
        .order('created_at', { ascending: false });

      if (error) {
        const isRls = error.code === '42501' || error.message.toLowerCase().includes('permission denied');
        return {
          data: [],
          error: error.message,
          isRlsDenied: isRls
        };
      }

      const messages = (data || []).map(mapRowToNetworkMessage);
      return {
        data: messages,
        error: null,
        isRlsDenied: false
      };
    } catch (err: any) {
      return {
        data: [],
        error: err?.message || 'Network exception during fetch',
        isRlsDenied: false
      };
    }
  },

  /**
   * Subscribes to Supabase Realtime postgres_changes on public.emergency_reports.
   */
  subscribeToEmergencyReports(
    onInsert: (newReport: EmergencyReport) => void,
    onUpdate: (updatedReport: EmergencyReport) => void
  ): () => void {
    const client = supabase;
    if (!client) {
      return () => {};
    }

    const channel = client
      .channel('realtime_emergency_reports')
      .on(
        'postgres_changes',
        { event: 'INSERT', schema: 'public', table: 'emergency_reports' },
        (payload) => {
          if (payload.new) {
            const report = mapRowToEmergencyReport(payload.new);
            onInsert(report);
          }
        }
      )
      .on(
        'postgres_changes',
        { event: 'UPDATE', schema: 'public', table: 'emergency_reports' },
        (payload) => {
          if (payload.new) {
            const report = mapRowToEmergencyReport(payload.new);
            onUpdate(report);
          }
        }
      )
      .subscribe((status) => {
        console.log(`[Supabase Realtime] emergency_reports channel status: ${status}`);
      });

    return () => {
      client.removeChannel(channel);
    };
  }
};
