export const PrimaryScreen = {
  MY_MUSIC: 'MY_MUSIC',
  DISCOVER_WATCH: 'DISCOVER_WATCH',
  CREATOR_HUB: 'CREATOR_HUB',
} as const;
export type PrimaryScreen = (typeof PrimaryScreen)[keyof typeof PrimaryScreen];

export const PRIMARY_SCREEN_LABELS: Record<PrimaryScreen, string> = {
  MY_MUSIC: 'My Music',
  DISCOVER_WATCH: 'Explore Videos',
  CREATOR_HUB: 'Creator Hub',
};

export const ActiveEngine = {
  NONE: 'NONE',
  YOUTUBE_EMBED: 'YOUTUBE_EMBED',
  LOCAL_AUDIO: 'LOCAL_AUDIO',
  LICENSED_CATALOG: 'LICENSED_CATALOG',
} as const;
export type ActiveEngine = (typeof ActiveEngine)[keyof typeof ActiveEngine];

export const YouTubeSourceBadge = {
  LIVE_YOUTUBE_API: 'LIVE · Official YouTube Data API v3',
  CURATED_FALLBACK: 'VERIFIED OEMBED · Official YouTube Metadata',
} as const;
export type YouTubeSourceBadge =
  (typeof YouTubeSourceBadge)[keyof typeof YouTubeSourceBadge];

export const ProvenanceStatus = {
  LOCAL_DEVICE: 'LOCAL_DEVICE',
  DEMO: 'DEMO',
  LICENSED: 'LICENSED',
  UNAVAILABLE: 'UNAVAILABLE',
} as const;
export type ProvenanceStatus =
  (typeof ProvenanceStatus)[keyof typeof ProvenanceStatus];

export const PROVENANCE_META: Record<
  ProvenanceStatus,
  { label: string; shortLabel: string; isPlayableByDefault: boolean }
> = {
  LOCAL_DEVICE: {
    label: 'LOCAL · Device File',
    shortLabel: 'Device Audio',
    isPlayableByDefault: true,
  },
  DEMO: {
    label: 'DEMO · First-Party Synth Preview',
    shortLabel: 'Synth Preview',
    isPlayableByDefault: true,
  },
  LICENSED: {
    label: 'LICENSED · Signed Artist Agreement',
    shortLabel: 'Licensed',
    isPlayableByDefault: false,
  },
  UNAVAILABLE: {
    label: 'UNAVAILABLE · Awaiting Clearance',
    shortLabel: 'Awaiting Clearance',
    isPlayableByDefault: false,
  },
};

export const RepeatMode = {
  OFF: 'OFF',
  ALL: 'ALL',
  ONE: 'ONE',
} as const;
export type RepeatMode = (typeof RepeatMode)[keyof typeof RepeatMode];

export const LibraryTab = {
  SONGS: 'SONGS',
  PLAYLISTS: 'PLAYLISTS',
  ALBUMS: 'ALBUMS',
  ARTISTS: 'ARTISTS',
  FOLDERS: 'FOLDERS',
} as const;
export type LibraryTab = (typeof LibraryTab)[keyof typeof LibraryTab];

export const LIBRARY_TAB_LABELS: Record<LibraryTab, string> = {
  SONGS: 'Tracks',
  PLAYLISTS: 'Queues',
  ALBUMS: 'Albums',
  ARTISTS: 'Artists',
  FOLDERS: 'Folders',
};

export const TrackSortOption = {
  TITLE: 'TITLE',
  ARTIST: 'ARTIST',
  DURATION: 'DURATION',
  YEAR: 'YEAR',
} as const;
export type TrackSortOption =
  (typeof TrackSortOption)[keyof typeof TrackSortOption];

export const TRACK_SORT_LABELS: Record<TrackSortOption, string> = {
  TITLE: 'Title',
  ARTIST: 'Artist',
  DURATION: 'Duration',
  YEAR: 'Year',
};

export interface VidIqSearchEstimate {
  query: string;
  monthlyNigerianSearches: number;
  category: string;
  isThirdPartyEstimate: boolean;
  framingNote: string;
}

const DEFAULT_FRAMING_NOTE =
  'Third-party vidIQ Nigerian monthly YouTube search estimate (directional indicator, never guaranteed demand)';

