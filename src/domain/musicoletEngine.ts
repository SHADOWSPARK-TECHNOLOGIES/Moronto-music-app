import {
  type CrossfadeSettings,
  type CustomPlaylist,
  type EqualizerSettings,
  MAX_QUEUES,
  type OfflineTrack,
  type PlaybackQueue,
  RepeatMode,
  SMART_PLAYLIST_GENRES,
  SPONSOR_TIERS,
  type SmartPlaylistGenre,
  SortOption,
  type SponsorBilling,
  type SponsorPledge,
  type SponsorTierId,
  type TenBandTuple,
} from '../model/musicoletModels';

export function filterAndSortTracks(
  tracks: OfflineTrack[],
  query: string,
  sortOption: SortOption,
): OfflineTrack[] {
  const q = query.trim().toLowerCase();
  const filtered = !q
    ? tracks
    : tracks.filter(
        (t) =>
          t.title.toLowerCase().includes(q) ||
          t.artist.toLowerCase().includes(q) ||
          t.album.toLowerCase().includes(q) ||
          t.folderName.toLowerCase().includes(q),
      );

  const copy = [...filtered];
  switch (sortOption) {
    case SortOption.TITLE:
      return copy.sort((a, b) =>
        a.title.toLowerCase().localeCompare(b.title.toLowerCase()),
      );
    case SortOption.ARTIST:
      return copy.sort(
        (a, b) =>
          a.artist.toLowerCase().localeCompare(b.artist.toLowerCase()) ||
          a.title.toLowerCase().localeCompare(b.title.toLowerCase()),
      );
    case SortOption.ALBUM:
      return copy.sort(
        (a, b) =>
          a.album.toLowerCase().localeCompare(b.album.toLowerCase()) ||
          a.title.toLowerCase().localeCompare(b.title.toLowerCase()),
      );
    case SortOption.DURATION:
      return copy.sort((a, b) => b.durationMs - a.durationMs);
    case SortOption.YEAR:
      return copy.sort(
        (a, b) =>
          b.releaseYear - a.releaseYear ||
          a.title.toLowerCase().localeCompare(b.title.toLowerCase()),
      );
  }
}

export function groupTracksBy(
  tracks: OfflineTrack[],
  key: 'folderName' | 'artist' | 'album',
): Array<[string, OfflineTrack[]]> {
  const groups = new Map<string, OfflineTrack[]>();
  for (const track of tracks) {
    const groupName = track[key];
    const list = groups.get(groupName) ?? [];
    list.push(track);
    groups.set(groupName, list);
  }
  return Array.from(groups.entries()).sort(([a], [b]) =>
    a.toLowerCase().localeCompare(b.toLowerCase()),
  );
}

/**
 * Musicolet signature feature: switches to another independent queue while saving
 * the outgoing queue's current track ID and playback positionMs.
 */
export function switchQueueWithResume(
  queues: PlaybackQueue[],
  activeQueueId: string,
  targetQueueId: string,
  currentPositionMs: number,
): { queues: PlaybackQueue[]; activeQueue: PlaybackQueue } {
  const updated = queues.map((q) =>
    q.id === activeQueueId
      ? { ...q, positionMs: Math.max(0, currentPositionMs) }
      : q,
  );
  const found =
    updated.find((q) => q.id === targetQueueId) ??
    updated[0];
  return { queues: updated, activeQueue: found };
}

