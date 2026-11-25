import {
  ChevronDown,
  ChevronUp,
  Disc3,
  Heart,
  Music,
  Pause,
  Play,
  Repeat,
  Repeat1,
  Shuffle,
  SkipBack,
  SkipForward,
} from 'lucide-react';
import type { MornicaUiState, PlaybackRouter } from '../domain/playbackRouter';
import {
  ActiveEngine,
  PROVENANCE_META,
  RepeatMode,
} from '../model/mornicaModels';
import { formatDurationMs } from '../utils/format';
import { LocalTrackItemCard } from './LocalTrackItemCard';

interface PlayerProps {
  uiState: MornicaUiState;
  router: PlaybackRouter;
}

export function OfflineMiniPlayerBar({ uiState, router }: PlayerProps) {
  const title =
    uiState.activeEngine === ActiveEngine.LICENSED_CATALOG
      ? uiState.activeCatalogTrack?.title
      : uiState.activeLocalTrack?.title;
  if (!title) return null;

  const artist =
    uiState.activeEngine === ActiveEngine.LICENSED_CATALOG
      ? uiState.activeCatalogTrack?.artist ?? ''
      : uiState.activeLocalTrack?.artist ?? '';

  const maxDuration = Math.max(uiState.localDurationMs, 1_000);
  const position = Math.max(0, Math.min(uiState.localPositionMs, maxDuration));
  const progressPct = Math.min(100, Math.max(0, (position / maxDuration) * 100));

  return (
    <div className="bg-[#1B2F4D] border-t border-[#24344D]">
      {/* Top 3px progress bar */}
      <div className="w-full h-[3px] bg-[#24344D]">
        <div
          className="h-full bg-[#12D9A1] transition-[width] duration-150"
          style={{ width: `${progressPct}%` }}
        />
      </div>

      <div className="max-w-6xl mx-auto px-3.5 py-2.5 flex items-center justify-between gap-3">
        <button
          type="button"
          onClick={() => router.setNowPlayingExpanded(true)}
          className="flex-1 min-w-0 flex items-center gap-2.5 text-left focus-visible:outline-2 focus-visible:outline-[#12D9A1] rounded-xl"
        >
          <div className="w-10 h-10 rounded-[10px] bg-[#14243A] border border-[#24344D] flex items-center justify-center shrink-0">
            <Music className="w-5 h-5 text-[#FFBE55]" aria-hidden="true" />
          </div>
          <div className="min-w-0 flex-1">
            <div className="text-sm font-bold text-[#F4FAFE] truncate">
              {title}
            </div>
            <div className="text-xs font-medium text-[#20B6EB] font-tabular truncate">
              {artist} · {formatDurationMs(position)} /{' '}
              {formatDurationMs(maxDuration)}
            </div>
          </div>
        </button>

        <div className="flex items-center gap-1 shrink-0">
          <button
            type="button"
            onClick={() => router.skipPrevious()}
            aria-label="Previous Track"
            className="min-w-[44px] min-h-[44px] flex items-center justify-center rounded-xl text-[#F4FAFE] hover:bg-[#14243A] transition-colors"
          >
            <SkipBack className="w-5 h-5" aria-hidden="true" />
          </button>

          {/* Signature Warm Amber-Orange Gradient Play/Pause Pill */}
          <button
            type="button"
            onClick={() => router.toggleLocalPlayPause()}
            aria-label={uiState.isLocalPlaying ? 'Pause' : 'Play'}
            className="min-w-[48px] h-[38px] px-4 rounded-full mornica-amber-gradient text-[#0B101B] flex items-center justify-center shadow-md hover:brightness-105 active:scale-95 transition-transform"
          >
            {uiState.isLocalPlaying ? (
              <Pause className="w-5 h-5 fill-current" aria-hidden="true" />
            ) : (
              <Play className="w-5 h-5 fill-current translate-x-[1px]" aria-hidden="true" />
            )}
          </button>

          <button
            type="button"
            onClick={() => router.skipNext()}
            aria-label="Next Track"
            className="min-w-[44px] min-h-[44px] flex items-center justify-center rounded-xl text-[#F4FAFE] hover:bg-[#14243A] transition-colors"
          >
            <SkipForward className="w-5 h-5" aria-hidden="true" />
          </button>

          <button
            type="button"
            onClick={() => router.setNowPlayingExpanded(true)}
            aria-label="Expand Now Playing"
            className="hidden sm:flex min-w-[44px] min-h-[44px] items-center justify-center rounded-xl text-[#A5B6CA] hover:text-[#F4FAFE] hover:bg-[#14243A] transition-colors"
          >
            <ChevronUp className="w-5 h-5" aria-hidden="true" />
          </button>
        </div>
      </div>
    </div>
  );
}

