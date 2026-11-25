import {
  ActiveEngine,
  type ArtistLaunchRequest,
  type CreatorProfile,
  LibraryTab,
  type LocalMediaTrack,
  PrimaryScreen,
  PROVENANCE_META,
  RepeatMode,
  TrackSortOption,
  type VidIqSearchEstimate,
  type YouTubeVideoCard,
  isTrackPlayableOffline,
  isYouTubeUri,
  validateArtistLaunchRequest,
} from '../model/mornicaModels';
import {
  type CatalogDownloadController,
  type CatalogDownloadManager,
  CatalogDownloadState,
} from './catalogDownloadManager';
import {
  type DownloadStore,
  createDownloadRecordFromTrack,
  mayPlayOfflineSnapshot,
} from './downloadStore';
import {
  LicensedCatalogConfig,
  LicensedCatalogRepository,
} from './licensedCatalogRepository';
import {
  CatalogAccess,
  CatalogDenial,
  type LicensedCatalogRights,
  type LicensedCatalogTrack,
} from './licensedCatalogRights';
import { LocalMediaRepository } from './localMediaRepository';
import type { OfflineReadinessVerifier } from './offlineReadinessVerifier';
import { YouTubeDiscoveryRepository } from './youtubeDiscoveryRepository';

export interface MornicaUiState {
  currentScreen: PrimaryScreen;
  activeEngine: ActiveEngine;
  // Domain A: YouTube Discover & Watch
  searchQuery: string;
  risingCreatorsOnly: boolean;
  researchEstimates: VidIqSearchEstimate[];
  youtubeVideos: YouTubeVideoCard[];
  externalYouTubeSearchUrl: string;
  activeYouTubeVideo: YouTubeVideoCard | null;
  isYouTubeEmbedPlaying: boolean;
  isYouTubeViewportVisible: boolean;
  // Domain B: Musicolet-style Local Audio
  hasAudioPermission: boolean;
  selectedLibraryTab: LibraryTab;
  localSearchQuery: string;
  trackSortOption: TrackSortOption;
  isNowPlayingExpanded: boolean;
  localTracks: LocalMediaTrack[];
  queueTrackIds: string[];
  activeLocalTrack: LocalMediaTrack | null;
  isLocalPlaying: boolean;
  localPositionMs: number;
  localDurationMs: number;
  shuffleEnabled: boolean;
  repeatMode: RepeatMode;
  playbackError: string | null;
  favoriteTrackIds: string[];
  playlists: Record<string, string[]>;
  // Domain D: Licensed streaming catalogue
  catalogTracks: LicensedCatalogTrack[];
  activeCatalogTrack: LicensedCatalogTrack | null;
  catalogDownloadStates: Record<string, CatalogDownloadState>;
  preferWifiOnlyDownloads: boolean;
  // Domain C: Creator Launch Hub
  creatorProfiles: CreatorProfile[];
  submittedLaunchRequests: ArtistLaunchRequest[];
  statusBannerMessage: string | null;
}

export interface LocalAudioController {
  bindRouter(router: PlaybackRouter): void;
  playUri(uri: string, metadata: { title: string; artist: string; album?: string }): void;
  setPlayWhenReady(playWhenReady: BooleanLike): void;
  seekTo(positionMs: number): void;
  stop(): void;
}

type BooleanLike = boolean;

const RESTART_TRACK_THRESHOLD_MS = 3_000;

export class PlaybackRouter {
  private readonly youtubeRepository: YouTubeDiscoveryRepository;
  private readonly localMediaRepository: LocalMediaRepository;
  private readonly licensedCatalogRepository: LicensedCatalogRepository;
  private readonly catalogRights: LicensedCatalogRights;
  private readonly territoryProvider: () => string;
  private catalogDownloadManager: CatalogDownloadManager | null = null;
  private downloadStore: DownloadStore | null = null;
  private offlineReadiness: OfflineReadinessVerifier | null = null;
  private catalogDownloadController: CatalogDownloadController | null = null;
  private localAudioController: LocalAudioController | null = null;

  private _state: MornicaUiState;
  private readonly listeners = new Set<(state: MornicaUiState) => void>();

