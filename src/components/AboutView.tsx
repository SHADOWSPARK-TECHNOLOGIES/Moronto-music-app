import { useState } from 'react';
import {
  Disc3,
  HeartHandshake,
  Layers,
  ShieldCheck,
  SlidersHorizontal,
} from 'lucide-react';
import studioHeroImg from '../assets/images/moronto_about_studio_1795686000142.jpg';
import { musicoletStore } from '../domain/musicoletStore';
import { LibraryViewTab } from '../model/musicoletModels';

const CAPABILITY_METRICS = [
  { label: 'Independent Queues', value: '20', detail: 'Saved resume positions' },
  { label: 'Studio Equalizer', value: '10-Band', detail: '31Hz–16kHz + Presets' },
  { label: 'Dual-Deck Crossfade', value: '0–12s', detail: 'Auto & manual skip' },
  { label: 'Offline Playback', value: '100%', detail: 'Zero data needed' },
] as const;

export function AboutView() {
  const [imgFailed, setImgFailed] = useState(false);

  return (
    <section aria-labelledby="about-heading" className="space-y-8">
      {/* Editorial Hero Split */}
      <div className="rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] overflow-hidden grid grid-cols-1 lg:grid-cols-12 items-stretch">
        <div className="lg:col-span-7 p-6 sm:p-8 flex flex-col justify-between gap-6">
          <div className="space-y-4">
            <p className="text-xs font-semibold text-[var(--mornica-emerald)]">
              About Moronto Music · ShadowSpark Technologies
            </p>
            <h1
              id="about-heading"
              className="text-2xl sm:text-4xl font-extrabold tracking-tight text-[#F4FAFE] leading-tight text-balance"
            >
              Built for listeners who own their music and demand studio clarity offline.
            </h1>
            <p className="text-sm sm:text-base text-[#A5B6CA] leading-relaxed max-w-2xl">
              Moronto Music brings multi-queue discipline together with a
              studio-grade 10-band Equalizer (Afrobeats Bass, Amapiano Log Drum,
              Gospel, Highlife, Flat), spatial control, and dual-deck crossfading.
              Created by Okoronkwo Stephen Chijioke and ShadowSpark Technologies.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button
              type="button"
              onClick={() => musicoletStore.selectTab(LibraryViewTab.GUIDE)}
              className="min-h-[44px] px-5 py-2.5 rounded-xl bg-[var(--mornica-emerald)] hover:brightness-105 text-[#04130E] text-xs font-bold whitespace-nowrap transition-all"
            >
              Read User Guide
            </button>
            <button
              type="button"
              onClick={() => musicoletStore.setEqModalOpen(true, 'EQUALIZER')}
              className="min-h-[44px] px-5 py-2.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] hover:border-[var(--mornica-cyan)] text-xs font-bold text-[#F4FAFE] whitespace-nowrap transition-colors"
            >
              Tune 10-Band EQ
            </button>
            <button
              type="button"
              onClick={() => musicoletStore.selectTab(LibraryViewTab.SPONSOR)}
              className="min-h-[44px] px-5 py-2.5 rounded-xl bg-transparent border border-[var(--mornica-slate-border)] hover:border-[#FFBE55] text-xs font-bold text-[#FFBE55] whitespace-nowrap transition-colors inline-flex items-center gap-1.5"
            >
              <HeartHandshake className="w-4 h-4" aria-hidden="true" />
              <span>Sponsor Moronto Music</span>
            </button>
            <button
              type="button"
              onClick={() => musicoletStore.selectTab(LibraryViewTab.PRIVACY)}
              className="min-h-[44px] px-4 py-2.5 rounded-xl bg-transparent border border-[var(--mornica-slate-border)] hover:border-[var(--mornica-emerald)] text-xs font-bold text-[#A5B6CA] hover:text-[#F4FAFE] whitespace-nowrap transition-colors"
            >
              Privacy Policy
            </button>
          </div>
        </div>

        <div className="lg:col-span-5 relative min-h-[240px] bg-[var(--mornica-slate-card)]">
          {!imgFailed ? (
            <>
              <img
                src={studioHeroImg}
                alt="Mastering console and reference headphones inside the Moronto Music audio studio"
                referrerPolicy="no-referrer"
                onError={() => setImgFailed(true)}
                className="w-full h-full object-cover"
              />
              <div
                aria-hidden="true"
                className="absolute inset-0 bg-gradient-to-t from-[var(--mornica-midnight-bg)]/85 via-[var(--mornica-midnight-bg)]/25 to-transparent"
              />
            </>
          ) : (
            <div className="w-full h-full flex flex-col items-center justify-center p-8 text-center bg-gradient-to-br from-[var(--mornica-slate-card-elevated)] to-[var(--mornica-midnight-bg)]">
              <Disc3 className="w-12 h-12 text-[var(--mornica-emerald)] mb-3" aria-hidden="true" />
              <p className="text-sm font-bold text-[#F4FAFE]">
                Moronto Offline Studio
              </p>
            </div>
          )}
          <div className="absolute bottom-4 left-4 right-4 text-xs text-[#F4FAFE]">
            <p className="font-bold">Lagos &amp; Global Independent Audio</p>
            <p className="text-[#A5B6CA]">
              Local-first MP3, WAV, FLAC, M4A, and OGG playback
            </p>
          </div>
        </div>
      </div>

      {/* 4-Column Capability Metrics Strip */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {CAPABILITY_METRICS.map((item) => (
          <div
            key={item.label}
            className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 space-y-1.5"
          >
            <div className="text-2xl sm:text-3xl font-extrabold text-[var(--mornica-emerald)] font-tabular">
              {item.value}
            </div>
            <div className="text-sm font-bold text-[#F4FAFE]">{item.label}</div>
            <div className="text-xs text-[#A5B6CA]">{item.detail}</div>
          </div>
        ))}
      </div>

      {/* Architecture Pillars */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="rounded-2xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 space-y-3">
          <Layers className="w-6 h-6 text-[var(--mornica-cyan)]" aria-hidden="true" />
          <h2 className="text-base font-bold text-[#F4FAFE]">
            Independent Multi-Queue Memory
          </h2>
          <p className="text-sm text-[#A5B6CA] leading-relaxed">
            Keep separate queues for commutes, workouts, and late-night writing
            sessions. Every queue stores its active track and exact playback
            timestamp on your device.
          </p>
        </div>

        <div className="rounded-2xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 space-y-3">
          <SlidersHorizontal
            className="w-6 h-6 text-[var(--mornica-emerald)]"
            aria-hidden="true"
          />
          <h2 className="text-base font-bold text-[#F4FAFE]">
            10-Band DSP &amp; Smooth Crossfade
          </h2>
          <p className="text-sm text-[#A5B6CA] leading-relaxed">
            Shape sound in real time with 10 frequency bands (31Hz–16kHz), gain
            preamp, bass boost, and spatial width, paired with dual-deck
            crossfading from 0 to 12 seconds.
          </p>
        </div>

        <div className="rounded-2xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 space-y-3">
          <ShieldCheck
            className="w-6 h-6 text-[#FFBE55]"
            aria-hidden="true"
          />
          <h2 className="text-base font-bold text-[#F4FAFE]">
            Licensed &amp; Local-First Integrity
          </h2>
          <p className="text-sm text-[#A5B6CA] leading-relaxed">
            Moronto Music plays files stored locally on your device plus
            first-party bundled demos. Your audio library, tags, and synced
            lyrics stay private without cloud lock-in.
          </p>
        </div>
      </div>
    </section>
  );
}
