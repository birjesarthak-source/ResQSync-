import React, { useEffect, useMemo, useState } from 'react';
import { AlertTriangle, MapPin, RefreshCw, Save } from 'lucide-react';
import {
  CircleMarker,
  MapContainer,
  Popup,
  TileLayer,
  useMap,
  useMapEvents
} from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import { useIncidents } from '../context/IncidentContext';
import { UrgencyBadge } from '../components/StatusBadge';
import { EmergencyReport } from '../types';

type LocatedReport = EmergencyReport & { latitude: number; longitude: number };
type Coordinates = [number, number];

const isLocated = (report: EmergencyReport): report is LocatedReport =>
  report.latitude !== null &&
  report.longitude !== null &&
  Number.isFinite(report.latitude) &&
  Number.isFinite(report.longitude) &&
  report.latitude >= -90 &&
  report.latitude <= 90 &&
  report.longitude >= -180 &&
  report.longitude <= 180;

const MapClickSelector: React.FC<{
  onSelect: (coordinates: Coordinates) => void;
}> = ({ onSelect }) => {
  useMapEvents({
    click(event) {
      onSelect([event.latlng.lat, event.latlng.lng]);
    }
  });
  return null;
};

const FitReports: React.FC<{ coordinates: Coordinates[] }> = ({ coordinates }) => {
  const map = useMap();

  useEffect(() => {
    if (coordinates.length === 1) {
      map.setView(coordinates[0], 14);
    } else if (coordinates.length > 1) {
      map.fitBounds(coordinates, { padding: [40, 40], maxZoom: 14 });
    }
  }, [coordinates, map]);

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => map.invalidateSize());
    return () => window.cancelAnimationFrame(frame);
  }, [map]);

  return null;
};

