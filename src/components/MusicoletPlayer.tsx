import {
  ArrowDown,
  ArrowUp,
  Blend,
  ChevronDown,
  ChevronUp,
  Disc3,
  Heart,
  Layers,
  Music,
  Pause,
  Play,
  Repeat,
  Repeat1,
  Share2,
  Shuffle,
  SkipBack,
  SkipForward,
  SlidersHorizontal,
  Timer,
  X,
} from 'lucide-react';
import {
  type MusicoletUiState,
  musicoletStore,
} from '../domain/musicoletStore';
import {
  type OfflineTrack,
  RepeatMode,
  parseLrcLyrics,
} from '../model/musicoletModels';
import { formatDurationMs } from '../utils/format';

interface PlayerProps {
  uiState: MusicoletUiState;
  activeTrack: OfflineTrack | null;
}

export function MiniPlayerBar({ uiState, activeTrack }: PlayerProps) {
  if (!activeTrack) return null;

  const activeQueue =
    uiState.queues.find((q) => q.id === uiState.activeQueueId) ??
    uiState.queues[0];
  const maxDuration = Math.max(uiState.durationMs, 1_000);
  const position = Math.max(0, Math.min(uiState.positionMs, maxDuration));
  const progressPct = Math.min(100, Math.max(0, (position / maxDuration) * 100));

  return (
    <div className="fixed bottom-0 left-0 right-0 z-40 bg-[var(--mornica-slate-card-elevated)] border-t border-[var(--mornica-slate-border)]">
      <div className="w-full h-[3px] bg-[var(--mornica-slate-border)]">
        <div
          className="h-full bg-[var(--mornica-emerald)] transition-[width] duration-150"
          style={{ width: `${progressPct}%` }}
        />
      </div>

      <div className="max-w-6xl mx-auto px-3.5 py-2.5 flex items-center justify-between gap-3">
        <button
          type="button"
          onClick={() => musicoletStore.setNowPlayingOpen(true)}
          className="flex-1 min-w-0 flex items-center gap-3 text-left focus-visible:outline-2 focus-visible:outline-[#12D9A1] rounded-xl"
        >
          <div className="w-10 h-10 rounded-[10px] bg-[#14243A] border border-[#24344D] flex items-center justify-center shrink-0">
            <Music className="w-5 h-5 text-[#FFBE55]" aria-hidden="true" />
          </div>
          <div className="min-w-0 flex-1">
            <div className="text-sm font-bold text-[#F4FAFE] truncate">
              {activeTrack.title}
            </div>
            <div className="text-xs font-medium text-[#20B6EB] font-tabular truncate">
              {activeTrack.artist} · {formatDurationMs(position)} /{' '}
              {formatDurationMs(maxDuration)} · {activeQueue.name}
            </div>
          </div>
        </button>

        <div className="flex items-center gap-1 shrink-0">
          <button
            type="button"
            onClick={() => musicoletStore.skipPrevious()}
            aria-label="Previous Track"
            className="min-w-[44px] min-h-[44px] flex items-center justify-center rounded-xl text-[#F4FAFE] hover:bg-[#14243A]"
          >
            <SkipBack className="w-5 h-5" aria-hidden="true" />
          </button>

          <button
            type="button"
            onClick={() => musicoletStore.togglePlayPause()}
            aria-label={uiState.isPlaying ? 'Pause' : 'Play'}
            className="min-w-[48px] h-[38px] px-4 rounded-full mornica-amber-gradient text-[#0B101B] flex items-center justify-center shadow-md hover:brightness-105 active:scale-95 transition-transform"
          >
            {uiState.isPlaying ? (
              <Pause className="w-5 h-5 fill-current" aria-hidden="true" />
            ) : (
              <Play
                className="w-5 h-5 fill-current translate-x-[1px]"
                aria-hidden="true"
              />
            )}
          </button>

          <button
            type="button"
            onClick={() => musicoletStore.skipNext(false)}
            aria-label="Next Track"
            className="min-w-[44px] min-h-[44px] flex items-center justify-center rounded-xl text-[#F4FAFE] hover:bg-[#14243A]"
          >
            <SkipForward className="w-5 h-5" aria-hidden="true" />
          </button>

          <button
            type="button"
            onClick={() => musicoletStore.setNowPlayingOpen(true)}
            aria-label="Expand Now Playing"
            className="hidden sm:flex min-w-[44px] min-h-[44px] items-center justify-center rounded-xl text-[#A5B6CA] hover:text-[#F4FAFE] hover:bg-[#14243A]"
          >
            <ChevronUp className="w-5 h-5" aria-hidden="true" />
          </button>
        </div>
      </div>
    </div>
  );
}

