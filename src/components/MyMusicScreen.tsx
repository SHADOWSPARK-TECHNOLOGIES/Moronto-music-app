import { useMemo, useRef } from 'react';
import {
  Disc3,
  Folder,
  ListMusic,
  Music,
  RefreshCw,
  Search,
  User,
} from 'lucide-react';
import type { LocalMediaRepository } from '../domain/localMediaRepository';
import type { MornicaUiState, PlaybackRouter } from '../domain/playbackRouter';
import {
  LIBRARY_TAB_LABELS,
  LibraryTab,
  TRACK_SORT_LABELS,
  TrackSortOption,
} from '../model/mornicaModels';
import { LicensedCatalogSection } from './LicensedCatalogSection';
import { LocalTrackItemCard } from './LocalTrackItemCard';

interface MyMusicScreenProps {
  uiState: MornicaUiState;
  router: PlaybackRouter;
  localRepo: LocalMediaRepository;
}

function LibraryTabIcon({ tab }: { tab: LibraryTab }) {
  switch (tab) {
    case LibraryTab.SONGS:
      return <Music className="w-4 h-4" aria-hidden="true" />;
    case LibraryTab.PLAYLISTS:
      return <ListMusic className="w-4 h-4" aria-hidden="true" />;
    case LibraryTab.ALBUMS:
      return <Disc3 className="w-4 h-4" aria-hidden="true" />;
    case LibraryTab.ARTISTS:
      return <User className="w-4 h-4" aria-hidden="true" />;
    case LibraryTab.FOLDERS:
      return <Folder className="w-4 h-4" aria-hidden="true" />;
  }
}

function LibraryGroupHeader({
  title,
  count,
}: {
  title: string;
  count: number;
}) {
  return (
    <div className="flex items-center justify-between pt-3 pb-1 border-b border-[#24344D]/60">
      <h3 className="text-sm font-bold text-[#20B6EB]">{title}</h3>
      <span className="text-xs font-medium text-[#A5B6CA] font-tabular">
        {count} {count === 1 ? 'track' : 'tracks'}
      </span>
    </div>
  );
}