export const IncidentMapPage: React.FC = () => {
  const {
    reports,
    isLoadingReports,
    refetchReports,
    updateReportCoordinates,
    isSupabaseAuth,
    isDemoMode
  } = useIncidents();
  const [selectedReportId, setSelectedReportId] = useState<string | null>(null);
  const [draftCoordinates, setDraftCoordinates] = useState<Coordinates | null>(null);
  const [isSavingCoordinates, setIsSavingCoordinates] = useState(false);
  const [coordinateMessage, setCoordinateMessage] = useState<string | null>(null);

  const locatedReports = useMemo(() => reports.filter(isLocated), [reports]);
  const mapCoordinates = useMemo(
    () => locatedReports.map((report) => [report.latitude, report.longitude] as Coordinates),
    [locatedReports]
  );
  const selectedReport =
    reports.find((report) => report.reportId === selectedReportId) ?? reports[0] ?? null;

  useEffect(() => {
    if (!selectedReportId && reports.length > 0) {
      setSelectedReportId(reports[0].reportId);
    } else if (selectedReportId && !reports.some((report) => report.reportId === selectedReportId)) {
      setSelectedReportId(reports[0]?.reportId ?? null);
    }
  }, [reports, selectedReportId]);

  useEffect(() => {
    setDraftCoordinates(null);
    setCoordinateMessage(null);
  }, [selectedReport?.reportId]);

  const canSaveCoordinates = isSupabaseAuth && !isDemoMode;

  const handleSaveCoordinates = async () => {
    if (!selectedReport || !draftCoordinates || !canSaveCoordinates) return;

    setIsSavingCoordinates(true);
    setCoordinateMessage(null);
    const result = await updateReportCoordinates(
      selectedReport.reportId,
      draftCoordinates[0],
      draftCoordinates[1]
    );
    setCoordinateMessage(
      result.error
        ? result.error
        : 'Coordinates saved to this report.'
    );
    if (!result.error) setDraftCoordinates(null);
    setIsSavingCoordinates(false);
  };

  const displayedCoordinates = draftCoordinates ??
    (selectedReport && isLocated(selectedReport)
      ? [selectedReport.latitude, selectedReport.longitude] as Coordinates
      : null);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-800 tracking-wide flex items-center gap-2">
            Disaster incident map
            <span className="text-xs px-2.5 py-0.5 rounded-full bg-blue-50 text-blue-600 border border-blue-200 font-mono font-bold">
              {locatedReports.length} located
            </span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Select a report, then click the map to place or adjust its location.
          </p>
        </div>

        <button
          onClick={() => void refetchReports()}
          disabled={isLoadingReports}
          className="px-3.5 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs flex items-center gap-2 shadow-xs transition-colors self-start sm:self-auto disabled:opacity-60"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoadingReports ? 'animate-spin' : ''}`} />
          <span>Refresh reports</span>
        </button>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-xs grid grid-cols-1 lg:grid-cols-3 min-h-[620px]">
        <section className="border-b lg:border-b-0 lg:border-r border-slate-200 flex flex-col min-h-[250px] lg:max-h-[760px] bg-slate-50/50">
          <div className="p-3 bg-slate-100 border-b border-slate-200 text-xs font-bold text-slate-700 uppercase tracking-wider">
            Incident reports ({reports.length})
          </div>

          <div className="flex-1 overflow-y-auto divide-y divide-slate-100 p-2 space-y-1">
            {reports.length === 0 ? (
              <div className="p-6 text-center text-slate-500 text-xs">
                {isLoadingReports ? 'Loading reports…' : 'No reports available.'}
              </div>
            ) : (
              reports.map((report) => {
                const hasCoordinates = isLocated(report);
                return (
                  <button
                    key={report.reportId}
                    type="button"
                    onClick={() => setSelectedReportId(report.reportId)}
                    className={`w-full text-left p-3 rounded-lg transition-colors ${
                      selectedReport?.reportId === report.reportId
                        ? 'bg-blue-50 border border-blue-300'
                        : 'bg-white hover:bg-slate-100 border border-slate-200'
                    }`}
                  >
                    <div className="flex items-center justify-between gap-2 mb-1">
                      <span className="font-bold text-slate-800 text-xs">
                        {report.incidentType.replace(/_/g, ' ')}
                      </span>
                      <UrgencyBadge urgency={report.urgency} />
                    </div>
                    <p className="text-slate-600 text-xs line-clamp-2">{report.description}</p>
                    <div className="text-[10px] text-slate-500 font-mono mt-2 flex items-center gap-1">
                      <MapPin className="w-3 h-3 text-blue-600 shrink-0" />
                      <span>
                        {hasCoordinates
                          ? `${report.latitude.toFixed(5)}, ${report.longitude.toFixed(5)}`
                          : 'Location unavailable · select to place pin'}
                      </span>
                    </div>
                  </button>
                );
              })
            )}
          </div>
        </section>

        <section className="lg:col-span-2 flex flex-col min-w-0">
          <div className="h-[460px] sm:h-[560px] lg:h-[660px] relative">
            <MapContainer
              center={[0, 0]}
              zoom={2}
              scrollWheelZoom
              className="h-full w-full z-0"
              style={{ minHeight: '100%' }}
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                maxZoom={19}
              />
              <FitReports coordinates={mapCoordinates} />
              <MapClickSelector
                onSelect={(coordinates) => {
                  setDraftCoordinates(coordinates);
                  setCoordinateMessage(null);
                }}
              />

              {locatedReports.map((report, index) => (
                <CircleMarker
                  key={report.reportId}
                  center={[report.latitude, report.longitude]}
                  radius={selectedReport?.reportId === report.reportId ? 11 : 8}
                  pathOptions={{
                    color: '#ffffff',
                    weight: 2,
                    fillColor:
                      report.urgency === 'CRITICAL'
                        ? '#dc2626'
                        : report.urgency === 'HIGH'
                          ? '#ea580c'
                          : '#2563eb',
                    fillOpacity: 0.95,
                    className: 'report-map-marker'
                  }}
                  eventHandlers={{
                    click: (event) => {
                      event.originalEvent.stopPropagation();
                      setSelectedReportId(report.reportId);
                    }
                  }}
                >
                  <Popup>
                    <strong>{index + 1}. {report.incidentType.replace(/_/g, ' ')}</strong>
                    <br />
                    {report.urgency} priority
                    <br />
                    {report.latitude.toFixed(5)}, {report.longitude.toFixed(5)}
                  </Popup>
                </CircleMarker>
              ))}

              {draftCoordinates && (
                <CircleMarker
                  center={draftCoordinates}
                  radius={12}
                  pathOptions={{
                    color: '#ffffff',
                    weight: 3,
                    fillColor: '#dc2626',
                    fillOpacity: 1
                  }}
                >
                  <Popup>Proposed report location</Popup>
                </CircleMarker>
              )}
            </MapContainer>
            <div className="absolute left-3 top-3 z-[1000] rounded-md bg-white/95 border border-slate-200 px-2.5 py-1.5 text-[10px] text-slate-600 shadow-sm">
              Map data © OpenStreetMap contributors
            </div>
          </div>

          <div className="border-t border-slate-200 bg-white p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="min-w-0">
              <p className="text-xs font-semibold text-slate-700">
                {selectedReport
                  ? `Pin for ${selectedReport.incidentType.replace(/_/g, ' ')} · ${selectedReport.reportId}`
                  : 'Select a report before placing a pin'}
              </p>
              <p className="text-[11px] font-mono text-slate-500 mt-1">
                {displayedCoordinates
                  ? `${draftCoordinates ? 'Proposed' : 'Current'}: ${displayedCoordinates[0].toFixed(6)}, ${displayedCoordinates[1].toFixed(6)}`
                  : 'Click the map to choose coordinates'}
              </p>
              {coordinateMessage && (
                <p
                  role="status"
                  className={`text-xs mt-1 ${
                    coordinateMessage.includes('saved')
                      ? 'text-emerald-700'
                      : 'text-red-700'
                  }`}
                >
                  {coordinateMessage}
                </p>
              )}
              {!canSaveCoordinates && (
                <p className="text-[11px] text-amber-700 mt-1">
                  Sign in with a live responder account to save coordinate changes.
                </p>
              )}
              {selectedReport && !draftCoordinates && (
                <p className="text-[11px] text-slate-500 mt-1">
                  Click the map to place or adjust this report’s pin.
                </p>
              )}
            </div>
            <button
              type="button"
              onClick={() => void handleSaveCoordinates()}
              disabled={!selectedReport || !draftCoordinates || !canSaveCoordinates || isSavingCoordinates}
              className="shrink-0 inline-flex items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-2.5 text-xs font-bold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isSavingCoordinates ? (
                <RefreshCw className="h-4 w-4 animate-spin" />
              ) : (
                <Save className="h-4 w-4" />
              )}
              Save report pin
            </button>
          </div>
        </section>
      </div>

      {reports.length > 0 && locatedReports.length === 0 && (
        <p className="flex items-center gap-2 text-xs text-slate-500">
          <AlertTriangle className="h-4 w-4 text-amber-500" />
          No reports currently have coordinates. Select one and place its pin on the map.
        </p>
      )}
    </div>
  );
};
