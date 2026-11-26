import { useRef } from 'react';
import {
  Blend,
  Crown,
  FolderPlus,
  Globe,
  LogOut,
  MailWarning,
  Music,
  SlidersHorizontal,
  Timer,
  X,
} from 'lucide-react';
import type { MusicoletUiState } from '../domain/musicoletStore';
import { musicoletStore } from '../domain/musicoletStore';
import { t } from '../i18n/translations';
import {
  APP_LANGUAGES,
  type AppLanguage,
  LibraryViewTab,
} from '../model/musicoletModels';

interface MusicoletHeaderProps {
  uiState: MusicoletUiState;
}

function formatTimerShort(totalSec: number): string {
  const m = Math.floor(totalSec / 60);
  const s = totalSec % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
}

export function MusicoletHeader({ uiState }: MusicoletHeaderProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const tr = t(uiState.language);

  const handleImportClick = () => {
    fileInputRef.current?.click();
  };

  const handleFilesChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      await musicoletStore.importLocalAudioFiles(files);
    }
    e.target.value = '';
  };

  return (
    <header className="sticky top-0 z-30 w-full bg-[var(--mornica-slate-header)]/95 backdrop-blur-md border-b border-[var(--mornica-slate-border)]">
      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept="audio/*,.mp3,.wav,.flac,.m4a,.ogg,.aac"
        onChange={handleFilesChange}
        className="sr-only"
        aria-label="Import local audio files"
      />

      <div className="max-w-6xl mx-auto px-3 sm:px-6 h-14 sm:h-16 flex items-center justify-between gap-2 sm:gap-3">
        {/* Zone 1: Single-line Brand Wordmark */}
        <button
          type="button"
          onClick={() => musicoletStore.selectTab(LibraryViewTab.TRACKS)}
          className="flex items-center gap-2 sm:gap-2.5 text-left whitespace-nowrap shrink-0 focus-visible:outline-2 focus-visible:outline-[var(--mornica-emerald)] rounded-xl"
        >
          <div className="w-8 h-8 sm:w-9 sm:h-9 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] flex items-center justify-center shrink-0">
            <Music className="w-4 h-4 text-[var(--mornica-emerald)]" aria-hidden="true" />
          </div>
          <span className="text-sm sm:text-base font-extrabold tracking-tight text-[#F4FAFE] whitespace-nowrap">
            {tr.brandName}
          </span>
        </button>

        {/* Zone 2: Clean Typographic Nav Links */}
        <nav
          aria-label="Primary sections"
          className="hidden lg:flex items-center gap-4 text-xs font-semibold"
        >
          {(
            [
              { tab: LibraryViewTab.TRACKS, label: tr.tracksTab },
              { tab: LibraryViewTab.LIVE, label: tr.liveTab },
              { tab: LibraryViewTab.UPGRADE, label: tr.upgradeTab },
              { tab: LibraryViewTab.BLOG, label: 'Pidgin Guide' },
              { tab: LibraryViewTab.STORAGE, label: tr.storageTab },
            ] as const
          ).map(({ tab, label }) => {
            const active = uiState.selectedTab === tab;
            return (
              <button
                key={tab}
                type="button"
                onClick={() => musicoletStore.selectTab(tab)}
                className={`py-1 whitespace-nowrap shrink-0 transition-colors border-b-2 ${
                  active
                    ? 'text-[var(--mornica-emerald)] border-[var(--mornica-emerald)]'
                    : 'text-[#A5B6CA] border-transparent hover:text-[#F4FAFE]'
                }`}
              >
                {label}
              </button>
            );
          })}
        </nav>

        {/* Zone 3: Language + Import + Sign Out */}
        <div className="flex items-center gap-1.5 shrink-0">
          {/* Language Switcher */}
          <label className="sr-only" htmlFor="moronto-lang-select">
            App Language
          </label>
          <div className="relative flex items-center">
            <Globe
              className="w-3.5 h-3.5 text-[var(--mornica-cyan)] absolute left-2 pointer-events-none hidden sm:block"
              aria-hidden="true"
            />
            <select
              id="moronto-lang-select"
              value={uiState.language}
              onChange={(e) =>
                musicoletStore.setLanguage(e.target.value as AppLanguage)
              }
              aria-label="Select Language"
              className="h-9 sm:h-[40px] max-w-[96px] sm:max-w-none pl-2 sm:pl-7 pr-1.5 sm:pr-2 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] text-[11px] sm:text-xs font-bold text-[#F4FAFE] focus:border-[var(--mornica-emerald)] focus:outline-none cursor-pointer"
            >
              {APP_LANGUAGES.map((lang) => (
                <option key={lang.code} value={lang.code} className="bg-[#0B1727]">
                  {lang.code.toUpperCase()} · {lang.label}
                </option>
              ))}
            </select>
          </div>

          {/* Pro Coming Soon Shortcut on tablet (sm..md) */}
          <button
            type="button"
            onClick={() => musicoletStore.selectTab(LibraryViewTab.UPGRADE)}
            aria-label="Pro Global Streaming Coming Soon"
            title="Pro Global Streaming Coming Soon"
            className="hidden sm:flex lg:hidden min-w-[40px] min-h-[40px] px-2 items-center justify-center rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] text-[#FFBE55] hover:border-[#FFBE55] transition-colors"
          >
            <Crown className="w-4 h-4 shrink-0" aria-hidden="true" />
          </button>

          {/* Equalizer (sm+) */}
          <button
            type="button"
            onClick={() => musicoletStore.setEqModalOpen(true, 'EQUALIZER')}
            aria-label="Open 10-band Equalizer"
            title="10-Band Equalizer"
            className={`hidden sm:flex min-w-[40px] min-h-[40px] px-2 items-center justify-center gap-1 rounded-xl border transition-colors ${
              uiState.equalizer.enabled
                ? 'bg-[var(--mornica-emerald-dark)] border-[var(--mornica-emerald)]/60 text-[var(--mornica-emerald)]'
                : 'bg-[var(--mornica-slate-card)] border-[var(--mornica-slate-border)] text-[#A5B6CA] hover:text-[#F4FAFE]'
            }`}
          >
            <SlidersHorizontal className="w-4 h-4 shrink-0" aria-hidden="true" />
            <span className="hidden xl:inline text-xs font-semibold whitespace-nowrap">
              10-EQ
            </span>
          </button>

          {/* Crossfade (sm+) */}
          <button
            type="button"
            onClick={() => musicoletStore.setEqModalOpen(true, 'CROSSFADE')}
            aria-label="Open Crossfade Settings"
            title="Crossfade Settings (0–12s)"
            className={`hidden sm:flex min-w-[40px] min-h-[40px] px-2 items-center justify-center gap-1 rounded-xl border transition-colors ${
              uiState.crossfade.enabled
                ? 'bg-[var(--mornica-emerald-dark)] border-[var(--mornica-cyan)]/70 text-[var(--mornica-cyan)]'
                : 'bg-[var(--mornica-slate-card)] border-[var(--mornica-slate-border)] text-[#A5B6CA] hover:text-[#F4FAFE]'
            }`}
          >
            <Blend className="w-4 h-4 shrink-0" aria-hidden="true" />
            {uiState.crossfade.enabled ? (
              <span className="text-xs font-bold font-tabular whitespace-nowrap">
                {uiState.crossfade.durationSec}s
              </span>
            ) : (
              <span className="hidden xl:inline text-xs font-semibold whitespace-nowrap">
                Fade
              </span>
            )}
          </button>

          {/* Sleep Timer (sm+) */}
          <button
            type="button"
            onClick={() => musicoletStore.setSleepTimerModalOpen(true)}
            aria-label="Open Sleep Timer"
            title="Sleep Timer"
            className={`hidden sm:flex min-w-[40px] min-h-[40px] px-2 items-center justify-center gap-1 rounded-xl border transition-colors ${
              uiState.sleepTimerRemainingSec !== null
                ? 'bg-[var(--mornica-slate-card-elevated)] border-[#FFBE55] text-[#FFBE55]'
                : 'bg-[var(--mornica-slate-card)] border-[var(--mornica-slate-border)] text-[#A5B6CA] hover:text-[#F4FAFE]'
            }`}
          >
            <Timer className="w-4 h-4 shrink-0" aria-hidden="true" />
            {uiState.sleepTimerRemainingSec !== null && (
              <span className="text-xs font-bold font-tabular whitespace-nowrap">
                {formatTimerShort(uiState.sleepTimerRemainingSec)}
              </span>
            )}
          </button>

          {/* Import Local Files */}
          <button
            type="button"
            onClick={handleImportClick}
            aria-label={tr.importAudio}
            className="h-9 sm:min-h-[40px] px-2.5 sm:px-3 py-1.5 rounded-xl bg-[var(--mornica-emerald)] hover:brightness-105 text-[#04130E] text-xs font-bold flex items-center gap-1.5 whitespace-nowrap shrink-0 transition-all"
          >
            <FolderPlus className="w-4 h-4 shrink-0" aria-hidden="true" />
            <span className="hidden md:inline">{tr.importAudio}</span>
          </button>

          {/* Sign Out */}
          {uiState.authUser && (
            <button
              type="button"
              onClick={() => void musicoletStore.signOutUser()}
              aria-label={`Sign out ${uiState.authUser.displayName}`}
              title={`Signed in as ${uiState.authUser.email} — Sign Out`}
              className="h-9 sm:min-h-[40px] px-2 sm:px-2.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] hover:border-[#FF6479] text-[#A5B6CA] hover:text-[#FF8A9A] flex items-center gap-1.5 text-xs font-semibold whitespace-nowrap shrink-0 transition-colors"
            >
              <LogOut className="w-4 h-4 shrink-0" aria-hidden="true" />
              <span className="hidden xl:inline max-w-[90px] truncate">
                {uiState.authUser.displayName}
              </span>
            </button>
          )}
        </div>
      </div>

      {/* Mobile Compact Quick Studio Strip (<sm only) */}
      <div className="sm:hidden border-t border-[var(--mornica-slate-border)]/50 px-3 py-1.5 grid grid-cols-4 gap-1.5">
        <button
          type="button"
          onClick={() => musicoletStore.selectTab(LibraryViewTab.UPGRADE)}
          aria-label="Pro Global Streaming Coming Soon"
          className="h-8 px-1.5 flex items-center justify-center gap-1 rounded-lg bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] text-[#FFBE55] text-[11px] font-bold whitespace-nowrap transition-colors"
        >
          <Crown className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
          <span>Pro Soon</span>
        </button>

        <button
          type="button"
          onClick={() => musicoletStore.setEqModalOpen(true, 'EQUALIZER')}
          aria-label="Open 10-band Equalizer"
          className={`h-8 px-1.5 flex items-center justify-center gap-1 rounded-lg border text-[11px] font-bold whitespace-nowrap transition-colors ${
            uiState.equalizer.enabled
              ? 'bg-[var(--mornica-emerald-dark)] border-[var(--mornica-emerald)]/60 text-[var(--mornica-emerald)]'
              : 'bg-[var(--mornica-slate-card)] border-[var(--mornica-slate-border)] text-[#A5B6CA]'
          }`}
        >
          <SlidersHorizontal className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
          <span>10-EQ</span>
        </button>

        <button
          type="button"
          onClick={() => musicoletStore.setEqModalOpen(true, 'CROSSFADE')}
          aria-label="Open Crossfade Settings"
          className={`h-8 px-1.5 flex items-center justify-center gap-1 rounded-lg border text-[11px] font-bold whitespace-nowrap transition-colors ${
            uiState.crossfade.enabled
              ? 'bg-[var(--mornica-emerald-dark)] border-[var(--mornica-cyan)]/70 text-[var(--mornica-cyan)]'
              : 'bg-[var(--mornica-slate-card)] border-[var(--mornica-slate-border)] text-[#A5B6CA]'
          }`}
        >
          <Blend className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
          <span className="font-tabular">
            {uiState.crossfade.enabled
              ? `${uiState.crossfade.durationSec}s`
              : 'Fade'}
          </span>
        </button>

        <button
          type="button"
          onClick={() => musicoletStore.setSleepTimerModalOpen(true)}
          aria-label="Open Sleep Timer"
          className={`h-8 px-1.5 flex items-center justify-center gap-1 rounded-lg border text-[11px] font-bold whitespace-nowrap transition-colors ${
            uiState.sleepTimerRemainingSec !== null
              ? 'bg-[var(--mornica-slate-card-elevated)] border-[#FFBE55] text-[#FFBE55]'
              : 'bg-[var(--mornica-slate-card)] border-[var(--mornica-slate-border)] text-[#A5B6CA]'
          }`}
        >
          <Timer className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
          <span className="font-tabular">
            {uiState.sleepTimerRemainingSec !== null
              ? formatTimerShort(uiState.sleepTimerRemainingSec)
              : 'Timer'}
          </span>
        </button>
      </div>

      {/* Email Verification Banner for Email/Password Accounts */}
      {uiState.authUser && !uiState.authUser.emailVerified && (
        <div className="bg-[#FFBE55]/12 border-t border-[#FFBE55]/40">
          <div className="max-w-6xl mx-auto px-3 sm:px-6 py-2 flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center gap-2 text-xs text-[#FFBE55] font-semibold">
              <MailWarning className="w-4 h-4 shrink-0" aria-hidden="true" />
              <span>
                Verify {uiState.authUser.email} to sync cloud preferences &amp; submit support tickets.
              </span>
            </div>
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => void musicoletStore.resendVerificationEmail()}
                className="px-2.5 py-1 rounded-lg bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] text-[11px] font-bold text-[#F4FAFE]"
              >
                Resend Email
              </button>
              <button
                type="button"
                onClick={() => void musicoletStore.refreshAuthVerification()}
                className="px-2.5 py-1 rounded-lg bg-[var(--mornica-emerald)] text-[#04130E] text-[11px] font-extrabold"
              >
                I&apos;ve Verified
              </button>
            </div>
          </div>
        </div>
      )}

      {uiState.statusMessage && (
        <div className="bg-[var(--mornica-slate-header)] border-t border-[var(--mornica-slate-border)]/60">
          <div className="max-w-6xl mx-auto px-4 sm:px-6 py-2 flex items-center justify-between gap-3">
            <p className="text-xs font-medium text-[#FFBE55]">
              {uiState.statusMessage}
            </p>
            <button
              type="button"
              onClick={() => musicoletStore.dismissStatus()}
              aria-label="Dismiss notification"
              className="p-1 text-[#A5B6CA] hover:text-[#F4FAFE] rounded"
            >
              <X className="w-3.5 h-3.5" aria-hidden="true" />
            </button>
          </div>
        </div>
      )}
    </header>
  );
}
