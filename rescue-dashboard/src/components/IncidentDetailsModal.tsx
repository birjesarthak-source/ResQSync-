import React, { useState, useEffect } from 'react';
import { X, MapPin, Users, Bot, Radio, CheckCircle, ShieldAlert, Volume2, Play, Square } from 'lucide-react';
import { EmergencyReport, ReportStatus } from '../types';
import { UrgencyBadge, StatusBadge } from './StatusBadge';
import { formatDate, getIncidentTypeLabel } from '../utils/formatters';
import { decodeAudioBase64ToWav } from '../utils/audioPlayback';
import { useIncidents } from '../context/IncidentContext';

interface IncidentDetailsModalProps {
  report: EmergencyReport | null;
  onClose: () => void;
}

export const IncidentDetailsModal: React.FC<IncidentDetailsModalProps> = ({ report, onClose }) => {
  const { teams, updateReportStatus, assignTeamToReport } = useIncidents();
  const [selectedTeamId, setSelectedTeamId] = useState<string>('');
  const [isPlayingAudio, setIsPlayingAudio] = useState<boolean>(false);
  const [activeAudioObj, setActiveAudioObj] = useState<HTMLAudioElement | null>(null);
  const [audioBlobUrl, setAudioBlobUrl] = useState<string | null>(null);
  const [isDecodingAudio, setIsDecodingAudio] = useState<boolean>(false);
  const [audioError, setAudioError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    let objectUrl: string | null = null;
    setAudioBlobUrl(null);
    setAudioError(null);
    setIsDecodingAudio(Boolean(report?.voiceBase64));

    const decodeAudio = async () => {
      if (!report?.voiceBase64) return;
      try {
        const wav = await decodeAudioBase64ToWav(report.voiceBase64);
        objectUrl = URL.createObjectURL(wav);
        if (cancelled) {
          URL.revokeObjectURL(objectUrl);
          objectUrl = null;
          return;
        }
        setAudioBlobUrl(objectUrl);
      } catch (err) {
        console.error('Failed to decode voice memo:', err);
        if (!cancelled) {
          setAudioError(err instanceof Error ? err.message : 'Audio decoding failed.');
        }
      } finally {
        if (!cancelled) setIsDecodingAudio(false);
      }
    };

    void decodeAudio();
    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [report?.voiceBase64]);

  useEffect(() => () => activeAudioObj?.pause(), [activeAudioObj, audioBlobUrl]);

  const handleTogglePlayAudio = () => {
    if (!audioBlobUrl) return;

    if (isPlayingAudio && activeAudioObj) {
      activeAudioObj.pause();
      activeAudioObj.currentTime = 0;
      setIsPlayingAudio(false);
      setActiveAudioObj(null);
    } else {
      const audio = new Audio(audioBlobUrl);
      audio.onerror = () => {
        console.error('Browser failed to decode voice memo:', audio.error);
        setAudioError('This browser cannot decode the attached audio format.');
        setIsPlayingAudio(false);
        setActiveAudioObj(null);
      };
      audio.onended = () => {
        setIsPlayingAudio(false);
        setActiveAudioObj(null);
      };
      audio.play().then(() => {
        setIsPlayingAudio(true);
        setActiveAudioObj(audio);
      }).catch((e) => {
        console.error('Error playing audio memo:', e);
        setAudioError(e instanceof Error ? e.message : 'Audio playback failed.');
        setIsPlayingAudio(false);
        setActiveAudioObj(null);
      });
    }
  };

  if (!report) return null;

  const handleStatusChange = (status: ReportStatus) => {
    updateReportStatus(report.reportId, status);
  };

  const handleAssignTeam = () => {
    if (!selectedTeamId) return;
    assignTeamToReport(report.reportId, selectedTeamId);
    setSelectedTeamId('');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white border border-slate-200 w-full max-w-2xl rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="p-5 border-b border-slate-200 bg-slate-50 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-red-50 border border-red-200 flex items-center justify-center text-red-600">
              <ShieldAlert className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-lg font-bold text-slate-800 tracking-wide">
                  {getIncidentTypeLabel(report.incidentType)}
                </h3>
                <UrgencyBadge urgency={report.urgency} />
                <StatusBadge status={report.status} />
              </div>
              <span className="text-xs text-slate-500 font-mono">
                ID: {report.reportId} • Logged: {formatDate(report.createdAt)}
              </span>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-2 rounded-lg text-slate-400 hover:text-slate-800 hover:bg-slate-200 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 space-y-6 overflow-y-auto">
          {/* Situation Description */}
          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-blue-600 mb-2">
              Citizen Field Dispatch Description
            </h4>
            <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 text-slate-800 text-sm leading-relaxed">
              {report.description}
            </div>
          </div>

          {/* Voice SOS Audio Memo Player */}
          {isDecodingAudio && (
            <p className="text-xs text-blue-700">Preparing voice memo playback...</p>
          )}

          {report.voiceBase64 && !audioBlobUrl && !isDecodingAudio && !audioError && (
            <p role="alert" className="text-xs text-red-700">
              The attached voice memo could not be decoded.
            </p>
          )}

          {audioBlobUrl && (
            <div className="p-4 rounded-xl bg-blue-50 border border-blue-200 space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-blue-800 font-bold text-xs">
                  <Volume2 className="w-4 h-4 text-blue-600" />
                  <span>VOICE SOS AUDIO MEMO ATTACHED</span>
                </div>
                <button
                  onClick={handleTogglePlayAudio}
                  className={`px-3 py-1.5 rounded-lg font-bold text-xs flex items-center gap-1.5 shadow-xs transition-colors ${
                    isPlayingAudio
                      ? 'bg-red-600 hover:bg-red-700 text-white'
                      : 'bg-blue-600 hover:bg-blue-700 text-white'
                  }`}
                >
                  {isPlayingAudio ? (
                    <>
                      <Square className="w-3.5 h-3.5 fill-current" />
                      <span>Stop Playing</span>
                    </>
                  ) : (
                    <>
                      <Play className="w-3.5 h-3.5 fill-current" />
                      <span>Play Voice SOS Memo</span>
                    </>
                  )}
                </button>
              </div>

              <audio
                controls
                src={audioBlobUrl}
                className="w-full h-10 rounded-lg"
                onError={(event) => {
                  console.error('Browser audio element failed:', event.currentTarget.error);
                  setAudioError('The browser could not play the decoded voice memo.');
                }}
              />
            </div>
          )}
          {audioError && (
            <p role="alert" className="text-xs text-red-700">{audioError}</p>
          )}

          {/* Quick Metrics Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div className="p-3 rounded-lg bg-slate-50 border border-slate-200 flex items-center gap-3">
              <MapPin className="w-5 h-5 text-blue-600 shrink-0" />
              <div className="overflow-hidden">
                <span className="text-[11px] text-slate-500 uppercase font-semibold block">Coordinates</span>
                <span className="text-xs font-mono font-bold text-slate-800 truncate block">
                  {report.latitude ? report.latitude.toFixed(4) : 'N/A'}, {report.longitude ? report.longitude.toFixed(4) : 'N/A'}
                </span>
              </div>
            </div>

            <div className="p-3 rounded-lg bg-slate-50 border border-slate-200 flex items-center gap-3">
              <Users className="w-5 h-5 text-red-600 shrink-0" />
              <div>
                <span className="text-[11px] text-slate-500 uppercase font-semibold block">Victims At Risk</span>
                <span className="text-xs font-mono font-bold text-red-600">
                  {report.peopleAffected} Persons
                </span>
              </div>
            </div>

            <div className="p-3 rounded-lg bg-slate-50 border border-slate-200 flex items-center gap-3">
              <Radio className="w-5 h-5 text-emerald-600 shrink-0" />
              <div className="overflow-hidden">
                <span className="text-[11px] text-slate-500 uppercase font-semibold block">Reporter Node</span>
                <span className="text-xs font-mono font-bold text-emerald-700 truncate block">
                  {report.reporterDeviceId}
                </span>
              </div>
            </div>
          </div>

          {/* Gemini AI Edge Triage Analysis */}
          <div className="p-4 rounded-xl bg-blue-50/60 border border-blue-200">
            <div className="flex items-center gap-2 mb-2 text-blue-700 font-semibold text-xs tracking-wide">
              <Bot className="w-4 h-4 text-blue-600" />
              <span>GEMINI AI TRIAGE DIGEST (ON-DEVICE HYBRID ANALYSIS)</span>
            </div>
            <p className="text-xs text-slate-700 mb-3 leading-relaxed">
              {report.aiSummary || 'Automated triage analysis verified field report.'}
            </p>
            {report.aiActions && report.aiActions.length > 0 && (
              <div>
                <span className="text-[11px] font-semibold text-slate-500 uppercase block mb-1.5">
                  Recommended Dispatch Protocols:
                </span>
                <ul className="space-y-1">
                  {report.aiActions.map((action, i) => (
                    <li key={i} className="text-xs text-slate-700 flex items-start gap-2">
                      <span className="text-blue-600 font-bold">•</span>
                      <span>{action}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>

          {/* Assigned Team Info & Reassignment */}
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
            <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-2">
              Assigned Field Rescue Team
            </h4>
            {report.assignedTeamName ? (
              <div className="flex items-center justify-between">
                <div>
                  <span className="text-sm font-bold text-emerald-700">{report.assignedTeamName}</span>
                  <span className="text-xs text-slate-500 block">Unit status actively working sector</span>
                </div>
                <button
                  onClick={() => handleStatusChange('RESOLVED')}
                  className="px-3 py-1.5 rounded-lg bg-emerald-100 hover:bg-emerald-200 text-emerald-800 border border-emerald-300 text-xs font-semibold flex items-center gap-1.5 transition-colors"
                >
                  <CheckCircle className="w-4 h-4" />
                  Mark Resolved
                </button>
              </div>
            ) : (
              <div className="flex flex-col sm:flex-row gap-2">
                <select
                  value={selectedTeamId}
                  onChange={(e) => setSelectedTeamId(e.target.value)}
                  className="flex-1 bg-white border border-slate-300 rounded-lg px-3 py-2 text-xs text-slate-800 focus:outline-none focus:border-blue-600"
                >
                  <option value="">-- Select Available Rescue Unit --</option>
                  {teams
                    .filter((t) => t.status === 'AVAILABLE')
                    .map((t) => (
                      <option key={t.teamId} value={t.teamId}>
                        {t.teamName} ({t.specialization} • {t.membersCount} crew)
                      </option>
                    ))}
                </select>
                <button
                  onClick={handleAssignTeam}
                  disabled={!selectedTeamId}
                  className="px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs disabled:opacity-40 transition-colors"
                >
                  Dispatch Unit
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Footer actions */}
        <div className="p-4 border-t border-slate-200 bg-slate-50 flex items-center justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-lg bg-slate-200 hover:bg-slate-300 text-slate-800 text-xs font-semibold"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
