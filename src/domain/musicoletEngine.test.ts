import test from 'node:test';
import assert from 'node:assert/strict';
import {
  BUNDLED_OFFLINE_TRACKS,
  DEFAULT_QUEUES,
  MAX_QUEUES,
  RepeatMode,
  SortOption,
  parseLrcLyrics,
} from '../model/musicoletModels';
import {
  addTracksToQueue,
  applyTrackTags,
  buildGenreSmartPlaylists,
  clampCrossfadeSettings,
  clampEqualizerSettings,
  computeCrossfadeTriggerMs,
  computeNextQueueTrack,
  computePreviousQueueTrack,
  createCustomPlaylist,
  createNewQueue,
  deleteCustomPlaylist,
  deleteQueueById,
  estimateStorageStats,
  filterAndSortTracks,
  isEqPresetAccessible,
  moveQueueTrack,
  removeTrackFromQueue,
  shouldShowRatingPrompt,
  switchQueueWithResume,
  validateBlogNotifyEmail,
  validateLiveNotifyEmail,
  validateProStreamingNotifyEmail,
  validateSponsorPledge,
  validateSupportRequestInput,
} from './musicoletEngine';

test('switchQueueWithResume saves outgoing queue positionMs and restores target queue', () => {
  const { queues, activeQueue } = switchQueueWithResume(
    DEFAULT_QUEUES,
    'queue-1',
    'queue-2',
    14_500,
  );
  assert.equal(activeQueue.id, 'queue-2');
  assert.equal(activeQueue.currentTrackId, 'track-lekki-logdrum');
  const q1 = queues.find((q) => q.id === 'queue-1');
  assert.equal(q1?.positionMs, 14_500);
});

test('createNewQueue enforces MAX_QUEUES limit and non-empty names', () => {
  const empty = createNewQueue(DEFAULT_QUEUES, '   ');
  assert.equal(empty.created, null);

  let current = [...DEFAULT_QUEUES];
  for (let i = current.length; i < MAX_QUEUES; i++) {
    const res = createNewQueue(current, `Extra ${i}`);
    assert.ok(res.created);
    current = res.queues;
  }
  const overflow = createNewQueue(current, 'Too Many');
  assert.equal(overflow.created, null);
  assert.ok(overflow.error?.includes('20'));
});

test('deleteQueueById refuses to delete the last remaining queue', () => {
  const single = [DEFAULT_QUEUES[0]];
  const res = deleteQueueById(single, 'queue-1', 'queue-1');
  assert.equal(res.deleted, false);
  assert.equal(res.queues.length, 1);
});

test('addTracksToQueue with playNextAfterCurrent inserts immediately after currentTrackId', () => {
  const updated = addTracksToQueue(
    DEFAULT_QUEUES,
    'queue-2',
    ['track-abuja-highlife'],
    true,
  );
  const q2 = updated.find((q) => q.id === 'queue-2');
  assert.deepEqual(q2?.trackIds, [
    'track-lekki-logdrum',
    'track-abuja-highlife',
    'track-eko-neon',
  ]);
});

test('moveQueueTrack and removeTrackFromQueue update order and fallback currentTrackId', () => {
  const moved = moveQueueTrack(DEFAULT_QUEUES, 'queue-2', 0, 1);
  assert.deepEqual(
    moved.find((q) => q.id === 'queue-2')?.trackIds,
    ['track-eko-neon', 'track-lekki-logdrum'],
  );

  const removed = removeTrackFromQueue(
    DEFAULT_QUEUES,
    'queue-2',
    'track-lekki-logdrum',
  );
  const q2 = removed.find((q) => q.id === 'queue-2');
  assert.deepEqual(q2?.trackIds, ['track-eko-neon']);
  assert.equal(q2?.currentTrackId, 'track-eko-neon');
});

test('computeNextQueueTrack and computePreviousQueueTrack respect repeat modes and restart threshold', () => {
  const q2 = DEFAULT_QUEUES[1]; // ['track-lekki-logdrum', 'track-eko-neon'], current='track-lekki-logdrum'
  const next1 = computeNextQueueTrack(q2, RepeatMode.OFF, false, false);
  assert.equal(next1.nextTrackId, 'track-eko-neon');

  const atEnd = { ...q2, currentTrackId: 'track-eko-neon' };
  assert.equal(
    computeNextQueueTrack(atEnd, RepeatMode.OFF, false, true).shouldStop,
    true,
  );
  assert.equal(
    computeNextQueueTrack(atEnd, RepeatMode.ALL, false, true).nextTrackId,
    'track-lekki-logdrum',
  );
  assert.equal(
    computeNextQueueTrack(atEnd, RepeatMode.ONE, false, true).restartCurrent,
    true,
  );

  const prevRestart = computePreviousQueueTrack(atEnd, RepeatMode.OFF, 5_000);
  assert.equal(prevRestart.restartCurrent, true);
  assert.equal(prevRestart.prevTrackId, 'track-eko-neon');

  const prevTrack = computePreviousQueueTrack(atEnd, RepeatMode.OFF, 1_000);
  assert.equal(prevTrack.restartCurrent, false);
  assert.equal(prevTrack.prevTrackId, 'track-lekki-logdrum');
});

