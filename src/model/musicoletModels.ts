export const LibraryViewTab = {
  TRACKS: 'TRACKS',
  QUEUES: 'QUEUES',
  PLAYLISTS: 'PLAYLISTS',
  FOLDERS: 'FOLDERS',
  ALBUMS: 'ALBUMS',
  ARTISTS: 'ARTISTS',
  LIVE: 'LIVE',
  UPGRADE: 'UPGRADE',
  STORAGE: 'STORAGE',
  GUIDE: 'GUIDE',
  SPONSOR: 'SPONSOR',
  BLOG: 'BLOG',
  ABOUT: 'ABOUT',
  PRIVACY: 'PRIVACY',
} as const;
export type LibraryViewTab =
  (typeof LibraryViewTab)[keyof typeof LibraryViewTab];

export const LIBRARY_VIEW_LABELS: Record<LibraryViewTab, string> = {
  TRACKS: 'Tracks',
  QUEUES: 'Queues',
  PLAYLISTS: 'Playlists',
  FOLDERS: 'Folders',
  ALBUMS: 'Albums',
  ARTISTS: 'Artists',
  LIVE: 'Live · Coming Soon',
  UPGRADE: 'Pro · Coming Soon',
  STORAGE: 'Storage',
  GUIDE: 'User Guide',
  SPONSOR: 'Sponsor',
  BLOG: 'Blog · Pidgin Guide',
  ABOUT: 'About',
  PRIVACY: 'Privacy',
};

export type AppLanguage = 'en' | 'pcm' | 'yo' | 'ig' | 'ha';

export const APP_LANGUAGES: Array<{ code: AppLanguage; label: string; native: string }> = [
  { code: 'en', label: 'English', native: 'English' },
  { code: 'pcm', label: 'Pidgin', native: 'Naija Pidgin' },
  { code: 'yo', label: 'Yoruba', native: 'Yorùbá' },
  { code: 'ig', label: 'Igbo', native: 'Igbo' },
  { code: 'ha', label: 'Hausa', native: 'Hausa' },
];

export type MorontoThemeId =
  | 'emerald-console'
  | 'sunset-brass'
  | 'lagos-indigo'
  | 'obsidian-rose';

export interface MorontoThemeOption {
  id: MorontoThemeId;
  name: string;
  description: string;
  bgHex: string;
  accentHex: string;
  proOnly: boolean;
}

export const MORONTO_THEMES: MorontoThemeOption[] = [
  {
    id: 'emerald-console',
    name: 'Emerald Console',
    description: 'Deep acoustic midnight slate with studio emerald clarity.',
    bgHex: '#071321',
    accentHex: '#12D9A1',
    proOnly: false,
  },
  {
    id: 'sunset-brass',
    name: 'Sunset Brass',
    description: 'Warm espresso mastering room with highlife brass gold.',
    bgHex: '#130E0B',
    accentHex: '#FFBE55',
    proOnly: false,
  },
  {
    id: 'lagos-indigo',
    name: 'Lagos Indigo',
    description: 'Late-night Victoria Island cobalt with crisp neon cyan.',
    bgHex: '#090C1E',
    accentHex: '#38BDF8',
    proOnly: false,
  },
  {
    id: 'obsidian-rose',
    name: 'Obsidian Rose',
    description: 'Velvet dark plum console with warm choral rose.',
    bgHex: '#120B11',
    accentHex: '#FB7185',
    proOnly: false,
  },
];

export type ProPlanId = 'monthly' | 'lifetime';

export interface ProPlanOption {
  id: ProPlanId;
  name: string;
  priceNaira: number;
  priceLabel: string;
  cadence: string;
  badgeText: string;
  features: string[];
}

