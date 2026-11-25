import {
  MORNICA_MEDIA_CACHE_NAME,
  catalogMediaCacheKey,
} from './catalogDownloadManager';
import type { LicensedCatalogTrack } from './licensedCatalogRights';

/**
 * Loop-6 readiness verifier: single authority for whether a catalogue track's
 * bytes are genuinely present offline in the browser Cache API.
 * Fails closed when bytes are absent, zero-length, or truncated.
 */
export interface OfflineReadinessVerifier {
  isGenuinelyReadyForOffline(track: LicensedCatalogTrack): boolean;
  markVerified(trackId: string, verified: boolean): void;
  hydrateFromCache(tracks: LicensedCatalogTrack[]): Promise<Set<string>>;
}

export class WebCacheOfflineReadinessVerifier
  implements OfflineReadinessVerifier
{
  private readonly verifiedTrackIds = new Set<string>();

  isGenuinelyReadyForOffline(track: LicensedCatalogTrack): boolean {
    return this.verifiedTrackIds.has(track.id);
  }

  markVerified(trackId: string, verified: boolean): void {
    if (verified) {
      this.verifiedTrackIds.add(trackId);
    } else {
      this.verifiedTrackIds.delete(trackId);
    }
  }

  async hydrateFromCache(
    tracks: LicensedCatalogTrack[],
  ): Promise<Set<string>> {
    this.verifiedTrackIds.clear();
    if (typeof window === 'undefined' || !('caches' in window)) {
      return new Set();
    }
    try {
      const cache = await window.caches.open(MORNICA_MEDIA_CACHE_NAME);
      for (const track of tracks) {
        const match = await cache.match(catalogMediaCacheKey(track.id));
        if (!match) continue;
        const contentLength = Number(match.headers.get('Content-Length') || 0);
        const blob = await match.blob();
        if (blob.size > 0 && (contentLength <= 0 || blob.size >= contentLength)) {
          this.verifiedTrackIds.add(track.id);
        }
      }
    } catch {
      // Fail closed on Cache API errors
    }
    return new Set(this.verifiedTrackIds);
  }
}
