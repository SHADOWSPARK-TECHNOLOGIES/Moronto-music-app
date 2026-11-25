import {
  LicensedCatalogRights,
  type LicensedCatalogTrack,
} from './licensedCatalogRights.ts';

/**
 * Trusted media host configuration and a fail-closed rights gate factory for the
 * licensed Mornica catalogue. Exact-host match only.
 */
export const LicensedCatalogConfig = {
  DEMO_TRUSTED_AUDIO_HOSTS: new Set<string>([
    'soundhelix.com',
    'www.soundhelix.com',
  ]),

  demoRights(): LicensedCatalogRights {
    return new LicensedCatalogRights(this.DEMO_TRUSTED_AUDIO_HOSTS);
  },
};

export interface CatalogSource {
  readonly label: string;
  readonly isDemo: boolean;
  tracks(): LicensedCatalogTrack[];
  byId(id: string): LicensedCatalogTrack | undefined;
}

/**
 * Demo fixtures: reachable, license-clear public sample audio used to prove the pipeline.
 * Every entry is visibly labeled DEMO and uses a `DEMO-*` licenseReference.
 */
export class DemoCatalogSource implements CatalogSource {
  readonly label =
    'DEMO · Public sample audio (SoundHelix) — NOT a Mornica licensed track';
  readonly isDemo = true;

  tracks(): LicensedCatalogTrack[] {
    const now = Date.now();
    const demoExpiry = now + 120 * 24 * 3600 * 1000;
    return [
      {
        id: 'catalog-demo-afrobeat-01',
        title: 'Demo Afrobeats Instrumental',
        artist: 'SoundHelix (Sample)',
        mediaUrl:
          'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
        sourceLabel: 'DEMO · Public sample audio (SoundHelix)',
        licenseReference: 'DEMO-CATALOG-AFROBEAT-01',
        allowedTerritories: ['NG', 'GH'],
        licenseExpiresAtEpochMs: demoExpiry,
        rightsConfirmed: true,
        streamAllowed: true,
        offlineAllowed: true,
      },
      {
        id: 'catalog-demo-stream-only-02',
        title: 'Demo Streaming-Only Track',
        artist: 'SoundHelix (Sample)',
        mediaUrl:
          'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
        sourceLabel:
          'DEMO · Public sample audio (SoundHelix) · offline NOT authorized',
        licenseReference: 'DEMO-CATALOG-STREAM-02',
        allowedTerritories: ['NG'],
        licenseExpiresAtEpochMs: demoExpiry,
        rightsConfirmed: true,
        streamAllowed: true,
        offlineAllowed: false,
      },
    ];
  }

  byId(id: string): LicensedCatalogTrack | undefined {
    return this.tracks().find((t) => t.id === id);
  }
}

/**
 * Future backend-backed licensed catalogue. Intentionally returns no tracks until an
 * authenticated backend issues real agreements/signed asset URLs.
 */
export class LicensedCatalogBackendSource implements CatalogSource {
  readonly label =
    'LICENSED · Requires an authenticated backend agreement (not yet connected)';
  readonly isDemo = false;

  tracks(): LicensedCatalogTrack[] {
    return [];
  }

  byId(): LicensedCatalogTrack | undefined {
    return undefined;
  }
}

export class LicensedCatalogRepository {
  private readonly source: CatalogSource;

  constructor(source: CatalogSource = new DemoCatalogSource()) {
    this.source = source;
  }

  demoCatalog(): LicensedCatalogTrack[] {
    return this.source.tracks();
  }

  byId(id: string): LicensedCatalogTrack | undefined {
    return this.source.byId(id);
  }
}