  constructor(options?: {
    youtubeRepository?: YouTubeDiscoveryRepository;
    localMediaRepository?: LocalMediaRepository;
    licensedCatalogRepository?: LicensedCatalogRepository;
    catalogRights?: LicensedCatalogRights;
    territoryProvider?: () => string;
  }) {
    this.youtubeRepository =
      options?.youtubeRepository ?? new YouTubeDiscoveryRepository();
    this.localMediaRepository =
      options?.localMediaRepository ?? new LocalMediaRepository();
    this.licensedCatalogRepository =
      options?.licensedCatalogRepository ?? new LicensedCatalogRepository();
    this.catalogRights =
      options?.catalogRights ?? LicensedCatalogConfig.demoRights();
    this.territoryProvider = options?.territoryProvider ?? (() => 'NG');

    const initialVideos = this.youtubeRepository.searchOfficialVideos(
      'Asake',
      false,
    );
    const initialTracks =
      this.localMediaRepository.defaultDemoAndCatalogTracks();
    const initialCatalog = this.licensedCatalogRepository.demoCatalog();
    const playableIds = initialTracks
      .filter(isTrackPlayableOffline)
      .map((t) => t.id);

    this._state = {
      currentScreen: PrimaryScreen.MY_MUSIC,
      activeEngine: ActiveEngine.NONE,
      searchQuery: 'Asake',
      risingCreatorsOnly: false,
      researchEstimates: this.youtubeRepository.getResearchEstimates(),
      youtubeVideos: initialVideos.items,
      externalYouTubeSearchUrl:
        this.youtubeRepository.buildExternalSearchUrl('Asake'),
      activeYouTubeVideo: null,
      isYouTubeEmbedPlaying: false,
      isYouTubeViewportVisible: false,
      hasAudioPermission: false,
      selectedLibraryTab: LibraryTab.SONGS,
      localSearchQuery: '',
      trackSortOption: TrackSortOption.TITLE,
      isNowPlayingExpanded: false,
      localTracks: initialTracks,
      queueTrackIds: playableIds,
      activeLocalTrack: null,
      isLocalPlaying: false,
      localPositionMs: 0,
      localDurationMs: 0,
      shuffleEnabled: false,
      repeatMode: RepeatMode.OFF,
      playbackError: null,
      favoriteTrackIds: [],
      playlists: {
        'Afrobeats Offline Queue': ['demo-lagos-pulse', 'demo-lekki-logdrum'],
      },
      catalogTracks: initialCatalog,
      activeCatalogTrack: null,
      catalogDownloadStates: {},
      preferWifiOnlyDownloads: false,
      creatorProfiles: this.localMediaRepository.verifiedCreatorProfiles(),
      submittedLaunchRequests: [],
      statusBannerMessage: null,
    };
  }

  get state(): MornicaUiState {
    return this._state;
  }

