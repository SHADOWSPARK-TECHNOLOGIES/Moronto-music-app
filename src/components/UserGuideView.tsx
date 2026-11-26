import {
  BookOpen,
  Compass,
  FolderPlus,
  Layers,
  ListMusic,
  Share2,
  SlidersHorizontal,
  Sparkles,
  Star,
} from 'lucide-react';
import {
  type MusicoletUiState,
  musicoletStore,
} from '../domain/musicoletStore';
import { t } from '../i18n/translations';
import { LibraryViewTab } from '../model/musicoletModels';

interface UserGuideViewProps {
  uiState: MusicoletUiState;
  onReplayIntro: () => void;
}

export function UserGuideView({ uiState, onReplayIntro }: UserGuideViewProps) {
  const tr = t(uiState.language);

  const guideSteps = [
    {
      num: '01.',
      title: 'Import Your Offline Audio Files',
      detail:
        'Tap Import Audio in the top header to add MP3, WAV, FLAC, M4A, or OGG tracks directly from your device storage. Files stay saved locally for offline listening.',
      actionLabel: 'Open Tracks Library',
      icon: FolderPlus,
      onAction: () => musicoletStore.selectTab(LibraryViewTab.TRACKS),
    },
    {
      num: '02.',
      title: 'Shape Sound with the 10-Band EQ & 0–12s Crossfade',
      detail:
        'Open 10-EQ from the header to dial 31Hz–16kHz bands or pick Afrobeats Bass, Amapiano Log Drum, Gospel, Highlife, or Flat. All presets, 0–12s crossfade, and Sleep Timer are 100% unlocked for everyone!',
      actionLabel: 'Open 10-Band EQ & Fade',
      icon: SlidersHorizontal,
      onAction: () => musicoletStore.setEqModalOpen(true, 'EQUALIZER'),
    },
    {
      num: '03.',
      title: 'Switch Between Up to 20 Resumable Queues',
      detail:
        'Separate your Commute, Praise, and Studio queues. Moronto Music automatically saves each queue’s active track and exact playback timestamp when you switch.',
      actionLabel: 'Manage Queues',
      icon: Layers,
      onAction: () => musicoletStore.selectTab(LibraryViewTab.QUEUES),
    },
    {
      num: '04.',
      title: 'Use Smart Genre Playlists & Studio Templates',
      detail:
        'Jump straight into auto-grouped Afrobeats, Amapiano, Gospel, and Highlife playlists inside Playlists, or apply a 1-click Studio Template on the Tracks home screen.',
      actionLabel: 'Open Smart Playlists',
      icon: ListMusic,
      onAction: () => musicoletStore.selectTab(LibraryViewTab.PLAYLISTS),
    },
    {
      num: '05.',
      title: 'Share WhatsApp Status Cards & Manage Storage',
      detail:
        'Tap the Share icon on any track row to generate a 1080×1920 vertical Moronto Music card for WhatsApp Status, or open Storage to toggle Data-Saver and clean up tracks.',
      actionLabel: 'Open Storage Manager',
      icon: Share2,
      onAction: () => musicoletStore.selectTab(LibraryViewTab.STORAGE),
    },
    {
      num: '06.',
      title: 'Read Our Pidgin Offline Guide or Contact Support',
      detail:
        'Wondering why offline music matters right now, or need help finding/moving audio files inside your phone? Open the Pidgin Guide & Support Desk anytime.',
      actionLabel: 'Open Pidgin Guide & Support',
      icon: BookOpen,
      onAction: () => musicoletStore.selectTab(LibraryViewTab.BLOG),
    },
  ];

  return (
    <section aria-labelledby="guide-heading" className="space-y-6">
      <div className="rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 sm:p-8 flex flex-col lg:flex-row lg:items-center justify-between gap-5">
        <div className="space-y-2.5 max-w-2xl">
          <p className="text-xs font-semibold text-[var(--mornica-emerald)] flex items-center gap-1.5">
            <Compass className="w-4 h-4" aria-hidden="true" />
            <span>Interactive Walkthrough · Moronto Music</span>
          </p>
          <h1
            id="guide-heading"
            className="text-2xl sm:text-4xl font-extrabold tracking-tight text-[#F4FAFE] leading-tight text-balance"
          >
            {tr.userGuideHeading}
          </h1>
          <p className="text-sm text-[#A5B6CA] leading-relaxed">
            Every core offline workflow explained cleanly without guesswork. Tap
            any step button to jump directly to that studio control.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5 shrink-0">
          <button
            type="button"
            onClick={() => musicoletStore.replayOnboarding()}
            className="min-h-[42px] px-4 py-2 rounded-xl bg-[var(--mornica-emerald)] text-[#04130E] text-xs font-extrabold inline-flex items-center gap-1.5 whitespace-nowrap"
          >
            <Sparkles className="w-4 h-4" aria-hidden="true" />
            <span>Replay 3-Screen Onboarding</span>
          </button>
          <button
            type="button"
            onClick={onReplayIntro}
            className="min-h-[42px] px-4 py-2 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] hover:border-[var(--mornica-cyan)] text-xs font-bold text-[#F4FAFE] whitespace-nowrap"
          >
            Replay Intro
          </button>
          <button
            type="button"
            onClick={() => musicoletStore.triggerTestFifthSessionPrompt()}
            className="min-h-[42px] px-4 py-2 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] hover:border-[#FFBE55] text-xs font-bold text-[#FFBE55] inline-flex items-center gap-1.5 whitespace-nowrap"
          >
            <Star className="w-4 h-4" aria-hidden="true" />
            <span>Preview Rating Prompt</span>
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {guideSteps.map((step) => {
          const Icon = step.icon;
          return (
            <div
              key={step.num}
              className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 flex flex-col justify-between gap-4"
            >
              <div className="space-y-2.5">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-sm font-extrabold text-[var(--mornica-emerald)] font-tabular">
                    {step.num}
                  </span>
                  <Icon className="w-5 h-5 text-[var(--mornica-cyan)]" aria-hidden="true" />
                </div>
                <h2 className="text-base font-bold text-[#F4FAFE]">
                  {step.title}
                </h2>
                <p className="text-xs sm:text-sm text-[#A5B6CA] leading-relaxed">
                  {step.detail}
                </p>
              </div>

              <button
                type="button"
                onClick={step.onAction}
                className="self-start min-h-[38px] px-3.5 py-1.5 rounded-xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] hover:border-[var(--mornica-emerald)] text-xs font-bold text-[#F4FAFE]"
              >
                {step.actionLabel}
              </button>
            </div>
          );
        })}
      </div>
    </section>
  );
}