export const VIDIQ_NIGERIA_SEARCH_ESTIMATES: VidIqSearchEstimate[] = [
  {
    query: 'Asake',
    monthlyNigerianSearches: 207_295,
    category: 'Street-Pop / Afrobeats',
    isThirdPartyEstimate: true,
    framingNote: DEFAULT_FRAMING_NOTE,
  },
  {
    query: 'Davido',
    monthlyNigerianSearches: 123_964,
    category: 'Afrobeats Essentials',
    isThirdPartyEstimate: true,
    framingNote: DEFAULT_FRAMING_NOTE,
  },
  {
    query: 'Wizkid',
    monthlyNigerianSearches: 94_555,
    category: 'Afrobeats Essentials',
    isThirdPartyEstimate: true,
    framingNote: DEFAULT_FRAMING_NOTE,
  },
  {
    query: 'Burna Boy',
    monthlyNigerianSearches: 94_014,
    category: 'Afro-Fusion',
    isThirdPartyEstimate: true,
    framingNote: DEFAULT_FRAMING_NOTE,
  },
  {
    query: 'Rema',
    monthlyNigerianSearches: 82_179,
    category: 'Global Afrobeats',
    isThirdPartyEstimate: true,
    framingNote: DEFAULT_FRAMING_NOTE,
  },
  {
    query: 'Omah Lay',
    monthlyNigerianSearches: 30_694,
    category: 'Afro-Soul',
    isThirdPartyEstimate: true,
    framingNote: DEFAULT_FRAMING_NOTE,
  },
  {
    query: 'Amapiano',
    monthlyNigerianSearches: 25_401,
    category: 'Amapiano Crossover',
    isThirdPartyEstimate: true,
    framingNote: DEFAULT_FRAMING_NOTE,
  },
];

export const VIDEO_ID_REGEX = /^[a-zA-Z0-9_-]{11}$/;
export const CHANNEL_ID_REGEX = /^UC[a-zA-Z0-9_-]{22}$/;

export interface YouTubeVideoCard {
  videoId: string;
  title: string;
  channelTitle: string;
  channelId: string;
  category: string;
  publishedAt: string;
  sourceBadge: YouTubeSourceBadge;
  isRisingCreator?: boolean;
  isFreeUnpaywalled?: boolean;
}

export function buildOfficialWatchUrl(videoId: string): string {
  return `https://www.youtube.com/watch?v=${videoId}`;
}

export function buildOfficialChannelUrl(channelId: string): string | null {
  return CHANNEL_ID_REGEX.test(channelId)
    ? `https://www.youtube.com/channel/${channelId}`
    : null;
}

export function isVideoPolicyCompliant(card: YouTubeVideoCard): boolean {
  const unpaywalled = card.isFreeUnpaywalled ?? true;
  return (
    VIDEO_ID_REGEX.test(card.videoId) &&
    (card.channelId.trim() === '' || CHANNEL_ID_REGEX.test(card.channelId)) &&
    unpaywalled
  );
}

export interface LocalMediaTrack {
  id: string;
  title: string;
  artist: string;
  album: string;
  folderName: string;
  durationMs: number;
  uri: string;
  provenance: ProvenanceStatus;
  rightsConfirmed: boolean;
  releaseYear: number;
}

export function isYouTubeUri(candidateUri: string): boolean {
  const lower = candidateUri.trim().toLowerCase();
  return (
    lower.includes('youtube.com') ||
    lower.includes('youtu.be') ||
    lower.includes('googlevideo.com') ||
    lower.startsWith('yt://')
  );
}

export function isTrackPlayableOffline(track: LocalMediaTrack): boolean {
  const meta = PROVENANCE_META[track.provenance];
  if (!track.rightsConfirmed || !meta.isPlayableByDefault) return false;
  if (isYouTubeUri(track.uri)) return false;
  return (
    track.uri.startsWith('/audio/') ||
    track.uri.startsWith('blob:') ||
    track.uri.startsWith('file://') ||
    track.uri.startsWith('data:audio/')
  );
}

export interface CreatorProfile {
  id: string;
  artistName: string;
  city: string;
  genre: string;
  bio: string;
  provenanceBadge: ProvenanceStatus;
  rightsConfirmed: boolean;
  previewTrackIds: string[];
  officialYouTubeChannelUrl: string | null;
}

export interface ArtistLaunchRequest {
  artistName: string;
  trackTitle: string;
  genre: string;
  contactEmailOrWhatsApp: string;
  rightsAttested: boolean;
  submittedAtIso?: string;
}

export interface ValidationResult {
  valid: boolean;
  errorMessage: string | null;
}

export function validateArtistLaunchRequest(
  request: ArtistLaunchRequest,
): ValidationResult {
  if (request.artistName.trim().length < 2) {
    return {
      valid: false,
      errorMessage: 'Artist or collective name must be at least 2 characters.',
    };
  }
  if (request.trackTitle.trim().length < 2) {
    return {
      valid: false,
      errorMessage: 'Track or EP title must be at least 2 characters.',
    };
  }
  if (request.contactEmailOrWhatsApp.trim().length < 5) {
    return {
      valid: false,
      errorMessage:
        'Provide a valid email or WhatsApp contact for onboarding.',
    };
  }
  if (!request.rightsAttested) {
    return {
      valid: false,
      errorMessage:
        'You must attest that you own or control 100% of the preview rights before submitting.',
    };
  }
  return { valid: true, errorMessage: null };
}

export const PUBLIC_PRIVACY_POLICY_URL =
  'https://github.com/SHADOWSPARK-TECHNOLOGIES/Moronto-music-app/blob/agent/antigravity-mornica-launch-contract-20261019/docs/PRIVACY_POLICY.md';