  subscribe(listener: (state: MornicaUiState) => void): () => void {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  private updateState(updater: (prev: MornicaUiState) => MornicaUiState): void {
    this._state = updater(this._state);
    for (const listener of this.listeners) {
      listener(this._state);
    }
  }

  attachAudioController(controller: LocalAudioController | null): void {
    this.localAudioController = controller;
    controller?.bindRouter(this);
  }

  attachDownloadManager(manager: CatalogDownloadManager | null): void {
    this.catalogDownloadManager = manager;
  }

  attachDownloadController(controller: CatalogDownloadController | null): void {
    this.catalogDownloadController = controller;
    controller?.setWifiOnlyDownload(this._state.preferWifiOnlyDownloads);
  }

  attachDownloadStore(store: DownloadStore | null): void {
    this.downloadStore = store;
    if (!store) return;
    const existing = store.all();
    if (existing.length === 0) return;

    this.updateState((current) => {
      const nextStates = { ...current.catalogDownloadStates };
      for (const record of existing) {
        const track = current.catalogTracks.find(
          (t) => t.id === record.trackId,
        );
        const snapshotOk = mayPlayOfflineSnapshot(
          record,
          this.territoryProvider(),
          Date.now(),
        );
        const bytesOk =
          !track ||
          (this.offlineReadiness
            ? this.offlineReadiness.isGenuinelyReadyForOffline(track)
            : true);
        if (snapshotOk && bytesOk) {
          nextStates[record.trackId] = CatalogDownloadState.COMPLETED;
        }
      }
      return { ...current, catalogDownloadStates: nextStates };
    });
  }

  attachOfflineReadiness(verifier: OfflineReadinessVerifier | null): void {
    this.offlineReadiness = verifier;
    if (!verifier) return;
    this.updateState((current) => {
      const nextStates = { ...current.catalogDownloadStates };
      for (const track of current.catalogTracks) {
        const snapshot = this.downloadStore?.get(track.id);
        const snapshotOk =
          snapshot !== undefined &&
          mayPlayOfflineSnapshot(snapshot, this.territoryProvider(), Date.now());
        if (verifier.isGenuinelyReadyForOffline(track) && snapshotOk) {
          nextStates[track.id] = CatalogDownloadState.COMPLETED;
        } else if (nextStates[track.id] === CatalogDownloadState.COMPLETED) {
          delete nextStates[track.id];
        }
      }
      return { ...current, catalogDownloadStates: nextStates };
    });
  }

  selectScreen(screen: PrimaryScreen): void {
    this.updateState((current) => ({
      ...current,
      currentScreen: screen,
      activeYouTubeVideo: null,
      isYouTubeEmbedPlaying: false,
      isYouTubeViewportVisible: false,
    }));
  }

  dismissStatusBanner(): void {
    this.updateState((current) => ({ ...current, statusBannerMessage: null }));
  }

  async updateSearchQuery(
    query: string,
    risingOnly: boolean = this._state.risingCreatorsOnly,
  ): Promise<void> {
    const syncResult = this.youtubeRepository.searchOfficialVideos(
      query,
      risingOnly,
    );
    const searchUrl = this.youtubeRepository.buildExternalSearchUrl(query);
    this.updateState((current) => ({
      ...current,
      searchQuery: query,
      risingCreatorsOnly: risingOnly,
      youtubeVideos: syncResult.items,
      externalYouTubeSearchUrl: searchUrl,
    }));

    if (!risingOnly) {
      const liveResult =
        await this.youtubeRepository.searchOfficialVideosLive(query, false);
      this.updateState((current) => {
        if (
          current.searchQuery !== query ||
          current.risingCreatorsOnly !== risingOnly
        ) {
          return current;
        }
        return {
          ...current,
          youtubeVideos: liveResult.items,
        };
      });
    }
  }

  startYouTubeEmbed(_video: YouTubeVideoCard): boolean {
    this.updateState((current) => ({
      ...current,
      activeYouTubeVideo: null,
      isYouTubeEmbedPlaying: false,
      isYouTubeViewportVisible: false,
      statusBannerMessage:
        'This video opens on YouTube. Mornica does not embed YouTube playback.',
    }));
    return false;
  }

  playLocalTrack(track: LocalMediaTrack): boolean {
    if (isYouTubeUri(track.uri)) {
      this.updateState((current) => ({
        ...current,
        statusBannerMessage:
          'Blocked: YouTube streams cannot be loaded into the offline local audio engine.',
      }));
      return false;
    }
    if (!isTrackPlayableOffline(track)) {
      this.updateState((current) => ({
        ...current,
        statusBannerMessage: `Track unavailable: '${track.title}' awaits rights clearance and cannot be played.`,
      }));
      return false;
    }

    const orderedQueue = this.buildOrderedPlayableQueue(
      this._state.localTracks,
      this._state.shuffleEnabled,
      track,
    );
    const duration = Math.max(track.durationMs, 1_000);

    this.localAudioController?.playUri(track.uri, {
      title: track.title,
      artist: track.artist,
      album: track.album,
    });

    this.updateState((current) => ({
      ...current,
      activeEngine: ActiveEngine.LOCAL_AUDIO,
      activeLocalTrack: track,
      activeCatalogTrack: null,
      queueTrackIds: orderedQueue.map((item) => item.id),
      isLocalPlaying: true,
      localPositionMs: 0,
      localDurationMs: duration,
      playbackError: null,
      isYouTubeEmbedPlaying: false,
      activeYouTubeVideo: null,
      statusBannerMessage: `Playing offline audio: ${track.title} (${PROVENANCE_META[track.provenance].label})`,
    }));
    return true;
  }

  /**
   * Domain D: Streams a licensed catalog track through the shared audio engine.
   * Fail-closed rights gate runs FIRST. NEVER sets `isLocalPlaying` optimistically:
   * it flips true only from the audio controller's `playing` callback.
   */
  playCatalogTrack(track: LicensedCatalogTrack): boolean {
    const territory = this.territoryProvider();
    const decision = this.catalogRights.evaluate(
      track,
      CatalogAccess.STREAM,
      territory,
    );
    if (!decision.allowed) {
      this.updateState((current) => ({
        ...current,
        statusBannerMessage: this.catalogDenialMessage(
          decision.denial,
          track.title,
        ),
      }));
      return false;
    }

    if (isYouTubeUri(track.mediaUrl)) {
      this.updateState((current) => ({
        ...current,
        statusBannerMessage:
          'Blocked: YouTube URIs are never permitted in the audio player.',
      }));
      return false;
    }

    this.localAudioController?.playUri(track.mediaUrl, {
      title: track.title,
      artist: track.artist,
      album: track.sourceLabel,
    });

    this.updateState((current) => ({
      ...current,
      activeEngine: ActiveEngine.LICENSED_CATALOG,
      activeCatalogTrack: track,
      activeLocalTrack: null,
      isLocalPlaying: false, // Never optimistic; flips on audio 'playing' event
      localPositionMs: 0,
      localDurationMs: 0,
      playbackError: null,
      isYouTubeEmbedPlaying: false,
      activeYouTubeVideo: null,
      statusBannerMessage: `Streaming licensed demo: ${track.title} · ${track.sourceLabel}`,
    }));
    return true;
  }

  async playDownloadedCatalogTrack(
    track: LicensedCatalogTrack,
  ): Promise<boolean> {
    if (!this.offlinePlaybackAllowed(track)) {
      this.updateState((current) => ({
        ...current,
        statusBannerMessage: `Not ready offline: '${track.title}' bytes are missing or rights expired.`,
      }));
      return false;
    }

    const territory = this.territoryProvider();
    const decision = this.catalogRights.evaluate(
      track,
      CatalogAccess.PLAY_DOWNLOADED,
      territory,
      true,
    );
    if (!decision.allowed) {
      this.updateState((current) => ({
        ...current,
        statusBannerMessage: this.catalogDenialMessage(
          decision.denial,
          track.title,
        ),
      }));
      return false;
    }

    const cachedUrl =
      await this.catalogDownloadManager?.resolveOfflinePlayableUrl(track.id);
    if (!cachedUrl) {
      this.offlineReadiness?.markVerified(track.id, false);
      this.updateState((current) => {
        const nextStates = { ...current.catalogDownloadStates };
        delete nextStates[track.id];
        return {
          ...current,
          catalogDownloadStates: nextStates,
          statusBannerMessage: `Not ready offline: '${track.title}' cached bytes could not be loaded.`,
        };
      });
      return false;
    }

    this.localAudioController?.playUri(cachedUrl, {
      title: track.title,
      artist: track.artist,
      album: track.sourceLabel,
    });

    this.updateState((current) => ({
      ...current,
      activeEngine: ActiveEngine.LICENSED_CATALOG,
      activeCatalogTrack: track,
      activeLocalTrack: null,
      isLocalPlaying: false, // Never optimistic; flips on audio 'playing' event
      localPositionMs: 0,
      localDurationMs: 0,
      playbackError: null,
      isYouTubeEmbedPlaying: false,
      activeYouTubeVideo: null,
      statusBannerMessage: `Playing offline: ${track.title} · ${track.sourceLabel}`,
    }));
    return true;
  }

  requestCatalogDownload(track: LicensedCatalogTrack): boolean {
    const manager = this.catalogDownloadManager;
    if (!manager) {
      this.updateState((current) => ({
        ...current,
        statusBannerMessage: 'Offline downloads are not enabled yet.',
      }));
      return false;
    }

    const decision = manager.enqueue(track);
    if (!decision.allowed) {
      this.updateState((current) => ({
        ...current,
        statusBannerMessage: this.catalogDenialMessage(
          decision.denial,
          track.title,
        ),
      }));
      return false;
    }

    this.updateState((current) => ({
      ...current,
      catalogDownloadStates: {
        ...current.catalogDownloadStates,
        [track.id]:
          current.catalogDownloadStates[track.id] ??
          CatalogDownloadState.QUEUED,
      },
      statusBannerMessage: `Download queued for offline: ${track.title} · ${track.sourceLabel}`,
    }));
    return true;
  }

  offlinePlaybackAllowed(track: LicensedCatalogTrack): boolean {
    if (
      this._state.catalogDownloadStates[track.id] !==
      CatalogDownloadState.COMPLETED
    ) {
      return false;
    }
    const record = this.downloadStore?.get(track.id);
    if (
      !record ||
      !mayPlayOfflineSnapshot(record, this.territoryProvider(), Date.now())
    ) {
      return false;
    }
    if (!this.offlineReadiness) return true;
    return this.offlineReadiness.isGenuinelyReadyForOffline(track);
  }

  onDownloadStateChanged(trackId: string, state: CatalogDownloadState): void {
    if (state === CatalogDownloadState.COMPLETED) {
      const track = this._state.catalogTracks.find((t) => t.id === trackId);
      if (!track) return;
      this.offlineReadiness?.markVerified(trackId, true);
      this.downloadStore?.save(
        createDownloadRecordFromTrack(
          track,
          this.territoryProvider(),
          Date.now(),
        ),
      );
    } else if (state === CatalogDownloadState.FAILED) {
      this.offlineReadiness?.markVerified(trackId, false);
      this.downloadStore?.remove(trackId);
    }

    this.updateState((current) => ({
      ...current,
      catalogDownloadStates: {
        ...current.catalogDownloadStates,
        [trackId]: state,
      },
      statusBannerMessage:
        state === CatalogDownloadState.COMPLETED
          ? 'Track saved · ready offline.'
          : state === CatalogDownloadState.FAILED
            ? 'Download failed. Check your connection and tap Retry.'
            : current.statusBannerMessage,
    }));
  }

  setWifiOnlyDownloads(enabled: boolean): void {
    this.catalogDownloadController?.setWifiOnlyDownload(enabled);
    this.updateState((current) => ({
      ...current,
      preferWifiOnlyDownloads: enabled,
      statusBannerMessage: enabled
        ? "Downloads now Wi-Fi-only. Your data won't waste."
        : 'Downloads can use mobile data now.',
    }));
  }

  cancelCatalogDownload(trackId: string): boolean {
    const track = this._state.catalogTracks.find((t) => t.id === trackId);
    if (!track) return false;
    const handled =
      this.catalogDownloadController?.cancelDownload(`catalog:${track.id}`) ??
      false;
    if (handled) {
      this.offlineReadiness?.markVerified(trackId, false);
      this.downloadStore?.remove(trackId);
      this.updateState((current) => {
        const nextStates = { ...current.catalogDownloadStates };
        delete nextStates[trackId];
        return {
          ...current,
          catalogDownloadStates: nextStates,
          statusBannerMessage: `Download cancelled for ${track.title}.`,
        };
      });
    }
    return handled;
  }

  toggleLocalPlayPause(): void {
    const current = this._state;
    const catalogActive = current.activeCatalogTrack !== null;
    const targetTrack = !catalogActive
      ? current.activeLocalTrack ??
        current.localTracks.find(isTrackPlayableOffline) ??
        null
      : current.activeLocalTrack;

    if (!catalogActive && !targetTrack) return;
    if (!catalogActive && !current.activeLocalTrack && targetTrack) {
      this.playLocalTrack(targetTrack);
      return;
    }

    const nextPlaying = !current.isLocalPlaying;
    this.localAudioController?.setPlayWhenReady(nextPlaying);

    if (!catalogActive) {
      this.updateState((prev) => ({
        ...prev,
        activeEngine: nextPlaying ? ActiveEngine.LOCAL_AUDIO : prev.activeEngine,
        isLocalPlaying: nextPlaying,
        playbackError: nextPlaying ? null : prev.playbackError,
      }));
    }
  }

  seekLocalTo(positionMs: number): void {
    const current = this._state;
    const maxDur = Math.max(
      current.localDurationMs > 0
        ? current.localDurationMs
        : current.activeLocalTrack?.durationMs ?? 30_000,
      1_000,
    );
    const clamped = Math.max(0, Math.min(positionMs, maxDur));
    this.localAudioController?.seekTo(clamped);
    this.updateState((prev) => ({
      ...prev,
      localPositionMs: clamped,
      localDurationMs: maxDur,
    }));
  }

  skipNext(): void {
    this.advanceToNextTrack(false);
  }

  onTrackEnded(): void {
    this.advanceToNextTrack(true);
  }

  private advanceToNextTrack(isAutoTrackEnd: boolean): void {
    const current = this._state;
    const playable = this.resolveActivePlayableQueue(current);
    if (playable.length === 0) return;

    const activeTrack = current.activeLocalTrack;
    if (current.repeatMode === RepeatMode.ONE && activeTrack) {
      this.localAudioController?.seekTo(0);
      this.localAudioController?.setPlayWhenReady(true);
      this.updateState((prev) => ({
        ...prev,
        isLocalPlaying: true,
        localPositionMs: 0,
        playbackError: null,
        statusBannerMessage: `Repeating track: ${activeTrack.title}`,
      }));
      return;
    }

    const currentIndex = playable.findIndex((t) => t.id === activeTrack?.id);
    if (currentIndex < 0) {
      this.playLocalTrack(playable[0]);
    } else if (currentIndex + 1 < playable.length) {
      this.playLocalTrack(playable[currentIndex + 1]);
    } else if (current.repeatMode === RepeatMode.ALL) {
      this.playLocalTrack(playable[0]);
    } else {
      this.localAudioController?.setPlayWhenReady(false);
      this.localAudioController?.seekTo(0);
      this.updateState((prev) => ({
        ...prev,
        isLocalPlaying: false,
        localPositionMs: 0,
        statusBannerMessage: isAutoTrackEnd
          ? 'Playback finished at end of queue.'
          : 'Reached end of offline queue (Repeat OFF).',
      }));
    }
  }

  skipPrevious(): void {
    const current = this._state;
    const playable = this.resolveActivePlayableQueue(current);
    if (playable.length === 0) return;

    if (
      current.activeLocalTrack &&
      current.localPositionMs > RESTART_TRACK_THRESHOLD_MS
    ) {
      this.seekLocalTo(0);
      return;
    }

    const currentIndex = playable.findIndex(
      (t) => t.id === current.activeLocalTrack?.id,
    );
    const prevTrack =
      currentIndex > 0
        ? playable[currentIndex - 1]
        : current.repeatMode === RepeatMode.ALL
          ? playable[playable.length - 1]
          : playable[0];
    this.playLocalTrack(prevTrack);
  }

  toggleShuffle(): void {
    const current = this._state;
    const nextShuffle = !current.shuffleEnabled;
    const ordered = this.buildOrderedPlayableQueue(
      current.localTracks,
      nextShuffle,
      current.activeLocalTrack,
    );
    this.updateState((prev) => ({
      ...prev,
      shuffleEnabled: nextShuffle,
      queueTrackIds: ordered.map((t) => t.id),
      statusBannerMessage: nextShuffle
        ? 'Shuffle enabled.'
        : 'Shuffle disabled.',
    }));
  }

  cycleRepeatMode(): void {
    const next =
      this._state.repeatMode === RepeatMode.OFF
        ? RepeatMode.ALL
        : this._state.repeatMode === RepeatMode.ALL
          ? RepeatMode.ONE
          : RepeatMode.OFF;
    this.updateState((prev) => ({ ...prev, repeatMode: next }));
  }

  onPlaybackPositionUpdated(
    positionMs: number,
    durationMs: number = this._state.localDurationMs,
  ): void {
    this.updateState((current) => {
      const effectiveDuration = Math.max(
        durationMs > 0
          ? durationMs
          : current.localDurationMs > 0
            ? current.localDurationMs
            : current.activeLocalTrack?.durationMs ?? 30_000,
        1_000,
      );
      return {
        ...current,
        localPositionMs: Math.max(0, Math.min(positionMs, effectiveDuration)),
        localDurationMs: effectiveDuration,
      };
    });
  }

  onPlayerIsPlayingChanged(isPlaying: boolean): void {
    this.updateState((current) => {
      if (!current.activeLocalTrack && !current.activeCatalogTrack) {
        return current;
      }
      const engine = current.activeCatalogTrack
        ? ActiveEngine.LICENSED_CATALOG
        : ActiveEngine.LOCAL_AUDIO;
      return {
        ...current,
        activeEngine: isPlaying ? engine : current.activeEngine,
        isLocalPlaying: isPlaying,
        isYouTubeEmbedPlaying: isPlaying ? false : current.isYouTubeEmbedPlaying,
      };
    });
  }

  onPlayerError(errorMessage: string): void {
    this.localAudioController?.stop();
    this.updateState((prev) => ({
      ...prev,
      isLocalPlaying: false,
      playbackError: errorMessage,
      statusBannerMessage: `Playback error: ${errorMessage}`,
    }));
  }

  selectLibraryTab(tab: LibraryTab): void {
    this.updateState((prev) => ({ ...prev, selectedLibraryTab: tab }));
  }

  updateLocalSearchQuery(query: string): void {
    this.updateState((prev) => ({ ...prev, localSearchQuery: query }));
  }

  selectTrackSortOption(sortOption: TrackSortOption): void {
    this.updateState((prev) => ({ ...prev, trackSortOption: sortOption }));
  }

  setNowPlayingExpanded(expanded: boolean): void {
    this.updateState((prev) => ({ ...prev, isNowPlayingExpanded: expanded }));
  }

  filteredAndSortedLocalTracks(): LocalMediaTrack[] {
    const current = this._state;
    const q = current.localSearchQuery.trim().toLowerCase();
    const filtered = !q
      ? current.localTracks
      : current.localTracks.filter(
          (track) =>
            track.title.toLowerCase().includes(q) ||
            track.artist.toLowerCase().includes(q) ||
            track.album.toLowerCase().includes(q) ||
            track.folderName.toLowerCase().includes(q),
        );

    const copy = [...filtered];
    switch (current.trackSortOption) {
      case TrackSortOption.TITLE:
        return copy.sort((a, b) =>
          a.title.toLowerCase().localeCompare(b.title.toLowerCase()),
        );
      case TrackSortOption.ARTIST:
        return copy.sort(
          (a, b) =>
            a.artist.toLowerCase().localeCompare(b.artist.toLowerCase()) ||
            a.title.toLowerCase().localeCompare(b.title.toLowerCase()),
        );
      case TrackSortOption.DURATION:
        return copy.sort((a, b) => b.durationMs - a.durationMs);
      case TrackSortOption.YEAR:
        return copy.sort(
          (a, b) =>
            b.releaseYear - a.releaseYear ||
            a.title.toLowerCase().localeCompare(b.title.toLowerCase()),
        );
    }
  }

  updateDeviceAudioTracks(
    hasPermission: boolean,
    tracks: LocalMediaTrack[],
  ): void {
    const resolvedTracks =
      tracks.length > 0
        ? tracks
        : this.localMediaRepository.defaultDemoAndCatalogTracks();
    const ordered = this.buildOrderedPlayableQueue(
      resolvedTracks,
      this._state.shuffleEnabled,
      this._state.activeLocalTrack,
    );
    const deviceCount = resolvedTracks.filter(
      (t) => t.provenance === 'LOCAL_DEVICE',
    ).length;

    this.updateState((prev) => ({
      ...prev,
      hasAudioPermission: hasPermission,
      localTracks: resolvedTracks,
      queueTrackIds: ordered.map((t) => t.id),
      statusBannerMessage:
        deviceCount > 0
          ? `Scanned ${deviceCount} local audio file${deviceCount === 1 ? '' : 's'} on your device.`
          : prev.statusBannerMessage,
    }));
  }

  toggleFavorite(trackId: string): void {
    this.updateState((current) => {
      const exists = current.favoriteTrackIds.includes(trackId);
      const updated = exists
        ? current.favoriteTrackIds.filter((id) => id !== trackId)
        : [...current.favoriteTrackIds, trackId];
      return { ...current, favoriteTrackIds: updated };
    });
  }

  addToPlaylist(playlistName: string, trackId: string): boolean {
    const track = this._state.localTracks.find((t) => t.id === trackId);
    if (!track || !isTrackPlayableOffline(track)) return false;
    this.updateState((current) => {
      const existing = current.playlists[playlistName] ?? [];
      const updatedList = existing.includes(trackId)
        ? existing
        : [...existing, trackId];
      return {
        ...current,
        playlists: { ...current.playlists, [playlistName]: updatedList },
        statusBannerMessage: `Added '${track.title}' to queue '${playlistName}'.`,
      };
    });
    return true;
  }

  submitArtistLaunchRequest(request: ArtistLaunchRequest): boolean {
    const validation = validateArtistLaunchRequest(request);
    if (!validation.valid) {
      this.updateState((prev) => ({
        ...prev,
        statusBannerMessage: validation.errorMessage,
      }));
      return false;
    }
    const recorded: ArtistLaunchRequest = {
      ...request,
      submittedAtIso: new Date().toISOString(),
    };
    this.updateState((current) => ({
      ...current,
      submittedLaunchRequests: [...current.submittedLaunchRequests, recorded],
      statusBannerMessage: `Launch package inquiry recorded for ${request.artistName} (${request.trackTitle}).`,
    }));
    return true;
  }

  private catalogDenialMessage(denial: CatalogDenial, title: string): string {
    switch (denial) {
      case CatalogDenial.NONE:
        return `Streaming ${title}.`;
      case CatalogDenial.INVALID_SOURCE:
        return `Blocked: '${title}' uses an unapproved or unrecognized media source and cannot stream.`;
      case CatalogDenial.MISSING_LICENSE:
        return `Unavailable: '${title}' has incomplete license metadata and cannot stream.`;
      case CatalogDenial.UNVERIFIED_RIGHTS:
        return `Unavailable: rights for '${title}' have not been verified and cannot stream.`;
      case CatalogDenial.TERRITORY_UNAVAILABLE:
        return `Not available in your region: '${title}' is restricted to specific territories.`;
      case CatalogDenial.LICENSE_EXPIRED:
        return `Expired: the streaming license for '${title}' is no longer valid.`;
      case CatalogDenial.STREAM_NOT_AUTHORIZED:
        return `Not authorized: '${title}' has no streaming entitlement.`;
      case CatalogDenial.OFFLINE_NOT_AUTHORIZED:
        return `Not authorized: offline playback of '${title}' is not permitted.`;
      case CatalogDenial.DOWNLOAD_INCOMPLETE:
        return `Not ready: '${title}' has finished streaming but no offline download is available.`;
    }
  }

  private resolveActivePlayableQueue(
    current: MornicaUiState,
  ): LocalMediaTrack[] {
    const playableById = new Map(
      current.localTracks
        .filter(isTrackPlayableOffline)
        .map((t) => [t.id, t]),
    );
    const fromQueueIds = current.queueTrackIds
      .map((id) => playableById.get(id))
      .filter((t): t is LocalMediaTrack => t !== undefined);
    if (fromQueueIds.length === playableById.size && fromQueueIds.length > 0) {
      return fromQueueIds;
    }
    return this.buildOrderedPlayableQueue(
      current.localTracks,
      current.shuffleEnabled,
      current.activeLocalTrack,
    );
  }

  private buildOrderedPlayableQueue(
    tracks: LocalMediaTrack[],
    shuffleEnabled: boolean,
    anchorTrack: LocalMediaTrack | null,
  ): LocalMediaTrack[] {
    const playable = tracks.filter(isTrackPlayableOffline);
    if (!shuffleEnabled || playable.length <= 1) return playable;
    const remaining = playable
      .filter((t) => t.id !== anchorTrack?.id)
      .reverse();
    if (anchorTrack && playable.some((t) => t.id === anchorTrack.id)) {
      return [anchorTrack, ...remaining];
    }
    return [...playable].reverse();
  }
}

/**
 * Browser HTML5 Audio + MediaSession controller.
 * Defense in depth: rejects any YouTube URI before touching <audio>.
 */
export class WebAudioController implements LocalAudioController {
  private readonly audio: HTMLAudioElement;
  private boundRouter: PlaybackRouter | null = null;

