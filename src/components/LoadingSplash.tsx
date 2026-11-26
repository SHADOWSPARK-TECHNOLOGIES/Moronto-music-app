import { useEffect, useState } from 'react';
import { ArrowRight } from 'lucide-react';

interface LoadingSplashProps {
  onComplete: () => void;
}

export function LoadingSplash({ onComplete }: LoadingSplashProps) {
  const [progress, setProgress] = useState(14);
  const [exiting, setExiting] = useState(false);

  useEffect(() => {
    const reducedMotion =
      typeof window !== 'undefined' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (reducedMotion) {
      setProgress(100);
      const timer = window.setTimeout(() => {
        setExiting(true);
        window.setTimeout(onComplete, 180);
      }, 1150);
      return () => window.clearTimeout(timer);
    }

    let rafId = 0;
    const start = performance.now();
    const durationMs = 1750;

    const tick = (now: number) => {
      const elapsed = now - start;
      const ratio = Math.min(1, elapsed / durationMs);
      const eased = 1 - Math.pow(1 - ratio, 2.2);
      setProgress(Math.round(14 + eased * 86));
      if (ratio < 1) {
        rafId = window.requestAnimationFrame(tick);
      } else {
        setExiting(true);
        window.setTimeout(onComplete, 220);
      }
    };

    rafId = window.requestAnimationFrame(tick);
    return () => window.cancelAnimationFrame(rafId);
  }, [onComplete]);

  const handleEnterNow = () => {
    setExiting(true);
    window.setTimeout(onComplete, 140);
  };

  return (
    <div
      role="status"
      aria-live="polite"
      aria-label="Loading Moronto Music"
      data-testid="loading-splash"
      className={`fixed inset-0 z-[100] bg-[var(--mornica-midnight-bg)] flex items-center justify-center px-6 transition-opacity duration-200 ${
        exiting ? 'opacity-0 pointer-events-none' : 'opacity-100'
      }`}
      style={{
        backgroundImage:
          'radial-gradient(circle at 50% 35%, rgba(18, 217, 161, 0.12), transparent 55%), radial-gradient(circle at 80% 80%, rgba(32, 182, 235, 0.09), transparent 45%)',
      }}
    >
      <div className="w-full max-w-md text-center space-y-7">
        {/* Custom Brand Emblem */}
        <div className="mx-auto w-24 h-24 rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] flex items-center justify-center shadow-2xl">
          <svg
            viewBox="0 0 64 64"
            className="w-14 h-14"
            fill="none"
            aria-hidden="true"
          >
            <circle
              cx="32"
              cy="32"
              r="24"
              stroke="var(--mornica-slate-border)"
              strokeWidth="2.5"
            />
            <circle
              cx="32"
              cy="32"
              r="18"
              stroke="var(--mornica-emerald)"
              strokeWidth="2"
              strokeDasharray="6 4"
            />
            <rect x="20" y="27" width="4" height="10" rx="2" fill="var(--mornica-cyan)" />
            <rect x="27" y="21" width="4" height="22" rx="2" fill="var(--mornica-emerald)" />
            <rect x="34" y="18" width="4" height="28" rx="2" fill="var(--mornica-amber-start)" />
            <rect x="41" y="25" width="4" height="14" rx="2" fill="var(--mornica-emerald)" />
          </svg>
        </div>

        <div className="space-y-2">
          <p className="text-xs font-semibold tracking-wider text-[var(--mornica-cyan)]">
            ShadowSpark Technologies
          </p>
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-[#F4FAFE] text-balance">
            Moronto Music
          </h1>
          <p className="text-sm text-[#A5B6CA]">
            Offline Audio Studio · Multi-Queue, 10-Band EQ &amp; Crossfade
          </p>
        </div>

        {/* Progress Bar */}
        <div className="space-y-2.5 max-w-xs mx-auto">
          <div className="w-full h-1.5 rounded-full bg-[var(--mornica-slate-card)] overflow-hidden border border-[var(--mornica-slate-border)]">
            <div
              className="h-full rounded-full bg-gradient-to-r from-[var(--mornica-emerald)] via-[var(--mornica-cyan)] to-[var(--mornica-amber-start)] origin-left transition-transform duration-75"
              style={{ transform: `scaleX(${progress / 100})` }}
            />
          </div>
          <div className="flex items-center justify-between text-xs text-[#A5B6CA] font-tabular">
            <span>Preparing local audio engine</span>
            <span>{progress}%</span>
          </div>
        </div>

        <button
          type="button"
          onClick={handleEnterNow}
          className="min-h-[44px] px-5 py-2.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] hover:border-[var(--mornica-emerald)] text-xs font-bold text-[#F4FAFE] inline-flex items-center gap-2 transition-colors whitespace-nowrap"
        >
          <span>Enter Studio</span>
          <ArrowRight className="w-4 h-4 text-[var(--mornica-emerald)]" aria-hidden="true" />
        </button>
      </div>
    </div>
  );
}
