import React, { useState, useEffect } from 'react';
import { Bell, Volume2, VolumeX, Check, AlertTriangle, X } from 'lucide-react';
import { EmergencyReport } from '../types';
import { useIncidents } from '../context/IncidentContext';

export interface WebNotification {
  id: string;
  title: string;
  message: string;
  timestamp: number;
  read: boolean;
  type: 'CRITICAL' | 'NEW_REPORT' | 'UPDATE';
}

export const NotificationPanel: React.FC = () => {
  const { reports } = useIncidents();
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState<WebNotification[]>([]);
  const [soundEnabled, setSoundEnabled] = useState(true);
  const [browserPermission, setBrowserPermission] = useState<NotificationPermission>(
    typeof Notification !== 'undefined' ? Notification.permission : 'default'
  );

  // Sync incoming reports into web notifications
  useEffect(() => {
    if (reports.length > 0) {
      const latest = reports[0];
      setNotifications((prev) => {
        if (prev.some((n) => n.id === latest.reportId)) return prev;
        const newNotif: WebNotification = {
          id: latest.reportId,
          title: latest.urgency === 'CRITICAL' ? '🚨 CRITICAL EMERGENCY' : '📡 New Emergency Report',
          message: `${latest.incidentType.replace('_', ' ')}: ${latest.description ? latest.description.slice(0, 60) : ''}`,
          timestamp: latest.createdAt,
          read: false,
          type: latest.urgency === 'CRITICAL' ? 'CRITICAL' : 'NEW_REPORT'
        };

        // Play audio alert if enabled
        if (soundEnabled && typeof Audio !== 'undefined') {
          try {
            const audio = new Audio('https://assets.mixkit.co/active_storage/sfx/2869/2869-preview.mp3');
            audio.volume = 0.4;
            audio.play().catch(() => {});
          } catch (e) {
            // ignore
          }
        }

        // Show browser HTML5 push notification if granted
        if (typeof Notification !== 'undefined' && Notification.permission === 'granted') {
          try {
            new Notification(newNotif.title, {
              body: newNotif.message,
              icon: '/favicon.ico'
            });
          } catch (e) {
            // ignore
          }
        }

        return [newNotif, ...prev];
      });
    }
  }, [reports, soundEnabled]);

  const requestBrowserPermission = async () => {
    if (typeof Notification !== 'undefined') {
      const perm = await Notification.requestPermission();
      setBrowserPermission(perm);
    }
  };

  const unreadCount = notifications.filter((n) => !n.read).length;

  const markAllAsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen((prev) => !prev)}
        className="p-2 rounded-lg text-slate-600 hover:text-slate-900 hover:bg-slate-100 relative transition-colors"
        title="Notifications"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute top-1 right-1 w-4 h-4 bg-red-600 text-white font-bold text-[10px] rounded-full flex items-center justify-center border border-white animate-pulse">
            {unreadCount}
          </span>
        )}
      </button>

      {/* Popover Drawer */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white rounded-xl shadow-2xl border border-slate-200 z-50 overflow-hidden">
          <div className="p-3 bg-slate-100 border-b border-slate-200 flex items-center justify-between">
            <div className="flex items-center gap-2 font-bold text-slate-800 text-sm">
              <Bell className="w-4 h-4 text-blue-600" />
              <span>Realtime Notifications</span>
            </div>

            <div className="flex items-center gap-1">
              <button
                onClick={() => setSoundEnabled((prev) => !prev)}
                className="p-1 rounded text-slate-500 hover:text-slate-800 hover:bg-slate-200"
                title={soundEnabled ? 'Mute Alert Sound' : 'Enable Alert Sound'}
              >
                {soundEnabled ? <Volume2 className="w-4 h-4 text-blue-600" /> : <VolumeX className="w-4 h-4 text-slate-400" />}
              </button>

              <button
                onClick={() => setIsOpen(false)}
                className="p-1 rounded text-slate-500 hover:text-slate-800 hover:bg-slate-200"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Browser Permission Prompt Banner */}
          {browserPermission === 'default' && (
            <div className="p-2.5 bg-blue-50 border-b border-blue-200 flex items-center justify-between text-xs text-blue-800">
              <span>Enable desktop notifications for live alerts?</span>
              <button
                onClick={requestBrowserPermission}
                className="px-2 py-0.5 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded text-[11px]"
              >
                Allow
              </button>
            </div>
          )}

          {/* Notification List */}
          <div className="max-h-80 overflow-y-auto divide-y divide-slate-100">
            {notifications.length === 0 ? (
              <div className="p-6 text-center text-slate-400 text-xs">
                No new notifications. Realtime stream active.
              </div>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.id}
                  className={`p-3 text-xs transition-colors ${
                    n.read ? 'bg-white text-slate-600' : 'bg-blue-50/50 text-slate-800 font-medium'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1">
                    <span className={`font-bold flex items-center gap-1 ${n.type === 'CRITICAL' ? 'text-red-600' : 'text-blue-600'}`}>
                      <AlertTriangle className="w-3.5 h-3.5" />
                      {n.title}
                    </span>
                    <span className="text-[10px] text-slate-400">
                      {new Date(n.timestamp).toLocaleTimeString()}
                    </span>
                  </div>
                  <p className="text-slate-600 text-[11px] leading-snug">{n.message}</p>
                </div>
              ))
            )}
          </div>

          {notifications.length > 0 && (
            <div className="p-2 bg-slate-50 border-t border-slate-200 flex justify-between items-center text-xs">
              <button
                onClick={markAllAsRead}
                className="text-blue-600 hover:text-blue-800 font-semibold flex items-center gap-1"
              >
                <Check className="w-3.5 h-3.5" /> Mark all as read
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