  constructor() {
    this.audio = new Audio();
    this.audio.preload = 'metadata';

    this.audio.addEventListener('playing', () => {
      this.boundRouter?.onPlayerIsPlayingChanged(true);
    });
    this.audio.addEventListener('pause', () => {
      this.boundRouter?.onPlayerIsPlayingChanged(false);
    });
    this.audio.addEventListener('ended', () => {
      this.boundRouter?.onTrackEnded();
    });
    this.audio.addEventListener('timeupdate', () => {
      const posMs = Math.round(this.audio.currentTime * 1000);
      const durMs = Number.isFinite(this.audio.duration)
        ? Math.round(this.audio.duration * 1000)
        : 0;
      this.boundRouter?.onPlaybackPositionUpdated(posMs, durMs);
    });
    this.audio.addEventListener('loadedmetadata', () => {
      const durMs = Number.isFinite(this.audio.duration)
        ? Math.round(this.audio.duration * 1000)
        : 0;
      if (durMs > 0) {
        this.boundRouter?.onPlaybackPositionUpdated(0, durMs);
      }
    });
    this.audio.addEventListener('error', () => {
      const code = this.audio.error?.code;
      const msg =
        code === MediaError.MEDIA_ERR_NETWORK
          ? 'Network error while streaming audio.'
          : 'Unable to play this audio source.';
      this.boundRouter?.onPlayerError(msg);
    });
  }