export const PRO_PLANS: ProPlanOption[] = [
  {
    id: 'monthly',
    name: 'Moronto Music Pro · Monthly',
    priceNaira: 1500,
    priceLabel: '₦1,500',
    cadence: 'per month',
    badgeText: 'Flexible Plan',
    features: [
      'Zero banner or rewarded ads anywhere',
      'Unlimited access to all 10-band EQ presets',
      'Dual-deck Crossfade (0–12 seconds)',
      'All 4 Studio Themes + Sleep Timer',
    ],
  },
  {
    id: 'lifetime',
    name: 'Moronto Music Pro · Lifetime',
    priceNaira: 12000,
    priceLabel: '₦12,000',
    cadence: 'one-time unlock',
    badgeText: 'Best Value',
    features: [
      'Permanent ad-free listening forever',
      'All 10-band EQ presets (Afrobeats Bass, Amapiano, Gospel, Highlife)',
      'Dual-deck Crossfade (0–12 seconds) & Gapless',
      'All Pro Studio Themes, Sleep Timer & Priority Updates',
    ],
  },
];

export const ADMOB_TEST_IDS = {
  BANNER_TRACKS: 'ca-app-pub-3940256099942544/6300978111',
  REWARDED_EQ: 'ca-app-pub-3940256099942544/5224354917',
} as const;

export type AdConsentStatus = 'unset' | 'personalized' | 'non_personalized';

export type SponsorTierId = 'supporter' | 'patron' | 'label';
export type SponsorBilling = 'monthly' | 'one-time';

export interface SponsorTier {
  id: SponsorTierId;
  name: string;
  monthlyUsd: number;
  oneTimeUsd: number;
  summary: string;
  perks: string[];
}

export interface SponsorPledge {
  id: string;
  name: string;
  email: string;
  tierId: SponsorTierId;
  billing: SponsorBilling;
  amountUsd: number;
  note: string;
  createdAtIso: string;
}

export interface BlogPreviewArticle {
  id: string;
  title: string;
  category: string;
  readTime: string;
  summary: string;
}

export interface LiveShowPreview {
  id: string;
  title: string;
  host: string;
  genre: string;
  scheduleLabel: string;
  summary: string;
}

export interface StudioTemplate {
  id: string;
  name: string;
  tagline: string;
  queueId: string;
  eqPresetId: string;
  crossfadeSec: number;
  crossfadeEnabled: boolean;
}

export const SPONSOR_TIERS: SponsorTier[] = [
  {
    id: 'supporter',
    name: 'Listener Supporter',
    monthlyUsd: 5,
    oneTimeUsd: 15,
    summary: 'Backs ongoing offline playback polish and clean audio tools.',
    perks: [
      'Direct support for offline codec & crossfade maintenance',
      'Supporter credit in upcoming Moronto Music release notes',
      'Early access to desktop & Android audio beta builds',
    ],
  },
  {
    id: 'patron',
    name: 'Studio Patron',
    monthlyUsd: 15,
    oneTimeUsd: 45,
    summary: 'Funds mastering presets, 10-band EQ curves, and local DSP research.',
    perks: [
      'Priority voting on custom EQ & crossfade features',
      'Quarterly behind-the-scenes studio engineering dispatches',
      'Dedicated patron record saved in your local studio ledger',
    ],
  },
  {
    id: 'label',
    name: 'Cultural Partner',
    monthlyUsd: 50,
    oneTimeUsd: 150,
    summary: 'Sponsors licensed African artist showcases and upcoming Live sessions.',
    perks: [
      'Featured partner spotlight on the Moronto Sponsor showcase',
      'Supports licensed bundled indie releases inside Moronto Music',
      'Direct engineering collaboration with ShadowSpark Technologies',
    ],
  },
];

export const SUPPORT_EMAIL = 'support@morontomusic.app';

export interface PidginArticleSection {
  heading: string;
  body: string;
}

