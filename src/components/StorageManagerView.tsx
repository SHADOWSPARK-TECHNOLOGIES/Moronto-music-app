import { HardDrive, Shield, Trash2, WifiOff } from 'lucide-react';
import { estimateStorageStats } from '../domain/musicoletEngine';
import {
  type MusicoletUiState,
  musicoletStore,
} from '../domain/musicoletStore';
import { LibraryViewTab } from '../model/musicoletModels';
import { formatDurationMs } from '../utils/format';

interface StorageManagerViewProps {
  uiState: MusicoletUiState;
}

function formatBytesMb(bytes: number): string {
  if (bytes < 1024 * 1024) {
    return `${Math.round(bytes / 1024)} KB`;
  }
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function StorageManagerView({ uiState }: StorageManagerViewProps) {
  const stats = estimateStorageStats(uiState.tracks);
  const deviceTracks = uiState.tracks.filter((t) => t.sourceType === 'DEVICE');

  return (
    <section aria-labelledby="storage-heading" className="space-y-6">
      <div className="rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 sm:p-8 flex flex-col lg:flex-row lg:items-center justify-between gap-6">
        <div className="space-y-2.5 max-w-2xl">
          <p className="text-xs font-semibold text-[var(--mornica-cyan)] flex items-center gap-1.5">
            <HardDrive className="w-4 h-4" aria-hidden="true" />
            <span>Offline Footprint · Moronto Music</span>
          </p>
          <h1
            id="storage-heading"
            className="text-2xl sm:text-3xl font-extrabold tracking-tight text-[#F4FAFE]"
          >
            Data-Saver &amp; Local Storage Manager
          </h1>
          <p className="text-sm text-[#A5B6CA] leading-relaxed">
            Monitor how much offline space your bundled and imported audio uses,
            toggle strict Data-Saver mode, and manage imported device tracks.
          </p>
        </div>

        {/* Data-Saver Toggle Card */}
        <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-4 flex items-center justify-between gap-4 min-w-[260px]">
          <div className="flex items-start gap-2.5">
            <WifiOff className="w-5 h-5 text-[var(--mornica-emerald)] shrink-0 mt-0.5" aria-hidden="true" />
            <div>
              <div className="text-sm font-bold text-[#F4FAFE]">Data-Saver Mode</div>
              <div className="text-[11px] text-[#A5B6CA]">
                Restrict assets to strictly offline local files
              </div>
            </div>
          </div>

          <button
            type="button"
            role="switch"
            aria-checked={uiState.dataSaverEnabled}
            aria-label="Toggle Data-Saver Mode"
            onClick={() => musicoletStore.toggleDataSaver()}
            className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors ${
              uiState.dataSaverEnabled
                ? 'bg-[var(--mornica-emerald)]'
                : 'bg-[var(--mornica-slate-border)]'
            }`}
          >
            <span
              className={`inline-block h-4 w-4 transform rounded-full bg-[#071321] transition-transform ${
                uiState.dataSaverEnabled ? 'translate-x-6' : 'translate-x-1'
              }`}
            />
          </button>
        </div>
      </div>

      {/* Metrics Breakdown */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 space-y-1">
          <div className="text-2xl font-extrabold text-[var(--mornica-emerald)] font-tabular">
            {formatBytesMb(stats.totalEstimatedBytes)}
          </div>
          <div className="text-sm font-bold text-[#F4FAFE]">Total Library Estimate</div>
          <div className="text-xs text-[#A5B6CA] font-tabular">
            {stats.totalTracks} offline tracks active
          </div>
        </div>

        <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 space-y-1">
          <div className="text-2xl font-extrabold text-[var(--mornica-cyan)] font-tabular">
            {formatBytesMb(stats.bundledEstimatedBytes)}
          </div>
          <div className="text-sm font-bold text-[#F4FAFE]">Bundled Demo Tracks</div>
          <div className="text-xs text-[#A5B6CA] font-tabular">
            {stats.bundledTracks} licensed showcase songs
          </div>
        </div>

        <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 space-y-1">
          <div className="text-2xl font-extrabold text-[#FFBE55] font-tabular">
            {formatBytesMb(stats.deviceEstimatedBytes)}
          </div>
          <div className="text-sm font-bold text-[#F4FAFE]">Imported Device Audio</div>
          <div className="text-xs text-[#A5B6CA] font-tabular">
            {stats.deviceTracks} user files in IndexedDB
          </div>
        </div>
      </div>

      {/* Imported Files Cleanup */}
      <div className="rounded-2xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-5 space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div>
            <h2 className="text-base font-bold text-[#F4FAFE]">
              Imported Device Files ({deviceTracks.length})
            </h2>
            <p className="text-xs text-[#A5B6CA]">
              Remove imported local songs any time to free browser IndexedDB space.
            </p>
          </div>
          <button
            type="button"
            onClick={() => musicoletStore.selectTab(LibraryViewTab.PRIVACY)}
            className="text-xs font-semibold text-[var(--mornica-cyan)] hover:underline inline-flex items-center gap-1"
          >
            <Shield className="w-3.5 h-3.5" aria-hidden="true" />
            <span>Privacy &amp; Ad Consent Settings</span>
          </button>
        </div>

        {deviceTracks.length === 0 ? (
          <p className="text-xs text-[#A5B6CA] py-2">
            No personal device audio imported yet. Use Import Audio in the top bar to add your offline MP3, WAV, or FLAC files.
          </p>
        ) : (
          <ul className="divide-y divide-[var(--mornica-slate-border)]">
            {deviceTracks.map((track) => (
              <li
                key={track.id}
                className="py-3 flex items-center justify-between gap-3"
              >
                <div className="min-w-0">
                  <div className="text-sm font-bold text-[#F4FAFE] truncate">
                    {track.title}
                  </div>
                  <div className="text-xs text-[#A5B6CA] font-tabular truncate">
                    {track.artist} · {formatDurationMs(track.durationMs)} ·{' '}
                    {track.folderName}
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => void musicoletStore.deleteImportedTrack(track.id)}
                  className="min-h-[38px] px-3 py-1.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] hover:border-[#FF6479] text-xs font-bold text-[#FF6479] inline-flex items-center gap-1.5 whitespace-nowrap shrink-0"
                >
                  <Trash2 className="w-3.5 h-3.5" aria-hidden="true" />
                  <span>Delete</span>
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  );
}
