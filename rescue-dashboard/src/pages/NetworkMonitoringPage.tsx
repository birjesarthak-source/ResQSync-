import React from 'react';
import {
  Network,
  Cpu,
  Battery,
  Wifi,
  Radio,
  Server,
  ArrowRightLeft,
  CheckCircle2,
  Clock
} from 'lucide-react';
import { useIncidents } from '../context/IncidentContext';
import { MetricCard } from '../components/MetricCard';
import { formatRelativeTime } from '../utils/formatters';

export const NetworkMonitoringPage: React.FC = () => {
  const { nodes } = useIncidents();

  const totalNodes = nodes.length;
  const gatewayNodes = nodes.filter((n) => n.isGateway).length;
  const relayNodes = nodes.filter((n) => !n.isGateway).length;
  const totalPendingMessages = nodes.reduce((acc, n) => acc + n.pendingMessagesCount, 0);

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
            Topology diagnostics for Wi-Fi Direct and BLE Nearby Connections peer hops
          </p>
        </div>

        <div className="flex items-center gap-2 text-xs font-mono text-cyan bg-navy-900 border border-navy-700 px-3 py-1.5 rounded-lg">
          <Radio className="w-3.5 h-3.5 text-cyan animate-spin" />
          <span>MESH PROTOCOL: ResQSync-P2P v1.0</span>
        </div>
      </div>

      {/* Network Metrics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Peer Relays Active"
          value={totalNodes}
          subtitle={`${relayNodes} hops, ${gatewayNodes} gateway`}
          icon={Network}
          variant="cyan"
          trend={{ label: 'Healthy topology', positive: true }}
        />
        <MetricCard
          title="Gateway Uplinks"
          value={gatewayNodes}
          subtitle="Cellular backhaul active"
          icon={Server}
          variant="green"
          trend={{ label: 'Direct internet', positive: true }}
        />
        <MetricCard
          title="Messages in Transit"
          value={totalPendingMessages}
          subtitle="Store-and-forward queue"
          icon={ArrowRightLeft}
          variant="amber"
          trend={{ label: 'Buffered on devices' }}
        />
        <MetricCard
          title="Average Hop Count"
          value="1.8 Hops"
          subtitle="Sub-second local hop"
          icon={Cpu}
          variant="default"
          trend={{ label: 'Max limit: 10 hops' }}
        />
      </div>

      {/* Node Cards Grid */}
      <div className="bg-navy-900 border border-navy-700 rounded-2xl p-5 shadow-2xl">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
            <Radio className="w-4 h-4 text-cyan" />
            Detected Smartphone Nodes in Mesh Cluster ({nodes.length})
          </h2>
          <span className="text-xs text-slate-400 font-mono">
            Auto-refreshed via store-and-forward beacons
          </span>
        </div>

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
                  <span>Buffered: {node.pendingMessagesCount} msgs</span>
                </div>

                <div className="flex items-center gap-1.5 text-slate-400">
                  <Clock className="w-3.5 h-3.5" />
                  <span>Seen: {formatRelativeTime(node.lastSeen)}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
