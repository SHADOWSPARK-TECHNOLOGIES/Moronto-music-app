import { useEffect, useMemo, useRef, useState } from 'react';
import {
  BookOpen,
  Compass,
  Crown,
  Disc3,
  Folder,
  FolderPlus,
  HardDrive,
  HeartHandshake,
  Info,
  Layers,
  ListMusic,
  Music,
  Pause,
  Play,
  Radio,
  Search,
  ShieldCheck,
  SlidersHorizontal,
  Sparkles,
  User,
} from 'lucide-react';
import { AboutView } from './components/AboutView';
import { AuthGateScreen } from './components/AuthGateScreen';
import { BlogView } from './components/BlogView';
import { GroupedLibraryView, PlaylistsView } from './components/LibraryViews';
import { LiveComingSoonView } from './components/LiveComingSoonView';
import { LoadingSplash } from './components/LoadingSplash';
import { MusicoletHeader } from './components/MusicoletHeader';
import {
  EqualizerModal,
  OnboardingModal,
  RatingPromptModal,
  SleepTimerModal,
  TagEditorModal,
  WhatsAppShareModal,
} from './components/MusicoletModals';
import {
  MiniPlayerBar,
  NowPlayingFullModal,
} from './components/MusicoletPlayer';
import { PrivacyPolicyView } from './components/PrivacyPolicyView';
import { QueuesView } from './components/QueuesView';
import { SponsorView } from './components/SponsorView';
import { StorageManagerView } from './components/StorageManagerView';
import { TrackRowCard } from './components/TrackRowCard';
import { UpgradeView } from './components/UpgradeView';
import { UserGuideView } from './components/UserGuideView';
import {
  musicoletStore,
  useMusicoletState,
} from './domain/musicoletStore';
import { t } from './i18n/translations';
import {
  LIBRARY_VIEW_LABELS,
  LibraryViewTab,
  SORT_OPTION_LABELS,
  STUDIO_TEMPLATES,
  SortOption,
} from './model/musicoletModels';
import { formatDurationMs } from './utils/format';

function TabIcon({ tab }: { tab: LibraryViewTab }) {
  switch (tab) {
    case LibraryViewTab.QUEUES:
      return <Layers className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.TRACKS:
      return <Music className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.FOLDERS:
      return <Folder className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.ALBUMS:
      return <Disc3 className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.ARTISTS:
      return <User className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.PLAYLISTS:
      return <ListMusic className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.LIVE:
      return <Radio className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.UPGRADE:
      return <Crown className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.STORAGE:
      return <HardDrive className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.GUIDE:
      return <Compass className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.SPONSOR:
      return <HeartHandshake className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.BLOG:
      return <BookOpen className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.ABOUT:
      return <Info className="w-4 h-4 shrink-0" aria-hidden="true" />;
    case LibraryViewTab.PRIVACY:
      return <ShieldCheck className="w-4 h-4 shrink-0" aria-hidden="true" />;
  }
}

