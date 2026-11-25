import {
  CatalogAccess,
  type CatalogAccessDecision,
  type LicensedCatalogRights,
  type LicensedCatalogTrack,
} from './licensedCatalogRights';

export const CatalogDownloadState = {
  NONE: 'NONE',
  QUEUED: 'QUEUED',
  DOWNLOADING: 'DOWNLOADING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
} as const;
export type CatalogDownloadState =
  (typeof CatalogDownloadState)[keyof typeof CatalogDownloadState];

export const MORNICA_MEDIA_CACHE_NAME = 'mornica-media-cache-v1';

export function catalogMediaCacheKey(trackId: string): string {
  return `/api/catalog/media?id=${encodeURIComponent(trackId)}`;
}

export function catalogTrackToMediaId(track: LicensedCatalogTrack): string {
  return `catalog:${track.id}`;
}

export interface CatalogDownloadController {
  cancelDownload(mediaId: string): boolean;
  setWifiOnlyDownload(enabled: boolean): void;
}

/**
 * Loop 3 & 4: Gate-enforced offline download manager backed by the browser Cache API.
 * Enforces `LicensedCatalogRights.evaluate(SAVE_OFFLINE)` BEFORE any network fetch.
 */
export class CatalogDownloadManager implements CatalogDownloadController {
  private readonly rights: LicensedCatalogRights;
  private readonly territoryProvider: () => string;
  private readonly onStateChange: (
    trackId: string,
    state: CatalogDownloadState,
    cachedBlobUrl?: string,
  ) => void;
  private readonly activeControllers = new Map<string, AbortController>();
  private readonly activeStates = new Map<string, CatalogDownloadState>();
  private readonly blobUrls = new Map<string, string>();
  private wifiOnly = false;

  constructor(
    rights: LicensedCatalogRights,
    territoryProvider: () => string,
    onStateChange: (
      trackId: string,
      state: CatalogDownloadState,
      cachedBlobUrl?: string,
    ) => void,
  ) {
    this.rights = rights;
    this.territoryProvider = territoryProvider;
    this.onStateChange = onStateChange;
  }

  canDownload(track: LicensedCatalogTrack): CatalogAccessDecision {
    return this.rights.evaluate(
      track,
      CatalogAccess.SAVE_OFFLINE,
      this.territoryProvider(),
    );
  }

  enqueue(track: LicensedCatalogTrack): CatalogAccessDecision {
    const decision = this.canDownload(track);
    if (!decision.allowed) {
      return decision;
    }

    // Respect Wi-Fi-only preference if the browser reports a cellular connection
    const conn = (
      navigator as Navigator & {
        connection?: { type?: string };
      }
    ).connection;
    if (this.wifiOnly && conn?.type === 'cellular') {
      this.activeStates.set(track.id, CatalogDownloadState.QUEUED);
      this.onStateChange(track.id, CatalogDownloadState.QUEUED);
      return decision;
    }

    this.startFetch(track);
    return decision;
  }

  private async startFetch(track: LicensedCatalogTrack): Promise<void> {
    const existing = this.activeControllers.get(track.id);
    if (existing) {
      existing.abort();
    }

    const controller = new AbortController();
    this.activeControllers.set(track.id, controller);
    this.activeStates.set(track.id, CatalogDownloadState.DOWNLOADING);
    this.onStateChange(track.id, CatalogDownloadState.DOWNLOADING);

    try {
      const cacheKey = catalogMediaCacheKey(track.id);
      const response = await fetch(cacheKey, { signal: controller.signal });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const blob = await response.blob();
      if (blob.size <= 0) {
        throw new Error('Empty media payload');
      }

      if ('caches' in window) {
        const cache = await window.caches.open(MORNICA_MEDIA_CACHE_NAME);
        const cacheResponse = new Response(blob, {
          status: 200,
          headers: {
            'Content-Type': blob.type || 'audio/mpeg',
            'Content-Length': String(blob.size),
          },
        });
        await cache.put(cacheKey, cacheResponse);
      }

      const prevUrl = this.blobUrls.get(track.id);
      if (prevUrl) {
        URL.revokeObjectURL(prevUrl);
      }
      const objectUrl = URL.createObjectURL(blob);
      this.blobUrls.set(track.id, objectUrl);

      this.activeControllers.delete(track.id);
      this.activeStates.set(track.id, CatalogDownloadState.COMPLETED);
      this.onStateChange(track.id, CatalogDownloadState.COMPLETED, objectUrl);
    } catch (err) {
      this.activeControllers.delete(track.id);
      if (err instanceof DOMException && err.name === 'AbortError') {
        this.activeStates.delete(track.id);
        return;
      }
      this.activeStates.set(track.id, CatalogDownloadState.FAILED);
      this.onStateChange(track.id, CatalogDownloadState.FAILED);
    }
  }

  getCachedBlobUrl(trackId: string): string | undefined {
    return this.blobUrls.get(trackId);
  }

  async resolveOfflinePlayableUrl(trackId: string): Promise<string | null> {
    const inMem = this.blobUrls.get(trackId);
    if (inMem) return inMem;

    try {
      if (!('caches' in window)) return null;
      const cache = await window.caches.open(MORNICA_MEDIA_CACHE_NAME);
      const match = await cache.match(catalogMediaCacheKey(trackId));
      if (!match) return null;
      const blob = await match.blob();
      if (blob.size <= 0) return null;
      const objectUrl = URL.createObjectURL(blob);
      this.blobUrls.set(trackId, objectUrl);
      return objectUrl;
    } catch {
      return null;
    }
  }

  cancelDownload(mediaId: string): boolean {
    const trackId = mediaId.startsWith('catalog:')
      ? mediaId.slice('catalog:'.length)
      : mediaId;
    const controller = this.activeControllers.get(trackId);
    if (controller) {
      controller.abort();
      this.activeControllers.delete(trackId);
    }
    this.activeStates.delete(trackId);
    const prevUrl = this.blobUrls.get(trackId);
    if (prevUrl) {
      URL.revokeObjectURL(prevUrl);
      this.blobUrls.delete(trackId);
    }
    if ('caches' in window) {
      window.caches
        .open(MORNICA_MEDIA_CACHE_NAME)
        .then((cache) => cache.delete(catalogMediaCacheKey(trackId)))
        .catch(() => {});
    }
    return true;
  }

  setWifiOnlyDownload(enabled: boolean): void {
    this.wifiOnly = enabled;
  }

  isDownloadedOrQueued(track: LicensedCatalogTrack): boolean {
    const state = this.activeStates.get(track.id);
    return (
      state === CatalogDownloadState.QUEUED ||
      state === CatalogDownloadState.DOWNLOADING ||
      state === CatalogDownloadState.COMPLETED
    );
  }
}
