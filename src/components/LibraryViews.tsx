import { useState } from 'react';
import { Disc3, Play, Plus, Trash2 } from 'lucide-react';
import { buildGenreSmartPlaylists } from '../domain/musicoletEngine';
import {
  type MusicoletUiState,
  musicoletStore,
} from '../domain/musicoletStore';
import { t } from '../i18n/translations';
import { type OfflineTrack } from '../model/musicoletModels';
import { TrackRowCard } from './TrackRowCard';

interface GroupedLibraryViewProps {
  groups: Array<[string, OfflineTrack[]]>;
  groupLabel: string;
  uiState: MusicoletUiState;
  activeTrackId: string | null;
}

export function GroupedLibraryView({
  groups,
  groupLabel,
  uiState,
  activeTrackId,
}: GroupedLibraryViewProps) {
  if (groups.length === 0) {
    return (
      <div className="rounded-2xl bg-[#14243A] border border-[#24344D] p-6 text-center text-sm text-[#A5B6CA]">
        No matching {groupLabel.toLowerCase()}s found.
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {groups.map(([name, tracks]) => {
        const trackIds = tracks.map((t) => t.id);
        return (
          <section key={name} className="space-y-2.5">
            <div className="flex flex-wrap items-center justify-between gap-2 pb-2 border-b border-[#24344D]">
              <div>
                <h2 className="text-sm sm:text-base font-bold text-[#20B6EB]">
                  {groupLabel} · {name}
                </h2>
                <p className="text-xs text-[#A5B6CA] font-tabular">
                  {tracks.length} {tracks.length === 1 ? 'track' : 'tracks'}
                </p>
              </div>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() =>
                    musicoletStore.addTracksToSpecificQueue(
                      uiState.activeQueueId,
                      trackIds,
                    )
                  }
                  className="min-h-[36px] px-3 py-1.5 rounded-xl bg-[#14243A] border border-[#24344D] hover:border-[#20B6EB] text-xs font-semibold text-[#F4FAFE]"
                >
                  + Queue
                </button>
                <button
                  type="button"
                  onClick={() =>
                    musicoletStore.playTrackListAsActiveQueue(trackIds)
                  }
                  className="min-h-[36px] px-3 py-1.5 rounded-xl bg-[#12D9A1] hover:bg-[#10c390] text-[#04130E] text-xs font-bold inline-flex items-center gap-1"
                >
                  <Play className="w-3.5 h-3.5 fill-current" aria-hidden="true" />
                  <span>Play All</span>
                </button>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {tracks.map((track) => (
                <TrackRowCard
                  key={`${groupLabel}-${name}-${track.id}`}
                  track={track}
                  uiState={uiState}
                  activeTrackId={activeTrackId}
                />
              ))}
            </div>
          </section>
        );
      })}
    </div>
  );
}

interface PlaylistsViewProps {
  uiState: MusicoletUiState;
  activeTrackId: string | null;
}

