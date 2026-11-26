import { useEffect, useRef, useState } from 'react';
import {
  CheckCircle2,
  Crown,
  Download,
  FolderPlus,
  Share2,
  SlidersHorizontal,
  Star,
  X,
} from 'lucide-react';
import {
  type MusicoletUiState,
  musicoletStore,
} from '../domain/musicoletStore';
import {
  EQ_BAND_LABELS,
  EQ_PRESETS,
  LibraryViewTab,
  type TenBandTuple,
} from '../model/musicoletModels';
import { formatDurationMs } from '../utils/format';

interface ModalsProps {
  uiState: MusicoletUiState;
}

const CROSSFADE_PRESETS = [0, 2, 4, 6, 8, 12] as const;

export function EqualizerModal({ uiState }: ModalsProps) {
  if (!uiState.isEqModalOpen) return null;

  const eq = uiState.equalizer;
  const cf = uiState.crossfade;

  const updateBand = (index: number, valueDb: number) => {
    const nextBands = [...eq.bandsDb] as TenBandTuple;
    nextBands[index] = valueDb;
    musicoletStore.updateEqualizer({
      ...eq,
      enabled: true,
      presetId: 'custom',
      bandsDb: nextBands,
    });
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="10-Band Equalizer and Crossfade Settings"
      onClick={(e) => {
        if (e.target === e.currentTarget) musicoletStore.setEqModalOpen(false);
      }}
      className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div className="w-full max-w-xl rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-5 space-y-4 max-h-[92vh] overflow-y-auto">
        <div className="flex items-center justify-between gap-2">
          <div>
            <h2 className="text-lg font-bold text-[#F4FAFE]">
              10-Band Audio FX &amp; Crossfade
            </h2>
            <p className="text-xs text-[#A5B6CA]">
              Offline 10-band Web Audio Equalizer (31Hz–16kHz), Preamp &amp; 0–12s Crossfade
            </p>
          </div>
          <button
            type="button"
            onClick={() => musicoletStore.setEqModalOpen(false)}
            aria-label="Close Audio FX and Crossfade Settings"
            className="p-2 rounded-xl text-[#A5B6CA] hover:text-[#F4FAFE] hover:bg-[var(--mornica-slate-card)]"
          >
            <X className="w-5 h-5" aria-hidden="true" />
          </button>
        </div>

        {/* Modal Section Tabs */}
        <div
          role="tablist"
          aria-label="Audio settings tabs"
          className="grid grid-cols-2 p-1 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)]"
        >
          <button
            type="button"
            role="tab"
            aria-selected={uiState.audioModalTab === 'EQUALIZER'}
            onClick={() => musicoletStore.setAudioModalTab('EQUALIZER')}
            className={`py-2 rounded-lg text-xs font-bold transition-colors ${
              uiState.audioModalTab === 'EQUALIZER'
                ? 'bg-[var(--mornica-emerald)] text-[#04130E]'
                : 'text-[#A5B6CA] hover:text-[#F4FAFE]'
            }`}
          >
            10-Band EQ {eq.enabled ? '· On' : '· Off'}
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={uiState.audioModalTab === 'CROSSFADE'}
            onClick={() => musicoletStore.setAudioModalTab('CROSSFADE')}
            className={`py-2 rounded-lg text-xs font-bold transition-colors ${
              uiState.audioModalTab === 'CROSSFADE'
                ? 'bg-[var(--mornica-cyan)] text-[#071321]'
                : 'text-[#A5B6CA] hover:text-[#F4FAFE]'
            }`}
          >
            Crossfade {cf.enabled ? `· ${cf.durationSec}s` : '· 0–12s'}
          </button>
        </div>

        {uiState.audioModalTab === 'EQUALIZER' ? (
          <div className="space-y-4">
            {/* Master Enable & Reset */}
            <div className="flex items-center justify-between rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] px-3.5 py-2.5">
              <div>
                <div className="text-sm font-bold text-[#F4FAFE]">
                  Enable 10-Band Equalizer
                </div>
                <div className="text-[11px] text-[#A5B6CA]">
                  Real-time offline tone shaping · Zero network required
                </div>
              </div>
              <div className="flex items-center gap-2.5">
                <button
                  type="button"
                  onClick={() => musicoletStore.resetEqualizerToFlat()}
                  className="px-2.5 py-1 rounded-lg bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] hover:border-[var(--mornica-emerald)] text-[11px] font-semibold text-[#A5B6CA] hover:text-[#F4FAFE]"
                >
                  Reset Flat
                </button>
                <button
                  type="button"
                  role="switch"
                  aria-checked={eq.enabled}
                  aria-label="Enable Equalizer"
                  onClick={() =>
                    musicoletStore.updateEqualizer({ ...eq, enabled: !eq.enabled })
                  }
                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                    eq.enabled ? 'bg-[var(--mornica-emerald)]' : 'bg-[var(--mornica-slate-border)]'
                  }`}
                >
                  <span
                    className={`inline-block h-4 w-4 transform rounded-full bg-[#071321] transition-transform ${
                      eq.enabled ? 'translate-x-6' : 'translate-x-1'
                    }`}
                  />
                </button>
              </div>
            </div>

            {/* Presets (All Unlocked) */}
            <div className="flex flex-wrap gap-2">
              {EQ_PRESETS.map((preset) => {
                const active = eq.presetId === preset.id;
                return (
                  <button
                    key={preset.id}
                    type="button"
                    onClick={() => musicoletStore.applyEqPreset(preset.id)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-bold inline-flex items-center gap-1.5 transition-colors ${
                      active
                        ? 'bg-[var(--mornica-emerald)] text-[#04130E]'
                        : 'bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] text-[#A5B6CA] hover:text-[#F4FAFE]'
                    }`}
                  >
                    <span>{preset.name}</span>
                  </button>
                );
              })}
            </div>

            {/* 10 Frequency Bands */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-5 gap-y-2.5">
              {EQ_BAND_LABELS.map((label, idx) => {
                const val = eq.bandsDb[idx] ?? 0;
                return (
                  <div key={label} className="flex items-center gap-2.5">
                    <span className="w-11 text-xs font-bold text-[var(--mornica-cyan)] font-tabular">
                      {label}
                    </span>
                    <input
                      type="range"
                      min={-12}
                      max={12}
                      step={1}
                      value={val}
                      aria-label={`${label} gain`}
                      onChange={(e) => updateBand(idx, Number(e.target.value))}
                      className="flex-1 min-w-0 mornica-slider bg-[var(--mornica-slate-border)]"
                    />
                    <span className="w-12 text-right text-xs font-semibold text-[#F4FAFE] font-tabular">
                      {val > 0 ? `+${val}` : val} dB
                    </span>
                  </div>
                );
              })}
            </div>

            {/* Preamp, Bass Boost & Virtualizer */}
            <div className="pt-3 border-t border-[var(--mornica-slate-border)] space-y-2.5">
              <div className="flex items-center gap-3">
                <span className="w-24 text-xs font-bold text-[var(--mornica-emerald)]">
                  Preamp
                </span>
                <input
                  type="range"
                  min={-12}
                  max={12}
                  step={1}
                  value={eq.preampDb}
                  aria-label="Preamp gain"
                  onChange={(e) =>
                    musicoletStore.updateEqualizer({
                      ...eq,
                      enabled: true,
                      preampDb: Number(e.target.value),
                    })
                  }
                  className="flex-1 min-w-0 mornica-slider bg-[var(--mornica-slate-border)]"
                />
                <span className="w-12 text-right text-xs font-semibold text-[#F4FAFE] font-tabular">
                  {eq.preampDb > 0 ? `+${eq.preampDb}` : eq.preampDb} dB
                </span>
              </div>

              <div className="flex items-center gap-3">
                <span className="w-24 text-xs font-bold text-[#FFBE55]">
                  Bass Boost
                </span>
                <input
                  type="range"
                  min={0}
                  max={100}
                  step={5}
                  value={eq.bassBoostPercent}
                  aria-label="Bass Boost"
                  onChange={(e) =>
                    musicoletStore.updateEqualizer({
                      ...eq,
                      enabled: true,
                      presetId: 'custom',
                      bassBoostPercent: Number(e.target.value),
                    })
                  }
                  className="flex-1 min-w-0 mornica-slider bg-[var(--mornica-slate-border)]"
                />
                <span className="w-12 text-right text-xs font-semibold text-[#F4FAFE] font-tabular">
                  {eq.bassBoostPercent}%
                </span>
              </div>

              <div className="flex items-center gap-3">
                <span className="w-24 text-xs font-bold text-[var(--mornica-cyan)]">
                  Virtualizer
                </span>
                <input
                  type="range"
                  min={0}
                  max={100}
                  step={5}
                  value={eq.virtualizerPercent}
                  aria-label="Spatial Virtualizer"
                  onChange={(e) =>
                    musicoletStore.updateEqualizer({
                      ...eq,
                      enabled: true,
                      virtualizerPercent: Number(e.target.value),
                    })
                  }
                  className="flex-1 min-w-0 mornica-slider bg-[var(--mornica-slate-border)]"
                />
                <span className="w-12 text-right text-xs font-semibold text-[#F4FAFE] font-tabular">
                  {eq.virtualizerPercent}%
                </span>
              </div>
            </div>
          </div>
        ) : (
          <div className="space-y-4">
            {/* Enable Crossfade */}
            <div className="flex items-center justify-between rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] px-3.5 py-3">
              <div>
                <div className="text-sm font-bold text-[#F4FAFE]">
                  Smooth Track Crossfade (On / Off)
                </div>
                <div className="text-[11px] text-[#A5B6CA]">
                  Fade out the ending track while fading in the next track offline
                </div>
              </div>
              <button
                type="button"
                role="switch"
                aria-checked={cf.enabled}
                aria-label="Enable Crossfade"
                onClick={() =>
                  musicoletStore.updateCrossfade({
                    ...cf,
                    enabled: !cf.enabled,
                  })
                }
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                  cf.enabled
                    ? 'bg-[var(--mornica-cyan)]'
                    : 'bg-[var(--mornica-slate-border)]'
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-[#071321] transition-transform ${
                    cf.enabled ? 'translate-x-6' : 'translate-x-1'
                  }`}
                />
              </button>
            </div>

            {/* Duration Slider (0-12s) + Presets */}
            <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-4 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-[var(--mornica-cyan)]">
                  Crossfade Duration (0–12 seconds)
                </span>
                <span className="text-sm font-extrabold text-[#F4FAFE] font-tabular">
                  {cf.durationSec === 0 ? '0s (Off)' : `${cf.durationSec}s`}
                </span>
              </div>

              <input
                type="range"
                min={0}
                max={12}
                step={1}
                value={cf.durationSec}
                aria-label="Crossfade duration in seconds"
                onChange={(e) => {
                  const val = Number(e.target.value);
                  musicoletStore.updateCrossfade({
                    ...cf,
                    enabled: val > 0,
                    durationSec: val,
                  });
                }}
                className="w-full mornica-slider bg-[var(--mornica-slate-border)]"
              />

              <div className="flex flex-wrap gap-2">
                {CROSSFADE_PRESETS.map((sec) => {
                  const selected =
                    sec === 0
                      ? !cf.enabled || cf.durationSec === 0
                      : cf.durationSec === sec && cf.enabled;
                  return (
                    <button
                      key={sec}
                      type="button"
                      onClick={() =>
                        musicoletStore.updateCrossfade({
                          ...cf,
                          enabled: sec > 0,
                          durationSec: sec,
                        })
                      }
                      className={`px-3 py-1.5 rounded-xl text-xs font-bold font-tabular transition-colors ${
                        selected
                          ? 'bg-[var(--mornica-cyan)] text-[#071321]'
                          : 'bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] text-[#A5B6CA] hover:text-[#F4FAFE]'
                      }`}
                    >
                      {sec === 0 ? '0s (Off)' : `${sec}s`}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Fade on Manual Skip */}
            <div className="flex items-center justify-between rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] px-3.5 py-3">
              <div>
                <div className="text-xs font-bold text-[#F4FAFE]">
                  Crossfade on Manual Skip
                </div>
                <div className="text-[11px] text-[#A5B6CA]">
                  Apply crossfade when tapping Next, Previous, or another track
                </div>
              </div>
              <button
                type="button"
                role="switch"
                aria-checked={cf.fadeOnManualSkip}
                aria-label="Crossfade on manual skip"
                onClick={() =>
                  musicoletStore.updateCrossfade({
                    ...cf,
                    fadeOnManualSkip: !cf.fadeOnManualSkip,
                  })
                }
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                  cf.fadeOnManualSkip
                    ? 'bg-[var(--mornica-emerald)]'
                    : 'bg-[var(--mornica-slate-border)]'
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-[#071321] transition-transform ${
                    cf.fadeOnManualSkip ? 'translate-x-6' : 'translate-x-1'
                  }`}
                />
              </button>
            </div>

            {/* Gapless Playback */}
            <div className="flex items-center justify-between rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] px-3.5 py-3">
              <div>
                <div className="text-xs font-bold text-[#F4FAFE]">
                  Gapless Playback
                </div>
                <div className="text-[11px] text-[#A5B6CA]">
                  Seamless transition between tracks when crossfade is set to 0s
                </div>
              </div>
              <button
                type="button"
                role="switch"
                aria-checked={cf.gaplessEnabled}
                aria-label="Gapless playback"
                onClick={() =>
                  musicoletStore.updateCrossfade({
                    ...cf,
                    gaplessEnabled: !cf.gaplessEnabled,
                  })
                }
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                  cf.gaplessEnabled
                    ? 'bg-[var(--mornica-emerald)]'
                    : 'bg-[var(--mornica-slate-border)]'
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-[#071321] transition-transform ${
                    cf.gaplessEnabled ? 'translate-x-6' : 'translate-x-1'
                  }`}
                />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

const SLEEP_OPTIONS = [5, 15, 30, 45, 60] as const;

export function SleepTimerModal({ uiState }: ModalsProps) {
  if (!uiState.isSleepTimerModalOpen) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Sleep Timer"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          musicoletStore.setSleepTimerModalOpen(false);
        }
      }}
      className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div className="w-full max-w-sm rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-5 space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold text-[#F4FAFE]">Sleep Timer</h2>
            <p className="text-[11px] text-[var(--mornica-emerald)] font-semibold">
              Unlocked Free for All Listeners
            </p>
          </div>
          <button
            type="button"
            onClick={() => musicoletStore.setSleepTimerModalOpen(false)}
            aria-label="Close Sleep Timer"
            className="p-2 rounded-xl text-[#A5B6CA] hover:text-[#F4FAFE] hover:bg-[var(--mornica-slate-card)]"
          >
            <X className="w-5 h-5" aria-hidden="true" />
          </button>
        </div>

        <p className="text-xs text-[#A5B6CA]">
          Automatically pause offline audio playback after a countdown.
        </p>

        <div className="grid grid-cols-3 gap-2">
          {SLEEP_OPTIONS.map((mins) => (
            <button
              key={mins}
              type="button"
              onClick={() => musicoletStore.setSleepTimer(mins)}
              className={`min-h-[44px] rounded-xl text-sm font-bold font-tabular transition-colors ${
                uiState.sleepTimerMinutes === mins
                  ? 'bg-[#FFBE55] text-[#071321]'
                  : 'bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] text-[#F4FAFE] hover:border-[#FFBE55]'
              }`}
            >
              {mins} min
            </button>
          ))}
          <button
            type="button"
            onClick={() => {
              musicoletStore.setSleepTimer(null);
              musicoletStore.setSleepTimerModalOpen(false);
            }}
            className="min-h-[44px] rounded-xl text-sm font-bold bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] text-[#FF6479] hover:border-[#FF6479]"
          >
            Turn Off
          </button>
        </div>
      </div>
    </div>
  );
}

export function OnboardingModal({ uiState }: ModalsProps) {
  const [step, setStep] = useState<0 | 1 | 2>(0);
  const [pickedPresetId, setPickedPresetId] = useState<string>(uiState.equalizer.presetId || 'afrobeats-bass');
  const fileRef = useRef<HTMLInputElement>(null);

  if (uiState.hasCompletedOnboarding) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Moronto Music Onboarding"
      data-testid="onboarding-modal"
      className="fixed inset-0 z-[60] bg-black/80 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div className="w-full max-w-lg rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 space-y-5">
        <input
          ref={fileRef}
          type="file"
          multiple
          accept="audio/*,.mp3,.wav,.flac,.m4a,.ogg,.aac"
          onChange={async (e) => {
            if (e.target.files && e.target.files.length > 0) {
              await musicoletStore.importLocalAudioFiles(e.target.files);
            }
            e.target.value = '';
          }}
          className="sr-only"
        />

        <div className="flex items-center justify-between">
          <span className="text-xs font-bold text-[var(--mornica-emerald)] font-tabular">
            Moronto Music · Step {step + 1} of 3
          </span>
          <button
            type="button"
            onClick={() => musicoletStore.completeOnboarding()}
            className="text-xs font-semibold text-[#A5B6CA] hover:text-[#F4FAFE]"
          >
            Skip
          </button>
        </div>

        {step === 0 && (
          <div className="space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] flex items-center justify-center">
              <FolderPlus className="w-6 h-6 text-[var(--mornica-emerald)]" aria-hidden="true" />
            </div>
            <h2 className="text-2xl font-extrabold text-[#F4FAFE] text-balance">
              1. Import Your Offline Music
            </h2>
            <p className="text-sm text-[#A5B6CA] leading-relaxed">
              Add MP3, WAV, FLAC, M4A, or OGG files straight from your device. Everything stays saved locally on your device — zero data streaming needed.
            </p>
            <div className="flex flex-wrap gap-2.5 pt-1">
              <button
                type="button"
                onClick={() => fileRef.current?.click()}
                className="min-h-[44px] px-4 py-2.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-emerald)] text-xs font-bold text-[var(--mornica-emerald)] inline-flex items-center gap-2"
              >
                <FolderPlus className="w-4 h-4" aria-hidden="true" />
                <span>Choose Audio Files</span>
              </button>
              <button
                type="button"
                onClick={() => setStep(1)}
                className="min-h-[44px] px-5 py-2.5 rounded-xl bg-[var(--mornica-emerald)] text-[#04130E] text-xs font-extrabold"
              >
                Next: Pick EQ Preset
              </button>
            </div>
          </div>
        )}

        {step === 1 && (
          <div className="space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] flex items-center justify-center">
              <SlidersHorizontal className="w-6 h-6 text-[var(--mornica-cyan)]" aria-hidden="true" />
            </div>
            <h2 className="text-2xl font-extrabold text-[#F4FAFE] text-balance">
              2. Pick a Favourite 10-Band EQ Preset
            </h2>
            <p className="text-sm text-[#A5B6CA]">
              Choose your starting studio sound profile across 10 bands (31Hz–16kHz):
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {EQ_PRESETS.map((preset) => {
                const selected = pickedPresetId === preset.id;
                return (
                  <button
                    key={preset.id}
                    type="button"
                    onClick={() => {
                      setPickedPresetId(preset.id);
                      musicoletStore.applyEqPreset(preset.id, true);
                    }}
                    className={`p-3 rounded-xl border text-left transition-colors ${
                      selected
                        ? 'bg-[var(--mornica-slate-card-elevated)] border-[var(--mornica-emerald)]'
                        : 'bg-[var(--mornica-slate-card)] border-[var(--mornica-slate-border)]'
                    }`}
                  >
                    <div className="text-xs font-bold text-[#F4FAFE]">{preset.name}</div>
                    <div className="text-[11px] text-[#A5B6CA] font-tabular">
                      Bass Boost {preset.bassBoostPercent}%
                    </div>
                  </button>
                );
              })}
            </div>

            <div className="flex items-center justify-between pt-1">
              <button
                type="button"
                onClick={() => setStep(0)}
                className="min-h-[44px] px-4 py-2 rounded-xl border border-[var(--mornica-slate-border)] text-xs font-bold text-[#A5B6CA]"
              >
                Back
              </button>
              <button
                type="button"
                onClick={() => setStep(2)}
                className="min-h-[44px] px-5 py-2.5 rounded-xl bg-[var(--mornica-emerald)] text-[#04130E] text-xs font-extrabold"
              >
                Next: Full Offline Access
              </button>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] flex items-center justify-center">
              <Crown className="w-6 h-6 text-[#FFBE55]" aria-hidden="true" />
            </div>
            <h2 className="text-2xl font-extrabold text-[#F4FAFE] text-balance">
              3. Full Offline Access · Pro Streaming Coming Soon
            </h2>
            <ul className="space-y-2 text-xs sm:text-sm text-[#F4FAFE]/90">
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-[var(--mornica-emerald)] shrink-0" aria-hidden="true" />
                <span>All 5 10-band EQ presets, 0–12s Crossfade, Sleep Timer &amp; 4 Themes unlocked free</span>
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-[var(--mornica-emerald)] shrink-0" aria-hidden="true" />
                <span>Zero ads anywhere while listening offline</span>
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#FFBE55] shrink-0" aria-hidden="true" />
                <span>Pro launches soon with Global Catalog Music Streaming</span>
              </li>
            </ul>

            <div className="flex flex-wrap items-center justify-between gap-2 pt-2">
              <button
                type="button"
                onClick={() => setStep(1)}
                className="min-h-[44px] px-4 py-2 rounded-xl border border-[var(--mornica-slate-border)] text-xs font-bold text-[#A5B6CA]"
              >
                Back
              </button>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => musicoletStore.completeOnboarding()}
                  className="min-h-[44px] px-4 py-2.5 rounded-xl bg-[var(--mornica-emerald)] text-[#04130E] text-xs font-extrabold"
                >
                  Enter Moronto Music
                </button>
                <button
                  type="button"
                  onClick={() => {
                    musicoletStore.completeOnboarding();
                    musicoletStore.selectTab(LibraryViewTab.BLOG);
                  }}
                  className="min-h-[44px] px-4 py-2.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] text-xs font-bold text-[#FFBE55]"
                >
                  Read Pidgin Guide
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export function RatingPromptModal({ uiState }: ModalsProps) {
  const [stars, setStars] = useState(5);
  const [comment, setComment] = useState('');

  if (!uiState.isRatingPromptOpen) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Rate Moronto Music"
      className="fixed inset-0 z-[65] bg-black/75 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <form
        onSubmit={(e) => {
          e.preventDefault();
          musicoletStore.submitRating(stars, comment);
        }}
        className="w-full max-w-md rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 space-y-4"
      >
        <div className="flex items-start justify-between gap-2">
          <div>
            <p className="text-[11px] font-bold text-[var(--mornica-emerald)] font-tabular">
              Session #{uiState.sessionCount} Milestone
            </p>
            <h2 className="text-lg font-extrabold text-[#F4FAFE] mt-0.5">
              Enjoying Moronto Music?
            </h2>
          </div>
          <button
            type="button"
            onClick={() => musicoletStore.dismissRatingPrompt()}
            aria-label="Dismiss rating prompt"
            className="p-1.5 rounded-lg text-[#A5B6CA] hover:text-[#F4FAFE]"
          >
            <X className="w-5 h-5" aria-hidden="true" />
          </button>
        </div>

        <p className="text-xs text-[#A5B6CA]">
          You have opened Moronto Music {uiState.sessionCount} times! Rate your offline audio experience below.
        </p>

        <div className="flex items-center justify-center gap-2 py-1">
          {[1, 2, 3, 4, 5].map((value) => (
            <button
              key={value}
              type="button"
              onClick={() => setStars(value)}
              aria-label={`Rate ${value} stars`}
              className="p-1.5 rounded-xl hover:bg-[var(--mornica-slate-card)]"
            >
              <Star
                className={`w-7 h-7 ${
                  value <= stars ? 'fill-[#FFBE55] text-[#FFBE55]' : 'text-[#A5B6CA]'
                }`}
                aria-hidden="true"
              />
            </button>
          ))}
        </div>

        <textarea
          rows={2}
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder="Optional feedback for ShadowSpark Technologies..."
          className="w-full p-3 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-xs text-[#F4FAFE]"
        />

        <div className="flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={() => musicoletStore.dismissRatingPrompt()}
            className="min-h-[40px] px-4 py-2 rounded-xl border border-[var(--mornica-slate-border)] text-xs font-semibold text-[#A5B6CA]"
          >
            Maybe Later
          </button>
          <button
            type="submit"
            className="min-h-[40px] px-5 py-2 rounded-xl bg-[var(--mornica-emerald)] text-[#04130E] text-xs font-extrabold"
          >
            Submit {stars}★ Rating
          </button>
        </div>
      </form>
    </div>
  );
}

export function WhatsAppShareModal({ uiState }: ModalsProps) {
  const track = uiState.tracks.find((t) => t.id === uiState.sharingTrackId);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [dataUrl, setDataUrl] = useState<string | null>(null);

  useEffect(() => {
    if (!track || !canvasRef.current) return;
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Draw 1080x1920 WhatsApp Status vertical track card
    const w = 1080;
    const h = 1920;
    const grad = ctx.createLinearGradient(0, 0, w, h);
    grad.addColorStop(0, '#071321');
    grad.addColorStop(0.5, '#0F253E');
    grad.addColorStop(1, '#062E22');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, w, h);

    // Subtle acoustic rings
    ctx.strokeStyle = 'rgba(18, 217, 161, 0.25)';
    ctx.lineWidth = 4;
    ctx.beginPath();
    ctx.arc(540, 760, 310, 0, Math.PI * 2);
    ctx.stroke();

    ctx.strokeStyle = 'rgba(255, 190, 85, 0.35)';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.arc(540, 760, 230, 0, Math.PI * 2);
    ctx.stroke();

    // Equalizer bars motif
    const barHeights = [140, 230, 320, 260, 360, 290, 200, 150];
    barHeights.forEach((bh, idx) => {
      const bx = 340 + idx * 54;
      const by = 760 - bh / 2;
      ctx.fillStyle = idx % 2 === 0 ? '#12D9A1' : '#FFBE55';
      ctx.fillRect(bx, by, 30, bh);
    });

    // Top branding
    ctx.fillStyle = '#12D9A1';
    ctx.font = 'bold 42px sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText('MORONTO MUSIC', 540, 210);

    ctx.fillStyle = '#A5B6CA';
    ctx.font = '600 28px sans-serif';
    ctx.fillText('OFFLINE STUDIO · SHADOWSPARK TECHNOLOGIES', 540, 265);

    // Track metadata
    ctx.fillStyle = '#F4FAFE';
    ctx.font = 'bold 64px sans-serif';
    ctx.fillText(track.title.slice(0, 28), 540, 1260);

    ctx.fillStyle = '#20B6EB';
    ctx.font = 'bold 44px sans-serif';
    ctx.fillText(track.artist.slice(0, 32), 540, 1345);

    ctx.fillStyle = '#A5B6CA';
    ctx.font = '500 34px sans-serif';
    ctx.fillText(
      `${track.album} · ${track.folderName} · ${formatDurationMs(track.durationMs)}`,
      540,
      1420,
    );

    // EQ / Studio badge text
    ctx.fillStyle = '#FFBE55';
    ctx.font = 'bold 32px sans-serif';
    ctx.fillText(
      `10-Band EQ: ${uiState.equalizer.presetId.toUpperCase()} · Zero Data Playback`,
      540,
      1660,
    );

    setDataUrl(canvas.toDataURL('image/png'));
  }, [track, uiState.equalizer.presetId]);

  if (!track) return null;

  const handleDownload = () => {
    if (!dataUrl) return;
    const link = document.createElement('a');
    link.href = dataUrl;
    link.download = `moronto-status-${track.title.toLowerCase().replace(/[^a-z0-9]+/g, '-')}.png`;
    link.click();
  };

  const handleNativeShare = async () => {
    if (!dataUrl || !navigator.share) {
      handleDownload();
      return;
    }
    try {
      const res = await fetch(dataUrl);
      const blob = await res.blob();
      const file = new File([blob], 'moronto-whatsapp-status.png', {
        type: 'image/png',
      });
      await navigator.share({
        title: `${track.title} — Moronto Music`,
        text: `Listening to ${track.title} by ${track.artist} on Moronto Music`,
        files: [file],
      });
    } catch {
      handleDownload();
    }
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="WhatsApp Status Track Card"
      onClick={(e) => {
        if (e.target === e.currentTarget) musicoletStore.setSharingTrackId(null);
      }}
      className="fixed inset-0 z-[70] bg-black/75 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div className="w-full max-w-md rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-5 space-y-4">
        <div className="flex items-center justify-between gap-2">
          <div>
            <h2 className="text-lg font-bold text-[#F4FAFE]">
              WhatsApp Status Track Card
            </h2>
            <p className="text-xs text-[#A5B6CA]">
              1080×1920 branded Moronto Music vertical card
            </p>
          </div>
          <button
            type="button"
            onClick={() => musicoletStore.setSharingTrackId(null)}
            aria-label="Close Share Card"
            className="p-2 rounded-xl text-[#A5B6CA] hover:text-[#F4FAFE]"
          >
            <X className="w-5 h-5" aria-hidden="true" />
          </button>
        </div>

        <canvas
          ref={canvasRef}
          width={1080}
          height={1920}
          className="hidden"
        />

        <div className="flex justify-center">
          {dataUrl ? (
            <img
              src={dataUrl}
              alt={`WhatsApp Status Card for ${track.title}`}
              className="w-52 aspect-[9/16] rounded-2xl border border-[var(--mornica-slate-border)] shadow-xl object-cover"
            />
          ) : (
            <div className="w-52 aspect-[9/16] rounded-2xl bg-[var(--mornica-slate-card)] flex items-center justify-center text-xs text-[#A5B6CA]">
              Rendering card...
            </div>
          )}
        </div>

        <div className="flex flex-col sm:flex-row gap-2.5">
          <button
            type="button"
            onClick={handleDownload}
            className="flex-1 min-h-[44px] px-4 py-2.5 rounded-xl bg-[var(--mornica-emerald)] hover:brightness-105 text-[#04130E] text-xs font-extrabold inline-flex items-center justify-center gap-2 whitespace-nowrap"
          >
            <Download className="w-4 h-4" aria-hidden="true" />
            <span>Save Status PNG</span>
          </button>
          <button
            type="button"
            onClick={() => void handleNativeShare()}
            className="min-h-[44px] px-4 py-2.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] hover:border-[var(--mornica-cyan)] text-xs font-bold text-[#F4FAFE] inline-flex items-center justify-center gap-2 whitespace-nowrap"
          >
            <Share2 className="w-4 h-4" aria-hidden="true" />
            <span>Share</span>
          </button>
        </div>
      </div>
    </div>
  );
}

export function TagEditorModal({ uiState }: ModalsProps) {
  const track = uiState.tracks.find((t) => t.id === uiState.editingTrackId);

  const [title, setTitle] = useState('');
  const [artist, setArtist] = useState('');
  const [album, setAlbum] = useState('');
  const [folderName, setFolderName] = useState('');
  const [releaseYear, setReleaseYear] = useState('2026');
  const [lyricsText, setLyricsText] = useState('');

  useEffect(() => {
    if (!track) return;
    setTitle(track.title);
    setArtist(track.artist);
    setAlbum(track.album);
    setFolderName(track.folderName);
    setReleaseYear(String(track.releaseYear));
    setLyricsText(track.lyricsText ?? '');
  }, [track]);

  if (!track) return null;

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    musicoletStore.saveTrackTags(track.id, {
      title,
      artist,
      album,
      folderName,
      releaseYear: Number(releaseYear) || track.releaseYear,
      lyricsText,
    });
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Tag Editor"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          musicoletStore.setEditingTrackId(null);
        }
      }}
      className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <form
        onSubmit={handleSave}
        className="w-full max-w-lg rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-5 space-y-3.5 max-h-[90vh] overflow-y-auto"
      >
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold text-[var(--mornica-emerald)]">
              Tag Editor &amp; Lyrics
            </h2>
            <p className="text-xs text-[#A5B6CA]">
              Changes update Tracks, Smart Playlists, Folders, Albums, and Artists immediately
            </p>
          </div>
          <button
            type="button"
            onClick={() => musicoletStore.setEditingTrackId(null)}
            aria-label="Close Tag Editor"
            className="p-2 rounded-xl text-[#A5B6CA] hover:text-[#F4FAFE] hover:bg-[var(--mornica-slate-card)]"
          >
            <X className="w-5 h-5" aria-hidden="true" />
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label
              htmlFor="tag-title"
              className="block text-xs font-medium text-[#A5B6CA] mb-1"
            >
              Track Title
            </label>
            <input
              id="tag-title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full h-10 px-3 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE]"
            />
          </div>

          <div>
            <label
              htmlFor="tag-artist"
              className="block text-xs font-medium text-[#A5B6CA] mb-1"
            >
              Artist
            </label>
            <input
              id="tag-artist"
              type="text"
              value={artist}
              onChange={(e) => setArtist(e.target.value)}
              className="w-full h-10 px-3 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE]"
            />
          </div>

          <div>
            <label
              htmlFor="tag-album"
              className="block text-xs font-medium text-[#A5B6CA] mb-1"
            >
              Album
            </label>
            <input
              id="tag-album"
              type="text"
              value={album}
              onChange={(e) => setAlbum(e.target.value)}
              className="w-full h-10 px-3 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE]"
            />
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label
                htmlFor="tag-folder"
                className="block text-xs font-medium text-[#A5B6CA] mb-1"
              >
                Folder
              </label>
              <input
                id="tag-folder"
                type="text"
                value={folderName}
                onChange={(e) => setFolderName(e.target.value)}
                className="w-full h-10 px-3 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE]"
              />
            </div>

            <div>
              <label
                htmlFor="tag-year"
                className="block text-xs font-medium text-[#A5B6CA] mb-1"
              >
                Year
              </label>
              <input
                id="tag-year"
                type="number"
                value={releaseYear}
                onChange={(e) => setReleaseYear(e.target.value)}
                className="w-full h-10 px-3 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE] font-tabular"
              />
            </div>
          </div>
        </div>

        <div>
          <label
            htmlFor="tag-lyrics"
            className="block text-xs font-medium text-[#A5B6CA] mb-1"
          >
            Lyrics (supports [mm:ss.xx] LRC timestamps)
          </label>
          <textarea
            id="tag-lyrics"
            rows={4}
            value={lyricsText}
            onChange={(e) => setLyricsText(e.target.value)}
            placeholder="[00:00.00] Paste LRC or plain lyrics..."
            className="w-full p-3 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-xs text-[#F4FAFE]"
          />
        </div>

        <div className="flex items-center justify-end gap-2 pt-2">
          <button
            type="button"
            onClick={() => musicoletStore.setEditingTrackId(null)}
            className="min-h-[40px] px-4 py-2 rounded-xl border border-[var(--mornica-slate-border)] text-xs font-semibold text-[#A5B6CA] hover:text-[#F4FAFE]"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="min-h-[40px] px-5 py-2 rounded-xl bg-[var(--mornica-emerald)] hover:brightness-105 text-[#04130E] text-xs font-bold"
          >
            Save Tags
          </button>
        </div>
      </form>
    </div>
  );
}