export function MyMusicScreen({
  uiState,
  router,
  localRepo,
}: MyMusicScreenProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const filteredSortedTracks = useMemo(
    () => router.filteredAndSortedLocalTracks(),
    [
      router,
      uiState.localTracks,
      uiState.localSearchQuery,
      uiState.trackSortOption,
    ],
  );

  const handleScanTrigger = () => {
    fileInputRef.current?.click();
  };

  const handleFilesSelected = async (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const files = event.target.files;
    if (!files || files.length === 0) return;
    const scanned = await localRepo.scanSelectedAudioFiles(files);
    router.updateDeviceAudioTracks(true, scanned);
    event.target.value = '';
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-5">
      {/* Hidden local audio file picker — strictly on-device, never uploaded */}
      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept="audio/*,.mp3,.wav,.flac,.m4a,.ogg,.aac"
        onChange={handleFilesSelected}
        className="sr-only"
        aria-label="Select local audio files on your device"
      />

      {/* Licensed Streaming + Offline Catalogue Section */}
      <LicensedCatalogSection uiState={uiState} router={router} />

      {/* 5-Tab Musicolet-Style Library Bar */}
      <div
        role="tablist"
        aria-label="Music library views"
        className="bg-[#0B1727] border border-[#24344D] rounded-2xl grid grid-cols-5 overflow-hidden mb-5"
      >
        {(Object.values(LibraryTab) as LibraryTab[]).map((tab) => {
          const isSelected = uiState.selectedLibraryTab === tab;
          return (
            <button
              key={tab}
              type="button"
              role="tab"
              aria-selected={isSelected}
              onClick={() => router.selectLibraryTab(tab)}
              className={`relative py-3 px-2 flex flex-col sm:flex-row items-center justify-center gap-1.5 text-xs font-semibold transition-colors focus-visible:outline-2 focus-visible:outline-[#12D9A1] ${
                isSelected
                  ? 'text-[#12D9A1] bg-[#14243A]/70'
                  : 'text-[#A5B6CA] hover:text-[#F4FAFE] hover:bg-[#14243A]/40'
              }`}
            >
              <LibraryTabIcon tab={tab} />
              <span className="whitespace-nowrap truncate">
                {LIBRARY_TAB_LABELS[tab]}
              </span>
              {isSelected && (
                <span
                  aria-hidden="true"
                  className="absolute bottom-0 left-0 right-0 h-[3px] bg-[#12D9A1]"
                />
              )}
            </button>
          );
        })}
      </div>

      {/* Search + Square Scan Button Row */}
      <div className="flex items-center gap-2.5 mb-4">
        <div className="relative flex-1">
          <Search
            className="w-4 h-4 text-[#A5B6CA] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none"
            aria-hidden="true"
          />
          <input
            type="search"
            value={uiState.localSearchQuery}
            onChange={(e) => router.updateLocalSearchQuery(e.target.value)}
            placeholder="Find your next jam"
            aria-label="Search tracks"
            className="w-full h-[50px] pl-10 pr-4 rounded-[14px] bg-[#14243A] border border-[#24344D] focus:border-[#12D9A1] focus:outline-none text-sm text-[#F4FAFE] placeholder:text-[#A5B6CA]"
          />
        </div>

        <button
          type="button"
          onClick={handleScanTrigger}
          aria-label="Scan Device Audio Files"
          title="Scan Device Audio Files"
          className="w-[50px] h-[50px] rounded-[14px] bg-[#14243A] border border-[#24344D] hover:border-[#20B6EB] flex items-center justify-center text-[#20B6EB] shrink-0 transition-colors focus-visible:outline-2 focus-visible:outline-[#12D9A1]"
        >
          <RefreshCw className="w-5 h-5" aria-hidden="true" />
        </button>
      </div>

      {/* Track Count + Sort Segmented Controls */}
      <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
        <span className="text-xs font-semibold text-[#A5B6CA] font-tabular">
          {filteredSortedTracks.length}{' '}
          {filteredSortedTracks.length === 1 ? 'track' : 'tracks'}
        </span>

        <div className="flex items-center gap-1.5 overflow-x-auto">
          <span className="text-xs text-[#A5B6CA] mr-1">Sort:</span>
          {(Object.values(TrackSortOption) as TrackSortOption[]).map(
            (option) => {
              const selected = uiState.trackSortOption === option;
              return (
                <button
                  key={option}
                  type="button"
                  onClick={() => router.selectTrackSortOption(option)}
                  className={`px-3 py-1.5 rounded-[10px] text-xs font-bold whitespace-nowrap shrink-0 transition-colors focus-visible:outline-2 focus-visible:outline-[#12D9A1] ${
                    selected
                      ? 'bg-[#12D9A1] text-[#04130E]'
                      : 'bg-[#14243A] border border-[#24344D] text-[#A5B6CA] hover:text-[#F4FAFE]'
                  }`}
                >
                  {TRACK_SORT_LABELS[option]}
                </button>
              );
            },
          )}
        </div>
      </div>

      {/* Local Audio Scan Banner */}
      {!uiState.hasAudioPermission && (
        <div className="rounded-2xl bg-[#14243A] border border-[#24344D] p-4 mb-5 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <p className="text-sm font-bold text-[#12D9A1]">
              Your music, your way · No data? No wahala.
            </p>
            <p className="text-xs text-[#A5B6CA] mt-0.5">
              Play audio files already on your device, even offline. Tap Scan
              Phone to add local MP3, WAV, FLAC, or M4A tracks.
            </p>
          </div>
          <button
            type="button"
            onClick={handleScanTrigger}
            className="min-h-[42px] px-4 py-2 rounded-[10px] bg-[#12D9A1] hover:bg-[#10c390] text-[#04130E] text-sm font-bold whitespace-nowrap shrink-0 transition-colors"
          >
            Scan Phone
          </button>
        </div>
      )}

      {/* Library Content */}
      {filteredSortedTracks.length === 0 ? (
        <div className="rounded-2xl bg-[#14243A] border border-[#24344D] p-6 text-center">
          <p className="text-sm font-semibold text-[#F4FAFE]">
            No matching tracks found
          </p>
          <p className="text-xs text-[#A5B6CA] mt-1">
            Try clearing your search filter or scan local audio files from your
            device.
          </p>
        </div>
      ) : uiState.selectedLibraryTab === LibraryTab.SONGS ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {filteredSortedTracks.map((track) => (
            <LocalTrackItemCard
              key={track.id}
              track={track}
              uiState={uiState}
              router={router}
            />
          ))}
        </div>
      ) : uiState.selectedLibraryTab === LibraryTab.FOLDERS ? (
        <div className="space-y-4">
          {localRepo.groupByFolder(filteredSortedTracks).map(([folder, tracks]) => (
            <div key={folder} className="space-y-2.5">
              <LibraryGroupHeader
                title={`Folder · ${folder}`}
                count={tracks.length}
              />
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {tracks.map((track) => (
                  <LocalTrackItemCard
                    key={`folder-${track.id}`}
                    track={track}
                    uiState={uiState}
                    router={router}
                  />
                ))}
              </div>
            </div>
          ))}
        </div>
      ) : uiState.selectedLibraryTab === LibraryTab.ARTISTS ? (
        <div className="space-y-4">
          {localRepo.groupByArtist(filteredSortedTracks).map(([artist, tracks]) => (
            <div key={artist} className="space-y-2.5">
              <LibraryGroupHeader
                title={`Artist · ${artist}`}
                count={tracks.length}
              />
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {tracks.map((track) => (
                  <LocalTrackItemCard
                    key={`artist-${track.id}`}
                    track={track}
                    uiState={uiState}
                    router={router}
                  />
                ))}
              </div>
            </div>
          ))}
        </div>
      ) : uiState.selectedLibraryTab === LibraryTab.ALBUMS ? (
        <div className="space-y-4">
          {localRepo.groupByAlbum(filteredSortedTracks).map(([album, tracks]) => (
            <div key={album} className="space-y-2.5">
              <LibraryGroupHeader
                title={`Album · ${album}`}
                count={tracks.length}
              />
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {tracks.map((track) => (
                  <LocalTrackItemCard
                    key={`album-${track.id}`}
                    track={track}
                    uiState={uiState}
                    router={router}
                  />
                ))}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="space-y-4">
          {Object.entries(uiState.playlists).map(([playlistName, trackIds]) => {
            const tracks = filteredSortedTracks.filter((t) =>
              trackIds.includes(t.id),
            );
            return (
              <div key={playlistName} className="space-y-2.5">
                <LibraryGroupHeader
                  title={`Queue · ${playlistName}`}
                  count={tracks.length}
                />
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  {tracks.map((track) => (
                    <LocalTrackItemCard
                      key={`pl-${playlistName}-${track.id}`}
                      track={track}
                      uiState={uiState}
                      router={router}
                    />
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
