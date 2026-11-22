import React from 'react';
import {
  Network,
  Cpu,
  Battery,
  Wifi,
  Radio,
  Server,
  ArrowRightLeft,
  Clock,
  RefreshCw,
  Lock,
  Database
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
    isDemoMode,
    toggleDemoMode,
    refetchMessages
  } = useIncidents();

  const totalNodes = nodes.length;
  const gatewayNodes = nodes.filter((n) => n.isGateway).length;
  const relayNodes = nodes.filter((n) => !n.isGateway).length;
  const totalPendingMessages = networkMessages.length;

  const getBatteryColor = (level: number) => {
    if (level >= 70) return 'text-emerald-400';
    if (level >= 30) return 'text-amber-400';
    return 'text-red-400';
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-navy-800">
        <div>
          <h1 className="text-2xl font-extrabold text-white tracking-wide flex items-center gap-2">
            Offline Smartphone Mesh Network Monitoring
            <span className="w-2.5 h-2.5 rounded-full bg-cyan animate-pulse" />
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Connected to Supabase table: <span className="font-mono text-cyan">public.network_messages</span>
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 text-xs font-mono text-cyan bg-navy-900 border border-navy-700 px-3 py-1.5 rounded-lg">
            <Radio className="w-3.5 h-3.5 text-cyan animate-spin" />
            <span>MESH PROTOCOL: ResQSync-P2P</span>
          </div>

          <button
            onClick={() => refetchMessages()}
            disabled={isLoadingMessages}
            className="p-2 rounded-lg bg-navy-900 hover:bg-navy-800 border border-navy-700 text-slate-300 hover:text-white transition-colors"
            title="Refresh network telemetry"
          >
            <RefreshCw className={`w-4 h-4 ${isLoadingMessages ? 'animate-spin text-cyan' : ''}`} />
          </button>
        </div>
      </div>

      {/* RLS Error Banner */}
      {messagesError && (
        <div className="p-4 rounded-xl bg-amber-950/40 border border-amber-500/40 text-amber-200 text-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-lg">
          <div className="flex items-center gap-2.5">
            <Lock className="w-5 h-5 text-amber-400 shrink-0" />
            <div>
              <strong className="font-semibold text-amber-100 block">
                Row Level Security (RLS) Active on Telemetry
              </strong>
              <p className="text-amber-200/90 text-[11px] leading-snug">
                {messagesError.includes('permission denied')
                  ? 'Access restricted to authorized coordinators and administrators. Sign in with your operator credentials to view network telemetry.'
                  : messagesError}
              </p>
            </div>
          </div>
          <button
            onClick={toggleDemoMode}
            className="px-3 py-1.5 rounded-lg bg-navy-800 hover:bg-navy-700 text-slate-200 border border-navy-700 text-xs font-medium shrink-0"
          >
            {isDemoMode ? 'Exit Demo Preview' : 'Preview Demo Topology'}
          </button>
        </div>
      )}

      {/* Network Metrics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Detected Nodes"
          value={totalNodes}
          subtitle={`${relayNodes} relays, ${gatewayNodes} gateway`}
          icon={Network}
          variant="cyan"
          trend={{ label: totalNodes > 0 ? 'Active cluster' : 'Awaiting nodes', positive: totalNodes > 0 }}
        />
        <MetricCard
          title="Gateway Uplinks"
          value={gatewayNodes}
          subtitle="Direct cloud connections"
          icon={Server}
          variant="green"
          trend={{ label: 'Backhaul links', positive: true }}
        />
        <MetricCard
          title="Messages Relayed"
          value={totalPendingMessages}
          subtitle="Packets in database"
          icon={ArrowRightLeft}
          variant="amber"
          trend={{ label: 'Mesh hops' }}
        />
        <MetricCard
          title="Average Hop Count"
          value={totalNodes > 0 ? `${(nodes.reduce((a, n) => a + n.hopDistance, 0) / totalNodes).toFixed(1)} Hops` : '0 Hops'}
          subtitle="Sub-second hop propagation"
          icon={Cpu}
          variant="default"
          trend={{ label: 'Max: 20 hops' }}
        />
      </div>

      {/* Node Cards Grid / State Handlers */}
      <div className="bg-navy-900 border border-navy-700 rounded-2xl p-5 shadow-2xl">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
            <Radio className="w-4 h-4 text-cyan" />
            Active Mesh Peers in Cluster ({nodes.length})
          </h2>
          <span className="text-xs text-slate-400 font-mono">
            Derived from public.network_messages
          </span>
        </div>

        {isLoadingMessages ? (
          /* Loading State */
          <div className="py-8 space-y-3">
            <div className="flex items-center justify-center gap-2 text-cyan text-xs font-mono">
              <RefreshCw className="w-4 h-4 animate-spin" />
              <span>Querying network telemetry from Supabase...</span>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
              {[1, 2, 3].map((i) => (
                <div key={i} className="h-28 bg-navy-950/60 rounded-xl border border-navy-800 animate-pulse" />
              ))}
            </div>
          </div>
        ) : nodes.length === 0 ? (
          /* Empty State */
          <div className="py-12 px-4 text-center space-y-3">
            <div className="w-12 h-12 rounded-2xl bg-navy-950 border border-navy-800 flex items-center justify-center mx-auto text-cyan">
              <Database className="w-6 h-6" />
            </div>
            <div className="space-y-1">
              <h3 className="text-sm font-bold text-white">No Network Messages Recorded</h3>
              <p className="text-xs text-slate-400 max-w-md mx-auto leading-relaxed">
                Table <span className="font-mono text-cyan">public.network_messages</span> currently contains 0 telemetry records. As Android devices broadcast Nearby Connections packets, peer hops will display here.
              </p>
            </div>
            <div className="pt-2">
              <button
                onClick={toggleDemoMode}
                className="px-3.5 py-1.5 rounded-lg bg-navy-800 hover:bg-navy-700 text-cyan text-xs font-semibold border border-navy-700"
              >
                {isDemoMode ? 'Disable Demo Data' : 'Preview with Simulated Mesh Topology'}
              </button>
            </div>
          </div>
        ) : (
          /* Populated Grid */
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {nodes.map((node) => (
              <div
                key={node.nodeId}
                className={`p-4 rounded-xl border bg-navy-950 transition-all ${
                  node.isGateway
                    ? 'border-emerald-500/40 shadow-lg shadow-emerald-950/20'
                    : 'border-navy-800 hover:border-cyan/40'
                }`}
              >
                {/* Card Header */}
                <div className="flex items-start justify-between gap-2 mb-3">
                  <div className="space-y-0.5">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-white text-xs">{node.deviceName}</span>
                    </div>
                    <span className="text-[10px] font-mono text-slate-400">{node.nodeId}</span>
                  </div>

                  <span
                    className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded border ${
                      node.isGateway
                        ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/40'
                        : 'bg-cyan/15 text-cyan border-cyan/30'
                    }`}
                  >
                    {node.isGateway ? 'GATEWAY' : `${node.hopDistance} HOPS`}
                  </span>
                </div>

                {/* Node Telemetry Grid */}
                <div className="grid grid-cols-2 gap-2 text-[11px] font-mono pt-2 border-t border-navy-800">
                  <div className="flex items-center gap-1.5 text-slate-300">
                    <Battery className={`w-3.5 h-3.5 ${getBatteryColor(node.batteryLevel)}`} />
                    <span>Battery: {node.batteryLevel}%</span>
                  </div>

                  <div className="flex items-center gap-1.5 text-slate-300">
                    <Wifi className="w-3.5 h-3.5 text-cyan" />
                    <span>Signal: {node.signalStrength} dBm</span>
                  </div>

                  <div className="flex items-center gap-1.5 text-slate-300">
                    <ArrowRightLeft className="w-3.5 h-3.5 text-amber-400" />
                    <span>Packets: {node.pendingMessagesCount}</span>
                  </div>

                  <div className="flex items-center gap-1.5 text-slate-400">
                    <Clock className="w-3.5 h-3.5" />
                    <span>Seen: {formatRelativeTime(node.lastSeen)}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
