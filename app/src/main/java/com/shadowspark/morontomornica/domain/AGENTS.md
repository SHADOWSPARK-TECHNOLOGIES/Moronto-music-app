# Gate & router (domain package) — nearest-file rules

This nested file binds edits to `LicensedCatalogRights`, `LicensedCatalogRepository` /
`LicensedCatalogConfig`, `PlaybackRouter`, and the Media3 controller in this package. The closest
`AGENTS.md` wins; these rules override the root file when they conflict.

## License / catalog gate invariants
- **Default-deny.** `evaluate()` returns ALLOW only when: `rightsConfirmed == true` AND track
  territory matches the supplied territory AND not expired AND `streamAllowed` (AND
  `offlineAllowed` / `downloadCompleted` for offline/PLAY_DOWNLOADED actions) AND the media host is
  in the allowlist. Anything not explicitly authorized is DENIED. Do not weaken this.
- The host allowlist is configuration, never derived from a track record. The gate must hard-block
  `youtube.com`, `youtu.be`, `googlevideo.com` and their subdomains even if someone tries to add
  them to the allowlist.
- The gate is PURE: no side effects, no network, no I/O, no time source other than the injected
  `nowEpochMs` / `clockMs`.

## Media3 wiring invariants
- No MediaItem reaches ExoPlayer unless its catalog access was ALLOW. Defense in depth: the
  service-layer `Media3LocalAudioController.playMediaItems()` also rejects YouTube URIs.
- `isLocalPlaying` is set ONLY by `onPlayerIsPlayingChanged` (the controller callback), never
  optimistically inside `playCatalogTrack` / `playLocalTrack`.
- Catalog MediaIds use the `catalog:<id>` prefix; local tracks use their plain id.

## Data adapter invariants
- `rightsConfirmed` may be true ONLY from a trusted backend confirming a real agreement; demo
  fixtures are visibly labeled `DEMO` and use `licenseReference = "DEMO-*"`.
- Never hardcode a streaming host outside `LicensedCatalogConfig`.
