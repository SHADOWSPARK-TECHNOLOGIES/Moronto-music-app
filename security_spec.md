# Moronto Music — Firestore Security Specification (Phase 0)

## 1. Data Invariants
1. **Default-Deny Catch-All**: Any path outside `/users/{userId}/private/profile` and `/support_requests/{requestId}` MUST deny both reads and writes.
2. **Verified Identity**: All reads and writes to Firestore collections require `request.auth != null && request.auth.token.email_verified == true`.
3. **Strict Ownership & PII Isolation**:
   - `/users/{userId}/private/profile` holds user PII (`email`) and may ONLY be read or written when `request.auth.uid == userId`. No `list` operations allowed.
   - `/support_requests/{requestId}` holds support tickets and may ONLY be read (`get` or `list` via `resource.data.userId == request.auth.uid`) or updated by the owning `userId`.
4. **Immutable Identity & Temporal Integrity**: `uid`/`userId` and `createdAt` cannot be modified after creation. `createdAt == request.time` on create; `updatedAt == request.time` on update.
5. **Terminal State Lock**: Resolved support requests (`status == 'resolved'`) cannot be modified by clients.

## 2. Dirty Dozen Adversarial Payloads (Must Return PERMISSION_DENIED)
1. **Unauthenticated read** on `/users/u1/private/profile`.
2. **Unverified email write** (`email_verified: false`) creating `/users/u1/private/profile`.
3. **Cross-user profile hijack** (`uid: "u2"` written by authenticated user `"u1"`).
4. **Shadow field injection** on profile create (adding `isAdmin: true`).
5. **Path ID poisoning** with oversized 200-char or invalid-char document ID (`../admin`).
6. **Oversized displayName** (> 80 chars) attempting storage exhaustion.
7. **Invalid themeId/language enum** (`themeId: "hacked"`, `language: "xx"`).
8. **Forged client timestamp** (`createdAt` set in past rather than `request.time`).
9. **Immutable field mutation** changing `createdAt` or `uid` on profile update.
10. **Support ticket creation with pre-resolved status** (`status: "resolved"` on create).
11. **Cross-user support ticket read/list** without matching `userId == request.auth.uid`.
12. **Post-resolution mutation** updating `message` on a `resolved` support ticket.