test('filterAndSortTracks and applyTrackTags update metadata cleanly', () => {
  const filtered = filterAndSortTracks(
    BUNDLED_OFFLINE_TRACKS,
    'lekki',
    SortOption.TITLE,
  );
  assert.equal(filtered.length, 1);
  assert.equal(filtered[0].id, 'track-lekki-logdrum');

  const tagged = applyTrackTags(BUNDLED_OFFLINE_TRACKS, 'track-eko-neon', {
    title: 'Eko Pulse (Remastered)',
    artist: 'Teniola Blaze',
    album: 'Victoria Island EP',
    folderName: 'Lagos WAVs',
    releaseYear: 2026,
  });
  const updated = tagged.find((t) => t.id === 'track-eko-neon');
  assert.equal(updated?.title, 'Eko Pulse (Remastered)');
  assert.equal(updated?.album, 'Victoria Island EP');
});

test('parseLrcLyrics parses synced LRC timestamps accurately', () => {
  const lines = parseLrcLyrics(
    '[00:05.50] Midnight rhythm\n[00:12.00] Zero data needed',
  );
  assert.equal(lines.length, 2);
  assert.equal(lines[0].timeMs, 5_500);
  assert.equal(lines[0].text, 'Midnight rhythm');
  assert.equal(lines[1].timeMs, 12_000);
});

test('shuffle visits every track in even-length queues without getting stuck', () => {
  for (const len of [2, 4, 6]) {
    const ids = Array.from({ length: len }, (_, i) => `t-${i}`);
    const visited = new Set<string>([ids[0]]);
    let current = ids[0];
    for (let step = 0; step < len - 1; step++) {
      const res = computeNextQueueTrack(
        { id: 'q', name: 'Q', trackIds: ids, currentTrackId: current, positionMs: 0 },
        RepeatMode.ALL,
        true,
        false,
      );
      assert.ok(res.nextTrackId);
      current = res.nextTrackId;
      visited.add(current);
    }
    assert.equal(visited.size, len);
  }
});

test('createCustomPlaylist and deleteCustomPlaylist manage custom playlists cleanly', () => {
  const created = createCustomPlaylist([], 'Afrobeats Cruise');
  assert.equal(created.playlists.length, 1);
  const id = created.created!.id;
  const remaining = deleteCustomPlaylist(created.playlists, id);
  assert.equal(remaining.length, 0);
});

test('clampEqualizerSettings and clampCrossfadeSettings enforce safe 10-band and 0-12s ranges', () => {
  const eq = clampEqualizerSettings({
    enabled: true,
    presetId: 'custom',
    bandsDb: [20, -25, 4, 0, 12, -2, 5, 19, -20, 3],
    bassBoostPercent: 140,
    preampDb: -18,
    virtualizerPercent: 120,
  });
  assert.deepEqual(eq.bandsDb, [12, -12, 4, 0, 12, -2, 5, 12, -12, 3]);
  assert.equal(eq.bassBoostPercent, 100);
  assert.equal(eq.preampDb, -12);
  assert.equal(eq.virtualizerPercent, 100);

  const cfMax = clampCrossfadeSettings({
    enabled: true,
    durationSec: 25,
    fadeOnManualSkip: true,
    gaplessEnabled: false,
  });
  assert.equal(cfMax.durationSec, 12);

  const cfZero = clampCrossfadeSettings({
    enabled: true,
    durationSec: -4,
    fadeOnManualSkip: false,
    gaplessEnabled: true,
  });
  assert.equal(cfZero.durationSec, 0);
});

test('computeCrossfadeTriggerMs caps overlap at 40% of track length', () => {
  const triggerLong = computeCrossfadeTriggerMs(30_000, {
    enabled: true,
    durationSec: 6,
    fadeOnManualSkip: true,
    gaplessEnabled: true,
  });
  assert.equal(triggerLong, 6_000);

  const triggerShort = computeCrossfadeTriggerMs(10_000, {
    enabled: true,
    durationSec: 8,
    fadeOnManualSkip: true,
    gaplessEnabled: true,
  });
  assert.equal(triggerShort, 4_000);

  const triggerOff = computeCrossfadeTriggerMs(30_000, {
    enabled: false,
    durationSec: 6,
    fadeOnManualSkip: true,
    gaplessEnabled: true,
  });
  assert.equal(triggerOff, 0);
});

