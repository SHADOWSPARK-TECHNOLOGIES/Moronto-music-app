/**
 * Explicit rights metadata for tracks delivered by a licensed Mornica catalogue.
 *
 * Client-side flags are a defense-in-depth UI gate, NOT proof of a content license.
 * The catalogue backend must validate rights, territory, expiry, signed asset URLs and
 * offline entitlement on every stream/download request.
 */
export interface LicensedCatalogTrack {
  id: string;
  title: string;
  artist: string;
  mediaUrl: string;
  sourceLabel: string;
  licenseReference: string;
  allowedTerritories: string[];
  licenseExpiresAtEpochMs: number | null;
  rightsConfirmed: boolean;
  streamAllowed: boolean;
  offlineAllowed: boolean;
}

export const CatalogAccess = {
  STREAM: 'STREAM',
  SAVE_OFFLINE: 'SAVE_OFFLINE',
  PLAY_DOWNLOADED: 'PLAY_DOWNLOADED',
} as const;
export type CatalogAccess = (typeof CatalogAccess)[keyof typeof CatalogAccess];

export const CatalogDenial = {
  NONE: 'NONE',
  INVALID_SOURCE: 'INVALID_SOURCE',
  MISSING_LICENSE: 'MISSING_LICENSE',
  UNVERIFIED_RIGHTS: 'UNVERIFIED_RIGHTS',
  TERRITORY_UNAVAILABLE: 'TERRITORY_UNAVAILABLE',
  LICENSE_EXPIRED: 'LICENSE_EXPIRED',
  STREAM_NOT_AUTHORIZED: 'STREAM_NOT_AUTHORIZED',
  OFFLINE_NOT_AUTHORIZED: 'OFFLINE_NOT_AUTHORIZED',
  DOWNLOAD_INCOMPLETE: 'DOWNLOAD_INCOMPLETE',
} as const;
export type CatalogDenial = (typeof CatalogDenial)[keyof typeof CatalogDenial];

export interface CatalogAccessDecision {
  allowed: boolean;
  denial: CatalogDenial;
}

function allowDecision(): CatalogAccessDecision {
  return { allowed: true, denial: CatalogDenial.NONE };
}

function denyDecision(reason: CatalogDenial): CatalogAccessDecision {
  return { allowed: false, denial: reason };
}

/**
 * Evaluates access before giving a remote track to the audio player or
 * queueing a licensed asset in the offline download manager.
 * Default-deny: fails closed on any missing or invalid entitlement.
 */
export class LicensedCatalogRights {
  private readonly hosts: Set<string>;
  private readonly clockMs: () => number;

  constructor(
    trustedMediaHosts: Iterable<string>,
    clockMs: () => number = () => Date.now(),
  ) {
    this.hosts = new Set(
      Array.from(trustedMediaHosts).map((h) => h.toLowerCase()),
    );
    this.clockMs = clockMs;
  }

  evaluate(
    track: LicensedCatalogTrack,
    action: CatalogAccess,
    territory: string,
    downloadCompleted: boolean = false,
  ): CatalogAccessDecision {
    if (!this.isTrustedHttpsMediaUrl(track.mediaUrl)) {
      return denyDecision(CatalogDenial.INVALID_SOURCE);
    }
    if (
      !track.id.trim() ||
      !track.licenseReference.trim() ||
      !track.sourceLabel.trim() ||
      !track.title.trim() ||
      !track.artist.trim()
    ) {
      return denyDecision(CatalogDenial.MISSING_LICENSE);
    }

    // Rights must be explicitly confirmed before any territory, expiry or streaming decision.
    if (!track.rightsConfirmed) {
      return denyDecision(CatalogDenial.UNVERIFIED_RIGHTS);
    }

    const normalizedTerritory = territory.trim().toUpperCase();
    const permittedTerritories = track.allowedTerritories.map((t) =>
      t.trim().toUpperCase(),
    );
    if (
      normalizedTerritory.length !== 2 ||
      !permittedTerritories.includes(normalizedTerritory)
    ) {
      return denyDecision(CatalogDenial.TERRITORY_UNAVAILABLE);
    }

    const expiry = track.licenseExpiresAtEpochMs;
    if (expiry !== null && this.clockMs() >= expiry) {
      return denyDecision(CatalogDenial.LICENSE_EXPIRED);
    }
    if (!track.streamAllowed) {
      return denyDecision(CatalogDenial.STREAM_NOT_AUTHORIZED);
    }
    if (action !== CatalogAccess.STREAM && !track.offlineAllowed) {
      return denyDecision(CatalogDenial.OFFLINE_NOT_AUTHORIZED);
    }
    if (action === CatalogAccess.PLAY_DOWNLOADED && !downloadCompleted) {
      return denyDecision(CatalogDenial.DOWNLOAD_INCOMPLETE);
    }
    return allowDecision();
  }

  isTrustedHttpsMediaUrl(rawUrl: string): boolean {
    if (!rawUrl.trim()) return false;
    try {
      const uri = new URL(rawUrl);
      const scheme = uri.protocol.toLowerCase();
      const host = uri.hostname.toLowerCase();
      if (
        scheme !== 'https:' ||
        !host ||
        uri.username !== '' ||
        uri.password !== '' ||
        uri.hash !== '' ||
        (uri.port !== '' && uri.port !== '443')
      ) {
        return false;
      }
      const forbidden =
        host === 'youtube.com' ||
        host.endsWith('.youtube.com') ||
        host === 'youtu.be' ||
        host.endsWith('.youtu.be') ||
        host === 'googlevideo.com' ||
        host.endsWith('.googlevideo.com');
      return !forbidden && this.hosts.has(host);
    } catch {
      return false;
    }
  }
}
