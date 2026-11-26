import {
  type CrossfadeSettings,
  DEFAULT_CROSSFADE_SETTINGS,
  EQ_BAND_FREQUENCIES,
  type EqualizerSettings,
} from '../model/musicoletModels';
import { computeCrossfadeTriggerMs } from './musicoletEngine';

const EQ_FREQUENCIES = EQ_BAND_FREQUENCIES;

export interface WebAudioCallbacks {
  onPlayingChange: (isPlaying: boolean) => void;
  onTimeUpdate: (positionMs: number, durationMs: number) => void;
  onNearEnd: () => void;
  onEnded: () => void;
  onError: (message: string) => void;
  onPrev: () => void;
  onNext: () => void;
  onSeek: (positionMs: number) => void;
}

function dbToLinearGain(db: number): number {
  return Math.pow(10, db / 20);
}

export class WebAudioPlayer {
  private readonly decks: [HTMLAudioElement, HTMLAudioElement];
  private activeDeckIndex: 0 | 1 = 0;
  private audioCtx: AudioContext | null = null;
  private deckGains: [GainNode, GainNode] | null = null;
  private preampNode: GainNode | null = null;
  private bassNode: BiquadFilterNode | null = null;
  private eqFilters: BiquadFilterNode[] = [];
  private virtualizerWetGain: GainNode | null = null;
  private eqWired = false;
  private pendingStartPositions: [number, number] = [0, 0];
  private nearEndFired = false;
  private crossfadeSettings: CrossfadeSettings = DEFAULT_CROSSFADE_SETTINGS;
  private outgoingStopTimer: number | null = null;
  private fallbackFadeTimers: number[] = [];
  private callbacks: WebAudioCallbacks | null = null;

  constructor() {
    this.decks = [new Audio(), new Audio()];
    this.decks.forEach((deck, index) => {
      const idx = index as 0 | 1;
      deck.preload = 'metadata';

      deck.addEventListener('playing', () => {
        if (this.activeDeckIndex === idx) {
          this.callbacks?.onPlayingChange(true);
        }
      });

      deck.addEventListener('pause', () => {
        if (this.activeDeckIndex === idx) {
          this.callbacks?.onPlayingChange(false);
        }
      });

      deck.addEventListener('ended', () => {
        if (this.activeDeckIndex === idx) {
          this.callbacks?.onEnded();
        }
      });

      deck.addEventListener('loadedmetadata', () => {
        const pendingMs = this.pendingStartPositions[idx];
        if (pendingMs > 0) {
          const targetSec = pendingMs / 1000;
          const maxSec = Number.isFinite(deck.duration) ? deck.duration : targetSec;
          deck.currentTime = Math.min(targetSec, Math.max(0, maxSec - 0.5));
          this.pendingStartPositions[idx] = 0;
        }
        if (this.activeDeckIndex === idx) {
          const durMs = Number.isFinite(deck.duration)
            ? Math.round(deck.duration * 1000)
            : 0;
          if (durMs > 0) {
            this.callbacks?.onTimeUpdate(Math.round(deck.currentTime * 1000), durMs);
          }
        }
      });

      deck.addEventListener('timeupdate', () => {
        if (this.activeDeckIndex !== idx) return;
        const posMs = Math.round(deck.currentTime * 1000);
        const durMs = Number.isFinite(deck.duration)
          ? Math.round(deck.duration * 1000)
          : 0;
        this.callbacks?.onTimeUpdate(posMs, durMs);

        if (
          this.crossfadeSettings.enabled &&
          !this.nearEndFired &&
          durMs > 3_000 &&
          !deck.paused
        ) {
          const triggerMs = computeCrossfadeTriggerMs(durMs, this.crossfadeSettings);
          if (triggerMs > 0 && posMs >= 1_500 && durMs - posMs <= triggerMs) {
            this.nearEndFired = true;
            this.callbacks?.onNearEnd();
          }
        }
      });

      deck.addEventListener('error', () => {
        if (this.activeDeckIndex === idx) {
          this.callbacks?.onError('Unable to decode this local audio file.');
        }
      });
    });
  }

  private get activeDeck(): HTMLAudioElement {
    return this.decks[this.activeDeckIndex];
  }

  bindCallbacks(callbacks: WebAudioCallbacks): void {
    this.callbacks = callbacks;
    if ('mediaSession' in navigator) {
      navigator.mediaSession.setActionHandler('play', () => this.resume());
      navigator.mediaSession.setActionHandler('pause', () => this.pause());
      navigator.mediaSession.setActionHandler('previoustrack', () =>
        this.callbacks?.onPrev(),
      );
      navigator.mediaSession.setActionHandler('nexttrack', () =>
        this.callbacks?.onNext(),
      );
      navigator.mediaSession.setActionHandler('seekto', (details) => {
        if (typeof details.seekTime === 'number') {
          this.callbacks?.onSeek(Math.round(details.seekTime * 1000));
        }
      });
    }
  }

  setCrossfadeSettings(settings: CrossfadeSettings): void {
    this.crossfadeSettings = settings;
  }

