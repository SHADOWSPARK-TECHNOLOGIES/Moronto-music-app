import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';

const rulesSource = fs.readFileSync(new URL('./firestore.rules', import.meta.url), 'utf8');
const blueprint = JSON.parse(
  fs.readFileSync(new URL('./firebase-blueprint.json', import.meta.url), 'utf8'),
);

interface MockAuth {
  uid: string;
  token: { email_verified: boolean };
}

const REQUEST_TIME = '2026-11-26T10:00:00.000Z';

function isValidId(id: unknown): id is string {
  return (
    typeof id === 'string' &&
    id.length >= 1 &&
    id.length <= 128 &&
    /^[a-zA-Z0-9_\-]+$/.test(id)
  );
}

function hasExactKeys(obj: Record<string, unknown>, required: string[]): boolean {
  const keys = Object.keys(obj);
  return (
    required.every((k) => keys.includes(k)) &&
    keys.every((k) => required.includes(k))
  );
}

function affectedKeys(
  nextDoc: Record<string, unknown>,
  prevDoc: Record<string, unknown>,
): string[] {
  const all = new Set([...Object.keys(nextDoc), ...Object.keys(prevDoc)]);
  const changed: string[] = [];
  for (const k of all) {
    if (nextDoc[k] !== prevDoc[k]) {
      changed.push(k);
    }
  }
  return changed;
}

function canWriteProfile(
  mode: 'create' | 'update',
  auth: MockAuth | null,
  userId: string,
  incoming: Record<string, unknown>,
  existing?: Record<string, unknown>,
): boolean {
  if (!isValidId(userId) || !auth || !auth.token.email_verified || auth.uid !== userId) {
    return false;
  }
  const fields = blueprint.entities.UserProfile.required as string[];
  if (!hasExactKeys(incoming, fields)) return false;
  if (!isValidId(incoming.uid) || incoming.uid !== userId || incoming.uid !== auth.uid) {
    return false;
  }
  if (typeof incoming.displayName !== 'string' || incoming.displayName.length < 1 || incoming.displayName.length > 80) {
    return false;
  }
  if (typeof incoming.email !== 'string' || incoming.email.length < 3 || incoming.email.length > 160) {
    return false;
  }
  if (!['google.com', 'password', 'other'].includes(String(incoming.providerId))) return false;
  if (!['en', 'pcm', 'yo', 'ig', 'ha'].includes(String(incoming.language))) return false;
  if (
    !['emerald-console', 'sunset-brass', 'lagos-indigo', 'obsidian-rose'].includes(
      String(incoming.themeId),
    )
  ) {
    return false;
  }
  if (mode === 'create') {
    return incoming.createdAt === REQUEST_TIME && incoming.updatedAt === REQUEST_TIME;
  }
  if (!existing) return false;
  if (
    incoming.uid !== existing.uid ||
    incoming.email !== existing.email ||
    incoming.createdAt !== existing.createdAt ||
    incoming.updatedAt !== REQUEST_TIME
  ) {
    return false;
  }
  const changed = affectedKeys(incoming, existing);
  return changed.every((k) => ['displayName', 'language', 'themeId', 'updatedAt'].includes(k));
}

function canWriteSupportRequest(
  mode: 'create' | 'update',
  auth: MockAuth | null,
  requestId: string,
  incoming: Record<string, unknown>,
  existing?: Record<string, unknown>,
): boolean {
  if (!isValidId(requestId) || !auth || !auth.token.email_verified) return false;
  const fields = blueprint.entities.SupportRequest.required as string[];
  if (!hasExactKeys(incoming, fields)) return false;
  if (!isValidId(incoming.userId) || incoming.userId !== auth.uid) return false;
  if (typeof incoming.userEmail !== 'string' || incoming.userEmail.length < 3 || incoming.userEmail.length > 160) {
    return false;
  }
  if (typeof incoming.displayName !== 'string' || incoming.displayName.length < 1 || incoming.displayName.length > 80) {
    return false;
  }
  if (!['android', 'iphone', 'windows-mac', 'other'].includes(String(incoming.deviceType))) {
    return false;
  }
  if (typeof incoming.topic !== 'string' || incoming.topic.length < 3 || incoming.topic.length > 120) {
    return false;
  }
  if (typeof incoming.message !== 'string' || incoming.message.length < 10 || incoming.message.length > 1000) {
    return false;
  }
  if (!['open', 'resolved'].includes(String(incoming.status))) return false;

  if (mode === 'create') {
    return (
      incoming.status === 'open' &&
      incoming.createdAt === REQUEST_TIME &&
      incoming.updatedAt === REQUEST_TIME
    );
  }
  if (!existing) return false;
  if (existing.userId !== auth.uid || existing.status === 'resolved') return false;
  if (
    incoming.userId !== existing.userId ||
    incoming.userEmail !== existing.userEmail ||
    incoming.createdAt !== existing.createdAt ||
    incoming.updatedAt !== REQUEST_TIME
  ) {
    return false;
  }
  const changed = affectedKeys(incoming, existing);
  return changed.every((k) => ['message', 'updatedAt'].includes(k));
}