export function createNewQueue(
  queues: PlaybackQueue[],
  name: string,
  initialTrackIds: string[] = [],
): { queues: PlaybackQueue[]; created: PlaybackQueue | null; error?: string } {
  if (queues.length >= MAX_QUEUES) {
    return {
      queues,
      created: null,
      error: `Maximum of ${MAX_QUEUES} independent queues reached.`,
    };
  }
  const cleanName = name.trim();
  if (cleanName.length < 1) {
    return { queues, created: null, error: 'Queue name cannot be empty.' };
  }
  const uniqueIds = Array.from(new Set(initialTrackIds));
  const created: PlaybackQueue = {
    id: `queue-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
    name: cleanName,
    trackIds: uniqueIds,
    currentTrackId: uniqueIds[0] ?? null,
    positionMs: 0,
  };
  return { queues: [...queues, created], created };
}

export function deleteQueueById(
  queues: PlaybackQueue[],
  activeQueueId: string,
  queueIdToDelete: string,
): { queues: PlaybackQueue[]; nextActiveQueueId: string; deleted: boolean } {
  if (queues.length <= 1) {
    return { queues, nextActiveQueueId: activeQueueId, deleted: false };
  }
  const remaining = queues.filter((q) => q.id !== queueIdToDelete);
  if (remaining.length === queues.length) {
    return { queues, nextActiveQueueId: activeQueueId, deleted: false };
  }
  const nextActive =
    activeQueueId === queueIdToDelete ? remaining[0].id : activeQueueId;
  return { queues: remaining, nextActiveQueueId: nextActive, deleted: true };
}

export function addTracksToQueue(
  queues: PlaybackQueue[],
  queueId: string,
  trackIds: string[],
  playNextAfterCurrent: boolean = false,
): PlaybackQueue[] {
  return queues.map((q) => {
    if (q.id !== queueId) return q;
    const existingSet = new Set(q.trackIds);
    const incoming = trackIds.filter((id) => !existingSet.has(id));
    if (incoming.length === 0) return q;

    let nextIds: string[];
    if (playNextAfterCurrent && q.currentTrackId) {
      const idx = q.trackIds.indexOf(q.currentTrackId);
      if (idx >= 0) {
        nextIds = [
          ...q.trackIds.slice(0, idx + 1),
          ...incoming,
          ...q.trackIds.slice(idx + 1),
        ];
      } else {
        nextIds = [...q.trackIds, ...incoming];
      }
    } else {
      nextIds = [...q.trackIds, ...incoming];
    }

    return {
      ...q,
      trackIds: nextIds,
      currentTrackId: q.currentTrackId ?? nextIds[0] ?? null,
    };
  });
}

export function removeTrackFromQueue(
  queues: PlaybackQueue[],
  queueId: string,
  trackId: string,
): PlaybackQueue[] {
  return queues.map((q) => {
    if (q.id !== queueId) return q;
    const idx = q.trackIds.indexOf(trackId);
    if (idx < 0) return q;
    const nextIds = q.trackIds.filter((id) => id !== trackId);
    let nextCurrent = q.currentTrackId;
    let nextPos = q.positionMs;
    if (q.currentTrackId === trackId) {
      nextCurrent = nextIds[idx] ?? nextIds[idx - 1] ?? null;
      nextPos = 0;
    }
    return {
      ...q,
      trackIds: nextIds,
      currentTrackId: nextCurrent,
      positionMs: nextPos,
    };
  });
}

export function moveQueueTrack(
  queues: PlaybackQueue[],
  queueId: string,
  fromIndex: number,
  toIndex: number,
): PlaybackQueue[] {
  return queues.map((q) => {
    if (q.id !== queueId) return q;
    if (
      fromIndex < 0 ||
      toIndex < 0 ||
      fromIndex >= q.trackIds.length ||
      toIndex >= q.trackIds.length ||
      fromIndex === toIndex
    ) {
      return q;
    }
    const copy = [...q.trackIds];
    const [moved] = copy.splice(fromIndex, 1);
    copy.splice(toIndex, 0, moved);
    return { ...q, trackIds: copy };
  });
}

function gcd(a: number, b: number): number {
  let x = Math.abs(a);
  let y = Math.abs(b);
  while (y !== 0) {
    const t = y;
    y = x % y;
    x = t;
  }
  return x;
}

function getCoprimeShuffleStride(length: number): number {
  if (length <= 2) return 1;
  let candidate = Math.max(2, Math.floor(length / 2) - 1);
  while (candidate < length && gcd(candidate, length) !== 1) {
    candidate++;
  }
  return candidate < length ? candidate : 1;
}

export function computeNextQueueTrack(
  queue: PlaybackQueue,
  repeatMode: RepeatMode,
  shuffleEnabled: boolean,
  isAutoAdvance: boolean,
): { nextTrackId: string | null; shouldStop: boolean; restartCurrent: boolean } {
  const ids = queue.trackIds;
  if (ids.length === 0) {
    return { nextTrackId: null, shouldStop: true, restartCurrent: false };
  }
  if (repeatMode === RepeatMode.ONE && queue.currentTrackId && isAutoAdvance) {
    return {
      nextTrackId: queue.currentTrackId,
      shouldStop: false,
      restartCurrent: true,
    };
  }
  const currentIdx = queue.currentTrackId
    ? ids.indexOf(queue.currentTrackId)
    : -1;

  if (shuffleEnabled && ids.length > 1) {
    const stride = getCoprimeShuffleStride(ids.length);
    const baseIdx = currentIdx < 0 ? 0 : currentIdx;
    const nextIdx = (baseIdx + stride) % ids.length;
    const nextId =
      ids[nextIdx] === queue.currentTrackId
        ? ids[(nextIdx + 1) % ids.length]
        : ids[nextIdx];
    return { nextTrackId: nextId, shouldStop: false, restartCurrent: false };
  }

  if (currentIdx < 0) {
    return { nextTrackId: ids[0], shouldStop: false, restartCurrent: false };
  }
  if (currentIdx + 1 < ids.length) {
    return {
      nextTrackId: ids[currentIdx + 1],
      shouldStop: false,
      restartCurrent: false,
    };
  }
  if (repeatMode === RepeatMode.ALL) {
    return { nextTrackId: ids[0], shouldStop: false, restartCurrent: false };
  }
  return { nextTrackId: null, shouldStop: true, restartCurrent: false };
}

export function computePreviousQueueTrack(
  queue: PlaybackQueue,
  repeatMode: RepeatMode,
  positionMs: number,
  restartThresholdMs: number = 3_000,
): { prevTrackId: string | null; restartCurrent: boolean } {
  const ids = queue.trackIds;
  if (ids.length === 0) {
    return { prevTrackId: null, restartCurrent: false };
  }
  if (queue.currentTrackId && positionMs > restartThresholdMs) {
    return { prevTrackId: queue.currentTrackId, restartCurrent: true };
  }
  const currentIdx = queue.currentTrackId
    ? ids.indexOf(queue.currentTrackId)
    : -1;
  if (currentIdx > 0) {
    return { prevTrackId: ids[currentIdx - 1], restartCurrent: false };
  }
  if (repeatMode === RepeatMode.ALL) {
    return { prevTrackId: ids[ids.length - 1], restartCurrent: false };
  }
  return { prevTrackId: ids[0], restartCurrent: true };
}

export interface TrackTagPatch {
  title: string;
  artist: string;
  album: string;
  folderName: string;
  releaseYear: number;
  lyricsText?: string;
}

export function applyTrackTags(
  tracks: OfflineTrack[],
  trackId: string,
  patch: TrackTagPatch,
): OfflineTrack[] {
  return tracks.map((t) => {
    if (t.id !== trackId) return t;
    return {
      ...t,
      title: patch.title.trim() || t.title,
      artist: patch.artist.trim() || t.artist,
      album: patch.album.trim() || t.album,
      folderName: patch.folderName.trim() || t.folderName,
      releaseYear:
        Number.isFinite(patch.releaseYear) &&
        patch.releaseYear >= 1900 &&
        patch.releaseYear <= 2100
          ? patch.releaseYear
          : t.releaseYear,
      lyricsText:
        patch.lyricsText !== undefined ? patch.lyricsText : t.lyricsText,
    };
  });
}

export function createCustomPlaylist(
  playlists: CustomPlaylist[],
  name: string,
): { playlists: CustomPlaylist[]; created: CustomPlaylist | null } {
  const clean = name.trim();
  if (!clean) return { playlists, created: null };
  const created: CustomPlaylist = {
    id: `pl-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
    name: clean,
    trackIds: [],
  };
  return { playlists: [...playlists, created], created };
}

