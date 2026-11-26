import { useEffect, useRef, useState } from 'react';
import { AudioLines, Heart, MoreVertical, Music, Share2 } from 'lucide-react';
import {
  type MusicoletUiState,
  musicoletStore,
} from '../domain/musicoletStore';
import type { OfflineTrack } from '../model/musicoletModels';
import { formatDurationMs } from '../utils/format';

interface TrackRowCardProps {
  track: OfflineTrack;
  uiState: MusicoletUiState;
  activeTrackId: string | null;
}

export function TrackRowCard({
  track,
  uiState,
  activeTrackId,
}: TrackRowCardProps) {
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  const isActive = activeTrackId === track.id;
  const isSaved = uiState.favoriteTrackIds.includes(track.id);

  useEffect(() => {
    if (!menuOpen) return;
    const onDown = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setMenuOpen(false);
    };
    document.addEventListener('mousedown', onDown);
    window.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onDown);
      window.removeEventListener('keydown', onKey);
    };
  }, [menuOpen]);

  return (
    <div
      className={`rounded-2xl border transition-colors ${
        isActive
          ? 'bg-[var(--mornica-slate-card-elevated)] border-[var(--mornica-emerald)]'
          : 'bg-[var(--mornica-slate-card)] border-[var(--mornica-slate-border)] hover:border-[var(--mornica-cyan)]/50'
      }`}
    >
      <div className="px-3.5 py-3 flex items-center justify-between gap-2">
        <button
          type="button"
          onClick={() => musicoletStore.playTrackInActiveQueue(track.id, 0)}
          className="flex-1 min-w-0 flex items-center gap-3 text-left focus-visible:outline-2 focus-visible:outline-[var(--mornica-emerald)] rounded-xl cursor-pointer"
        >
          <div
            className={`w-11 h-11 rounded-[10px] border flex items-center justify-center shrink-0 ${
              isActive
                ? 'bg-[var(--mornica-emerald-dark)] border-[var(--mornica-emerald)]'
                : 'bg-[var(--mornica-slate-header)] border-[var(--mornica-slate-border)]'
            }`}
          >
            {isActive && uiState.isPlaying ? (
              <AudioLines className="w-5 h-5 text-[var(--mornica-emerald)]" aria-hidden="true" />
            ) : (
              <Music className="w-5 h-5 text-[var(--mornica-emerald)]" aria-hidden="true" />
            )}
          </div>

          <div className="min-w-0 flex-1">
            <div
              className={`text-sm sm:text-base font-bold truncate ${
                isActive ? 'text-[var(--mornica-emerald)]' : 'text-[#F4FAFE]'
              }`}
            >
              {track.title}
            </div>
            <div className="text-xs text-[#A5B6CA] truncate mt-0.5">
              {track.artist} · {track.album}
            </div>
            <div className="text-[11px] font-semibold text-[var(--mornica-cyan)] font-tabular mt-0.5 truncate">
              {track.folderName} · {formatDurationMs(track.durationMs)} ·{' '}
              {track.releaseYear}
              {track.sourceType === 'DEVICE' ? ' · On Device' : ''}
            </div>
          </div>
        </button>

        <div className="flex items-center shrink-0">
          <button
            type="button"
            onClick={() => musicoletStore.setSharingTrackId(track.id)}
            aria-label={`Share ${track.title} status card`}
            title="Share WhatsApp Status Card"
            className="min-w-[40px] min-h-[44px] flex items-center justify-center rounded-xl text-[#A5B6CA] hover:text-[var(--mornica-emerald)] hover:bg-[var(--mornica-slate-header)]/60 transition-colors"
          >
            <Share2 className="w-4 h-4" aria-hidden="true" />
          </button>

          <button
            type="button"
            onClick={() => musicoletStore.toggleFavorite(track.id)}
            aria-label={
              isSaved
                ? `Remove '${track.title}' from favorites`
                : `Favorite '${track.title}'`
            }
            aria-pressed={isSaved}
            className="min-w-[40px] min-h-[44px] flex items-center justify-center rounded-xl hover:bg-[var(--mornica-slate-header)]/60 transition-colors"
          >
            <Heart
              className={`w-5 h-5 ${
                isSaved ? 'fill-[#FF6479] text-[#FF6479]' : 'text-[#A5B6CA]'
              }`}
              aria-hidden="true"
            />
          </button>

          <div className="relative" ref={menuRef}>
            <button
              type="button"
              onClick={() => setMenuOpen((prev) => !prev)}
              aria-label={`Track options for ${track.title}`}
              aria-expanded={menuOpen}
              className="min-w-[44px] min-h-[44px] flex items-center justify-center rounded-xl text-[#A5B6CA] hover:text-[#F4FAFE] hover:bg-[#0B1727]/60 transition-colors"
            >
              <MoreVertical className="w-5 h-5" aria-hidden="true" />
            </button>

            {menuOpen && (
              <div
                role="menu"
                className="absolute right-0 mt-1 w-64 max-h-80 overflow-y-auto rounded-xl bg-[#0B1727] border border-[#24344D] shadow-xl py-1.5 z-50"
              >
                <button
                  type="button"
                  role="menuitem"
                  onClick={() => {
                    setMenuOpen(false);
                    musicoletStore.playTrackInActiveQueue(track.id, 0);
                  }}
                  className="w-full px-3.5 py-2 text-left text-xs font-medium text-[#F4FAFE] hover:bg-[#1B2F4D]"
                >
                  Play Now
                </button>
                <button
                  type="button"
                  role="menuitem"
                  onClick={() => {
                    setMenuOpen(false);
                    musicoletStore.addTracksToSpecificQueue(
                      uiState.activeQueueId,
                      [track.id],
                      true,
                    );
                  }}
                  className="w-full px-3.5 py-2 text-left text-xs font-medium text-[#F4FAFE] hover:bg-[#1B2F4D]"
                >
                  Play Next in Active Queue
                </button>
                <button
                  type="button"
                  role="menuitem"
                  onClick={() => {
                    setMenuOpen(false);
                    musicoletStore.setEditingTrackId(track.id);
                  }}
                  className="w-full px-3.5 py-2 text-left text-xs font-medium text-[var(--mornica-emerald)] hover:bg-[var(--mornica-slate-card-elevated)]"
                >
                  Edit Tags &amp; Lyrics
                </button>
                <button
                  type="button"
                  role="menuitem"
                  onClick={() => {
                    setMenuOpen(false);
                    musicoletStore.setSharingTrackId(track.id);
                  }}
                  className="w-full px-3.5 py-2 text-left text-xs font-medium text-[var(--mornica-cyan)] hover:bg-[var(--mornica-slate-card-elevated)]"
                >
                  Share WhatsApp Status Card
                </button>

                <div className="my-1 border-t border-[#24344D]" />
                <div className="px-3.5 py-1 text-[10px] font-bold text-[#A5B6CA]">
                  Add to Queue
                </div>
                {uiState.queues.map((q) => (
                  <button
                    key={q.id}
                    type="button"
                    role="menuitem"
                    onClick={() => {
                      setMenuOpen(false);
                      musicoletStore.addTracksToSpecificQueue(q.id, [track.id]);
                    }}
                    className="w-full px-3.5 py-1.5 text-left text-xs text-[#F4FAFE] hover:bg-[#1B2F4D] truncate"
                  >
                    + {q.name}
                  </button>
                ))}

                {uiState.playlists.length > 0 && (
                  <>
                    <div className="my-1 border-t border-[#24344D]" />
                    <div className="px-3.5 py-1 text-[10px] font-bold text-[#A5B6CA]">
                      Add to Playlist
                    </div>
                    {uiState.playlists.map((pl) => (
                      <button
                        key={pl.id}
                        type="button"
                        role="menuitem"
                        onClick={() => {
                          setMenuOpen(false);
                          musicoletStore.addToPlaylist(pl.id, track.id);
                        }}
                        className="w-full px-3.5 py-1.5 text-left text-xs text-[#F4FAFE] hover:bg-[#1B2F4D] truncate"
                      >
                        + {pl.name}
                      </button>
                    ))}
                  </>
                )}

                {track.sourceType === 'DEVICE' && (
                  <>
                    <div className="my-1 border-t border-[#24344D]" />
                    <button
                      type="button"
                      role="menuitem"
                      onClick={() => {
                        setMenuOpen(false);
                        void musicoletStore.deleteImportedTrack(track.id);
                      }}
                      className="w-full px-3.5 py-2 text-left text-xs font-medium text-[#FF6479] hover:bg-[#1B2F4D]"
                    >
                      Remove Imported File
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