export const PIDGIN_OFFLINE_ARTICLE = {
  eyebrow: 'Moronto Journal · Featured Naija Pidgin Special',
  title:
    'Why You Supposed Get Your Own Offline Songs Inside Phone (No Let Network Disgrace You Inside Danfo!)',
  subtitle:
    'Right now Moronto Music na 100% offline music studio — every 10-band EQ preset, 0–12s crossfade, Sleep Timer, and all 4 Themes don open free for everybody while Global Catalog Streaming Pro dey cook.',
  readTime: '3 min laugh & learn',
  sections: [
    {
      heading: '1. Third Mainland Bridge No Send Your 5G Bar',
      body: 'Imagine say you just wear your fine earpiece inside Oshodi-to-VI traffic, spirit don high, beat about to drop — gbam! Circle start to spin like ceiling fan because network carry "E" enter bridge. With local MP3, WAV, or FLAC files inside your phone, even inside NEPA blackout or underground parking lot, your Afrobeats bass dey knock nonstop.',
    },
    {
      heading: '2. Save Your Data Money for Suya & Shawarma',
      body: 'Why you go dey dash telecom company 500MB every week just to replay that same jam wey you fit keep for phone storage once? Keep your own bought albums, producer studio demos, Bandcamp/artist downloads, and church choir recordings inside your Downloads or Music folder, import am once, and enjoy zero-data playback forever.',
    },
    {
      heading: '3. Every Paid Tool Don Free Right Now (No Plan Stress!)',
      body: 'We don comot every lock for now! Open 10-Band EQ (Afrobeats Bass, Amapiano Log Drum, Gospel, Highlife, Flat), set 0–12s smooth DJ crossfade, put Sleep Timer before night sleep, and switch to any Studio Theme wey sweet your eye. Pro go only land when our Global Catalog Streaming engine enter town.',
    },
    {
      heading: '4. Your Files Dey Hide? Holla Support Sharp-Sharp!',
      body: 'If you get music files inside WhatsApp Documents, Telegram folder, SD Card, or laptop and you no sabi move am enter Moronto Music, no stress yourself. Message our Support Desk right below or email support@morontomusic.app — we go guide you step-by-step on Android, iPhone, Windows, or Mac!',
    },
  ] as PidginArticleSection[],
} as const;

export const PRO_COMING_SOON_HIGHLIGHTS = [
  'Global Licensed Music Catalog Streaming (Afrobeats, Amapiano, Gospel, Highlife & Worldwide)',
  'Cloud Queue & Playlist Sync across Android and Web devices',
  'All current offline studio tools (10-band EQ, 0–12s Crossfade, Sleep Timer, 4 Themes, 20 Queues) stay 100% free & unlocked right now',
] as const;

export const BLOG_PREVIEW_POSTS: BlogPreviewArticle[] = [
  {
    id: 'blog-dual-deck-crossfade',
    title: 'Dual-Deck Crossfading Without Cloud Latency',
    category: 'Audio Engineering',
    readTime: '6 min read',
    summary:
      'How Moronto Music overlaps two local audio decks cleanly and caps transitions at 40% of track length so short interludes stay intact.',
  },
  {
    id: 'blog-afrobeats-eq-curves',
    title: 'Tuning 10-Band EQ Curves for Afrobeats & Amapiano Log-Drums',
    category: 'Sound Design',
    readTime: '5 min read',
    summary:
      'Balancing 31Hz sub-weight, 125Hz log-drum punch, and 4kHz vocal clarity without clipping your output limiter.',
  },
  {
    id: 'blog-20-queue-architecture',
    title: 'Inside Our 20-Queue Offline Memory Architecture',
    category: 'Product Craft',
    readTime: '4 min read',
    summary:
      'Why preserving independent playback timestamps across commute, gym, and studio queues keeps offline listening effortless.',
  },
];

export const LIVE_SHOW_PREVIEWS: LiveShowPreview[] = [
  {
    id: 'live-lagos-friday',
    title: 'Lagos Friday Analog Sessions',
    host: 'Hosted by Teniola Blaze',
    genre: 'Afrobeats & Brass',
    scheduleLabel: 'Weekly Broadcast · Coming Soon',
    summary: 'Intimate live horn and percussion sessions streamed directly from our Lagos partner studio.',
  },
  {
    id: 'live-sunday-gospel',
    title: 'Sunday Morning Choir Room',
    host: 'Hosted by Korede Worship',
    genre: 'Gospel & Harmonies',
    scheduleLabel: 'Sunday Special · Coming Soon',
    summary: 'Live vocal harmonies and acoustic praise rehearsals with listener song requests.',
  },
  {
    id: 'live-amapiano-deck',
    title: 'Midnight Log-Drum Selectors',
    host: 'Hosted by Kofi & The LogDrums',
    genre: 'Amapiano & Deep Groove',
    scheduleLabel: 'Weekend Mix · Coming Soon',
    summary: 'Real-time DJ sets mixed with 10-band studio EQ and low-data audio streams.',
  },
];