export function addTrackToCustomPlaylist(
  playlists: CustomPlaylist[],
  playlistId: string,
  trackId: string,
): CustomPlaylist[] {
  return playlists.map((p) => {
    if (p.id !== playlistId) return p;
    if (p.trackIds.includes(trackId)) return p;
    return { ...p, trackIds: [...p.trackIds, trackId] };
  });
}

export function deleteCustomPlaylist(
  playlists: CustomPlaylist[],
  playlistId: string,
): CustomPlaylist[] {
  return playlists.filter((p) => p.id !== playlistId);
}

function clampNum(value: number, min: number, max: number): number {
  if (!Number.isFinite(value)) return min;
  return Math.max(min, Math.min(max, value));
}

export function clampEqualizerSettings(
  settings: EqualizerSettings,
): EqualizerSettings {
  const clampedBands = Array.from({ length: 10 }, (_, idx) =>
    Math.round(clampNum(settings.bandsDb[idx] ?? 0, -12, 12)),
  ) as TenBandTuple;
  return {
    ...settings,
    bandsDb: clampedBands,
    bassBoostPercent: Math.round(clampNum(settings.bassBoostPercent, 0, 100)),
    preampDb: Math.round(clampNum(settings.preampDb, -12, 12)),
    virtualizerPercent: Math.round(
      clampNum(settings.virtualizerPercent, 0, 100),
    ),
  };
}

