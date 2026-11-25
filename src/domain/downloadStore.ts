import type { LicensedCatalogTrack } from './licensedCatalogRights';

export interface DownloadRecord {
  trackId: string;
  downloadId: string;
  mediaUrl: string;
  licenseReference: string;
  rightsConfirmed: boolean;
  offlineAllowed: boolean;
  territoryGranted: string;
  grantEpochMs: number;
  licenseExpiresAtEpochMs: number | null;
}

export function mayPlayOfflineSnapshot(
  record: DownloadRecord,
  territory: string,
  nowEpochMs: number,
): boolean {
  return (
    record.rightsConfirmed &&
    record.offlineAllowed &&
    territory.trim().toUpperCase() ===
      record.territoryGranted.trim().toUpperCase() &&
    (record.licenseExpiresAtEpochMs === null ||
      nowEpochMs < record.licenseExpiresAtEpochMs)
  );
}

export function createDownloadRecordFromTrack(
  track: LicensedCatalogTrack,
  territory: string,
  nowEpochMs: number,
): DownloadRecord {
  return {
    trackId: track.id,
    downloadId: `catalog:${track.id}`,
    mediaUrl: track.mediaUrl,
    licenseReference: track.licenseReference,
    rightsConfirmed: track.rightsConfirmed,
    offlineAllowed: track.offlineAllowed,
    territoryGranted: territory,
    grantEpochMs: nowEpochMs,
    licenseExpiresAtEpochMs: track.licenseExpiresAtEpochMs,
  };
}

export interface DownloadStore {
  save(record: DownloadRecord): void;
  remove(trackId: string): void;
  get(trackId: string): DownloadRecord | undefined;
  all(): DownloadRecord[];
}

export class InMemoryDownloadStore implements DownloadStore {
  private readonly map = new Map<string, DownloadRecord>();

  save(record: DownloadRecord): void {
    this.map.set(record.trackId, record);
  }

  remove(trackId: string): void {
    this.map.delete(trackId);
  }

  get(trackId: string): DownloadRecord | undefined {
    return this.map.get(trackId);
  }

  all(): DownloadRecord[] {
    return Array.from(this.map.values());
  }
}

const STORAGE_KEY = 'mornica_catalog_downloads_v1';

/**
 * localStorage-backed rights-snapshot store for offline downloads.
 * Corrupt or inaccessible storage fails closed (starts empty).
 */
export class LocalStorageDownloadStore implements DownloadStore {
  private readonly memory = new InMemoryDownloadStore();

  constructor() {
    this.load();
  }

  save(record: DownloadRecord): void {
    this.memory.save(record);
    this.persist();
  }

  remove(trackId: string): void {
    this.memory.remove(trackId);
    this.persist();
  }

  get(trackId: string): DownloadRecord | undefined {
    return this.memory.get(trackId);
  }

  all(): DownloadRecord[] {
    return this.memory.all();
  }

  private load(): void {
    try {
      const raw = window.localStorage.getItem(STORAGE_KEY);
      if (!raw) return;
      const parsed: unknown = JSON.parse(raw);
      if (!Array.isArray(parsed)) return;
      for (const item of parsed) {
        if (
          item &&
          typeof item === 'object' &&
          typeof (item as DownloadRecord).trackId === 'string' &&
          typeof (item as DownloadRecord).licenseReference === 'string'
        ) {
          this.memory.save(item as DownloadRecord);
        }
      }
    } catch {
      // Corrupt/unreadable store => fail closed
    }
  }

  private persist(): void {
    try {
      window.localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify(this.memory.all()),
      );
    } catch {
      // Best-effort persistence
    }
  }
}
