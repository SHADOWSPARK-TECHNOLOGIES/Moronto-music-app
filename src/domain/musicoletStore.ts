import { onAuthStateChanged, type User } from 'firebase/auth';
import { useSyncExternalStore } from 'react';
import {
  auth,
  createSupportRequestInFirestore,
  listUserSupportRequests,
  reloadCurrentUser,
  resendVerificationEmailForCurrentUser,
  signOutAccount,
  syncPrivateUserProfile,
  type SupportDeviceType,
  type SupportRequestRecord,
} from '../firebase';
import {
  type AdConsentStatus,
  type AppLanguage,
  BUNDLED_OFFLINE_TRACKS,
  type CrossfadeSettings,
  type CustomPlaylist,
  DEFAULT_CROSSFADE_SETTINGS,
  DEFAULT_EQUALIZER_SETTINGS,
  DEFAULT_PLAYLISTS,
  DEFAULT_QUEUES,
  EQ_PRESETS,
  type EqualizerSettings,
  LibraryViewTab,
  MORONTO_THEMES,
  type MorontoThemeId,
  type OfflineTrack,
  type PlaybackQueue,
  type ProPlanId,
  RepeatMode,
  type SmartPlaylistGenre,
  SortOption,
  STUDIO_TEMPLATES,
  type SponsorBilling,
  type SponsorPledge,
  type SponsorTierId,
  type TenBandTuple,
} from '../model/musicoletModels';
import { morontoBillingProvider } from './billing';
import {
  loadImportedTracksFromDb,
  readAudioFileDurationMs,
  removeImportedTrackFromDb,
  saveImportedTracksToDb,
} from './localLibraryDb';
import {
  type TrackTagPatch,
  addTrackToCustomPlaylist,
  addTracksToQueue,
  applyTrackTags,
  buildGenreSmartPlaylists,
  clampCrossfadeSettings,
  clampEqualizerSettings,
  computeNextQueueTrack,
  computePreviousQueueTrack,
  createCustomPlaylist,
  createNewQueue,
  deleteCustomPlaylist,
  deleteQueueById,
  filterAndSortTracks,
  groupTracksBy,
  moveQueueTrack,
  removeTrackFromQueue,
  shouldShowRatingPrompt,
  switchQueueWithResume,
  validateBlogNotifyEmail,
  validateLiveNotifyEmail,
  validateProStreamingNotifyEmail,
  validateSponsorPledge,
  validateSupportRequestInput,
} from './musicoletEngine';
import { WebAudioPlayer } from './webAudioPlayer';

export interface AuthenticatedUserSummary {
  uid: string;
  displayName: string;
  email: string;
  emailVerified: boolean;
  providerId: string;
}

export interface MusicoletUiState {
  selectedTab: LibraryViewTab;
  searchQuery: string;
  sortOption: SortOption;
  tracks: OfflineTrack[];
  queues: PlaybackQueue[];
  activeQueueId: string;
  playlists: CustomPlaylist[];
  favoriteTrackIds: string[];
  isPlaying: boolean;
  positionMs: number;
  durationMs: number;
  playbackRate: number;
  shuffleEnabled: boolean;
  repeatMode: RepeatMode;
  equalizer: EqualizerSettings;
  crossfade: CrossfadeSettings;
  sleepTimerMinutes: number | null;
  sleepTimerRemainingSec: number | null;
  isNowPlayingOpen: boolean;
  nowPlayingTab: 'ARTWORK' | 'LYRICS' | 'QUEUE';
  isEqModalOpen: boolean;
  audioModalTab: 'EQUALIZER' | 'CROSSFADE';
  isSleepTimerModalOpen: boolean;
  editingTrackId: string | null;
  sharingTrackId: string | null;
  statusMessage: string | null;
  sponsorPledges: SponsorPledge[];
  blogNotifyEmail: string | null;
  liveNotifyEmail: string | null;
  proNotifyEmail: string | null;
  language: AppLanguage;
  themeId: MorontoThemeId;
  isPro: boolean;
  proPlan: ProPlanId | null;
  eqRewardUnlockExpiresAtMs: number | null;
  isRewardedAdModalOpen: boolean;
  adConsentStatus: AdConsentStatus;
  isAdConsentModalOpen: boolean;
  dataSaverEnabled: boolean;
  hasCompletedOnboarding: boolean;
  sessionCount: number;
  hasDismissedOrRated: boolean;
  isRatingPromptOpen: boolean;
  authReady: boolean;
  authUser: AuthenticatedUserSummary | null;
  supportRequests: SupportRequestRecord[];
  supportSubmitting: boolean;
}

const STORAGE_KEYS = {
  QUEUES: 'mornica_musicolet_queues_v1',
  ACTIVE_QUEUE_ID: 'mornica_musicolet_active_queue_v1',
  PLAYLISTS: 'mornica_musicolet_playlists_v1',
  FAVORITES: 'mornica_musicolet_favorites_v1',
  TAGS: 'mornica_musicolet_tags_v1',
  EQ: 'mornica_musicolet_eq_v1',
  CROSSFADE: 'mornica_musicolet_crossfade_v1',
  SPONSORS: 'mornica_sponsor_pledges_v1',
  BLOG_NOTIFY: 'mornica_blog_notify_email_v1',
  LIVE_NOTIFY: 'moronto_live_notify_email_v1',
  PRO_NOTIFY: 'moronto_pro_notify_email_v1',
  LANGUAGE: 'moronto_lang_v1',
  THEME: 'moronto_theme_v1',
  PRO_PLAN: 'moronto_pro_plan_v1',
  EQ_REWARD_EXP: 'moronto_eq_reward_exp_v1',
  AD_CONSENT: 'moronto_ad_consent_v1',
  DATA_SAVER: 'moronto_data_saver_v1',
  ONBOARDING_DONE: 'moronto_onboarding_done_v1',
  SESSION_COUNT: 'moronto_session_count_v1',
  RATING_DONE: 'moronto_rating_done_v1',
} as const;

function safeReadJson<T>(key: string, fallback: T): T {
  try {
    const raw = window.localStorage.getItem(key);
    if (!raw) return fallback;
    return JSON.parse(raw) as T;
  } catch {
    return fallback;
  }
}

function safeWriteJson(key: string, value: unknown): void {
  try {
    window.localStorage.setItem(key, JSON.stringify(value));
  } catch {
    // Ignore storage quota errors
  }
}

