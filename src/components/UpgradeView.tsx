import { useState } from 'react';
import {
  Bell,
  Check,
  CheckCircle2,
  Crown,
  Globe2,
  Palette,
  Sparkles,
} from 'lucide-react';
import {
  type MusicoletUiState,
  musicoletStore,
} from '../domain/musicoletStore';
import { t } from '../i18n/translations';
import {
  LibraryViewTab,
  MORONTO_THEMES,
  PRO_COMING_SOON_HIGHLIGHTS,
} from '../model/musicoletModels';

interface UpgradeViewProps {
  uiState: MusicoletUiState;
}

export function UpgradeView({ uiState }: UpgradeViewProps) {
  const tr = t(uiState.language);
  const [emailInput, setEmailInput] = useState('');
  const [error, setError] = useState<string | null>(null);

  const handleWaitlistSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const res = musicoletStore.submitProNotifyEmail(emailInput);
    if (!res.ok) {
      setError(res.error ?? 'Enter a valid email address.');
      return;
    }
    setError(null);
    setEmailInput('');
  };

  return (
    <section aria-labelledby="upgrade-heading" className="space-y-8">
      {/* Pro Coming Soon Hero */}
      <div className="rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 sm:p-8 flex flex-col lg:flex-row lg:items-center justify-between gap-6">
        <div className="space-y-3 max-w-2xl">
          <p className="text-xs font-semibold text-[#FFBE55] flex items-center gap-1.5">
            <Crown className="w-4 h-4" aria-hidden="true" />
            <span>Global Catalog Streaming · Coming Soon</span>
          </p>
          <h1
            id="upgrade-heading"
            className="text-2xl sm:text-4xl font-extrabold tracking-tight text-[#F4FAFE] leading-tight text-balance"
          >
            {tr.proUpgradeHeading}
          </h1>
          <p className="text-sm sm:text-base text-[#A5B6CA] leading-relaxed">
            Every paid plan lock has been removed! Right now Moronto Music is a
            100% offline audio studio with full access to all 10-band EQ presets,
            0–12s crossfade, Sleep Timer, all 4 Studio Themes, and zero ads. Pro
            will launch only when we integrate global music catalog streaming.
          </p>
        </div>

        {/* Pro Global Streaming Waitlist */}
        <div className="w-full lg:w-[380px] shrink-0 rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 space-y-3">
          <div className="flex items-center gap-2 text-sm font-bold text-[#F4FAFE]">
            <Bell className="w-4 h-4 text-[#FFBE55]" aria-hidden="true" />
            <span>Notify Me When Pro Streaming Launches</span>
          </div>

          {uiState.proNotifyEmail ? (
            <div className="space-y-3">
              <div className="flex items-start gap-2.5 text-xs text-[var(--mornica-emerald)] font-semibold">
                <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" aria-hidden="true" />
                <span>
                  Registered {uiState.proNotifyEmail} for Global Catalog Streaming
                  updates.
                </span>
              </div>
              <button
                type="button"
                onClick={() => musicoletStore.clearProNotifyEmail()}
                className="text-xs font-semibold text-[#A5B6CA] hover:text-[#F4FAFE] underline"
              >
                Change or remove email
              </button>
            </div>
          ) : (
            <form onSubmit={handleWaitlistSubmit} noValidate className="space-y-2.5">
              <label htmlFor="pro-waitlist-email" className="sr-only">
                Email for Pro Global Streaming launch updates
              </label>
              <input
                id="pro-waitlist-email"
                type="email"
                value={emailInput}
                onChange={(e) => setEmailInput(e.target.value)}
                placeholder={uiState.authUser?.email || 'you@domain.com'}
                className="w-full h-11 px-3.5 rounded-xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE]"
              />
              {error && (
                <p role="alert" className="text-xs font-semibold text-[#FF6479]">
                  {error}
                </p>
              )}
              <button
                type="submit"
                className="w-full min-h-[42px] px-4 py-2 rounded-xl mornica-amber-gradient text-[#0B101B] text-xs font-extrabold whitespace-nowrap"
              >
                Join Pro Streaming Waitlist
              </button>
            </form>
          )}
        </div>
      </div>

      {/* Today vs Upcoming Pro Comparison */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        <div className="rounded-3xl bg-[var(--mornica-slate-card-elevated)] border border-[var(--mornica-emerald)] p-6 flex flex-col justify-between gap-5">
          <div className="space-y-4">
            <div className="flex items-start justify-between gap-3">
              <div>
                <span className="text-xs font-bold text-[var(--mornica-emerald)]">
                  Active Right Now
                </span>
                <h2 className="text-lg font-extrabold text-[#F4FAFE] mt-0.5">
                  Moronto Offline Studio · Full Access
                </h2>
              </div>
              <span className="text-xl font-extrabold text-[var(--mornica-emerald)]">
                100% Free
              </span>
            </div>

            <ul className="space-y-2.5 pt-3 border-t border-[var(--mornica-slate-border)]">
              {[
                'Zero banner ads & zero rewarded ad gates anywhere',
                'All 5 10-band EQ presets (Afrobeats Bass, Amapiano Log Drum, Gospel, Highlife, Flat)',
                'Dual-deck Crossfade (0–12 seconds) & Gapless Playback',
                'Sleep Timer, 20 Resumable Queues, Smart Playlists & WhatsApp Status Cards',
                'All 4 Studio Color Themes unlocked immediately',
              ].map((feat) => (
                <li
                  key={feat}
                  className="flex items-start gap-2.5 text-xs sm:text-sm text-[#F4FAFE]/90"
                >
                  <Check
                    className="w-4 h-4 text-[var(--mornica-emerald)] shrink-0 mt-0.5"
                    aria-hidden="true"
                  />
                  <span>{feat}</span>
                </li>
              ))}
            </ul>
          </div>

          <button
            type="button"
            onClick={() => musicoletStore.selectTab(LibraryViewTab.TRACKS)}
            className="min-h-[44px] w-full px-5 py-2.5 rounded-xl bg-[var(--mornica-emerald)] text-[#04130E] text-xs font-extrabold inline-flex items-center justify-center gap-2"
          >
            <Sparkles className="w-4 h-4" aria-hidden="true" />
            <span>Open Tracks &amp; Start Listening</span>
          </button>
        </div>

        <div className="rounded-3xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-6 flex flex-col justify-between gap-5">
          <div className="space-y-4">
            <div className="flex items-start justify-between gap-3">
              <div>
                <span className="text-xs font-bold text-[#FFBE55]">
                  Coming Soon
                </span>
                <h2 className="text-lg font-extrabold text-[#F4FAFE] mt-0.5">
                  Moronto Music Pro · Global Catalog Streaming
                </h2>
              </div>
              <Globe2 className="w-6 h-6 text-[#FFBE55] shrink-0" aria-hidden="true" />
            </div>

            <ul className="space-y-2.5 pt-3 border-t border-[var(--mornica-slate-border)]">
              {PRO_COMING_SOON_HIGHLIGHTS.map((feat) => (
                <li
                  key={feat}
                  className="flex items-start gap-2.5 text-xs sm:text-sm text-[#A5B6CA]"
                >
                  <Check
                    className="w-4 h-4 text-[#FFBE55] shrink-0 mt-0.5"
                    aria-hidden="true"
                  />
                  <span>{feat}</span>
                </li>
              ))}
            </ul>
          </div>

          <button
            type="button"
            onClick={() => musicoletStore.selectTab(LibraryViewTab.BLOG)}
            className="min-h-[44px] w-full px-5 py-2.5 rounded-xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] hover:border-[var(--mornica-cyan)] text-xs font-bold text-[#F4FAFE]"
          >
            Need Local Music Files Now? Read Pidgin Guide &amp; Contact Support
          </button>
        </div>
      </div>

      {/* All 4 Studio Color Themes Unlocked */}
      <div className="rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 space-y-4">
        <div className="flex items-center gap-2">
          <Palette className="w-5 h-5 text-[var(--mornica-cyan)]" aria-hidden="true" />
          <div>
            <h2 className="text-base font-bold text-[#F4FAFE]">
              Studio Color Themes (All Unlocked)
            </h2>
            <p className="text-xs text-[#A5B6CA]">
              Switch freely between all 4 Moronto Music console themes — synced to your verified account.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {MORONTO_THEMES.map((theme) => {
            const active = uiState.themeId === theme.id;
            return (
              <button
                key={theme.id}
                type="button"
                onClick={() => musicoletStore.setTheme(theme.id)}
                aria-pressed={active}
                className={`text-left rounded-2xl border p-4 space-y-2.5 transition-colors ${
                  active
                    ? 'bg-[var(--mornica-slate-card-elevated)] border-[var(--mornica-emerald)]'
                    : 'bg-[var(--mornica-slate-card)] border-[var(--mornica-slate-border)] hover:border-[var(--mornica-cyan)]'
                }`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1.5">
                    <span
                      className="w-4 h-4 rounded-full border border-white/20"
                      style={{ backgroundColor: theme.bgHex }}
                      aria-hidden="true"
                    />
                    <span
                      className="w-4 h-4 rounded-full border border-white/20"
                      style={{ backgroundColor: theme.accentHex }}
                      aria-hidden="true"
                    />
                  </div>
                  {active && (
                    <span className="text-[11px] font-bold text-[var(--mornica-emerald)]">
                      Active
                    </span>
                  )}
                </div>
                <div>
                  <div className="text-sm font-bold text-[#F4FAFE]">{theme.name}</div>
                  <p className="text-[11px] text-[#A5B6CA] leading-snug mt-0.5">
                    {theme.description}
                  </p>
                </div>
              </button>
            );
          })}
        </div>
      </div>
    </section>
  );
}
