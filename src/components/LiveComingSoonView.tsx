import { useState } from 'react';
import { Bell, CheckCircle2, Radio } from 'lucide-react';
import liveStageImg from '../assets/images/moronto_live_stage_1795686813462.jpg';
import {
  type MusicoletUiState,
  musicoletStore,
} from '../domain/musicoletStore';
import { t } from '../i18n/translations';
import { LIVE_SHOW_PREVIEWS } from '../model/musicoletModels';

interface LiveComingSoonViewProps {
  uiState: MusicoletUiState;
}

export function LiveComingSoonView({ uiState }: LiveComingSoonViewProps) {
  const [emailInput, setEmailInput] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [imgFailed, setImgFailed] = useState(false);
  const tr = t(uiState.language);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const res = musicoletStore.submitLiveNotifyEmail(emailInput);
    if (!res.ok) {
      setError(res.error ?? 'Enter a valid email address.');
      return;
    }
    setError(null);
    setEmailInput('');
  };

  return (
    <section aria-labelledby="live-heading" className="space-y-8">
      {/* Studio Stage Hero Split */}
      <div className="rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] overflow-hidden grid grid-cols-1 lg:grid-cols-12 items-stretch">
        <div className="lg:col-span-7 p-6 sm:p-8 flex flex-col justify-between gap-6">
          <div className="space-y-3">
            <p className="text-xs font-semibold text-[#FFBE55] flex items-center gap-1.5">
              <Radio className="w-4 h-4" aria-hidden="true" />
              <span>Moronto Studio Radio · Coming Soon</span>
            </p>
            <h1
              id="live-heading"
              className="text-2xl sm:text-4xl font-extrabold tracking-tight text-[#F4FAFE] leading-tight text-balance"
            >
              {tr.liveComingSoonHeading}
            </h1>
            <p className="text-sm sm:text-base text-[#A5B6CA] leading-relaxed max-w-2xl">
              Live Streaming is coming soon to Moronto Music. Tune into curated
              African acoustic sessions, Sunday gospel rehearsals, and DJ
              log-drum mixes engineered with low-data adaptive streaming.
            </p>
          </div>

          {/* Waitlist Form */}
          <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-4 sm:p-5 space-y-3 max-w-md">
            <div className="flex items-center gap-2 text-xs font-bold text-[#F4FAFE]">
              <Bell className="w-4 h-4 text-[var(--mornica-emerald)]" aria-hidden="true" />
              <span>Get Notified When Live Broadcasts Open</span>
            </div>

            {uiState.liveNotifyEmail ? (
              <div className="space-y-2.5">
                <div className="flex items-start gap-2 text-xs font-semibold text-[var(--mornica-emerald)]">
                  <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" aria-hidden="true" />
                  <span>
                    Saved {uiState.liveNotifyEmail} for Live Streaming launch alerts.
                  </span>
                </div>
                <button
                  type="button"
                  onClick={() => musicoletStore.clearLiveNotifyEmail()}
                  className="text-xs font-semibold text-[#A5B6CA] hover:text-[#F4FAFE] underline"
                >
                  Remove alert email
                </button>
              </div>
            ) : (
              <form onSubmit={handleSubmit} noValidate className="flex flex-col sm:flex-row gap-2">
                <label htmlFor="live-waitlist-email" className="sr-only">
                  Email for Live Streaming alerts
                </label>
                <input
                  id="live-waitlist-email"
                  type="email"
                  value={emailInput}
                  onChange={(e) => setEmailInput(e.target.value)}
                  placeholder="you@domain.com"
                  className="flex-1 h-11 px-3.5 rounded-xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE]"
                />
                <button
                  type="submit"
                  className="min-h-[44px] px-4 py-2 rounded-xl bg-[var(--mornica-emerald)] hover:brightness-105 text-[#04130E] text-xs font-extrabold whitespace-nowrap"
                >
                  Notify Me
                </button>
              </form>
            )}
            {error && (
              <p role="alert" className="text-xs font-semibold text-[#FF6479]">
                {error}
              </p>
            )}
          </div>
        </div>

        <div className="lg:col-span-5 relative min-h-[240px] bg-[var(--mornica-slate-card)]">
          {!imgFailed ? (
            <>
              <img
                src={liveStageImg}
                alt="Intimate Lagos live audio studio session stage prepared for upcoming broadcasts"
                referrerPolicy="no-referrer"
                onError={() => setImgFailed(true)}
                className="w-full h-full object-cover"
              />
              <div
                aria-hidden="true"
                className="absolute inset-0 bg-gradient-to-t from-[var(--mornica-midnight-bg)]/85 via-[var(--mornica-midnight-bg)]/20 to-transparent"
              />
            </>
          ) : (
            <div className="w-full h-full flex flex-col items-center justify-center p-8 text-center">
              <Radio className="w-12 h-12 text-[#FFBE55] mb-2" aria-hidden="true" />
              <p className="text-sm font-bold text-[#F4FAFE]">Moronto Live Stage</p>
            </div>
          )}
          <div className="absolute bottom-4 left-4 right-4 text-xs text-[#F4FAFE]">
            <p className="font-bold">Lagos Partner Studio · Live Soon</p>
            <p className="text-[#A5B6CA]">
              Zero-clutter low-bandwidth audio broadcasts
            </p>
          </div>
        </div>
      </div>

      {/* Upcoming Live Lineup */}
      <div className="space-y-4">
        <div className="flex items-center justify-between gap-2">
          <h2 className="text-base font-bold text-[#F4FAFE]">
            Planned Live Sessions (Coming Soon)
          </h2>
          <span className="text-xs font-semibold text-[#A5B6CA] font-tabular">
            {LIVE_SHOW_PREVIEWS.length} upcoming formats
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {LIVE_SHOW_PREVIEWS.map((show) => (
            <article
              key={show.id}
              className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 space-y-3"
            >
              <div className="text-xs font-semibold text-[#FFBE55]">
                {show.genre} · {show.scheduleLabel}
              </div>
              <h3 className="text-base font-bold text-[#F4FAFE] leading-snug">
                {show.title}
              </h3>
              <p className="text-xs text-[#A5B6CA] leading-relaxed">
                {show.summary}
              </p>
              <div className="pt-2 border-t border-[var(--mornica-slate-border)] text-xs font-semibold text-[var(--mornica-cyan)]">
                {show.host}
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