class MusicoletStore {
  private readonly player = new WebAudioPlayer();
  private readonly listeners = new Set<(state: MusicoletUiState) => void>();
  private sleepIntervalId: number | null = null;
  private tagOverrides: Record<string, TrackTagPatch> = {};
  private state: MusicoletUiState;

  constructor() {
    this.tagOverrides = safeReadJson<Record<string, TrackTagPatch>>(
      STORAGE_KEYS.TAGS,
      {},
    );
    const savedQueues = safeReadJson<PlaybackQueue[]>(
      STORAGE_KEYS.QUEUES,
      DEFAULT_QUEUES,
    );
    const queues = savedQueues.length > 0 ? savedQueues : DEFAULT_QUEUES;
    const savedActiveQueueId = safeReadJson<string>(
      STORAGE_KEYS.ACTIVE_QUEUE_ID,
      queues[0].id,
    );
    const activeQueue =
      queues.find((q) => q.id === savedActiveQueueId) ?? queues[0];
    const playlists = safeReadJson<CustomPlaylist[]>(
      STORAGE_KEYS.PLAYLISTS,
      DEFAULT_PLAYLISTS,
    );
    const favoriteTrackIds = safeReadJson<string[]>(STORAGE_KEYS.FAVORITES, []);
    const rawEq = safeReadJson<Partial<EqualizerSettings>>(
      STORAGE_KEYS.EQ,
      DEFAULT_EQUALIZER_SETTINGS,
    );
    const equalizer = clampEqualizerSettings({
      ...DEFAULT_EQUALIZER_SETTINGS,
      ...rawEq,
      bandsDb:
        Array.isArray(rawEq.bandsDb) && rawEq.bandsDb.length === 10
          ? (rawEq.bandsDb as TenBandTuple)
          : [...DEFAULT_EQUALIZER_SETTINGS.bandsDb],
    });
    const rawCrossfade = safeReadJson<Partial<CrossfadeSettings>>(
      STORAGE_KEYS.CROSSFADE,
      DEFAULT_CROSSFADE_SETTINGS,
    );
    const crossfade = clampCrossfadeSettings({
      ...DEFAULT_CROSSFADE_SETTINGS,
      ...rawCrossfade,
    });

    const tracks = this.applyTagOverrides(BUNDLED_OFFLINE_TRACKS);
    const activeTrack =
      tracks.find((t) => t.id === activeQueue.currentTrackId) ??
      tracks[0] ??
      null;

    const sponsorPledges = safeReadJson<SponsorPledge[]>(
      STORAGE_KEYS.SPONSORS,
      [],
    );
    const blogNotifyEmail = safeReadJson<string | null>(
      STORAGE_KEYS.BLOG_NOTIFY,
      null,
    );
    const liveNotifyEmail = safeReadJson<string | null>(
      STORAGE_KEYS.LIVE_NOTIFY,
      null,
    );
    const proNotifyEmail = safeReadJson<string | null>(
      STORAGE_KEYS.PRO_NOTIFY,
      null,
    );
    const language = safeReadJson<AppLanguage>(STORAGE_KEYS.LANGUAGE, 'en');
    const savedTheme = safeReadJson<MorontoThemeId>(
      STORAGE_KEYS.THEME,
      'emerald-console',
    );
    const themeId = MORONTO_THEMES.some((x) => x.id === savedTheme)
      ? savedTheme
      : 'emerald-console';
    const dataSaverEnabled = safeReadJson<boolean>(
      STORAGE_KEYS.DATA_SAVER,
      false,
    );
    const hasCompletedOnboarding = safeReadJson<boolean>(
      STORAGE_KEYS.ONBOARDING_DONE,
      false,
    );
    const prevSessions = safeReadJson<number>(STORAGE_KEYS.SESSION_COUNT, 0);
    const sessionCount = prevSessions + 1;
    safeWriteJson(STORAGE_KEYS.SESSION_COUNT, sessionCount);
    const hasDismissedOrRated = safeReadJson<boolean>(
      STORAGE_KEYS.RATING_DONE,
      false,
    );
    const autoSkipPrompt =
      typeof window !== 'undefined' &&
      new URLSearchParams(window.location.search).get('autostart') === '1';

    this.state = {
      selectedTab: LibraryViewTab.TRACKS,
      searchQuery: '',
      sortOption: SortOption.TITLE,
      tracks,
      queues,
      activeQueueId: activeQueue.id,
      playlists,
      favoriteTrackIds,
      isPlaying: false,
      positionMs: activeQueue.positionMs,
      durationMs: activeTrack?.durationMs ?? 30_000,
      playbackRate: 1.0,
      shuffleEnabled: false,
      repeatMode: RepeatMode.OFF,
      equalizer,
      crossfade,
      sleepTimerMinutes: null,
      sleepTimerRemainingSec: null,
      isNowPlayingOpen: false,
      nowPlayingTab: 'ARTWORK',
      isEqModalOpen: false,
      audioModalTab: 'EQUALIZER',
      isSleepTimerModalOpen: false,
      editingTrackId: null,
      sharingTrackId: null,
      statusMessage: null,
      sponsorPledges,
      blogNotifyEmail,
      liveNotifyEmail,
      proNotifyEmail,
      language,
      themeId,
      isPro: false,
      proPlan: null,
      eqRewardUnlockExpiresAtMs: null,
      isRewardedAdModalOpen: false,
      adConsentStatus: 'non_personalized',
      isAdConsentModalOpen: false,
      dataSaverEnabled,
      hasCompletedOnboarding,
      sessionCount,
      hasDismissedOrRated,
      isRatingPromptOpen:
        shouldShowRatingPrompt(sessionCount, hasDismissedOrRated) && !autoSkipPrompt,
      authReady: false,
      authUser: null,
      supportRequests: [],
      supportSubmitting: false,
    };

    if (typeof document !== 'undefined') {
      document.documentElement.setAttribute('data-theme', themeId);
    }

    this.player.setCrossfadeSettings(crossfade);
    this.player.bindCallbacks({
      onPlayingChange: (isPlaying) => {
        this.setState((prev) => ({ ...prev, isPlaying }));
        if (!isPlaying) {
          this.persistQueues();
        }
      },
      onTimeUpdate: (positionMs, durationMs) => {
        this.setState((prev) => {
          const nextDur = durationMs > 0 ? durationMs : prev.durationMs;
          const nextQueues = prev.queues.map((q) =>
            q.id === prev.activeQueueId ? { ...q, positionMs } : q,
          );
          return {
            ...prev,
            positionMs,
            durationMs: nextDur,
            queues: nextQueues,
          };
        });
      },
      onNearEnd: () => {
        if (
          !this.state.crossfade.enabled ||
          this.state.crossfade.durationSec <= 0
        ) {
          return;
        }
        const step = computeNextQueueTrack(
          this.getActiveQueue(),
          this.state.repeatMode,
          this.state.shuffleEnabled,
          true,
        );
        if (!step.shouldStop && step.nextTrackId) {
          this.playTrackInActiveQueue(
            step.nextTrackId,
            0,
            this.state.crossfade.durationSec,
          );
        }
      },
      onEnded: () => this.skipNext(true),
      onError: (msg) => {
        this.setState((prev) => ({
          ...prev,
          isPlaying: false,
          statusMessage: msg,
        }));
      },
      onPrev: () => this.skipPrevious(),
      onNext: () => this.skipNext(false),
      onSeek: (posMs) => this.seekTo(posMs),
    });

    if (typeof window !== 'undefined') {
      window.addEventListener('beforeunload', () => this.persistQueues());
    }

    onAuthStateChanged(auth, (user) => {
      this.handleAuthUserChanged(user);
    });

    void this.hydrateImportedTracks();
  }