export function clampCrossfadeSettings(
  settings: CrossfadeSettings,
): CrossfadeSettings {
  return {
    enabled: Boolean(settings.enabled),
    durationSec: Math.round(clampNum(settings.durationSec, 0, 12)),
    fadeOnManualSkip: Boolean(settings.fadeOnManualSkip),
    gaplessEnabled: Boolean(settings.gaplessEnabled),
  };
}

/**
 * Computes the effective crossfade window in ms for a track of given duration.
 * Never consumes more than 40% of the track so short clips don't skip immediately.
 */
export function computeCrossfadeTriggerMs(
  durationMs: number,
  crossfade: CrossfadeSettings,
): number {
  if (!crossfade.enabled || crossfade.durationSec <= 0 || durationMs <= 3_000) {
    return 0;
  }
  const requestedMs = clampNum(crossfade.durationSec, 0, 12) * 1000;
  const safeMaxMs = Math.floor(durationMs * 0.4);
  return Math.min(requestedMs, safeMaxMs);
}

export function isEqPresetAccessible(
  _presetId: string,
  _isPro: boolean = true,
  _rewardUnlockExpiresAtMs: number | null = null,
  _nowMs: number = Date.now(),
): boolean {
  return true;
}

export function shouldShowRatingPrompt(
  sessionCount: number,
  hasDismissedOrRated: boolean,
): boolean {
  return sessionCount >= 5 && !hasDismissedOrRated;
}

export interface SmartPlaylistGroup {
  genre: SmartPlaylistGenre;
  tracks: OfflineTrack[];
}

export function buildGenreSmartPlaylists(
  tracks: OfflineTrack[],
): SmartPlaylistGroup[] {
  return SMART_PLAYLIST_GENRES.map((genre) => {
    const token = genre.toLowerCase();
    const matched = tracks.filter(
      (t) =>
        t.folderName.toLowerCase().includes(token) ||
        t.album.toLowerCase().includes(token) ||
        t.title.toLowerCase().includes(token) ||
        t.artist.toLowerCase().includes(token),
    );
    return { genre, tracks: matched };
  });
}

export interface StorageStats {
  totalTracks: number;
  bundledTracks: number;
  deviceTracks: number;
  bundledEstimatedBytes: number;
  deviceEstimatedBytes: number;
  totalEstimatedBytes: number;
}

export function estimateStorageStats(tracks: OfflineTrack[]): StorageStats {
  let bundledTracks = 0;
  let deviceTracks = 0;
  let bundledEstimatedBytes = 0;
  let deviceEstimatedBytes = 0;

  for (const t of tracks) {
    // ~24 KB/sec estimate (approx. 192 kbps offline audio)
    const bytes = Math.max(120_000, Math.round((t.durationMs / 1000) * 24_000));
    if (t.sourceType === 'BUNDLED') {
      bundledTracks++;
      bundledEstimatedBytes += bytes;
    } else {
      deviceTracks++;
      deviceEstimatedBytes += bytes;
    }
  }

  return {
    totalTracks: tracks.length,
    bundledTracks,
    deviceTracks,
    bundledEstimatedBytes,
    deviceEstimatedBytes,
    totalEstimatedBytes: bundledEstimatedBytes + deviceEstimatedBytes,
  };
}