test('firestore.rules contains Eight Pillars hardening structures', () => {
  assert.match(rulesSource, /match \/\{document=\*\*\}\s*\{\s*allow read, write: if false;/);
  assert.match(rulesSource, /request\.auth\.token\.email_verified == true/);
  assert.match(rulesSource, /function isValidId\(id\)/);
  assert.match(rulesSource, /hasAll\(/);
  assert.match(rulesSource, /hasOnly\(/);
  assert.match(rulesSource, /affectedKeys\(\)\.hasOnly\(/);
  assert.match(rulesSource, /existing\(\)\.status != 'resolved'/);
});

test('Dirty Dozen adversarial payloads are rejected and valid requests pass', () => {
  const verifiedAuth: MockAuth = { uid: 'user-1', token: { email_verified: true } };
  const unverifiedAuth: MockAuth = { uid: 'user-1', token: { email_verified: false } };

  const validProfile = {
    uid: 'user-1',
    displayName: 'Teniola',
    email: 'teniola@moronto.app',
    providerId: 'google.com',
    language: 'pcm',
    themeId: 'sunset-brass',
    createdAt: REQUEST_TIME,
    updatedAt: REQUEST_TIME,
  };

  assert.equal(canWriteProfile('create', verifiedAuth, 'user-1', validProfile), true);
  // 1-2: Unauthenticated & unverified
  assert.equal(canWriteProfile('create', null, 'user-1', validProfile), false);
  assert.equal(canWriteProfile('create', unverifiedAuth, 'user-1', validProfile), false);
  // 3: Cross-user hijack
  assert.equal(canWriteProfile('create', verifiedAuth, 'user-2', { ...validProfile, uid: 'user-2' }), false);
  // 4: Shadow field injection
  assert.equal(canWriteProfile('create', verifiedAuth, 'user-1', { ...validProfile, isAdmin: true }), false);
  // 5: Path ID poisoning
  assert.equal(canWriteProfile('create', verifiedAuth, '../admin', validProfile), false);
  // 6: Oversized displayName
  assert.equal(
    canWriteProfile('create', verifiedAuth, 'user-1', { ...validProfile, displayName: 'x'.repeat(81) }),
    false,
  );
  // 7: Invalid enum
  assert.equal(
    canWriteProfile('create', verifiedAuth, 'user-1', { ...validProfile, themeId: 'hacked' }),
    false,
  );
  // 8: Forged timestamp
  assert.equal(
    canWriteProfile('create', verifiedAuth, 'user-1', { ...validProfile, createdAt: '1999-01-01T00:00:00.000Z' }),
    false,
  );
  // 9: Immutable field mutation on update
  assert.equal(
    canWriteProfile('update', verifiedAuth, 'user-1', { ...validProfile, email: 'attacker@evil.com' }, validProfile),
    false,
  );

  const validTicket = {
    userId: 'user-1',
    userEmail: 'teniola@moronto.app',
    displayName: 'Teniola',
    deviceType: 'android',
    topic: 'How to move MP3 files to Android Music folder',
    message: 'Abeg help me check where my downloaded songs hide on Android 14.',
    status: 'open',
    createdAt: REQUEST_TIME,
    updatedAt: REQUEST_TIME,
  };
  assert.equal(canWriteSupportRequest('create', verifiedAuth, 'req-1', validTicket), true);
  // 10: Pre-resolved support ticket
  assert.equal(
    canWriteSupportRequest('create', verifiedAuth, 'req-1', { ...validTicket, status: 'resolved' }),
    false,
  );
  // 11: Cross-user ticket creation
  assert.equal(
    canWriteSupportRequest('create', verifiedAuth, 'req-1', { ...validTicket, userId: 'user-2' }),
    false,
  );
  // 12: Post-resolution mutation
  assert.equal(
    canWriteSupportRequest(
      'update',
      verifiedAuth,
      'req-1',
      { ...validTicket, status: 'resolved', message: 'Trying to overwrite closed ticket' },
      { ...validTicket, status: 'resolved' },
    ),
    false,
  );
});
