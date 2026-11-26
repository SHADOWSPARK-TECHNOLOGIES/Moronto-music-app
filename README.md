# Moronto Mornica

**Music for Real People · African Music at the Center**

Moronto Mornica is a 100% offline-first, Musicolet-style audio player built with React, TypeScript, Tailwind CSS, Web Audio API, and Express.

## Features

- **Multiple Resumable Queues (up to 20)**: Create, rename/delete, reorder, and switch between independent queues while preserving each queue's exact track and playback position across sessions.
- **6-Tab Offline Library**: Browse by `Queues`, `Tracks`, `Folders`, `Albums`, `Artists`, and `Playlists` with live search, multi-field sorting (`Title`, `Artist`, `Album`, `Duration`, `Year`), favorites, and custom playlists.
- **On-Device Audio Import**: Import `MP3`, `WAV`, `FLAC`, `M4A`, `OGG`, and `AAC` files directly into browser IndexedDB storage for persistent offline playback alongside bundled Nigerian synth demos.
- **5-Band Web Audio Equalizer & Bass Boost**: Real-time `BiquadFilterNode` EQ (`60Hz`, `230Hz`, `910Hz`, `3.6kHz`, `14kHz`), Bass Boost, and 6 presets (`Flat`, `Afrobeats Punch`, `Amapiano Log-Drum`, `Deep Bass`, `Vocal Clarity`, `Late Night`).
- **Tag Editor+ & Synced Lyrics**: Edit track metadata (`Title`, `Artist`, `Album`, `Folder`, `Year`) and `[mm:ss.xx]` LRC or plain lyrics with click-to-seek live lyric highlighting.
- **Transport, Speed & Sleep Timer**: Persistent mini-player and full Now Playing modal with `0.75x–1.5x` playback speed, shuffle, repeat (`OFF`, `ALL`, `ONE`), sleep timer countdown, and Web Media Session controls.

## Development

```bash
npm install
npm run dev
```

Runs on `http://0.0.0.0:3000`.