export const STUDIO_TEMPLATES: StudioTemplate[] = [
  {
    id: 'tpl-afrobeats-commute',
    name: 'Afrobeats Commute',
    tagline: 'Queue 1 · Afrobeats Bass EQ · 4s Fade',
    queueId: 'queue-1',
    eqPresetId: 'afrobeats-bass',
    crossfadeSec: 4,
    crossfadeEnabled: true,
  },
  {
    id: 'tpl-amapiano-club',
    name: 'Amapiano Log Drum',
    tagline: 'Queue 2 · Amapiano Log Drum EQ · 6s Fade',
    queueId: 'queue-2',
    eqPresetId: 'amapiano-log-drum',
    crossfadeSec: 6,
    crossfadeEnabled: true,
  },
  {
    id: 'tpl-sunday-gospel',
    name: 'Sunday Gospel',
    tagline: 'Queue 3 · Gospel EQ · 3s Fade',
    queueId: 'queue-3',
    eqPresetId: 'gospel',
    crossfadeSec: 3,
    crossfadeEnabled: true,
  },
  {
    id: 'tpl-studio-flat',
    name: 'Pure Reference',
    tagline: 'Queue 1 · Flat EQ · 0s Gapless',
    queueId: 'queue-1',
    eqPresetId: 'flat',
    crossfadeSec: 0,
    crossfadeEnabled: false,
  },
];

export const SMART_PLAYLIST_GENRES = ['Afrobeats', 'Amapiano', 'Gospel', 'Highlife'] as const;
export type SmartPlaylistGenre = (typeof SMART_PLAYLIST_GENRES)[number];

export const SortOption = {
  TITLE: 'TITLE',
  ARTIST: 'ARTIST',
  ALBUM: 'ALBUM',
  DURATION: 'DURATION',
  YEAR: 'YEAR',
} as const;
export type SortOption = (typeof SortOption)[keyof typeof SortOption];

export const SORT_OPTION_LABELS: Record<SortOption, string> = {
  TITLE: 'Title',
  ARTIST: 'Artist',
  ALBUM: 'Album',
  DURATION: 'Duration',
  YEAR: 'Year',
};

export const RepeatMode = {
  OFF: 'OFF',
  ALL: 'ALL',
  ONE: 'ONE',
} as const;
export type RepeatMode = (typeof RepeatMode)[keyof typeof RepeatMode];

export interface OfflineTrack {
  id: string;
  title: string;
  artist: string;
  album: string;
  folderName: string;
  durationMs: number;
  uri: string;
  releaseYear: number;
  sourceType: 'BUNDLED' | 'DEVICE';
  lyricsText?: string;
}

export interface PlaybackQueue {
  id: string;
  name: string;
  trackIds: string[];
  currentTrackId: string | null;
  positionMs: number;
}

export interface CustomPlaylist {
  id: string;
  name: string;
  trackIds: string[];
}

export type TenBandTuple = [
  number,
  number,
  number,
  number,
  number,
  number,
  number,
  number,
  number,
  number,
];

export interface EqPreset {
  id: string;
  name: string;
  /** Gains in dB for [31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz] */
  bandsDb: TenBandTuple;
  bassBoostPercent: number;
  freeTierAllowed?: boolean;
}

export const EQ_BAND_LABELS = [
  '31Hz',
  '62Hz',
  '125Hz',
  '250Hz',
  '500Hz',
  '1kHz',
  '2kHz',
  '4kHz',
  '8kHz',
  '16kHz',
] as const;