  private handleAuthUserChanged(user: User | null): void {
    if (!user) {
      this.player.pause();
      this.setState((prev) => ({
        ...prev,
        authReady: true,
        authUser: null,
        isPlaying: false,
        supportRequests: [],
      }));
      return;
    }

    const summary: AuthenticatedUserSummary = {
      uid: user.uid,
      displayName:
        user.displayName?.trim() ||
        user.email?.split('@')[0] ||
        'Moronto Listener',
      email: user.email ?? '',
      emailVerified: user.emailVerified,
      providerId: user.providerData[0]?.providerId ?? 'password',
    };

    this.setState((prev) => ({
      ...prev,
      authReady: true,
      authUser: summary,
    }));

    if (summary.emailVerified) {
      void syncPrivateUserProfile({
        language: this.state.language,
        themeId: this.state.themeId,
      }).catch(() => undefined);
      void this.refreshSupportRequests();
    }
  }

  private async hydrateImportedTracks(): Promise<void> {
    const imported = await loadImportedTracksFromDb();
    if (imported.length === 0) return;
    this.setState((prev) => {
      const existingIds = new Set(prev.tracks.map((t) => t.id));
      const newTracks = this.applyTagOverrides(
        imported.filter((t) => !existingIds.has(t.id)),
      );
      return {
        ...prev,
        tracks: [...newTracks, ...prev.tracks],
      };
    });
  }

  private applyTagOverrides(tracks: OfflineTrack[]): OfflineTrack[] {
    let result = tracks;
    for (const [id, patch] of Object.entries(this.tagOverrides)) {
      result = applyTrackTags(result, id, patch);
    }
    return result;
  }

  getSnapshot(): MusicoletUiState {
    return this.state;
  }