export function PlaylistsView({ uiState, activeTrackId }: PlaylistsViewProps) {
  const [playlistName, setPlaylistName] = useState('');

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (musicoletStore.createPlaylist(playlistName)) {
      setPlaylistName('');
    }
  };

  const favoriteTracks = uiState.tracks.filter((t) =>
    uiState.favoriteTrackIds.includes(t.id),
  );
  const smartGenres = buildGenreSmartPlaylists(uiState.tracks);
  const tr = t(uiState.language);

  return (
    <div className="space-y-6">
      {/* Genre Smart Playlists */}
      <section aria-label={tr.smartGenresHeading} className="space-y-3">
        <div className="flex items-center justify-between gap-2">
          <div>
            <h2 className="text-base font-bold text-[var(--mornica-emerald)]">
              {tr.smartGenresHeading}
            </h2>
            <p className="text-xs text-[#A5B6CA]">
              Auto-grouped offline mixes by folder, album, and track tag.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {smartGenres.map((group) => (
            <div
              key={group.genre}
              className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-4 flex flex-col justify-between gap-3"
            >
              <div className="flex items-start justify-between gap-2">
                <div>
                  <h3 className="text-sm font-extrabold text-[#F4FAFE]">
                    {group.genre}
                  </h3>
                  <p className="text-xs text-[#A5B6CA] font-tabular mt-0.5">
                    {group.tracks.length}{' '}
                    {group.tracks.length === 1 ? 'track' : 'tracks'}
                  </p>
                </div>
                <Disc3 className="w-4 h-4 text-[var(--mornica-cyan)] shrink-0" aria-hidden="true" />
              </div>

              <button
                type="button"
                onClick={() => musicoletStore.playSmartGenrePlaylist(group.genre)}
                className="min-h-[38px] px-3 py-1.5 rounded-xl bg-[var(--mornica-emerald)] hover:brightness-105 text-[#04130E] text-xs font-bold inline-flex items-center justify-center gap-1.5 whitespace-nowrap"
              >
                <Play className="w-3.5 h-3.5 fill-current" aria-hidden="true" />
                <span>Play {group.genre}</span>
              </button>
            </div>
          ))}
        </div>
      </section>

      {/* Create Playlist Bar */}
      <form
        onSubmit={handleCreate}
        className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3"
      >
        <div>
          <h2 className="text-base font-bold text-[var(--mornica-emerald)]">
            Custom Playlists &amp; Favorites
          </h2>
          <p className="text-xs text-[#A5B6CA] mt-0.5">
            Build persistent offline playlists stored locally in your browser.
          </p>
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <input
            type="text"
            value={playlistName}
            onChange={(e) => setPlaylistName(e.target.value)}
            placeholder="New playlist name..."
            aria-label="New playlist name"
            className="h-10 px-3.5 rounded-xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE] placeholder:text-[#A5B6CA] flex-1 min-w-0 sm:w-56"
          />
          <button
            type="submit"
            className="min-h-[40px] px-4 py-2 rounded-xl bg-[var(--mornica-emerald)] hover:brightness-105 text-[#04130E] text-xs font-bold inline-flex items-center gap-1.5 whitespace-nowrap shrink-0"
          >
            <Plus className="w-4 h-4" aria-hidden="true" />
            <span>Create</span>
          </button>
        </div>
      </form>

      {/* Favorites Smart Playlist */}
      <section className="space-y-2.5">
        <div className="flex items-center justify-between pb-2 border-b border-[#24344D]">
          <div>
            <h3 className="text-sm sm:text-base font-bold text-[#FF6479]">
              Favorites
            </h3>
            <p className="text-xs text-[#A5B6CA] font-tabular">
              {favoriteTracks.length}{' '}
              {favoriteTracks.length === 1 ? 'track' : 'tracks'}
            </p>
          </div>
          {favoriteTracks.length > 0 && (
            <button
              type="button"
              onClick={() =>
                musicoletStore.playTrackListAsActiveQueue(
                  favoriteTracks.map((t) => t.id),
                )
              }
              className="min-h-[36px] px-3 py-1.5 rounded-xl bg-[#12D9A1] hover:bg-[#10c390] text-[#04130E] text-xs font-bold inline-flex items-center gap-1"
            >
              <Play className="w-3.5 h-3.5 fill-current" aria-hidden="true" />
              <span>Play Favorites</span>
            </button>
          )}
        </div>

        {favoriteTracks.length === 0 ? (
          <p className="text-xs text-[#A5B6CA]">
            Tap the heart icon on any track to save it to Favorites.
          </p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {favoriteTracks.map((track) => (
              <TrackRowCard
                key={`fav-${track.id}`}
                track={track}
                uiState={uiState}
                activeTrackId={activeTrackId}
              />
            ))}
          </div>
        )}
      </section>

      {/* Custom Playlists */}
      {uiState.playlists.map((pl) => {
        const tracks = pl.trackIds
          .map((id) => uiState.tracks.find((t) => t.id === id))
          .filter((t): t is OfflineTrack => t !== undefined);

        return (
          <section key={pl.id} className="space-y-2.5">
            <div className="flex items-center justify-between pb-2 border-b border-[#24344D]">
              <div>
                <h3 className="text-sm sm:text-base font-bold text-[#20B6EB]">
                  {pl.name}
                </h3>
                <p className="text-xs text-[#A5B6CA] font-tabular">
                  {tracks.length} {tracks.length === 1 ? 'track' : 'tracks'}
                </p>
              </div>
              <div className="flex items-center gap-2">
                {tracks.length > 0 && (
                  <button
                    type="button"
                    onClick={() =>
                      musicoletStore.playTrackListAsActiveQueue(
                        tracks.map((t) => t.id),
                      )
                    }
                    className="min-h-[36px] px-3 py-1.5 rounded-xl bg-[#12D9A1] hover:bg-[#10c390] text-[#04130E] text-xs font-bold inline-flex items-center gap-1"
                  >
                    <Play className="w-3.5 h-3.5 fill-current" aria-hidden="true" />
                    <span>Play All</span>
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => musicoletStore.deletePlaylist(pl.id)}
                  aria-label={`Delete playlist ${pl.name}`}
                  className="min-w-[36px] min-h-[36px] flex items-center justify-center rounded-xl text-[#A5B6CA] hover:text-[#FF6479] hover:bg-[#14243A]"
                >
                  <Trash2 className="w-4 h-4" aria-hidden="true" />
                </button>
              </div>
            </div>

            {tracks.length === 0 ? (
              <p className="text-xs text-[#A5B6CA]">
                Empty playlist. Use any track&rsquo;s options menu to add songs here.
              </p>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {tracks.map((track) => (
                  <div key={`${pl.id}-${track.id}`} className="relative">
                    <TrackRowCard
                      track={track}
                      uiState={uiState}
                      activeTrackId={activeTrackId}
                    />
                    <button
                      type="button"
                      onClick={() =>
                        musicoletStore.removeFromPlaylist(pl.id, track.id)
                      }
                      aria-label={`Remove ${track.title} from ${pl.name}`}
                      className="mt-1 ml-1 inline-flex items-center gap-1 text-[11px] text-[#A5B6CA] hover:text-[#FF6479]"
                    >
                      <Trash2 className="w-3 h-3" aria-hidden="true" />
                      <span>Remove from {pl.name}</span>
                    </button>
                  </div>
                ))}
              </div>
            )}
          </section>
        );
      })}
    </div>
  );
}