  bindRouter(router: PlaybackRouter): void {
    this.boundRouter = router;
    if ('mediaSession' in navigator) {
      navigator.mediaSession.setActionHandler('play', () => {
        this.setPlayWhenReady(true);
      });
      navigator.mediaSession.setActionHandler('pause', () => {
        this.setPlayWhenReady(false);
      });
      navigator.mediaSession.setActionHandler('previoustrack', () => {
        this.boundRouter?.skipPrevious();
      });
      navigator.mediaSession.setActionHandler('nexttrack', () => {
        this.boundRouter?.skipNext();
      });
      navigator.mediaSession.setActionHandler('seekto', (details) => {
        if (typeof details.seekTime === 'number') {
          this.boundRouter?.seekLocalTo(Math.round(details.seekTime * 1000));
        }
      });
    }
  }

  playUri(
    uri: string,
    metadata: { title: string; artist: string; album?: string },
  ): void {
    if (isYouTubeUri(uri)) {
      this.stop();
      this.boundRouter?.onPlayerError(
        'Blocked YouTube URI attempted to reach the audio player.',
      );
      return;
    }

    this.audio.src = uri;
    this.audio.currentTime = 0;

    if ('mediaSession' in navigator && 'MediaMetadata' in window) {
      navigator.mediaSession.metadata = new MediaMetadata({
        title: metadata.title,
        artist: metadata.artist,
        album: metadata.album ?? 'Moronto Mornica',
      });
    }

    this.audio.play().catch((err: unknown) => {
      const msg =
        err instanceof Error ? err.message : 'Playback blocked by browser.';
      this.boundRouter?.onPlayerError(msg);
    });
  }

  setPlayWhenReady(playWhenReady: boolean): void {
    if (playWhenReady) {
      this.audio.play().catch((err: unknown) => {
        const msg =
          err instanceof Error ? err.message : 'Playback blocked by browser.';
        this.boundRouter?.onPlayerError(msg);
      });
    } else {
      this.audio.pause();
    }
  }

  seekTo(positionMs: number): void {
    this.audio.currentTime = Math.max(0, positionMs / 1000);
  }

  stop(): void {
    this.audio.pause();
    this.audio.removeAttribute('src');
    this.audio.load();
  }
}
