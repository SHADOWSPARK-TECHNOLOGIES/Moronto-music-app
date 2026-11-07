# Capability Contract — `LicensedCatalogRights`

A capability is "ready" only when all three contracts are present:
**Behavior** (states/transitions/guards), **Data** (machine-parseable types), and
**Authorization** (default-deny, scopes, pending decisions). Posture: **default-deny** — anything
not explicitly listed is denied. Current status: BEHAVIOR implemented, DATA implemented (types),
AUTHORIZATION implemented for STREAM/SAVE_OFFLINE/PLAY_DOWNLOADED (Loop 3 verification pending).

## 1. Behavior contract
State machine (per track, from an authorized backend standpoint):
`IDLE → AUTHORIZED → STREAMING → OFFLINE_SAVED`

Transitions are guarded by the gate:
- `IDLE → AUTHORIZED`: `rightsConfirmed && territoryMatch && !expired && streamAllowed`
- `AUTHORIZED → STREAMING`: `AUTHORIZED && hostAllowlisted(streamUrl)` (STREAM)
- `AUTHORIZED → OFFLINE_SAVED`: `AUTHORIZED && offlineAllowed && downloadCompleted` (SAVE_OFFLINE →
  PLAY_DOWNLOADED)

Safety invariants (must be enforced by any agent claim of "this capability works"):
1. No MediaItem reaches ExoPlayer unless the gate returned ALLOW for that action.
2. YouTube / googlevideo / youtu.be hosts are denied even if present in the allowlist.
3. `rightsConfirmed` is only ever true from a trusted backend with a real agreement — never
   fabricated client-side.
4. The gate is pure and deterministic for a fixed clock + data set.

## 2. Data contract
Type `LicensedCatalogTrack`:
- `id: String` (unique)
- `mediaUrl: String` (https only; host must be allowlisted)
- `sourceLabel: String` (human label incl. DEMO/LICENSED)
- `licenseReference: String` (blank => MISSING_LICENSE)
- `allowedTerritories: Set<String>` (uppercase ISO 3166-1 alpha-2)
- `licenseExpiresAtEpochMs: Long?` (null/elapsed => LICENSE_EXPIRED)
- `rightsConfirmed: Boolean` (false => UNVERIFIED_RIGHTS)
- `streamAllowed: Boolean` (false => STREAM_NOT_AUTHORIZED)
- `offlineAllowed: Boolean` (false for offline actions => OFFLINE_NOT_AUTHORIZED)

Actions enum `CatalogAccess`: `STREAM`, `SAVE_OFFLINE`, `PLAY_DOWNLOADED`.
Decision `CatalogAccessDecision(allowed: Boolean, denial: CatalogDenial)`.
`CatalogDenial`: NONE, INVALID_SOURCE, MISSING_LICENSE, UNVERIFIED_RIGHTS, TERRITORY_UNAVAILABLE,
LICENSE_EXPIRED, STREAM_NOT_AUTHORIZED, OFFLINE_NOT_AUTHORIZED, DOWNLOAD_INCOMPLETE.

## 3. Authorization contract
Scopes (actions): `STREAM`, `SAVE_OFFLINE`, `PLAY_DOWNLOADED`.
- A scope is GRANTED only to a track with a verified `licenseReference` where:
  - `STREAM`: rightsConfirmed && in-territory && unexpired && streamAllowed && allowlisted host.
  - `SAVE_OFFLINE`: all of STREAM **plus** `offlineAllowed`.
  - `PLAY_DOWNLOADED`: all of SAVE_OFFLINE **plus** `downloadCompleted == true`.
- Anything not listed (unknown source, missing rights, wrong territory, expired, stream-only for
  offline actions, YouTube host) is DENIED — the gate returns the specific `CatalogDenial`.
- There are no pending/undecided grants; every request returns ALLOW or a concrete DENIAL.

## Verification record
- Unit: `LicensedCatalogRightsTest` (7 tests) — covered alongside Loop 3 per its Sprint Contract.
- Device: offline download + airplane-mode PLAY_DOWNLOADED to be proven in Loop 3.