  private ensureEqGraph(settings: EqualizerSettings): void {
    if (this.eqWired) {
      this.applyEqualizer(settings);
      return;
    }
    try {
      const Ctx =
        window.AudioContext ||
        (window as unknown as { webkitAudioContext?: typeof AudioContext })
          .webkitAudioContext;
      if (!Ctx) return;

      const ctx = new Ctx();
      const src0 = ctx.createMediaElementSource(this.decks[0]);
      const src1 = ctx.createMediaElementSource(this.decks[1]);

      const gain0 = ctx.createGain();
      const gain1 = ctx.createGain();
      gain0.gain.value = this.activeDeckIndex === 0 ? 1 : 0;
      gain1.gain.value = this.activeDeckIndex === 1 ? 1 : 0;

      const preamp = ctx.createGain();
      preamp.gain.value = 1;

      const bass = ctx.createBiquadFilter();
      bass.type = 'lowshelf';
      bass.frequency.value = 95;
      bass.gain.value = 0;

      const bands = EQ_FREQUENCIES.map((freq, idx) => {
        const filter = ctx.createBiquadFilter();
        filter.type =
          idx === 0
            ? 'lowshelf'
            : idx === EQ_FREQUENCIES.length - 1
              ? 'highshelf'
              : 'peaking';
        filter.frequency.value = freq;
        filter.Q.value = 1.1;
        filter.gain.value = 0;
        return filter;
      });

      // Subtle spatial virtualizer branch (short Haas-style ambience)
      const delay = ctx.createDelay(0.05);
      delay.delayTime.value = 0.011;
      const virtHighpass = ctx.createBiquadFilter();
      virtHighpass.type = 'highpass';
      virtHighpass.frequency.value = 320;
      const virtGain = ctx.createGain();
      virtGain.gain.value = 0;

      // Safety limiter to avoid clipping during EQ boost + crossfade overlap
      const limiter = ctx.createDynamicsCompressor();
      limiter.threshold.value = -1.5;
      limiter.knee.value = 6;
      limiter.ratio.value = 8;
      limiter.attack.value = 0.003;
      limiter.release.value = 0.15;

      src0.connect(gain0);
      src1.connect(gain1);
      gain0.connect(preamp);
      gain1.connect(preamp);

      let node: AudioNode = preamp;
      node.connect(bass);
      node = bass;
      for (const band of bands) {
        node.connect(band);
        node = band;
      }

      // Dry path + spatial wet path -> limiter -> speakers
      node.connect(limiter);
      node.connect(virtHighpass);
      virtHighpass.connect(delay);
      delay.connect(virtGain);
      virtGain.connect(limiter);

      limiter.connect(ctx.destination);

      this.audioCtx = ctx;
      this.deckGains = [gain0, gain1];
      this.preampNode = preamp;
      this.bassNode = bass;
      this.eqFilters = bands;
      this.virtualizerWetGain = virtGain;
      this.eqWired = true;
      this.applyEqualizer(settings);
    } catch {
      // Fallback: plain HTMLAudioElement playback without Web Audio EQ
    }
  }

  applyEqualizer(settings: EqualizerSettings): void {
    if (!this.eqWired) return;
    const active = settings.enabled;
    this.eqFilters.forEach((filter, idx) => {
      filter.gain.value = active ? (settings.bandsDb[idx] ?? 0) : 0;
    });
    if (this.bassNode) {
      this.bassNode.gain.value = active
        ? (settings.bassBoostPercent / 100) * 8
        : 0;
    }
    if (this.preampNode) {
      this.preampNode.gain.value = active
        ? dbToLinearGain(settings.preampDb)
        : 1;
    }
    if (this.virtualizerWetGain) {
      this.virtualizerWetGain.gain.value = active
        ? (settings.virtualizerPercent / 100) * 0.22
        : 0;
    }
  }

  setPlaybackRate(rate: number): void {
    const safeRate = Math.max(0.5, Math.min(2.0, rate));
    this.decks[0].playbackRate = safeRate;
    this.decks[1].playbackRate = safeRate;
  }

  private clearFadeTimers(): void {
    if (this.outgoingStopTimer !== null) {
      window.clearTimeout(this.outgoingStopTimer);
      this.outgoingStopTimer = null;
    }
    for (const id of this.fallbackFadeTimers) {
      window.clearInterval(id);
    }
    this.fallbackFadeTimers = [];
  }

  private stopDeckImmediate(idx: 0 | 1): void {
    const deck = this.decks[idx];
    deck.pause();
    deck.volume = 1;
    if (this.audioCtx && this.deckGains) {
      const gain = this.deckGains[idx].gain;
      gain.cancelScheduledValues(this.audioCtx.currentTime);
      gain.setValueAtTime(0, this.audioCtx.currentTime);
    }
  }

