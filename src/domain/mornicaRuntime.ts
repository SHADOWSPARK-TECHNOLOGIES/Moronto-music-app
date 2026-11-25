import { useSyncExternalStore } from 'react';
import { CatalogDownloadManager } from './catalogDownloadManager';
import { LocalStorageDownloadStore } from './downloadStore';
import { LicensedCatalogConfig } from './licensedCatalogRepository';
import { LocalMediaRepository } from './localMediaRepository';
import { WebCacheOfflineReadinessVerifier } from './offlineReadinessVerifier';
import {
  type MornicaUiState,
  PlaybackRouter,
  WebAudioController,
} from './playbackRouter';
import { YouTubeDiscoveryRepository } from './youtubeDiscoveryRepository';

class MornicaRuntimeContainer {
  readonly localRepo = new LocalMediaRepository();
  readonly youtubeRepo = new YouTubeDiscoveryRepository();
  readonly router = new PlaybackRouter({
    youtubeRepository: this.youtubeRepo,
    localMediaRepository: this.localRepo,
  });
  readonly audioController = new WebAudioController();
  readonly downloadStore = new LocalStorageDownloadStore();
  readonly offlineVerifier = new WebCacheOfflineReadinessVerifier();
  readonly downloadManager: CatalogDownloadManager;

  constructor() {
    this.downloadManager = new CatalogDownloadManager(
      LicensedCatalogConfig.demoRights(),
      () => 'NG',
      (trackId, state) => {
        this.router.onDownloadStateChanged(trackId, state);
      },
    );

    this.router.attachAudioController(this.audioController);
    this.router.attachDownloadManager(this.downloadManager);
    this.router.attachDownloadController(this.downloadManager);
    this.router.attachDownloadStore(this.downloadStore);

    // Hydrate genuine Cache API offline bytes asynchronously on startup
    this.offlineVerifier
      .hydrateFromCache(this.router.state.catalogTracks)
      .then(() => {
        this.router.attachOfflineReadiness(this.offlineVerifier);
      })
      .catch(() => {
        this.router.attachOfflineReadiness(this.offlineVerifier);
      });
  }
}

export const mornicaRuntime = new MornicaRuntimeContainer();

export function useMornicaState(): MornicaUiState {
  return useSyncExternalStore(
    (onStoreChange) => mornicaRuntime.router.subscribe(onStoreChange),
    () => mornicaRuntime.router.state,
  );
}