const SPEED_OPTIONS = [0.75, 1.0, 1.25, 1.5] as const;

export function NowPlayingFullModal({ uiState, activeTrack }: PlayerProps) {
  if (!uiState.isNowPlayingOpen || !activeTrack) return null;

  const activeQueue =
    uiState.queues.find((q) => q.id === uiState.activeQueueId) ??
    uiState.queues[0];
  const maxDuration = Math.max(uiState.durationMs, 1_000);
  const position = Math.max(0, Math.min(uiState.positionMs, maxDuration));
  const sliderPct = Math.min(100, Math.max(0, (position / maxDuration) * 100));
  const isSaved = uiState.favoriteTrackIds.includes(activeTrack.id);

  const parsedLyrics = parseLrcLyrics(activeTrack.lyricsText);
  let activeLyricIndex = -1;
  parsedLyrics.forEach((line, idx) => {
    if (line.timeMs !== null && position >= line.timeMs) {
      activeLyricIndex = idx;
    }
  });

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Now Playing"
      className="fixed inset-0 z-50 bg-[var(--mornica-midnight-bg)] overflow-y-auto"
    >
      <div className="max-w-3xl mx-auto px-5 py-4 space-y-5 pb-12">
        {/* Header Bar */}
        <div className="flex items-center justify-between gap-3">
          <button
            type="button"
            onClick={() => musicoletStore.setNowPlayingOpen(false)}
            aria-label="Collapse Now Playing"
            className="min-w-[44px] min-h-[44px] flex items-center justify-center rounded-xl text-[#F4FAFE] hover:bg-[var(--mornica-slate-card)]"
          >
            <ChevronDown className="w-7 h-7" aria-hidden="true" />
          </button>

          <div className="text-center min-w-0">
            <div className="text-[11px] font-bold tracking-wide text-[var(--mornica-emerald)] flex items-center justify-center gap-1.5">
              <Layers className="w-3.5 h-3.5" aria-hidden="true" />
              <span className="truncate">{activeQueue.name}</span>
            </div>
            <div className="text-xs text-[#A5B6CA] truncate">
              {activeTrack.folderName}
            </div>
          </div>

          <div className="flex items-center gap-0.5">
            <button
              type="button"
              onClick={() => musicoletStore.setSharingTrackId(activeTrack.id)}
              aria-label="Share WhatsApp Status Card"
              className="min-w-[40px] min-h-[40px] flex items-center justify-center rounded-xl text-[#A5B6CA] hover:text-[var(--mornica-emerald)] hover:bg-[var(--mornica-slate-card)]"
            >
              <Share2 className="w-4 h-4" aria-hidden="true" />
            </button>
            <button
              type="button"
              onClick={() => musicoletStore.setEqModalOpen(true, 'EQUALIZER')}
              aria-label="Open 10-band Equalizer"
              className="min-w-[40px] min-h-[40px] flex items-center justify-center rounded-xl text-[#A5B6CA] hover:text-[var(--mornica-emerald)] hover:bg-[var(--mornica-slate-card)]"
            >
              <SlidersHorizontal className="w-4 h-4" aria-hidden="true" />
            </button>
            <button
              type="button"
              onClick={() => musicoletStore.setEqModalOpen(true, 'CROSSFADE')}
              aria-label="Open Crossfade Settings"
              className={`min-w-[40px] min-h-[40px] flex items-center justify-center rounded-xl hover:bg-[var(--mornica-slate-card)] ${
                uiState.crossfade.enabled
                  ? 'text-[var(--mornica-cyan)]'
                  : 'text-[#A5B6CA] hover:text-[var(--mornica-cyan)]'
              }`}
            >
              <Blend className="w-4 h-4" aria-hidden="true" />
            </button>
            <button
              type="button"
              onClick={() => musicoletStore.setSleepTimerModalOpen(true)}
              aria-label="Open Sleep Timer"
              className="min-w-[40px] min-h-[40px] flex items-center justify-center rounded-xl text-[#A5B6CA] hover:text-[#FFBE55] hover:bg-[var(--mornica-slate-card)]"
            >
              <Timer className="w-4 h-4" aria-hidden="true" />
            </button>
            <button
              type="button"
              onClick={() => musicoletStore.toggleFavorite(activeTrack.id)}
              aria-label="Favorite Track"
              aria-pressed={isSaved}
              className="min-w-[40px] min-h-[40px] flex items-center justify-center rounded-xl hover:bg-[var(--mornica-slate-card)]"
            >
              <Heart
                className={`w-5 h-5 ${
                  isSaved ? 'fill-[#FF6479] text-[#FF6479]' : 'text-[#A5B6CA]'
                }`}
                aria-hidden="true"
              />
            </button>
          </div>
        </div>

        {/* Mode Switcher: Artwork | Lyrics | Queue */}
        <div
          role="tablist"
          aria-label="Now Playing sections"
          className="grid grid-cols-3 p-1 rounded-xl bg-[#0B1727] border border-[#24344D]"
        >
          {(['ARTWORK', 'LYRICS', 'QUEUE'] as const).map((tab) => {
            const active = uiState.nowPlayingTab === tab;
            return (
              <button
                key={tab}
                type="button"
                role="tab"
                aria-selected={active}
                onClick={() => musicoletStore.setNowPlayingTab(tab)}
                className={`py-2 text-xs font-bold rounded-lg transition-colors ${
                  active
                    ? 'bg-[#14243A] text-[#12D9A1]'
                    : 'text-[#A5B6CA] hover:text-[#F4FAFE]'
                }`}
              >
                {tab === 'ARTWORK'
                  ? 'Player'
                  : tab === 'LYRICS'
                    ? 'Lyrics'
                    : `Queue (${activeQueue.trackIds.length})`}
              </button>
            );
          })}
        </div>

        {/* Center Content */}
        {uiState.nowPlayingTab === 'ARTWORK' && (
          <div className="w-full h-60 rounded-3xl border border-[#24344D] bg-gradient-to-br from-[#1B2F4D] via-[#062E22] to-[#14243A] flex flex-col items-center justify-center p-6 text-center">
            <div className="w-20 h-20 rounded-full bg-[#0B1727] border-2 border-[#12D9A1] flex items-center justify-center mb-3">
              <Disc3
                className={`w-11 h-11 text-[#12D9A1] ${
                  uiState.isPlaying ? 'animate-spin' : ''
                }`}
                style={{ animationDuration: '6s' }}
                aria-hidden="true"
              />
            </div>
            <p className="text-xs font-semibold text-[#20B6EB]">
              {activeTrack.sourceType === 'DEVICE'
                ? 'On-Device Offline Audio'
                : 'Bundled First-Party Offline Audio'}
            </p>
            <p className="text-xs text-[#A5B6CA] mt-1 font-tabular">
              {activeTrack.album} · {activeTrack.releaseYear}
            </p>
            <p className="text-[11px] font-semibold text-[#12D9A1] mt-2 font-tabular">
              {uiState.equalizer.enabled ? 'EQ Active' : 'EQ Bypassed'} ·{' '}
              {uiState.crossfade.enabled
                ? `Crossfade ${uiState.crossfade.durationSec}s`
                : uiState.crossfade.gaplessEnabled
                  ? 'Gapless Transitions'
                  : 'Standard Transitions'}
            </p>
          </div>
        )}

        {uiState.nowPlayingTab === 'LYRICS' && (
          <div className="w-full min-h-60 max-h-72 overflow-y-auto rounded-3xl border border-[#24344D] bg-[#0B1727] p-5 space-y-2.5">
            {parsedLyrics.length === 0 ? (
              <div className="text-center py-10 space-y-3">
                <p className="text-sm text-[#A5B6CA]">
                  No lyrics saved for this track yet.
                </p>
                <button
                  type="button"
                  onClick={() =>
                    musicoletStore.setEditingTrackId(activeTrack.id)
                  }
                  className="px-4 py-2 rounded-xl bg-[#14243A] border border-[#24344D] hover:border-[#12D9A1] text-xs font-bold text-[#12D9A1]"
                >
                  Add Synced (.lrc) or Plain Lyrics
                </button>
              </div>
            ) : (
              <>
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-bold text-[#20B6EB]">
                    Synchronized Lyrics
                  </span>
                  <button
                    type="button"
                    onClick={() =>
                      musicoletStore.setEditingTrackId(activeTrack.id)
                    }
                    className="text-xs font-semibold text-[#12D9A1] hover:underline"
                  >
                    Edit Lyrics
                  </button>
                </div>
                {parsedLyrics.map((line, idx) => {
                  const isCurrent = idx === activeLyricIndex;
                  return (
                    <button
                      key={`${idx}-${line.text}`}
                      type="button"
                      disabled={line.timeMs === null}
                      onClick={() =>
                        line.timeMs !== null &&
                        musicoletStore.seekTo(line.timeMs)
                      }
                      className={`block w-full text-left text-sm transition-colors ${
                        isCurrent
                          ? 'text-[#12D9A1] font-bold'
                          : 'text-[#A5B6CA] hover:text-[#F4FAFE]'
                      }`}
                    >
                      {line.text}
                    </button>
                  );
                })}
              </>
            )}
          </div>
        )}

        {uiState.nowPlayingTab === 'QUEUE' && (
          <div className="w-full min-h-60 max-h-72 overflow-y-auto rounded-3xl border border-[#24344D] bg-[#0B1727] p-4 divide-y divide-[#24344D]/60">
            {activeQueue.trackIds.map((trackId, index) => {
              const qTrack = uiState.tracks.find((t) => t.id === trackId);
              if (!qTrack) return null;
              const isCurrent = qTrack.id === activeTrack.id;
              return (
                <div
                  key={`np-q-${qTrack.id}-${index}`}
                  className="py-2 flex items-center justify-between gap-2"
                >
                  <button
                    type="button"
                    onClick={() =>
                      musicoletStore.playTrackInActiveQueue(qTrack.id, 0)
                    }
                    className="flex-1 min-w-0 text-left flex items-center gap-2.5"
                  >
                    <span className="w-5 text-xs font-tabular text-[#A5B6CA] text-right">
                      {index + 1}
                    </span>
                    <div className="min-w-0 flex-1">
                      <div
                        className={`text-sm font-semibold truncate ${
                          isCurrent ? 'text-[#12D9A1]' : 'text-[#F4FAFE]'
                        }`}
                      >
                        {qTrack.title}
                      </div>
                      <div className="text-xs text-[#A5B6CA] truncate">
                        {qTrack.artist}
                      </div>
                    </div>
                  </button>
                  <div className="flex items-center gap-0.5 shrink-0">
                    <button
                      type="button"
                      disabled={index === 0}
                      onClick={() =>
                        musicoletStore.reorderInQueue(
                          activeQueue.id,
                          index,
                          index - 1,
                        )
                      }
                      aria-label={`Move ${qTrack.title} up`}
                      className="p-1.5 text-[#A5B6CA] hover:text-[#F4FAFE] disabled:opacity-30"
                    >
                      <ArrowUp className="w-4 h-4" aria-hidden="true" />
                    </button>
                    <button
                      type="button"
                      disabled={index === activeQueue.trackIds.length - 1}
                      onClick={() =>
                        musicoletStore.reorderInQueue(
                          activeQueue.id,
                          index,
                          index + 1,
                        )
                      }
                      aria-label={`Move ${qTrack.title} down`}
                      className="p-1.5 text-[#A5B6CA] hover:text-[#F4FAFE] disabled:opacity-30"
                    >
                      <ArrowDown className="w-4 h-4" aria-hidden="true" />
                    </button>
                    <button
                      type="button"
                      onClick={() =>
                        musicoletStore.removeFromQueue(
                          activeQueue.id,
                          qTrack.id,
                        )
                      }
                      aria-label={`Remove ${qTrack.title} from queue`}
                      className="p-1.5 text-[#A5B6CA] hover:text-[#FF6479]"
                    >
                      <X className="w-4 h-4" aria-hidden="true" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Title & Speed Controls */}
        <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-3">
          <div>
            <h2 className="text-xl sm:text-2xl font-extrabold text-[#F4FAFE] leading-tight">
              {activeTrack.title}
            </h2>
            <p className="text-sm font-semibold text-[#20B6EB] mt-0.5">
              {activeTrack.artist} · {activeTrack.album}
            </p>
          </div>

          <div className="flex items-center gap-1 shrink-0">
            <span className="text-xs text-[#A5B6CA] mr-1">Speed:</span>
            {SPEED_OPTIONS.map((spd) => {
              const selected = uiState.playbackRate === spd;
              return (
                <button
                  key={spd}
                  type="button"
                  onClick={() => musicoletStore.setPlaybackRate(spd)}
                  className={`px-2.5 py-1 rounded-lg text-xs font-bold font-tabular transition-colors ${
                    selected
                      ? 'bg-[#12D9A1] text-[#04130E]'
                      : 'bg-[#14243A] border border-[#24344D] text-[#A5B6CA] hover:text-[#F4FAFE]'
                  }`}
                >
                  {spd}x
                </button>
              );
            })}
          </div>
        </div>

        {/* Seek Bar */}
        <div>
          <input
            type="range"
            min={0}
            max={maxDuration}
            value={position}
            onChange={(e) => musicoletStore.seekTo(Number(e.target.value))}
            aria-label="Playback position"
            aria-valuetext={`${formatDurationMs(position)} of ${formatDurationMs(maxDuration)}`}
            className="w-full mornica-slider"
            style={{
              background: `linear-gradient(to right, #12D9A1 ${sliderPct}%, #24344D ${sliderPct}%)`,
            }}
          />
          <div className="flex justify-between text-xs text-[#A5B6CA] font-tabular mt-1.5">
            <span>{formatDurationMs(position)}</span>
            <span>{formatDurationMs(maxDuration)}</span>
          </div>
        </div>

        {/* Primary Transport */}
        <div className="flex items-center justify-evenly py-2">
          <button
            type="button"
            onClick={() => musicoletStore.toggleShuffle()}
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
            onClick={() => musicoletStore.skipPrevious()}
            aria-label="Skip Previous"
            className="min-w-[48px] min-h-[48px] flex items-center justify-center rounded-xl text-[#F4FAFE] hover:bg-[#14243A]"
          >
            <SkipBack className="w-8 h-8" aria-hidden="true" />
          </button>

          <button
            type="button"
            onClick={() => musicoletStore.togglePlayPause()}
            aria-label={uiState.isPlaying ? 'Pause' : 'Play'}
            className="w-[68px] h-[68px] rounded-full mornica-amber-gradient text-[#0B101B] flex items-center justify-center shadow-lg hover:brightness-105 active:scale-95 transition-transform"
          >
            {uiState.isPlaying ? (
              <Pause className="w-8 h-8 fill-current" aria-hidden="true" />
            ) : (
              <Play
                className="w-8 h-8 fill-current translate-x-[2px]"
                aria-hidden="true"
              />
            )}
          </button>

          <button
            type="button"
            onClick={() => musicoletStore.skipNext(false)}
            aria-label="Skip Next"
            className="min-w-[48px] min-h-[48px] flex items-center justify-center rounded-xl text-[#F4FAFE] hover:bg-[#14243A]"
          >
            <SkipForward className="w-8 h-8" aria-hidden="true" />
          </button>

          <button
            type="button"
            onClick={() => musicoletStore.cycleRepeatMode()}
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
      </div>
    </div>
  );
}