  subscribe(listener: (state: MusicoletUiState) => void): () => void {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  private setState(updater: (prev: MusicoletUiState) => MusicoletUiState): void {
    this.state = updater(this.state);
    for (const listener of this.listeners) {
      listener(this.state);
    }
  }

  private persistQueues(): void {
    safeWriteJson(STORAGE_KEYS.QUEUES, this.state.queues);
    safeWriteJson(STORAGE_KEYS.ACTIVE_QUEUE_ID, this.state.activeQueueId);
  }

  getActiveQueue(): PlaybackQueue {
    return (
      this.state.queues.find((q) => q.id === this.state.activeQueueId) ??
      this.state.queues[0]
    );
  }

  getActiveTrack(): OfflineTrack | null {
    const q = this.getActiveQueue();
    if (q.trackIds.length === 0) return null;
    const candidateId = q.currentTrackId ?? q.trackIds[0];
    return (
      this.state.tracks.find((t) => t.id === candidateId) ??
      this.state.tracks.find((t) => t.id === q.trackIds[0]) ??
      null
    );
  }

  getFilteredSortedTracks(): OfflineTrack[] {
    return filterAndSortTracks(
      this.state.tracks,
      this.state.searchQuery,
      this.state.sortOption,
    );
  }

  getGroupedTracks(key: 'folderName' | 'artist' | 'album') {
    return groupTracksBy(this.getFilteredSortedTracks(), key);
  }

  selectTab(tab: LibraryViewTab): void {
    this.setState((prev) => ({ ...prev, selectedTab: tab }));
  }

  setSearchQuery(query: string): void {
    this.setState((prev) => ({ ...prev, searchQuery: query }));
  }

  setSortOption(sortOption: SortOption): void {
    this.setState((prev) => ({ ...prev, sortOption }));
  }

  dismissStatus(): void {
    this.setState((prev) => ({ ...prev, statusMessage: null }));
  }

  setNowPlayingOpen(open: boolean): void {
    this.setState((prev) => ({ ...prev, isNowPlayingOpen: open }));
  }

  setNowPlayingTab(tab: 'ARTWORK' | 'LYRICS' | 'QUEUE'): void {
    this.setState((prev) => ({ ...prev, nowPlayingTab: tab }));
  }

  setEqModalOpen(
    open: boolean,
    tab: 'EQUALIZER' | 'CROSSFADE' = this.state.audioModalTab,
  ): void {
    this.setState((prev) => ({
      ...prev,
      isEqModalOpen: open,
      audioModalTab: tab,
    }));
  }

  setAudioModalTab(tab: 'EQUALIZER' | 'CROSSFADE'): void {
    this.setState((prev) => ({ ...prev, audioModalTab: tab }));
  }

  setSleepTimerModalOpen(open: boolean): void {
    this.setState((prev) => ({ ...prev, isSleepTimerModalOpen: open }));
  }

  setEditingTrackId(trackId: string | null): void {
    this.setState((prev) => ({ ...prev, editingTrackId: trackId }));
  }

  playTrackInActiveQueue(
    trackId: string,
    startPositionMs: number = 0,
    crossfadeSecOverride?: number,
  ): void {
    const track = this.state.tracks.find((t) => t.id === trackId);
    if (!track) return;

    const fadeSec =
      crossfadeSecOverride !== undefined
        ? crossfadeSecOverride
        : this.state.crossfade.enabled &&
            this.state.crossfade.fadeOnManualSkip &&
            this.state.isPlaying
          ? this.state.crossfade.durationSec
          : 0;

    this.setState((prev) => {
      const nextQueues = prev.queues.map((q) => {
        if (q.id !== prev.activeQueueId) return q;
        const hasTrack = q.trackIds.includes(trackId);
        return {
          ...q,
          trackIds: hasTrack ? q.trackIds : [...q.trackIds, trackId],
          currentTrackId: trackId,
          positionMs: startPositionMs,
        };
      });
      return {
        ...prev,
        queues: nextQueues,
        positionMs: startPositionMs,
        durationMs: track.durationMs,
        isPlaying: true,
        statusMessage: null,
      };
    });
    this.persistQueues();

    this.player.loadAndPlay(
      track.uri,
      { title: track.title, artist: track.artist, album: track.album },
      startPositionMs,
      this.state.playbackRate,
      this.state.equalizer,
      true,
      fadeSec,
    );
  }

  playTrackListAsActiveQueue(trackIds: string[], startTrackId?: string): void {
    if (trackIds.length === 0) return;
    const targetId = startTrackId ?? trackIds[0];
    const track = this.state.tracks.find((t) => t.id === targetId);
    if (!track) return;

    this.setState((prev) => {
      const nextQueues = prev.queues.map((q) =>
        q.id === prev.activeQueueId
          ? {
              ...q,
              trackIds: [...trackIds],
              currentTrackId: targetId,
              positionMs: 0,
            }
          : q,
      );
      return {
        ...prev,
        queues: nextQueues,
        positionMs: 0,
        durationMs: track.durationMs,
        isPlaying: true,
      };
    });
    this.persistQueues();

    this.player.loadAndPlay(
      track.uri,
      { title: track.title, artist: track.artist, album: track.album },
      0,
      this.state.playbackRate,
      this.state.equalizer,
      true,
    );
  }

  togglePlayPause(): void {
    if (this.state.isPlaying) {
      this.player.pause();
      this.persistQueues();
      return;
    }
    const track = this.getActiveTrack();
    if (!track) return;
    this.player.loadAndPlay(
      track.uri,
      { title: track.title, artist: track.artist, album: track.album },
      this.state.positionMs,
      this.state.playbackRate,
      this.state.equalizer,
      true,
    );
  }

  seekTo(positionMs: number): void {
    const clamped = Math.max(0, Math.min(positionMs, this.state.durationMs));
    this.player.seekTo(clamped);
    this.setState((prev) => ({
      ...prev,
      positionMs: clamped,
      queues: prev.queues.map((q) =>
        q.id === prev.activeQueueId ? { ...q, positionMs: clamped } : q,
      ),
    }));
    this.persistQueues();
  }

  skipNext(isAutoAdvance: boolean = false): void {
    const queue = this.getActiveQueue();
    const step = computeNextQueueTrack(
      queue,
      this.state.repeatMode,
      this.state.shuffleEnabled,
      isAutoAdvance,
    );
    if (step.shouldStop || !step.nextTrackId) {
      this.player.pause();
      this.player.seekTo(0);
      this.setState((prev) => ({
        ...prev,
        isPlaying: false,
        positionMs: 0,
        statusMessage: 'Reached end of queue.',
      }));
      this.persistQueues();
      return;
    }
    const fadeSec =
      isAutoAdvance && this.state.crossfade.enabled
        ? this.state.crossfade.durationSec
        : !isAutoAdvance &&
            this.state.crossfade.enabled &&
            this.state.crossfade.fadeOnManualSkip &&
            this.state.isPlaying
          ? this.state.crossfade.durationSec
          : 0;
    this.playTrackInActiveQueue(step.nextTrackId, 0, fadeSec);
  }

  skipPrevious(): void {
    const queue = this.getActiveQueue();
    const step = computePreviousQueueTrack(
      queue,
      this.state.repeatMode,
      this.state.positionMs,
    );
    if (!step.prevTrackId) return;
    this.playTrackInActiveQueue(step.prevTrackId, 0);
  }

  toggleShuffle(): void {
    this.setState((prev) => ({
      ...prev,
      shuffleEnabled: !prev.shuffleEnabled,
      statusMessage: !prev.shuffleEnabled
        ? 'Shuffle enabled.'
        : 'Shuffle disabled.',
    }));
  }

  cycleRepeatMode(): void {
    this.setState((prev) => {
      const next =
        prev.repeatMode === RepeatMode.OFF
          ? RepeatMode.ALL
          : prev.repeatMode === RepeatMode.ALL
            ? RepeatMode.ONE
            : RepeatMode.OFF;
      return { ...prev, repeatMode: next };
    });
  }

  setPlaybackRate(rate: number): void {
    this.player.setPlaybackRate(rate);
    this.setState((prev) => ({ ...prev, playbackRate: rate }));
  }

  // --- Multiple Resumable Queues (Musicolet Core) ---
  switchQueue(targetQueueId: string, autoPlay: boolean = false): void {
    if (targetQueueId === this.state.activeQueueId) return;
    this.player.pause();

    const { queues, activeQueue } = switchQueueWithResume(
      this.state.queues,
      this.state.activeQueueId,
      targetQueueId,
      this.state.positionMs,
    );
    const targetTrack =
      this.state.tracks.find((t) => t.id === activeQueue.currentTrackId) ??
      this.state.tracks.find((t) => t.id === activeQueue.trackIds[0]) ??
      null;

    this.setState((prev) => ({
      ...prev,
      queues,
      activeQueueId: activeQueue.id,
      positionMs: activeQueue.positionMs,
      durationMs: targetTrack?.durationMs ?? 30_000,
      isPlaying: autoPlay && targetTrack !== null,
      statusMessage: `Switched to ${activeQueue.name}`,
    }));
    this.persistQueues();

    if (targetTrack && autoPlay) {
      this.player.loadAndPlay(
        targetTrack.uri,
        {
          title: targetTrack.title,
          artist: targetTrack.artist,
          album: targetTrack.album,
        },
        activeQueue.positionMs,
        this.state.playbackRate,
        this.state.equalizer,
        true,
      );
    }
  }

  createQueue(name: string, initialTrackIds: string[] = []): boolean {
    const res = createNewQueue(this.state.queues, name, initialTrackIds);
    if (!res.created) {
      this.setState((prev) => ({
        ...prev,
        statusMessage: res.error ?? 'Could not create queue.',
      }));
      return false;
    }
    this.setState((prev) => ({
      ...prev,
      queues: res.queues,
      statusMessage: `Created queue '${res.created!.name}'.`,
    }));
    this.persistQueues();
    return true;
  }

  deleteQueue(queueId: string): void {
    const res = deleteQueueById(
      this.state.queues,
      this.state.activeQueueId,
      queueId,
    );
    if (!res.deleted) {
      this.setState((prev) => ({
        ...prev,
        statusMessage: 'At least one queue must remain active.',
      }));
      return;
    }
    if (res.nextActiveQueueId !== this.state.activeQueueId) {
      this.player.pause();
    }
    const nextActive =
      res.queues.find((q) => q.id === res.nextActiveQueueId) ?? res.queues[0];
    this.setState((prev) => ({
      ...prev,
      queues: res.queues,
      activeQueueId: nextActive.id,
      positionMs: nextActive.positionMs,
      isPlaying:
        res.nextActiveQueueId !== prev.activeQueueId ? false : prev.isPlaying,
      statusMessage: 'Queue removed.',
    }));
    this.persistQueues();
  }

  addTracksToSpecificQueue(
    queueId: string,
    trackIds: string[],
    playNext: boolean = false,
  ): void {
    const target = this.state.queues.find((q) => q.id === queueId);
    if (!target) return;
    const nextQueues = addTracksToQueue(
      this.state.queues,
      queueId,
      trackIds,
      playNext,
    );
    this.setState((prev) => ({
      ...prev,
      queues: nextQueues,
      statusMessage: playNext
        ? `Added to play next in '${target.name}'.`
        : `Added ${trackIds.length} ${trackIds.length === 1 ? 'track' : 'tracks'} to '${target.name}'.`,
    }));
    this.persistQueues();
  }

  removeFromQueue(queueId: string, trackId: string): void {
    const nextQueues = removeTrackFromQueue(this.state.queues, queueId, trackId);
    this.setState((prev) => ({ ...prev, queues: nextQueues }));
    this.persistQueues();
  }

  reorderInQueue(queueId: string, fromIdx: number, toIdx: number): void {
    const nextQueues = moveQueueTrack(this.state.queues, queueId, fromIdx, toIdx);
    this.setState((prev) => ({ ...prev, queues: nextQueues }));
    this.persistQueues();
  }

  // --- Favorites & Custom Playlists ---
  toggleFavorite(trackId: string): void {
    this.setState((prev) => {
      const exists = prev.favoriteTrackIds.includes(trackId);
      const nextFavs = exists
        ? prev.favoriteTrackIds.filter((id) => id !== trackId)
        : [...prev.favoriteTrackIds, trackId];
      safeWriteJson(STORAGE_KEYS.FAVORITES, nextFavs);
      return { ...prev, favoriteTrackIds: nextFavs };
    });
  }

  createPlaylist(name: string): boolean {
    const res = createCustomPlaylist(this.state.playlists, name);
    if (!res.created) {
      this.setState((prev) => ({
        ...prev,
        statusMessage: 'Enter a playlist name.',
      }));
      return false;
    }
    this.setState((prev) => {
      safeWriteJson(STORAGE_KEYS.PLAYLISTS, res.playlists);
      return {
        ...prev,
        playlists: res.playlists,
        statusMessage: `Created playlist '${res.created!.name}'.`,
      };
    });
    return true;
  }

  addToPlaylist(playlistId: string, trackId: string): void {
    const pl = this.state.playlists.find((p) => p.id === playlistId);
    const next = addTrackToCustomPlaylist(
      this.state.playlists,
      playlistId,
      trackId,
    );
    safeWriteJson(STORAGE_KEYS.PLAYLISTS, next);
    this.setState((prev) => ({
      ...prev,
      playlists: next,
      statusMessage: pl ? `Added to playlist '${pl.name}'.` : prev.statusMessage,
    }));
  }

  removeFromPlaylist(playlistId: string, trackId: string): void {
    const next = this.state.playlists.map((p) =>
      p.id === playlistId
        ? { ...p, trackIds: p.trackIds.filter((id) => id !== trackId) }
        : p,
    );
    safeWriteJson(STORAGE_KEYS.PLAYLISTS, next);
    this.setState((prev) => ({ ...prev, playlists: next }));
  }

  deletePlaylist(playlistId: string): void {
    const target = this.state.playlists.find((p) => p.id === playlistId);
    const next = deleteCustomPlaylist(this.state.playlists, playlistId);
    safeWriteJson(STORAGE_KEYS.PLAYLISTS, next);
    this.setState((prev) => ({
      ...prev,
      playlists: next,
      statusMessage: target
        ? `Deleted playlist '${target.name}'.`
        : prev.statusMessage,
    }));
  }

  // --- Tag Editor+ ---
  saveTrackTags(trackId: string, patch: TrackTagPatch): void {
    this.tagOverrides[trackId] = patch;
    safeWriteJson(STORAGE_KEYS.TAGS, this.tagOverrides);
    this.setState((prev) => ({
      ...prev,
      tracks: applyTrackTags(prev.tracks, trackId, patch),
      editingTrackId: null,
      statusMessage: `Updated tags for '${patch.title}'.`,
    }));
  }

  // --- Equalizer & Crossfade ---
  updateEqualizer(nextEq: EqualizerSettings): void {
    const clamped = clampEqualizerSettings(nextEq);
    this.player.applyEqualizer(clamped);
    safeWriteJson(STORAGE_KEYS.EQ, clamped);
    this.setState((prev) => ({ ...prev, equalizer: clamped }));
  }

  applyEqPreset(presetId: string, _bypassGate: boolean = true): boolean {
    const preset = EQ_PRESETS.find((p) => p.id === presetId);
    if (!preset) return false;
    this.updateEqualizer({
      ...this.state.equalizer,
      enabled: true,
      presetId: preset.id,
      bandsDb: [...preset.bandsDb],
      bassBoostPercent: preset.bassBoostPercent,
    });
    return true;
  }

  resetEqualizerToFlat(): void {
    this.updateEqualizer({
      enabled: true,
      presetId: 'flat',
      bandsDb: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      bassBoostPercent: 0,
      preampDb: 0,
      virtualizerPercent: 0,
    });
  }

  updateCrossfade(nextCrossfade: CrossfadeSettings): void {
    const clamped = clampCrossfadeSettings(nextCrossfade);
    this.player.setCrossfadeSettings(clamped);
    safeWriteJson(STORAGE_KEYS.CROSSFADE, clamped);
    this.setState((prev) => ({ ...prev, crossfade: clamped }));
  }

  // --- Sleep Timer (Unlocked for All Listeners) ---
  setSleepTimer(minutes: number | null): void {
    if (this.sleepIntervalId !== null) {
      window.clearInterval(this.sleepIntervalId);
      this.sleepIntervalId = null;
    }
    if (minutes === null || minutes <= 0) {
      this.setState((prev) => ({
        ...prev,
        sleepTimerMinutes: null,
        sleepTimerRemainingSec: null,
        statusMessage: 'Sleep timer cleared.',
      }));
      return;
    }

    const totalSec = minutes * 60;
    this.setState((prev) => ({
      ...prev,
      sleepTimerMinutes: minutes,
      sleepTimerRemainingSec: totalSec,
      isSleepTimerModalOpen: false,
      statusMessage: `Sleep timer set for ${minutes} min.`,
    }));

    this.sleepIntervalId = window.setInterval(() => {
      const rem = this.state.sleepTimerRemainingSec;
      if (rem === null) return;
      if (rem <= 1) {
        if (this.sleepIntervalId !== null) {
          window.clearInterval(this.sleepIntervalId);
          this.sleepIntervalId = null;
        }
        this.player.pause();
        this.setState((prev) => ({
          ...prev,
          isPlaying: false,
          sleepTimerMinutes: null,
          sleepTimerRemainingSec: null,
          statusMessage: 'Sleep timer ended — playback paused.',
        }));
        return;
      }
      this.setState((prev) => ({
        ...prev,
        sleepTimerRemainingSec: (prev.sleepTimerRemainingSec ?? 1) - 1,
      }));
    }, 1000);
  }

  // --- Local Audio Import (Strictly On-Device / IndexedDB) ---
  async importLocalAudioFiles(files: FileList | File[]): Promise<void> {
    const validFiles = Array.from(files).filter(
      (f) =>
        f.type.startsWith('audio/') ||
        /\.(mp3|wav|flac|m4a|ogg|aac|opus)$/i.test(f.name),
    );
    if (validFiles.length === 0) {
      this.setState((prev) => ({
        ...prev,
        statusMessage: 'No supported audio files selected (MP3, WAV, FLAC, M4A, OGG).',
      }));
      return;
    }

    const entries: Array<{ track: OfflineTrack; file: File }> = [];
    for (let i = 0; i < validFiles.length; i++) {
      const file = validFiles[i];
      const objectUrl = URL.createObjectURL(file);
      const durationMs = await readAudioFileDurationMs(objectUrl);
      const baseName = file.name.replace(/\.[^.]+$/, '').trim() || 'Unknown Track';
      const parts = baseName.split(' - ');
      const artist = parts.length >= 2 ? parts[0].trim() : 'Device Library';
      const title = parts.length >= 2 ? parts.slice(1).join(' - ').trim() : baseName;
      const relPath =
        (file as File & { webkitRelativePath?: string }).webkitRelativePath ?? '';
      const segments = relPath.split('/').filter(Boolean);
      const folderName =
        segments.length >= 2 ? segments[segments.length - 2] : 'Imported Audio';

      const track: OfflineTrack = {
        id: `device-${file.lastModified}-${file.size}-${i}`,
        title,
        artist,
        album: 'Local Device Audio',
        folderName,
        durationMs: durationMs > 0 ? durationMs : 180_000,
        uri: objectUrl,
        releaseYear: new Date(file.lastModified || Date.now()).getFullYear() || 2026,
        sourceType: 'DEVICE',
      };
      entries.push({ track, file });
    }

    await saveImportedTracksToDb(entries);

    const newTracks = entries.map((e) => e.track);
    this.setState((prev) => {
      const existingIds = new Set(prev.tracks.map((t) => t.id));
      const deduped = newTracks.filter((t) => !existingIds.has(t.id));
      const addedIds = deduped.map((t) => t.id);
      const nextQueues = addTracksToQueue(
        prev.queues,
        prev.activeQueueId,
        addedIds,
      );
      return {
        ...prev,
        tracks: [...deduped, ...prev.tracks],
        queues: nextQueues,
        statusMessage: `Imported ${deduped.length} offline audio ${
          deduped.length === 1 ? 'file' : 'files'
        } into your library.`,
      };
    });
    this.persistQueues();
  }

  async deleteImportedTrack(trackId: string): Promise<void> {
    const target = this.state.tracks.find((t) => t.id === trackId);
    if (!target || target.sourceType !== 'DEVICE') return;
    const wasActive = this.getActiveTrack()?.id === trackId;
    if (wasActive) {
      this.player.pause();
    }
    if (target.uri.startsWith('blob:')) {
      URL.revokeObjectURL(target.uri);
    }
    await removeImportedTrackFromDb(trackId);
    this.setState((prev) => {
      const nextFavs = prev.favoriteTrackIds.filter((id) => id !== trackId);
      const nextPlaylists = prev.playlists.map((pl) => ({
        ...pl,
        trackIds: pl.trackIds.filter((id) => id !== trackId),
      }));
      safeWriteJson(STORAGE_KEYS.FAVORITES, nextFavs);
      safeWriteJson(STORAGE_KEYS.PLAYLISTS, nextPlaylists);
      return {
        ...prev,
        isPlaying: wasActive ? false : prev.isPlaying,
        positionMs: wasActive ? 0 : prev.positionMs,
        tracks: prev.tracks.filter((t) => t.id !== trackId),
        favoriteTrackIds: nextFavs,
        playlists: nextPlaylists,
        queues: prev.queues.map((q) => {
          const nextIds = q.trackIds.filter((id) => id !== trackId);
          return {
            ...q,
            trackIds: nextIds,
            currentTrackId:
              q.currentTrackId === trackId
                ? (nextIds[0] ?? null)
                : q.currentTrackId,
            positionMs: q.currentTrackId === trackId ? 0 : q.positionMs,
          };
        }),
        statusMessage: `Removed '${target.title}' from local library.`,
      };
    });
    this.persistQueues();
  }

  // --- Sponsor & Blog Waitlist ---
  submitSponsorPledge(input: {
    name: string;
    email: string;
    tierId: SponsorTierId;
    billing: SponsorBilling;
    note?: string;
  }): { ok: boolean; error?: string } {
    const result = validateSponsorPledge(input);
    if (!result.valid || !result.pledge) {
      return { ok: false, error: result.error ?? 'Invalid sponsorship details.' };
    }
    const pledge = result.pledge;
    this.setState((prev) => {
      const nextPledges = [pledge, ...prev.sponsorPledges].slice(0, 20);
      safeWriteJson(STORAGE_KEYS.SPONSORS, nextPledges);
      return {
        ...prev,
        sponsorPledges: nextPledges,
        statusMessage: `Recorded sponsorship pledge from ${pledge.name} ($${pledge.amountUsd}).`,
      };
    });
    return { ok: true };
  }

  submitBlogNotifyEmail(email: string): { ok: boolean; error?: string } {
    const result = validateBlogNotifyEmail(email);
    if (!result.valid || !result.normalizedEmail) {
      return { ok: false, error: result.error ?? 'Invalid email address.' };
    }
    safeWriteJson(STORAGE_KEYS.BLOG_NOTIFY, result.normalizedEmail);
    this.setState((prev) => ({
      ...prev,
      blogNotifyEmail: result.normalizedEmail ?? null,
      statusMessage: `Subscribed ${result.normalizedEmail} for Moronto Blog launch updates.`,
    }));
    return { ok: true };
  }

  clearBlogNotifyEmail(): void {
    safeWriteJson(STORAGE_KEYS.BLOG_NOTIFY, null);
    this.setState((prev) => ({
      ...prev,
      blogNotifyEmail: null,
      statusMessage: 'Removed email from Blog launch waitlist.',
    }));
  }

  submitLiveNotifyEmail(email: string): { ok: boolean; error?: string } {
    const result = validateLiveNotifyEmail(email);
    if (!result.valid || !result.normalizedEmail) {
      return { ok: false, error: result.error ?? 'Invalid email address.' };
    }
    safeWriteJson(STORAGE_KEYS.LIVE_NOTIFY, result.normalizedEmail);
    this.setState((prev) => ({
      ...prev,
      liveNotifyEmail: result.normalizedEmail ?? null,
      statusMessage: `Subscribed ${result.normalizedEmail} for Moronto Live Streaming alerts.`,
    }));
    return { ok: true };
  }

  clearLiveNotifyEmail(): void {
    safeWriteJson(STORAGE_KEYS.LIVE_NOTIFY, null);
    this.setState((prev) => ({
      ...prev,
      liveNotifyEmail: null,
      statusMessage: 'Removed email from Live Streaming waitlist.',
    }));
  }

  submitProNotifyEmail(email: string): { ok: boolean; error?: string } {
    const result = validateProStreamingNotifyEmail(email);
    if (!result.valid || !result.normalizedEmail) {
      return { ok: false, error: result.error ?? 'Invalid email address.' };
    }
    safeWriteJson(STORAGE_KEYS.PRO_NOTIFY, result.normalizedEmail);
    this.setState((prev) => ({
      ...prev,
      proNotifyEmail: result.normalizedEmail ?? null,
      statusMessage: `Subscribed ${result.normalizedEmail} for Moronto Pro Global Streaming launch alerts.`,
    }));
    return { ok: true };
  }

  clearProNotifyEmail(): void {
    safeWriteJson(STORAGE_KEYS.PRO_NOTIFY, null);
    this.setState((prev) => ({
      ...prev,
      proNotifyEmail: null,
      statusMessage: 'Removed email from Pro Global Streaming waitlist.',
    }));
  }

  // --- Account Auth & Support Desk (Firestore) ---
  async signOutUser(): Promise<void> {
    this.player.pause();
    await signOutAccount();
    this.setState((prev) => ({
      ...prev,
      isPlaying: false,
      statusMessage: null,
    }));
  }

  async resendVerificationEmail(): Promise<{ ok: boolean; error?: string }> {
    try {
      await resendVerificationEmailForCurrentUser();
      this.setState((prev) => ({
        ...prev,
        statusMessage:
          'Verification email sent! Open the link in your inbox, then tap "I\'ve Verified".',
      }));
      return { ok: true };
    } catch (error) {
      const msg =
        error instanceof Error
          ? error.message
          : 'Could not resend verification email right now.';
      this.setState((prev) => ({ ...prev, statusMessage: msg }));
      return { ok: false, error: msg };
    }
  }

  async refreshAuthVerification(): Promise<void> {
    const updated = await reloadCurrentUser();
    if (updated) {
      this.handleAuthUserChanged(updated);
      this.setState((prev) => ({
        ...prev,
        statusMessage: updated.emailVerified
          ? 'Email verified! Cloud profile & support ticket sync is active.'
          : 'Email is not verified yet. Check your inbox or spam folder.',
      }));
    }
  }

  async refreshSupportRequests(): Promise<void> {
    try {
      const items = await listUserSupportRequests();
      this.setState((prev) => ({ ...prev, supportRequests: items }));
    } catch {
      // Errors logged via handleFirestoreError
    }
  }

  async submitSupportRequest(input: {
    topic: string;
    message: string;
    deviceType: SupportDeviceType;
  }): Promise<{ ok: boolean; error?: string }> {
    const check = validateSupportRequestInput(input);
    if (!check.valid || !check.topic || !check.message || !check.deviceType) {
      return { ok: false, error: check.error ?? 'Check your support request fields.' };
    }
    this.setState((prev) => ({ ...prev, supportSubmitting: true }));
    try {
      const record = await createSupportRequestInFirestore({
        topic: check.topic,
        message: check.message,
        deviceType: check.deviceType,
      });
      this.setState((prev) => ({
        ...prev,
        supportSubmitting: false,
        supportRequests: [record, ...prev.supportRequests],
        statusMessage: 'Support request sent! Moronto Support will reply via email.',
      }));
      return { ok: true };
    } catch (error) {
      this.setState((prev) => ({ ...prev, supportSubmitting: false }));
      const msg =
        error instanceof Error
          ? error.message
          : 'Unable to send support ticket right now.';
      return { ok: false, error: msg };
    }
  }

  // --- Language, Themes, Smart Playlists, Templates, Onboarding, Rating, Share ---
  setLanguage(language: AppLanguage): void {
    safeWriteJson(STORAGE_KEYS.LANGUAGE, language);
    this.setState((prev) => ({
      ...prev,
      language,
      statusMessage: `Language switched to ${language.toUpperCase()}.`,
    }));
    if (this.state.authUser?.emailVerified) {
      void syncPrivateUserProfile({
        language,
        themeId: this.state.themeId,
      }).catch(() => undefined);
    }
  }

  setTheme(themeId: MorontoThemeId): void {
    const opt = MORONTO_THEMES.find((x) => x.id === themeId);
    if (!opt) return;
    safeWriteJson(STORAGE_KEYS.THEME, themeId);
    if (typeof document !== 'undefined') {
      document.documentElement.setAttribute('data-theme', themeId);
    }
    this.setState((prev) => ({
      ...prev,
      themeId,
      statusMessage: `Theme updated to ${opt.name}.`,
    }));
    if (this.state.authUser?.emailVerified) {
      void syncPrivateUserProfile({
        language: this.state.language,
        themeId,
      }).catch(() => undefined);
    }
  }

  async purchaseProPlan(planId: ProPlanId): Promise<void> {
    const res = await morontoBillingProvider.purchasePlan(planId);
    this.setState((prev) => ({
      ...prev,
      statusMessage: res.message,
    }));
  }

  async restoreProPurchases(): Promise<void> {
    const stored = safeReadJson<ProPlanId | null>(STORAGE_KEYS.PRO_PLAN, null);
    const res = await morontoBillingProvider.restorePurchases(stored);
    this.setState((prev) => ({
      ...prev,
      statusMessage: res.message,
    }));
  }

  grantRewardedEqUnlock(): void {
    this.setState((prev) => ({
      ...prev,
      isRewardedAdModalOpen: false,
      statusMessage: 'All 10-band EQ presets are already unlocked free for everyone.',
    }));
  }

  setRewardedAdModalOpen(open: boolean): void {
    this.setState((prev) => ({ ...prev, isRewardedAdModalOpen: open }));
  }

  setAdConsent(status: AdConsentStatus): void {
    safeWriteJson(STORAGE_KEYS.AD_CONSENT, status);
    this.setState((prev) => ({
      ...prev,
      adConsentStatus: status,
      isAdConsentModalOpen: false,
    }));
  }

  setAdConsentModalOpen(open: boolean): void {
    this.setState((prev) => ({ ...prev, isAdConsentModalOpen: open }));
  }

  toggleDataSaver(): void {
    this.setState((prev) => {
      const next = !prev.dataSaverEnabled;
      safeWriteJson(STORAGE_KEYS.DATA_SAVER, next);
      return {
        ...prev,
        dataSaverEnabled: next,
        statusMessage: next
          ? 'Data-Saver Mode enabled — purely offline assets active.'
          : 'Data-Saver Mode disabled.',
      };
    });
  }

  applyStudioTemplate(templateId: string): void {
    const tpl = STUDIO_TEMPLATES.find((x) => x.id === templateId);
    if (!tpl) return;
    if (this.state.queues.some((q) => q.id === tpl.queueId)) {
      this.switchQueue(tpl.queueId, false);
    }
    this.applyEqPreset(tpl.eqPresetId, true);
    this.updateCrossfade({
      ...this.state.crossfade,
      enabled: tpl.crossfadeEnabled && tpl.crossfadeSec > 0,
      durationSec: tpl.crossfadeSec,
    });
    this.setState((prev) => ({
      ...prev,
      statusMessage: `Applied studio template '${tpl.name}'.`,
    }));
  }

  playSmartGenrePlaylist(genre: SmartPlaylistGenre): void {
    const groups = buildGenreSmartPlaylists(this.state.tracks);
    const group = groups.find((g) => g.genre === genre);
    if (!group || group.tracks.length === 0) {
      this.setState((prev) => ({
        ...prev,
        statusMessage: `No ${genre} tracks in library yet. Import audio or tag a track's folder as '${genre}'.`,
      }));
      return;
    }
    this.playTrackListAsActiveQueue(group.tracks.map((t) => t.id));
    this.setState((prev) => ({
      ...prev,
      statusMessage: `Playing ${genre} Smart Playlist (${group.tracks.length} tracks).`,
    }));
  }

  completeOnboarding(): void {
    safeWriteJson(STORAGE_KEYS.ONBOARDING_DONE, true);
    this.setState((prev) => ({
      ...prev,
      hasCompletedOnboarding: true,
      isAdConsentModalOpen: false,
    }));
  }

  replayOnboarding(): void {
    this.setState((prev) => ({
      ...prev,
      hasCompletedOnboarding: false,
    }));
  }

  submitRating(stars: number, comment: string): void {
    safeWriteJson(STORAGE_KEYS.RATING_DONE, true);
    this.setState((prev) => ({
      ...prev,
      hasDismissedOrRated: true,
      isRatingPromptOpen: false,
      statusMessage: `Thank you for rating Moronto Music ${stars}/5${
        comment.trim() ? ' — feedback recorded locally.' : '.'
      }`,
    }));
  }

  dismissRatingPrompt(): void {
    safeWriteJson(STORAGE_KEYS.RATING_DONE, true);
    this.setState((prev) => ({
      ...prev,
      hasDismissedOrRated: true,
      isRatingPromptOpen: false,
    }));
  }

  triggerTestFifthSessionPrompt(): void {
    safeWriteJson(STORAGE_KEYS.SESSION_COUNT, 5);
    safeWriteJson(STORAGE_KEYS.RATING_DONE, false);
    this.setState((prev) => ({
      ...prev,
      sessionCount: 5,
      hasDismissedOrRated: false,
      isRatingPromptOpen: true,
    }));
  }

  setSharingTrackId(trackId: string | null): void {
    this.setState((prev) => ({ ...prev, sharingTrackId: trackId }));
  }
}

export const musicoletStore = new MusicoletStore();

export function useMusicoletState(): MusicoletUiState {
  return useSyncExternalStore(
    (cb) => musicoletStore.subscribe(cb),
    () => musicoletStore.getSnapshot(),
  );
}