export function App() {
  const uiState = useMusicoletState();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [showSplash, setShowSplash] = useState<boolean>(() => {
    if (typeof window === 'undefined') return false;
    const params = new URLSearchParams(window.location.search);
    return params.get('autostart') !== '1';
  });

  const tr = t(uiState.language);
  const activeTrack = musicoletStore.getActiveTrack();
  const activeQueue =
    uiState.queues.find((q) => q.id === uiState.activeQueueId) ??
    uiState.queues[0];
  const filteredTracks = useMemo(
    () => musicoletStore.getFilteredSortedTracks(),
    [uiState.tracks, uiState.searchQuery, uiState.sortOption],
  );

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        if (uiState.sharingTrackId) {
          musicoletStore.setSharingTrackId(null);
        } else if (uiState.editingTrackId) {
          musicoletStore.setEditingTrackId(null);
        } else if (uiState.isEqModalOpen) {
          musicoletStore.setEqModalOpen(false);
        } else if (uiState.isSleepTimerModalOpen) {
          musicoletStore.setSleepTimerModalOpen(false);
        } else if (uiState.isNowPlayingOpen) {
          musicoletStore.setNowPlayingOpen(false);
        }
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [
    uiState.sharingTrackId,
    uiState.editingTrackId,
    uiState.isEqModalOpen,
    uiState.isSleepTimerModalOpen,
    uiState.isNowPlayingOpen,
  ]);

  const handleFilesSelected = async (
    e: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      await musicoletStore.importLocalAudioFiles(files);
    }
    e.target.value = '';
  };

  const deviceTrackCount = uiState.tracks.filter(
    (t) => t.sourceType === 'DEVICE',
  ).length;

  const isLibrarySearchTab =
    uiState.selectedTab === LibraryViewTab.TRACKS ||
    uiState.selectedTab === LibraryViewTab.FOLDERS ||
    uiState.selectedTab === LibraryViewTab.ALBUMS ||
    uiState.selectedTab === LibraryViewTab.ARTISTS;

  const getTabLabel = (tab: LibraryViewTab): string => {
    switch (tab) {
      case LibraryViewTab.TRACKS:
        return tr.tracksTab;
      case LibraryViewTab.QUEUES:
        return tr.queuesTab;
      case LibraryViewTab.PLAYLISTS:
        return tr.playlistsTab;
      case LibraryViewTab.LIVE:
        return tr.liveTab;
      case LibraryViewTab.UPGRADE:
        return tr.upgradeTab;
      case LibraryViewTab.STORAGE:
        return tr.storageTab;
      case LibraryViewTab.GUIDE:
        return tr.guideTab;
      default:
        return LIBRARY_VIEW_LABELS[tab];
    }
  };

  if (showSplash) {
    return <LoadingSplash onComplete={() => setShowSplash(false)} />;
  }

  if (!uiState.authReady) {
    return (
      <div className="min-h-screen bg-[var(--mornica-midnight-bg)] text-[#F4FAFE] flex items-center justify-center p-6">
        <div className="text-sm font-semibold text-[#A5B6CA]">
          Loading Moronto Music Studio...
        </div>
      </div>
    );
  }

  if (!uiState.authUser) {
    return <AuthGateScreen />;
  }

  return (
    <div
      className="min-h-screen flex flex-col bg-[var(--mornica-midnight-bg)] text-[#F4FAFE]"
      style={{
        backgroundImage:
          'radial-gradient(circle at 15% 0%, rgba(18, 217, 161, 0.06), transparent 40%), radial-gradient(circle at 85% 15%, rgba(32, 182, 235, 0.05), transparent 35%)',
      }}
    >
      <MusicoletHeader uiState={uiState} />

      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept="audio/*,.mp3,.wav,.flac,.m4a,.ogg,.aac"
        onChange={handleFilesSelected}
        className="sr-only"
        aria-label="Add audio files from device"
      />

      <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 py-5 pb-28 space-y-6">
        {/* Scrollable Single-Line Navigation Tab Strip */}
        <div
          role="tablist"
          aria-label="Moronto Music sections"
          className="bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] rounded-2xl flex items-stretch overflow-x-auto"
        >
          {(Object.values(LibraryViewTab) as LibraryViewTab[]).map((tab) => {
            const active = uiState.selectedTab === tab;
            return (
              <button
                key={tab}
                type="button"
                role="tab"
                aria-selected={active}
                onClick={() => musicoletStore.selectTab(tab)}
                className={`relative py-3 px-4 flex items-center justify-center gap-2 text-xs font-semibold whitespace-nowrap shrink-0 transition-colors ${
                  active
                    ? 'text-[var(--mornica-emerald)] bg-[var(--mornica-slate-card)]/80'
                    : 'text-[#A5B6CA] hover:text-[#F4FAFE] hover:bg-[var(--mornica-slate-card)]/40'
                }`}
              >
                <TabIcon tab={tab} />
                <span>{getTabLabel(tab)}</span>
                {active && (
                  <span
                    aria-hidden="true"
                    className="absolute bottom-0 left-0 right-0 h-[3px] bg-[var(--mornica-emerald)]"
                  />
                )}
              </button>
            );
          })}
        </div>

        {/* Continue Listening Card + One-Click Studio Templates on Home (Tracks Tab) */}
        {uiState.selectedTab === LibraryViewTab.TRACKS && activeTrack && (
          <div className="space-y-4">
            <section
              aria-label={tr.continueListening}
              data-testid="continue-listening-card"
              className="rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-5 sm:p-6 flex flex-col lg:flex-row lg:items-center justify-between gap-5"
            >
              <div className="flex items-start sm:items-center gap-4 min-w-0">
                <div className="w-14 h-14 sm:w-16 sm:h-16 rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] flex items-center justify-center shrink-0">
                  <Disc3
                    className={`w-8 h-8 text-[var(--mornica-emerald)] ${
                      uiState.isPlaying ? 'animate-spin' : ''
                    }`}
                    style={{ animationDuration: '7s' }}
                    aria-hidden="true"
                  />
                </div>

                <div className="min-w-0 space-y-1">
                  <p className="text-xs font-bold text-[var(--mornica-emerald)] truncate">
                    {tr.continueListening} · {activeQueue.name}
                  </p>
                  <h1 className="text-xl sm:text-2xl font-extrabold text-[#F4FAFE] truncate text-balance">
                    {activeTrack.title}
                  </h1>
                  <p className="text-xs sm:text-sm text-[#A5B6CA] truncate font-tabular">
                    {activeTrack.artist} · {activeTrack.album} ·{' '}
                    {formatDurationMs(uiState.positionMs)} /{' '}
                    {formatDurationMs(Math.max(uiState.durationMs, 1_000))}
                  </p>
                </div>
              </div>

              <div className="flex flex-wrap items-center gap-2.5 shrink-0">
                <button
                  type="button"
                  onClick={() => musicoletStore.togglePlayPause()}
                  className="min-h-[44px] px-5 py-2.5 rounded-xl mornica-amber-gradient text-[#0B101B] text-xs font-extrabold inline-flex items-center gap-2 whitespace-nowrap hover:brightness-105 active:scale-[0.98] transition-all"
                >
                  {uiState.isPlaying ? (
                    <>
                      <Pause className="w-4 h-4 fill-current" aria-hidden="true" />
                      <span>{tr.pauseTrack}</span>
                    </>
                  ) : (
                    <>
                      <Play className="w-4 h-4 fill-current" aria-hidden="true" />
                      <span>{tr.resumePlayback}</span>
                    </>
                  )}
                </button>

                <button
                  type="button"
                  onClick={() => musicoletStore.setNowPlayingOpen(true)}
                  className="min-h-[44px] px-4 py-2.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] hover:border-[var(--mornica-emerald)] text-xs font-bold text-[#F4FAFE] whitespace-nowrap transition-colors"
                >
                  {tr.openStudioPlayer}
                </button>

                <button
                  type="button"
                  onClick={() => musicoletStore.setEqModalOpen(true, 'EQUALIZER')}
                  className="min-h-[44px] px-4 py-2.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] hover:border-[var(--mornica-cyan)] text-xs font-bold text-[var(--mornica-cyan)] inline-flex items-center gap-1.5 whitespace-nowrap transition-colors"
                >
                  <SlidersHorizontal className="w-4 h-4" aria-hidden="true" />
                  <span>{tr.eqAndCrossfade}</span>
                </button>
              </div>
            </section>

            {/* Studio Templates Strip */}
            <section
              aria-label={tr.studioTemplatesHeading}
              className="rounded-2xl bg-[var(--mornica-slate-header)]/70 border border-[var(--mornica-slate-border)] p-4 space-y-3"
            >
              <div className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <Sparkles className="w-4 h-4 text-[#FFBE55]" aria-hidden="true" />
                  <h2 className="text-xs sm:text-sm font-bold text-[#F4FAFE]">
                    {tr.studioTemplatesHeading}
                  </h2>
                </div>
                <span className="text-[11px] text-[#A5B6CA]">
                  Queue + 10-Band EQ + Crossfade
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2.5">
                {STUDIO_TEMPLATES.map((tpl) => (
                  <button
                    key={tpl.id}
                    type="button"
                    onClick={() => musicoletStore.applyStudioTemplate(tpl.id)}
                    className="text-left rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] hover:border-[var(--mornica-emerald)] p-3 transition-colors"
                  >
                    <div className="text-xs font-extrabold text-[#F4FAFE]">
                      {tpl.name}
                    </div>
                    <div className="text-[11px] text-[#A5B6CA] mt-0.5 truncate">
                      {tpl.tagline}
                    </div>
                  </button>
                ))}
              </div>
            </section>
          </div>
        )}

        {/* Search & Sort Controls (for library search tabs) */}
        {isLibrarySearchTab && (
          <div className="space-y-3">
            <div className="flex items-center gap-2.5">
              <div className="relative flex-1">
                <Search
                  className="w-4 h-4 text-[#A5B6CA] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none"
                  aria-hidden="true"
                />
                <input
                  type="search"
                  value={uiState.searchQuery}
                  onChange={(e) =>
                    musicoletStore.setSearchQuery(e.target.value)
                  }
                  placeholder={tr.searchPlaceholder}
                  aria-label="Search offline library"
                  className="w-full h-[48px] pl-10 pr-4 rounded-[14px] bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE] placeholder:text-[#A5B6CA]"
                />
              </div>
            </div>

            <div className="flex flex-wrap items-center justify-between gap-2.5">
              <span className="text-xs font-semibold text-[#A5B6CA] font-tabular">
                {filteredTracks.length}{' '}
                {filteredTracks.length === 1 ? 'track' : 'tracks'}
              </span>

              <div className="flex flex-wrap items-center gap-1.5">
                <span className="text-xs text-[#A5B6CA] mr-1">Sort:</span>
                {(Object.values(SortOption) as SortOption[]).map((opt) => {
                  const selected = uiState.sortOption === opt;
                  return (
                    <button
                      key={opt}
                      type="button"
                      onClick={() => musicoletStore.setSortOption(opt)}
                      className={`px-3 py-1.5 rounded-[10px] text-xs font-bold whitespace-nowrap shrink-0 transition-colors ${
                        selected
                          ? 'bg-[var(--mornica-emerald)] text-[#04130E]'
                          : 'bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] text-[#A5B6CA] hover:text-[#F4FAFE]'
                      }`}
                    >
                      {SORT_OPTION_LABELS[opt]}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* Offline Local Storage Callout when no user files imported yet */}
        {uiState.selectedTab === LibraryViewTab.TRACKS &&
          deviceTrackCount === 0 && (
            <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-4 flex flex-col lg:flex-row lg:items-center justify-between gap-3">
              <div>
                <h2 className="text-sm font-bold text-[var(--mornica-emerald)]">
                  100% Offline Audio Studio · Full Access Unlocked
                </h2>
                <p className="text-xs text-[#A5B6CA] mt-0.5">
                  Import MP3, WAV, FLAC, M4A, or OGG files from your device.
                  Global Catalog Streaming (Pro) is coming soon — read our funny
                  Pidgin guide or message Support if you need help moving songs.
                </p>
              </div>
              <div className="flex flex-wrap items-center gap-2 shrink-0">
                <button
                  type="button"
                  onClick={() => musicoletStore.selectTab(LibraryViewTab.BLOG)}
                  className="min-h-[42px] px-3.5 py-2 rounded-xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] hover:border-[#FFBE55] text-[#FFBE55] text-xs font-bold whitespace-nowrap"
                >
                  Pidgin Guide &amp; Support
                </button>
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="min-h-[42px] px-4 py-2 rounded-xl bg-[var(--mornica-emerald)] hover:brightness-105 text-[#04130E] text-xs font-bold inline-flex items-center gap-1.5 whitespace-nowrap shrink-0"
                >
                  <FolderPlus className="w-4 h-4" aria-hidden="true" />
                  <span>Add Audio Files</span>
                </button>
              </div>
            </div>
          )}

        {/* Active View */}
        {uiState.selectedTab === LibraryViewTab.QUEUES && (
          <QueuesView uiState={uiState} />
        )}

        {uiState.selectedTab === LibraryViewTab.TRACKS &&
          (filteredTracks.length === 0 ? (
            <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-6 text-center text-sm text-[#A5B6CA]">
              No tracks match your search.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {filteredTracks.map((track) => (
                <TrackRowCard
                  key={track.id}
                  track={track}
                  uiState={uiState}
                  activeTrackId={activeTrack?.id ?? null}
                />
              ))}
            </div>
          ))}

        {uiState.selectedTab === LibraryViewTab.FOLDERS && (
          <GroupedLibraryView
            groups={musicoletStore.getGroupedTracks('folderName')}
            groupLabel="Folder"
            uiState={uiState}
            activeTrackId={activeTrack?.id ?? null}
          />
        )}

        {uiState.selectedTab === LibraryViewTab.ALBUMS && (
          <GroupedLibraryView
            groups={musicoletStore.getGroupedTracks('album')}
            groupLabel="Album"
            uiState={uiState}
            activeTrackId={activeTrack?.id ?? null}
          />
        )}

        {uiState.selectedTab === LibraryViewTab.ARTISTS && (
          <GroupedLibraryView
            groups={musicoletStore.getGroupedTracks('artist')}
            groupLabel="Artist"
            uiState={uiState}
            activeTrackId={activeTrack?.id ?? null}
          />
        )}

        {uiState.selectedTab === LibraryViewTab.PLAYLISTS && (
          <PlaylistsView
            uiState={uiState}
            activeTrackId={activeTrack?.id ?? null}
          />
        )}

        {uiState.selectedTab === LibraryViewTab.LIVE && (
          <LiveComingSoonView uiState={uiState} />
        )}

        {uiState.selectedTab === LibraryViewTab.UPGRADE && (
          <UpgradeView uiState={uiState} />
        )}

        {uiState.selectedTab === LibraryViewTab.STORAGE && (
          <StorageManagerView uiState={uiState} />
        )}

        {uiState.selectedTab === LibraryViewTab.GUIDE && (
          <UserGuideView
            uiState={uiState}
            onReplayIntro={() => setShowSplash(true)}
          />
        )}

        {uiState.selectedTab === LibraryViewTab.SPONSOR && (
          <SponsorView uiState={uiState} />
        )}

        {uiState.selectedTab === LibraryViewTab.BLOG && (
          <BlogView uiState={uiState} />
        )}

        {uiState.selectedTab === LibraryViewTab.ABOUT && <AboutView />}

        {uiState.selectedTab === LibraryViewTab.PRIVACY && (
          <PrivacyPolicyView uiState={uiState} />
        )}

        {/* Quiet Editorial Footer */}
        <footer className="pt-6 border-t border-[var(--mornica-slate-border)] flex flex-col sm:flex-row sm:items-center justify-between gap-4 text-xs text-[#A5B6CA]">
          <p>© 2026 Moronto Music · ShadowSpark Technologies</p>
          <div className="flex flex-wrap items-center gap-4">
            <button
              type="button"
              onClick={() => musicoletStore.selectTab(LibraryViewTab.GUIDE)}
              className="hover:text-[#F4FAFE] transition-colors whitespace-nowrap"
            >
              User Guide
            </button>
            <button
              type="button"
              onClick={() => musicoletStore.selectTab(LibraryViewTab.ABOUT)}
              className="hover:text-[#F4FAFE] transition-colors whitespace-nowrap"
            >
              About
            </button>
            <button
              type="button"
              onClick={() => musicoletStore.selectTab(LibraryViewTab.PRIVACY)}
              className="hover:text-[#F4FAFE] transition-colors whitespace-nowrap"
            >
              Privacy Policy
            </button>
            <button
              type="button"
              onClick={() => musicoletStore.selectTab(LibraryViewTab.SPONSOR)}
              className="hover:text-[#F4FAFE] transition-colors whitespace-nowrap"
            >
              Sponsor
            </button>
            <button
              type="button"
              onClick={() => musicoletStore.selectTab(LibraryViewTab.LIVE)}
              className="hover:text-[#F4FAFE] transition-colors whitespace-nowrap"
            >
              Live · Coming Soon
            </button>
            <button
              type="button"
              onClick={() => musicoletStore.selectTab(LibraryViewTab.BLOG)}
              className="hover:text-[#F4FAFE] transition-colors whitespace-nowrap"
            >
              Blog · Pidgin Guide
            </button>
            <button
              type="button"
              onClick={() => setShowSplash(true)}
              className="hover:text-[var(--mornica-emerald)] transition-colors whitespace-nowrap"
            >
              Replay Intro
            </button>
          </div>
        </footer>
      </main>

      {!uiState.isNowPlayingOpen && (
        <MiniPlayerBar uiState={uiState} activeTrack={activeTrack} />
      )}

      <NowPlayingFullModal uiState={uiState} activeTrack={activeTrack} />
      <EqualizerModal uiState={uiState} />
      <SleepTimerModal uiState={uiState} />
      <TagEditorModal uiState={uiState} />
      <OnboardingModal uiState={uiState} />
      <RatingPromptModal uiState={uiState} />
      <WhatsAppShareModal uiState={uiState} />
    </div>
  );
}

export default App;
