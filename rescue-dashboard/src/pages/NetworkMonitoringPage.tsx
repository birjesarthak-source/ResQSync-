import React from 'react';
import {
  Network,
  Radio,
  RefreshCw,
  Cpu,
  Battery,
  Wifi,
  Server
} from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { MetricCard } from '../components/MetricCard';
import { formatRelativeTime } from '../utils/formatters';

export const NetworkMonitoringPage: React.FC = () => {
  const {
    nodes,
    networkMessages,
    isLoadingMessages,
    messagesError,
    refetchMessages
  } = useIncidents();

  const totalNodes = nodes.length;
  const gatewayNodes = nodes.filter((n) => n.isGateway).length;
  const relayNodes = nodes.filter((n) => !n.isGateway).length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-800 tracking-wide flex items-center gap-2">
            Offline Smartphone Mesh Network Telemetry
            <span className="w-2.5 h-2.5 rounded-full bg-blue-600 animate-pulse" />
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Connected to Supabase table: <span className="font-mono text-blue-600">public.network_messages</span>
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => refetchMessages()}
            disabled={isLoadingMessages}
            className="p-2 rounded-lg bg-white border border-slate-200 text-slate-600 hover:text-slate-900 transition-colors shadow-xs"
            title="Refresh network telemetry"
          >
            <RefreshCw className={`w-4 h-4 ${isLoadingMessages ? 'animate-spin text-blue-600' : ''}`} />
          </button>
        </div>
      </div>

      {/* Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Discovered Mesh Nodes"
          value={totalNodes}
          subtitle="Active peer smartphones"
          icon={Network}
          variant="blue"
        />
        <MetricCard
          title="Gateway Relays"
          value={gatewayNodes}
          subtitle="Connected to internet"
          icon={Server}
          variant="green"
        />
        <MetricCard
          title="Relay Nodes"
          value={relayNodes}
          subtitle="Multi-hop store-and-forward"
          icon={Cpu}
          variant="amber"
        />
        <MetricCard
          title="Logged Network Packets"
          value={networkMessages.length}
          subtitle="Telemetry messages"
          icon={Radio}
          variant="default"
        />
      </div>

      {/* Nodes Table */}
      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-xs">
        <div className="p-4 border-b border-slate-200 bg-slate-50 font-bold text-slate-800 text-xs uppercase tracking-wider">
          Mesh Peer Topology & Battery Telemetry
        </div>

        {messagesError && (
          <div className="p-4 bg-red-50 text-xs text-red-700 border-b border-red-200">
            {messagesError}
          </div>
        )}

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-[11px] font-bold uppercase tracking-wider text-slate-500">
                <th className="p-3.5">Node ID & Name</th>
                <th className="p-3.5">Mesh Role</th>
                <th className="p-3.5">Hop Distance</th>
                <th className="p-3.5">Battery</th>
                <th className="p-3.5">Signal</th>
                <th className="p-3.5">Last Seen</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-xs">
              {nodes.length === 0 ? (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-slate-400">
                    No active mesh nodes detected in network telemetry log.
                  </td>
                </tr>
              ) : (
                nodes.map((node) => (
                  <tr key={node.nodeId} className="hover:bg-slate-50/80 transition-colors">
                    <td className="p-3.5 font-bold text-slate-800 font-mono">
                      {node.deviceName}
                    </td>
                    <td className="p-3.5">
                      <span className={`px-2 py-0.5 text-[10px] font-bold rounded-full ${
                        node.isGateway ? 'bg-emerald-100 text-emerald-800' : 'bg-blue-100 text-blue-800'
                      }`}>
                        {node.isGateway ? 'INTERNET GATEWAY' : 'STORE-FORWARD RELAY'}
                      </span>
                    </td>
                    <td className="p-3.5 font-mono text-slate-700">
                      {node.hopDistance} hops
                    </td>
                    <td className="p-3.5 font-mono font-bold text-slate-700">
                      {node.batteryLevel}%
                    </td>
                    <td className="p-3.5 font-mono text-slate-600">
                      {node.signalStrength} dBm
                    </td>
                    <td className="p-3.5 text-slate-500 font-mono">
                      {formatRelativeTime(node.lastSeen)}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
