import { useEffect, useRef, useState } from 'react';
import { AudioLines, Heart, MoreVertical, Music } from 'lucide-react';
import type { MornicaUiState, PlaybackRouter } from '../domain/playbackRouter';
import {
  type LocalMediaTrack,
  PROVENANCE_META,
  isTrackPlayableOffline,
} from '../model/mornicaModels';
import { formatDurationMs } from '../utils/format';

interface LocalTrackItemCardProps {
  track: LocalMediaTrack;
  uiState: MornicaUiState;
  router: PlaybackRouter;
}

export function LocalTrackItemCard({
  track,
  uiState,
  router,
}: LocalTrackItemCardProps) {
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  const isSaved = uiState.favoriteTrackIds.includes(track.id);
  const isActive = uiState.activeLocalTrack?.id === track.id;
  const isPlayable = isTrackPlayableOffline(track);
  const provenanceShort = PROVENANCE_META[track.provenance].shortLabel;

  useEffect(() => {
    if (!menuOpen) return;
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [menuOpen]);

  return (
    <div
      className={`rounded-2xl border transition-colors ${
        isActive
          ? 'bg-[#1B2F4D] border-[#12D9A1]'
          : 'bg-[#14243A] border-[#24344D] hover:border-[#354b6d]'
      }`}
    >
      <div className="px-3.5 py-3 flex items-center justify-between gap-3">
        {/* Main clickable area */}
        <button
          type="button"
          disabled={!isPlayable}
          onClick={() => router.playLocalTrack(track)}
          className={`flex-1 min-w-0 flex items-center gap-3 text-left focus-visible:outline-2 focus-visible:outline-[#12D9A1] rounded-xl ${
            isPlayable ? 'cursor-pointer' : 'cursor-not-allowed opacity-75'
          }`}
        >
          <div
            className={`w-11 h-11 rounded-[10px] border flex items-center justify-center shrink-0 ${
              isActive
                ? 'bg-[#062E22] border-[#12D9A1]'
                : 'bg-[#0B1727] border-[#24344D]'
            }`}
          >
            {isActive && uiState.isLocalPlaying ? (
              <AudioLines
                className="w-5 h-5 text-[#12D9A1]"
                aria-hidden="true"
              />
            ) : (
              <Music
                className={`w-5 h-5 ${
                  isPlayable ? 'text-[#12D9A1]' : 'text-[#A5B6CA]'
                }`}
                aria-hidden="true"
              />
            )}
          </div>

          <div className="min-w-0 flex-1">
            <div
              className={`text-sm sm:text-base font-bold truncate ${
                isActive ? 'text-[#12D9A1]' : 'text-[#F4FAFE]'
              }`}
            >
              {track.title}
            </div>
            <div className="text-xs text-[#A5B6CA] truncate mt-0.5">
              {track.artist} · {track.folderName}
            </div>
            <div
              className={`text-[11px] font-semibold font-tabular mt-0.5 truncate ${
                isPlayable ? 'text-[#20B6EB]' : 'text-[#FFBE55]'
              }`}
            >
              {provenanceShort} · {formatDurationMs(track.durationMs)} ·{' '}
              {track.releaseYear}
            </div>
          </div>
        </button>

        {/* Actions */}
        <div className="flex items-center shrink-0">
          <button
            type="button"
            onClick={() => router.toggleFavorite(track.id)}
            aria-label={isSaved ? 'Remove Favorite' : 'Save Favorite'}
            aria-pressed={isSaved}
            className="min-w-[44px] min-h-[44px] flex items-center justify-center rounded-xl hover:bg-[#0B1727]/60 transition-colors focus-visible:outline-2 focus-visible:outline-[#12D9A1]"
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

          <div className="relative" ref={menuRef}>
            <button
              type="button"
              onClick={() => setMenuOpen((prev) => !prev)}
              aria-label="Track Options"
              aria-expanded={menuOpen}
              className="min-w-[44px] min-h-[44px] flex items-center justify-center rounded-xl text-[#A5B6CA] hover:text-[#F4FAFE] hover:bg-[#0B1727]/60 transition-colors focus-visible:outline-2 focus-visible:outline-[#12D9A1]"
            >
              <MoreVertical className="w-5 h-5" aria-hidden="true" />
            </button>

            {menuOpen && (
              <div
                role="menu"
                className="absolute right-0 mt-1 w-56 rounded-xl bg-[#0B1727] border border-[#24344D] shadow-xl py-1 z-40"
              >
                <button
                  type="button"
                  role="menuitem"
                  disabled={!isPlayable}
                  onClick={() => {
                    setMenuOpen(false);
                    router.playLocalTrack(track);
                  }}
                  className={`w-full px-3.5 py-2.5 text-left text-xs font-medium ${
                    isPlayable
                      ? 'text-[#F4FAFE] hover:bg-[#1B2F4D]'
                      : 'text-[#A5B6CA]/60 cursor-not-allowed'
                  }`}
                >
                  {isPlayable
                    ? 'Play Offline Now'
                    : 'Awaiting Rights Clearance'}
                </button>
                {isPlayable && (
                  <>
                    <button
                      type="button"
                      role="menuitem"
                      onClick={() => {
                        setMenuOpen(false);
                        router.playLocalTrack(track);
                        router.setNowPlayingExpanded(true);
                      }}
                      className="w-full px-3.5 py-2.5 text-left text-xs font-medium text-[#F4FAFE] hover:bg-[#1B2F4D]"
                    >
                      Open Full Now Playing
                    </button>
                    <button
                      type="button"
                      role="menuitem"
                      onClick={() => {
                        setMenuOpen(false);
                        router.addToPlaylist('Lagos Commute', track.id);
                      }}
                      className="w-full px-3.5 py-2.5 text-left text-xs font-medium text-[#F4FAFE] hover:bg-[#1B2F4D]"
                    >
                      Add to Lagos Commute Queue
                    </button>
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