const SIMPLE_EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validateSponsorPledge(input: {
  name: string;
  email: string;
  tierId: SponsorTierId;
  billing: SponsorBilling;
  note?: string;
}): { valid: boolean; error?: string; pledge?: SponsorPledge } {
  const cleanName = input.name.trim();
  const cleanEmail = input.email.trim().toLowerCase();
  const cleanNote = (input.note ?? '').trim();

  if (cleanName.length < 2) {
    return {
      valid: false,
      error: 'Enter your name or organization (at least 2 characters).',
    };
  }
  if (!SIMPLE_EMAIL_REGEX.test(cleanEmail)) {
    return {
      valid: false,
      error: 'Enter a valid email address for sponsorship confirmation.',
    };
  }
  const tier =
    SPONSOR_TIERS.find((t) => t.id === input.tierId) ?? SPONSOR_TIERS[0];
  const amountUsd =
    input.billing === 'monthly' ? tier.monthlyUsd : tier.oneTimeUsd;

  return {
    valid: true,
    pledge: {
      id: `pledge-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
      name: cleanName,
      email: cleanEmail,
      tierId: tier.id,
      billing: input.billing,
      amountUsd,
      note: cleanNote,
      createdAtIso: new Date().toISOString(),
    },
  };
}

export function validateBlogNotifyEmail(email: string): {
  valid: boolean;
  normalizedEmail?: string;
  error?: string;
} {
  const clean = email.trim().toLowerCase();
  if (!SIMPLE_EMAIL_REGEX.test(clean)) {
    return {
      valid: false,
      error: 'Enter a valid email address to get notified when the blog opens.',
    };
  }
  return { valid: true, normalizedEmail: clean };
}

export function validateLiveNotifyEmail(email: string): {
  valid: boolean;
  normalizedEmail?: string;
  error?: string;
} {
  const clean = email.trim().toLowerCase();
  if (!SIMPLE_EMAIL_REGEX.test(clean)) {
    return {
      valid: false,
      error: 'Enter a valid email address for Live Streaming alerts.',
    };
  }
  return { valid: true, normalizedEmail: clean };
}

export function validateProStreamingNotifyEmail(email: string): {
  valid: boolean;
  normalizedEmail?: string;
  error?: string;
} {
  const clean = email.trim().toLowerCase();
  if (!SIMPLE_EMAIL_REGEX.test(clean)) {
    return {
      valid: false,
      error: 'Enter a valid email address for Pro Global Streaming launch alerts.',
    };
  }
  return { valid: true, normalizedEmail: clean };
}

export function validateSupportRequestInput(input: {
  topic: string;
  message: string;
  deviceType: string;
}): {
  valid: boolean;
  error?: string;
  topic?: string;
  message?: string;
  deviceType?: 'android' | 'iphone' | 'windows-mac' | 'other';
} {
  const topic = input.topic.trim();
  const message = input.message.trim();
  const allowedDevices = ['android', 'iphone', 'windows-mac', 'other'] as const;
  const deviceType = allowedDevices.includes(
    input.deviceType as (typeof allowedDevices)[number],
  )
    ? (input.deviceType as (typeof allowedDevices)[number])
    : null;

  if (!deviceType) {
    return { valid: false, error: 'Choose your device type.' };
  }
  if (topic.length < 3 || topic.length > 120) {
    return {
      valid: false,
      error: 'Enter a short topic between 3 and 120 characters.',
    };
  }
  if (message.length < 10 || message.length > 1000) {
    return {
      valid: false,
      error: 'Describe how Support can help in 10 to 1000 characters.',
    };
  }
  return { valid: true, topic, message, deviceType };
}




