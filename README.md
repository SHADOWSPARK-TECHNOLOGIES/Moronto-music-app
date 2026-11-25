# Moronto Mornica

**Music for Real People · African Music at the Center**

Moronto Mornica is a Nigeria-first music discovery, offline local audio player, and independent creator showcase web application built with React, TypeScript, Tailwind CSS, and Express.

## Features

- **My Music (Offline & Licensed Demo Catalogue)**
  - **Licensed Catalogue (`LicensedCatalogRights` gate)**: Fail-closed rights enforcement (`STREAM`, `SAVE_OFFLINE`, `PLAY_DOWNLOADED`) with exact host allowlisting, territory checks, license expiry checks, and hard YouTube URI blocking.
  - **Offline Caching**: Browser Cache API + `localStorage` rights-snapshot persistence (`DownloadStore` + `WebCacheOfflineReadinessVerifier`) with Wi-Fi-only preference and in-flight cancellation.
  - **Local Audio Library**: 5-tab Musicolet-style library (`Tracks`, `Queues`, `Albums`, `Artists`, `Folders`), live search, sorting (`Title`, `Artist`, `Duration`, `Year`), favorites, queue management, and on-device audio file scanning (`MP3`, `WAV`, `FLAC`, `M4A`, `OGG`) that stays strictly in-browser.
  - **Persistent Mini-Player & Full Now Playing Sheet**: HTML5 Audio + Web Media Session controls with shuffle, repeat (`OFF`, `ALL`, `ONE`), seek bar, and active offline queue.
- **Explore Videos (External YouTube Discovery)**
  - Official music video search and Nigerian artist filters (`Asake`, `Davido`, `Wizkid`, `Burna Boy`, `Rema`, `Omah Lay`, `Amapiano`) with 24h bounded caching and clearly labeled external YouTube links (`Watch on YouTube`). Never embeds, downloads, or extracts YouTube streams.
- **Creator Hub**
  - Independent creator demo profiles playing first-party synthesized previews (`/audio/mornica_afrobeats_demo.wav`), a validated commercial pilot inquiry form with master/publishing rights attestation, and privacy/sources disclosures.

## Development

```bash
npm install
npm run dev
```

Runs on `http://0.0.0.0:3000`.