export const EQ_BAND_FREQUENCIES = [
  31,
  62,
  125,
  250,
  500,
  1000,
  2000,
  4000,
  8000,
  16000,
] as const;

export const EQ_PRESETS: EqPreset[] = [
  {
    id: 'afrobeats-bass',
    name: 'Afrobeats Bass',
    bandsDb: [6, 5, 3, 1, 0, 1, 2, 3, 2, 2],
    bassBoostPercent: 50,
  },
  {
    id: 'amapiano-log-drum',
    name: 'Amapiano Log Drum',
    bandsDb: [7, 6, 4, 1, -1, 0, 1, 2, 2, 1],
    bassBoostPercent: 65,
  },
  {
    id: 'gospel',
    name: 'Gospel',
    bandsDb: [3, 2, 1, 2, 3, 4, 3, 2, 2, 1],
    bassBoostPercent: 25,
  },
  {
    id: 'highlife',
    name: 'Highlife',
    bandsDb: [2, 3, 2, 1, 2, 3, 4, 3, 2, 1],
    bassBoostPercent: 20,
  },
  {
    id: 'flat',
    name: 'Flat',
    bandsDb: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    bassBoostPercent: 0,
    freeTierAllowed: true,
  },
];

export interface EqualizerSettings {
  enabled: boolean;
  presetId: string;
  bandsDb: TenBandTuple;
  bassBoostPercent: number;
  preampDb: number;
  virtualizerPercent: number;
}

export interface CrossfadeSettings {
  enabled: boolean;
  durationSec: number; // 0-12 seconds
  fadeOnManualSkip: boolean;
  gaplessEnabled: boolean;
}

export const DEFAULT_EQUALIZER_SETTINGS: EqualizerSettings = {
  enabled: true,
  presetId: 'flat',
  bandsDb: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
  bassBoostPercent: 0,
  preampDb: 0,
  virtualizerPercent: 15,
};

export const DEFAULT_CROSSFADE_SETTINGS: CrossfadeSettings = {
  enabled: false,
  durationSec: 4,
  fadeOnManualSkip: true,
  gaplessEnabled: true,
};

export interface LrcLine {
  timeMs: number | null;
  text: string;
}

export function parseLrcLyrics(raw: string | undefined): LrcLine[] {
  if (!raw || !raw.trim()) return [];
  const lines = raw.split(/\r?\n/);
  const result: LrcLine[] = [];
  const tagRegex = /^\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?\]\s*(.*)$/;

  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed) continue;
    const match = tagRegex.exec(trimmed);
    if (match) {
      const mins = Number(match[1]);
      const secs = Number(match[2]);
      const fracRaw = match[3] ?? '0';
      const ms = Number(fracRaw.padEnd(3, '0').slice(0, 3));
      const text = match[4].trim();
      if (text) {
        result.push({
          timeMs: mins * 60_000 + secs * 1000 + ms,
          text,
        });
      }
    } else {
      result.push({ timeMs: null, text: trimmed });
    }
  }
  return result;
}

export const MAX_QUEUES = 20;

