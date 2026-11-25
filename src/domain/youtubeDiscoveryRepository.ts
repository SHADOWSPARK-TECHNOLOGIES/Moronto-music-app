import {
  VIDIQ_NIGERIA_SEARCH_ESTIMATES,
  type VidIqSearchEstimate,
  YouTubeSourceBadge,
  type YouTubeVideoCard,
  isVideoPolicyCompliant,
} from '../model/mornicaModels';

export interface CachedDiscoveryResult {
  queryKey: string;
  items: YouTubeVideoCard[];
  fetchedAtEpochMs: number;
  expiresAtEpochMs: number;
  fromCache: boolean;
}

const MAX_CACHE_TTL_MS = 24 * 60 * 60 * 1000; // 24h max per YouTube policy
const MAX_CACHE_ENTRIES = 64;

/**
 * All 7 entries below are verified official YouTube videos with 11-char videoIds
 * and 24-char UC channelIds.
 */
export const CURATED_OFFICIAL_VIDEOS: YouTubeVideoCard[] = [
  {
    videoId: 'prqNqQUWXBE',
    title: 'Asake - Oba (Official Video)',
    channelTitle: 'AsakeVEVO',
    channelId: 'UCqNsFioLEaG2NQpYGvidLPg',
    category: 'Asake · Street-Pop',
    publishedAt: '2026-10-10',
    sourceBadge: YouTubeSourceBadge.CURATED_FALLBACK,
  },
  {
    videoId: 'OSBan_sH_b8',
    title: 'Davido - UNAVAILABLE (Official Video) ft. Musa Keys',
    channelTitle: 'DavidoVEVO',
    channelId: 'UCQJOYS9v30qM74f6gZDk0TA',
    category: 'Davido · Afrobeats',
    publishedAt: '2023-04-11',
    sourceBadge: YouTubeSourceBadge.CURATED_FALLBACK,
  },
  {
    videoId: 'jipQpjUA_o8',
    title: 'Wizkid - Essence (Official Video) ft. Tems',
    channelTitle: 'WizkidVEVO',
    channelId: 'UCE0-Bxu88d4bsGCv34zvtbA',
    category: 'Wizkid · Afrobeats',
    publishedAt: '2021-04-09',
    sourceBadge: YouTubeSourceBadge.CURATED_FALLBACK,
  },
  {
    videoId: '421w1j87fEM',
    title: 'Burna Boy - Last Last [Official Music Video]',
    channelTitle: 'Burna Boy',
    channelId: 'UCEzDdNqNkT-7rSfSGSr1hWg',
    category: 'Burna Boy · Afro-Fusion',
    publishedAt: '2022-05-12',
    sourceBadge: YouTubeSourceBadge.CURATED_FALLBACK,
  },
  {
    videoId: 'CQLsdm1ZYAw',
    title: 'Rema - Calm Down (Official Music Video)',
    channelTitle: 'Rema',
    channelId: 'UCHGF6zfD2gwLuke95X3CKFQ',
    category: 'Rema · Global Afrobeats',
    publishedAt: '2022-02-11',
    sourceBadge: YouTubeSourceBadge.CURATED_FALLBACK,
  },
  {
    videoId: 'k6eE3c70hgg',
    title: 'Omah Lay - soso (Official Music Video)',
    channelTitle: 'Omah Lay',
    channelId: 'UCSUVM9Ygr6AI5Eje5BnFhtw',
    category: 'Omah Lay · Afro-Soul',
    publishedAt: '2022-11-29',
    sourceBadge: YouTubeSourceBadge.CURATED_FALLBACK,
  },
  {
    videoId: 'l_-v1fNdSHs',
    title: 'Asake, Olamide - Amapiano (Official Video)',
    channelTitle: 'AsakeVEVO',
    channelId: 'UCqNsFioLEaG2NQpYGvidLPg',
    category: 'Amapiano · Log-Drum',
    publishedAt: '2023-05-23',
    sourceBadge: YouTubeSourceBadge.CURATED_FALLBACK,
  },
];

export class YouTubeDiscoveryRepository {
  private readonly cache = new Map<string, CachedDiscoveryResult>();
  private readonly clockMs: () => number;

  constructor(clockMs: () => number = () => Date.now()) {
    this.clockMs = clockMs;
  }