test('validateSponsorPledge enforces name and email and resolves tier pricing', () => {
  const invalidName = validateSponsorPledge({
    name: 'A',
    email: 'patron@example.com',
    tierId: 'patron',
    billing: 'monthly',
  });
  assert.equal(invalidName.valid, false);

  const invalidEmail = validateSponsorPledge({
    name: 'Lagos Studio Collective',
    email: 'not-an-email',
    tierId: 'patron',
    billing: 'monthly',
  });
  assert.equal(invalidEmail.valid, false);

  const valid = validateSponsorPledge({
    name: 'Lagos Studio Collective',
    email: 'hello@lagosstudio.ng',
    tierId: 'patron',
    billing: 'one-time',
    note: 'Keep African offline audio independent.',
  });
  assert.equal(valid.valid, true);
  assert.equal(valid.pledge?.amountUsd, 45);
  assert.equal(valid.pledge?.email, 'hello@lagosstudio.ng');
});

test('validateBlogNotifyEmail and validateLiveNotifyEmail validate cleanly', () => {
  assert.equal(validateBlogNotifyEmail('invalid').valid, false);
  const ok = validateBlogNotifyEmail('  Listener@Moronto.app  ');
  assert.equal(ok.valid, true);
  assert.equal(ok.normalizedEmail, 'listener@moronto.app');

  assert.equal(validateLiveNotifyEmail('bad-email').valid, false);
  const liveOk = validateLiveNotifyEmail('  LiveFan@Moronto.ng ');
  assert.equal(liveOk.valid, true);
  assert.equal(liveOk.normalizedEmail, 'livefan@moronto.ng');
});

test('isEqPresetAccessible unlocks all 10-band EQ presets for every listener', () => {
  assert.equal(isEqPresetAccessible('flat', false, null, 1000), true);
  assert.equal(isEqPresetAccessible('afrobeats-bass', false, null, 1000), true);
  assert.equal(isEqPresetAccessible('amapiano-log-drum', false, null, 1000), true);
  assert.equal(isEqPresetAccessible('gospel', false, null, 1000), true);
  assert.equal(isEqPresetAccessible('highlife', false, null, 1000), true);
});

test('buildGenreSmartPlaylists builds Afrobeats, Amapiano, Gospel, Highlife groups', () => {
  const smart = buildGenreSmartPlaylists(BUNDLED_OFFLINE_TRACKS);
  assert.equal(smart.length, 4);
  for (const group of smart) {
    assert.ok(group.tracks.length > 0, `Expected tracks for ${group.genre}`);
  }
});

test('shouldShowRatingPrompt triggers after 5th session unless dismissed', () => {
  assert.equal(shouldShowRatingPrompt(4, false), false);
  assert.equal(shouldShowRatingPrompt(5, false), true);
  assert.equal(shouldShowRatingPrompt(6, true), false);
});

test('estimateStorageStats calculates bundled and device storage totals', () => {
  const stats = estimateStorageStats(BUNDLED_OFFLINE_TRACKS);
  assert.equal(stats.bundledTracks, BUNDLED_OFFLINE_TRACKS.length);
  assert.equal(stats.deviceTracks, 0);
  assert.ok(stats.totalEstimatedBytes > 0);
});

test('validateSupportRequestInput and validateProStreamingNotifyEmail validate bounds cleanly', () => {
  assert.equal(validateProStreamingNotifyEmail('bad').valid, false);
  const proOk = validateProStreamingNotifyEmail(' Listener@MorontoMusic.App ');
  assert.equal(proOk.valid, true);
  assert.equal(proOk.normalizedEmail, 'listener@morontomusic.app');

  const shortMsg = validateSupportRequestInput({
    topic: 'MP3',
    message: 'too short',
    deviceType: 'android',
  });
  assert.equal(shortMsg.valid, false);

  const badDevice = validateSupportRequestInput({
    topic: 'Finding files',
    message: 'Where can I find downloaded audio files on my phone?',
    deviceType: 'toaster',
  });
  assert.equal(badDevice.valid, false);

  const ok = validateSupportRequestInput({
    topic: 'Android downloads folder help',
    message: 'Please show me how to select my audio files inside Telegram Documents.',
    deviceType: 'android',
  });
  assert.equal(ok.valid, true);
  assert.equal(ok.deviceType, 'android');
});