export function NowPlayingFullSheet({ uiState, router }: PlayerProps) {
  const activeLocal = uiState.activeLocalTrack;
  const activeCatalog = uiState.activeCatalogTrack;
  if (!uiState.isNowPlayingExpanded || (!activeLocal && !activeCatalog)) {
    return null;
  }

  const title = activeLocal?.title ?? activeCatalog?.title ?? '';
  const artist = activeLocal?.artist ?? activeCatalog?.artist ?? '';
  const subtitle = activeLocal
    ? `${activeLocal.artist} · ${activeLocal.album}`
    : `${activeCatalog?.artist ?? ''} · ${activeCatalog?.sourceLabel ?? ''}`;
  const headerFolder = activeLocal
    ? activeLocal.folderName
    : 'Licensed Demo Catalogue';
  const headerModeLabel = activeLocal
    ? 'NOW PLAYING OFFLINE'
    : activeCatalog && router.offlinePlaybackAllowed(activeCatalog)
      ? 'PLAYING OFFLINE'
      : 'STREAMING LICENSED DEMO';
  const provenanceLabel = activeLocal
    ? PROVENANCE_META[activeLocal.provenance].label
    : activeCatalog?.sourceLabel ?? '';

  const maxDuration = Math.max(uiState.localDurationMs, 1_000);
  const position = Math.max(0, Math.min(uiState.localPositionMs, maxDuration));
  const trackId = activeLocal?.id ?? activeCatalog?.id ?? '';
  const isSaved = uiState.favoriteTrackIds.includes(trackId);

  const queueTracks = uiState.queueTrackIds
    .map((id) => uiState.localTracks.find((t) => t.id === id))
    .filter((t): t is NonNullable<typeof t> => t !== undefined);

  const sliderProgress = Math.min(
    100,
    Math.max(0, (position / maxDuration) * 100),
  );

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Now Playing"
      className="fixed inset-0 z-50 bg-[#071321] overflow-y-auto"
    >
      <div className="max-w-3xl mx-auto px-5 py-4 space-y-5 pb-12">
        {/* Top Sheet Bar */}
        <div className="flex items-center justify-between gap-4">
          <button
            type="button"
            onClick={() => router.setNowPlayingExpanded(false)}
            aria-label="Collapse Player"
            className="min-w-[44px] min-h-[44px] flex items-center justify-center rounded-xl text-[#F4FAFE] hover:bg-[#14243A]"
          >
            <ChevronDown className="w-7 h-7" aria-hidden="true" />
          </button>

          <div className="text-center min-w-0">
            <div className="text-[11px] font-bold tracking-wide text-[#12D9A1]">
              {headerModeLabel}
            </div>
            <div className="text-xs text-[#A5B6CA] truncate">
              {headerFolder}
            </div>
          </div>

          <button
            type="button"
            onClick={() => trackId && router.toggleFavorite(trackId)}
            aria-label="Favorite Track"
            aria-pressed={isSaved}
            className="min-w-[44px] min-h-[44px] flex items-center justify-center rounded-xl hover:bg-[#14243A]"
          >
            <Heart
              className={`w-5 h-5 ${
                isSaved
                  ? 'fill-[#FF6479] text-[#FF6479]'
                  : 'text-[#A5B6CA]'
              }`}
              aria-hidden="true"
            />
          </button>
        </div>

        {/* Artwork Showcase Card */}
        <div className="w-full h-60 rounded-3xl border border-[#24344D] bg-gradient-to-br from-[#1B2F4D] via-[#062E22] to-[#14243A] flex flex-col items-center justify-center p-6 text-center">
          <div className="w-20 h-20 rounded-full bg-[#0B1727] border-2 border-[#12D9A1] flex items-center justify-center mb-3">
            <Disc3
              className={`w-11 h-11 text-[#12D9A1] ${
                uiState.isLocalPlaying ? 'animate-spin' : ''
              }`}
              style={{ animationDuration: '6s' }}
              aria-hidden="true"
            />
          </div>
          <p className="text-xs font-semibold text-[#20B6EB]">
            {provenanceLabel}
          </p>
          {activeLocal && (
            <p className="text-xs text-[#A5B6CA] mt-1 font-tabular">
              {activeLocal.album} · {activeLocal.releaseYear}
            </p>
          )}
        </div>

        {/* Title & Artist */}
        <div>
          <h2 className="text-xl sm:text-2xl font-extrabold text-[#F4FAFE] leading-tight">
            {title}
          </h2>
          <p className="text-sm font-semibold text-[#20B6EB] mt-1">
            {subtitle || artist}
          </p>
          {uiState.playbackError && (
            <p className="text-xs font-medium text-[#FF6479] mt-2">
              Playback error: {uiState.playbackError}
            </p>
          )}
        </div>

        {/* Seek Bar */}
        <div>
          <input
            type="range"
            min={0}
            max={maxDuration}
            value={position}
            onChange={(e) => router.seekLocalTo(Number(e.target.value))}
            aria-label="Playback position"
            aria-valuetext={`${formatDurationMs(position)} of ${formatDurationMs(maxDuration)}`}
            className="w-full mornica-slider"
            style={{
              background: `linear-gradient(to right, #12D9A1 ${sliderProgress}%, #24344D ${sliderProgress}%)`,
            }}
          />
          <div className="flex justify-between text-xs text-[#A5B6CA] font-tabular mt-1.5">
            <span>{formatDurationMs(position)}</span>
            <span>{formatDurationMs(maxDuration)}</span>
          </div>
        </div>

        {/* Transport Controls */}
        <div className="flex items-center justify-evenly py-2">
          <button
            type="button"
            onClick={() => router.toggleShuffle()}
            aria-label="Toggle Shuffle"
            aria-pressed={uiState.shuffleEnabled}
            className="min-w-[48px] min-h-[48px] flex items-center justify-center rounded-xl hover:bg-[#14243A]"
          >
            <Shuffle
              className={`w-6 h-6 ${
                uiState.shuffleEnabled ? 'text-[#12D9A1]' : 'text-[#A5B6CA]'
              }`}
              aria-hidden="true"
            />
          </button>

          <button
            type="button"
            onClick={() => router.skipPrevious()}
            aria-label="Skip Previous"
            className="min-w-[48px] min-h-[48px] flex items-center justify-center rounded-xl text-[#F4FAFE] hover:bg-[#14243A]"
          >
            <SkipBack className="w-8 h-8" aria-hidden="true" />
          </button>

          <button
            type="button"
            onClick={() => router.toggleLocalPlayPause()}
            aria-label={uiState.isLocalPlaying ? 'Pause' : 'Play'}
            className="w-[68px] h-[68px] rounded-full mornica-amber-gradient text-[#0B101B] flex items-center justify-center shadow-lg hover:brightness-105 active:scale-95 transition-transform"
          >
            {uiState.isLocalPlaying ? (
              <Pause className="w-8 h-8 fill-current" aria-hidden="true" />
            ) : (
              <Play className="w-8 h-8 fill-current translate-x-[2px]" aria-hidden="true" />
            )}
          </button>

          <button
            type="button"
            onClick={() => router.skipNext()}
            aria-label="Skip Next"
            className="min-w-[48px] min-h-[48px] flex items-center justify-center rounded-xl text-[#F4FAFE] hover:bg-[#14243A]"
          >
            <SkipForward className="w-8 h-8" aria-hidden="true" />
          </button>

          <button
            type="button"
            onClick={() => router.cycleRepeatMode()}
            aria-label={`Repeat Mode: ${uiState.repeatMode}`}
            className="min-w-[48px] min-h-[48px] flex items-center justify-center rounded-xl hover:bg-[#14243A]"
          >
            {uiState.repeatMode === RepeatMode.ONE ? (
              <Repeat1 className="w-6 h-6 text-[#12D9A1]" aria-hidden="true" />
            ) : (
              <Repeat
                className={`w-6 h-6 ${
                  uiState.repeatMode !== RepeatMode.OFF
                    ? 'text-[#12D9A1]'
                    : 'text-[#A5B6CA]'
                }`}
                aria-hidden="true"
              />
            )}
          </button>
        </div>

        {/* Active Offline Queue */}
        <div className="pt-3 border-t border-[#24344D] space-y-3">
          <h3 className="text-xs font-bold text-[#20B6EB] font-tabular">
            ACTIVE OFFLINE QUEUE ({queueTracks.length} tracks)
          </h3>
          <div className="space-y-2.5">
            {queueTracks.map((qTrack) => (
              <LocalTrackItemCard
                key={`np-queue-${qTrack.id}`}
                track={qTrack}
                uiState={uiState}
                router={router}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