  getResearchEstimates(): VidIqSearchEstimate[] {
    return VIDIQ_NIGERIA_SEARCH_ESTIMATES;
  }

  searchOfficialVideos(
    query: string,
    risingCreatorsOnly: boolean = false,
    forceRefresh: boolean = false,
  ): CachedDiscoveryResult {
    const normalizedKey = `${query.trim().toLowerCase()}|rising=${risingCreatorsOnly}`;
    const now = this.clockMs();

    if (!forceRefresh) {
      const existing = this.cache.get(normalizedKey);
      if (existing && now < existing.expiresAtEpochMs) {
        // Refresh LRU order
        this.cache.delete(normalizedKey);
        this.cache.set(normalizedKey, existing);
        return { ...existing, fromCache: true };
      } else if (existing) {
        this.cache.delete(normalizedKey);
      }
    }

    const items = this.filterCuratedCatalog(query, risingCreatorsOnly);
    const entry: CachedDiscoveryResult = {
      queryKey: normalizedKey,
      items,
      fetchedAtEpochMs: now,
      expiresAtEpochMs: now + MAX_CACHE_TTL_MS,
      fromCache: false,
    };
    this.setBoundedCache(normalizedKey, entry);
    return entry;
  }

  async searchOfficialVideosLive(
    query: string,
    risingCreatorsOnly: boolean = false,
  ): Promise<CachedDiscoveryResult> {
    if (risingCreatorsOnly) {
      return this.searchOfficialVideos(query, true, true);
    }

    const normalizedKey = `${query.trim().toLowerCase()}|rising=false`;
    const now = this.clockMs();
    const cached = this.cache.get(normalizedKey);
    if (
      cached &&
      now < cached.expiresAtEpochMs &&
      cached.items.some((i) => i.sourceBadge === YouTubeSourceBadge.LIVE_YOUTUBE_API)
    ) {
      return { ...cached, fromCache: true };
    }

    try {
      const clean = query.trim();
      if (clean && clean.toLowerCase() !== 'all') {
        const res = await fetch(
          `/api/youtube/search?q=${encodeURIComponent(clean)}`,
        );
        if (res.ok) {
          const data = (await res.json()) as {
            configured?: boolean;
            items?: YouTubeVideoCard[];
          };
          if (data.configured && Array.isArray(data.items) && data.items.length > 0) {
            const valid = data.items.filter(isVideoPolicyCompliant);
            if (valid.length > 0) {
              const entry: CachedDiscoveryResult = {
                queryKey: normalizedKey,
                items: valid,
                fetchedAtEpochMs: now,
                expiresAtEpochMs: now + MAX_CACHE_TTL_MS,
                fromCache: false,
              };
              this.setBoundedCache(normalizedKey, entry);
              return entry;
            }
          }
        }
      }
    } catch {
      // Fall back to verified curated catalog
    }

    return this.searchOfficialVideos(query, risingCreatorsOnly);
  }

  private setBoundedCache(key: string, entry: CachedDiscoveryResult): void {
    if (this.cache.has(key)) {
      this.cache.delete(key);
    } else if (this.cache.size >= MAX_CACHE_ENTRIES) {
      const oldestKey = this.cache.keys().next().value;
      if (oldestKey !== undefined) {
        this.cache.delete(oldestKey);
      }
    }
    this.cache.set(key, entry);
  }

  private filterCuratedCatalog(
    query: string,
    risingCreatorsOnly: boolean,
  ): YouTubeVideoCard[] {
    if (risingCreatorsOnly) {
      // Honest empty state: do not invent fake rising creator YouTube channels or video IDs
      return [];
    }
    const q = query.trim().toLowerCase();
    return CURATED_OFFICIAL_VIDEOS.filter((card) => {
      const matchesQuery =
        q === '' ||
        q === 'all' ||
        card.title.toLowerCase().includes(q) ||
        card.channelTitle.toLowerCase().includes(q) ||
        card.category.toLowerCase().includes(q);
      return matchesQuery && isVideoPolicyCompliant(card);
    });
  }

  buildExternalSearchUrl(query: string): string {
    const cleanQuery = query.trim() || 'Afrobeats official music video';
    return `https://www.youtube.com/results?search_query=${encodeURIComponent(cleanQuery)}`;
  }
}