export const BUNDLED_OFFLINE_TRACKS: OfflineTrack[] = [
  {
    id: 'track-eko-neon',
    title: 'Eko Neon Pulse',
    artist: 'Teniola Blaze',
    album: 'Lagos Midnight Sessions',
    folderName: 'Afrobeats',
    durationMs: 30_000,
    uri: '/audio/mornica_afrobeats_demo.wav',
    releaseYear: 2026,
    sourceType: 'BUNDLED',
    lyricsText: `[00:00.00] Third Mainland bridge lights reflecting on the water
[00:05.00] Midnight rhythm rolling through Victoria Island
[00:10.00] Zero data needed when the bassline hits the speakers
[00:16.00] Moronto Music groove, keep the queue alive
[00:22.00] Eko never sleeps, turn the dial up one more time`,
  },
  {
    id: 'track-lekki-logdrum',
    title: 'Lekki Sunrise Log-Drum',
    artist: 'Kofi & The LogDrums',
    album: 'Amapiano Pilot Tapes',
    folderName: 'Amapiano',
    durationMs: 45_000,
    uri: '/audio/mornica_afrobeats_demo.wav',
    releaseYear: 2025,
    sourceType: 'BUNDLED',
    lyricsText: `[00:00.00] Early morning breeze off the Lekki coast
[00:07.00] Log-drums knocking steady in the back seat
[00:14.00] Save the queue, switch the groove, never lose your spot
[00:21.00] Pure offline percussion for the morning commute`,
  },
  {
    id: 'track-grace-overflow',
    title: 'Grace Overflow (Choir Live)',
    artist: 'Korede Worship',
    album: 'Sunday Sanctuary Tapes',
    folderName: 'Gospel',
    durationMs: 52_000,
    uri: '/audio/mornica_afrobeats_demo.wav',
    releaseYear: 2026,
    sourceType: 'BUNDLED',
    lyricsText: `[00:00.00] Morning harmonies rising across the hall
[00:08.00] Grateful hearts singing loud with zero cloud buffer
[00:16.00] Lift the chorus higher in crystal offline clarity`,
  },
  {
    id: 'track-yaba-strings',
    title: 'Yaba Midnight Strings',
    artist: 'Zainab Soul',
    album: 'Afro-Soul Showcase',
    folderName: 'Afrobeats',
    durationMs: 60_000,
    uri: '/audio/mornica_afrobeats_demo.wav',
    releaseYear: 2024,
    sourceType: 'BUNDLED',
    lyricsText: `[00:00.00] Warm acoustic strings over Yaba rooftops
[00:08.00] Rain tapping softly on the studio window
[00:16.00] Slow down the tempo, breathe in the melody`,
  },
  {
    id: 'track-abuja-highlife',
    title: 'Abuja Sunset Highlife',
    artist: 'Chinedu Brass',
    album: 'Golden Palm Collective',
    folderName: 'Highlife',
    durationMs: 42_000,
    uri: '/audio/mornica_afrobeats_demo.wav',
    releaseYear: 2025,
    sourceType: 'BUNDLED',
    lyricsText: `[00:00.00] Brass horns echoing across Aso Rock at dusk
[00:09.00] Classic highlife guitar lines woven into modern drums`,
  },
  {
    id: 'track-ibadan-groove',
    title: 'Ibadan Acoustic Groove',
    artist: 'Teniola Blaze',
    album: 'Lagos Midnight Sessions',
    folderName: 'Afrobeats',
    durationMs: 36_000,
    uri: '/audio/mornica_afrobeats_demo.wav',
    releaseYear: 2026,
    sourceType: 'BUNDLED',
  },
];

export const DEFAULT_QUEUES: PlaybackQueue[] = [
  {
    id: 'queue-1',
    name: 'Queue 1 · Main Mix',
    trackIds: [
      'track-eko-neon',
      'track-lekki-logdrum',
      'track-grace-overflow',
      'track-yaba-strings',
      'track-abuja-highlife',
      'track-ibadan-groove',
    ],
    currentTrackId: 'track-eko-neon',
    positionMs: 0,
  },
  {
    id: 'queue-2',
    name: 'Queue 2 · Lagos Commute',
    trackIds: ['track-lekki-logdrum', 'track-eko-neon'],
    currentTrackId: 'track-lekki-logdrum',
    positionMs: 0,
  },
  {
    id: 'queue-3',
    name: 'Queue 3 · Sunday Praise & Soul',
    trackIds: ['track-grace-overflow', 'track-yaba-strings', 'track-abuja-highlife'],
    currentTrackId: 'track-grace-overflow',
    positionMs: 0,
  },
];

export const DEFAULT_PLAYLISTS: CustomPlaylist[] = [
  {
    id: 'pl-favorites-mix',
    name: 'Roadtrip Essentials',
    trackIds: ['track-eko-neon', 'track-abuja-highlife'],
  },
];