  loadAndPlay(
    uri: string,
    metadata: { title: string; artist: string; album: string },
    startPositionMs: number,
    playbackRate: number,
    eqSettings: EqualizerSettings,
    autoPlay: boolean,
    crossfadeDurationSec: number = 0,
  ): void {
    this.ensureEqGraph(eqSettings);
    if (this.audioCtx && this.audioCtx.state === 'suspended') {
      void this.audioCtx.resume().catch(() => {});
    }

    this.clearFadeTimers();
    this.nearEndFired = false;

    const outgoingIdx = this.activeDeckIndex;
    const outgoingDeck = this.decks[outgoingIdx];
    const shouldCrossfade =
      autoPlay && crossfadeDurationSec > 0 && !outgoingDeck.paused;

    if (shouldCrossfade) {
      const incomingIdx: 0 | 1 = outgoingIdx === 0 ? 1 : 0;
      const incomingDeck = this.decks[incomingIdx];
      this.activeDeckIndex = incomingIdx;

      this.prepareDeck(incomingDeck, incomingIdx, uri, startPositionMs, playbackRate);

      const fadeSec = Math.max(0.5, crossfadeDurationSec);
      if (this.audioCtx && this.deckGains) {
        const now = this.audioCtx.currentTime;
        const outGain = this.deckGains[outgoingIdx].gain;
        const inGain = this.deckGains[incomingIdx].gain;
        outGain.cancelScheduledValues(now);
        inGain.cancelScheduledValues(now);
        outGain.setValueAtTime(outGain.value, now);
        outGain.linearRampToValueAtTime(0, now + fadeSec);
        inGain.setValueAtTime(0.001, now);
        inGain.linearRampToValueAtTime(1, now + fadeSec);
        incomingDeck.volume = 1;
      } else {
        // Fallback element-volume crossfade
        incomingDeck.volume = 0;
        const steps = Math.max(8, Math.round(fadeSec * 15));
        let step = 0;
        const stepMs = (fadeSec * 1000) / steps;
        const timerId = window.setInterval(() => {
          step++;
          const ratio = Math.min(1, step / steps);
          outgoingDeck.volume = Math.max(0, 1 - ratio);
          incomingDeck.volume = ratio;
          if (step >= steps) {
            window.clearInterval(timerId);
          }
        }, stepMs);
        this.fallbackFadeTimers.push(timerId);
      }

      this.outgoingStopTimer = window.setTimeout(() => {
        this.stopDeckImmediate(outgoingIdx);
        this.outgoingStopTimer = null;
      }, fadeSec * 1000 + 80);

      this.updateMediaSession(metadata);
      incomingDeck.play().catch(() => {
        this.callbacks?.onPlayingChange(false);
      });
      return;
    }

    // Standard immediate transition (no crossfade overlap)
    const otherIdx: 0 | 1 = outgoingIdx === 0 ? 1 : 0;
    this.stopDeckImmediate(otherIdx);

    if (this.audioCtx && this.deckGains) {
      const now = this.audioCtx.currentTime;
      const activeGain = this.deckGains[outgoingIdx].gain;
      activeGain.cancelScheduledValues(now);
      activeGain.setValueAtTime(1, now);
    }
    outgoingDeck.volume = 1;

    this.prepareDeck(outgoingDeck, outgoingIdx, uri, startPositionMs, playbackRate);
    this.updateMediaSession(metadata);

    if (autoPlay) {
      outgoingDeck.play().catch(() => {
        this.callbacks?.onPlayingChange(false);
      });
    }
  }

  private prepareDeck(
    deck: HTMLAudioElement,
    idx: 0 | 1,
    uri: string,
    startPositionMs: number,
    playbackRate: number,
  ): void {
    const sameSrc = deck.getAttribute('data-track-uri') === uri;
    deck.playbackRate = Math.max(0.5, Math.min(2.0, playbackRate));

    if (!sameSrc) {
      this.pendingStartPositions[idx] = Math.max(0, startPositionMs);
      deck.setAttribute('data-track-uri', uri);
      deck.src = uri;
    } else if (startPositionMs >= 0) {
      this.pendingStartPositions[idx] = 0;
      deck.currentTime = startPositionMs / 1000;
    }
  }

  private updateMediaSession(metadata: {
    title: string;
    artist: string;
    album: string;
  }): void {
    if ('mediaSession' in navigator && 'MediaMetadata' in window) {
      navigator.mediaSession.metadata = new MediaMetadata({
        title: metadata.title,
        artist: metadata.artist,
        album: metadata.album,
      });
    }
  }

  resume(): void {
    if (this.audioCtx && this.audioCtx.state === 'suspended') {
      void this.audioCtx.resume().catch(() => {});
    }
    this.activeDeck.play().catch(() => {
      this.callbacks?.onPlayingChange(false);
    });
  }

  pause(): void {
    this.clearFadeTimers();
    this.decks[0].pause();
    this.decks[1].pause();
    if (this.audioCtx && this.deckGains) {
      const now = this.audioCtx.currentTime;
      this.deckGains[0].gain.setValueAtTime(
        this.activeDeckIndex === 0 ? 1 : 0,
        now,
      );
      this.deckGains[1].gain.setValueAtTime(
        this.activeDeckIndex === 1 ? 1 : 0,
        now,
      );
    }
    this.activeDeck.volume = 1;
  }

  seekTo(positionMs: number): void {
    this.nearEndFired = false;
    this.activeDeck.currentTime = Math.max(0, positionMs / 1000);
  }
}
