import { LifeBuoy, ShieldCheck } from 'lucide-react';
import {
  type MusicoletUiState,
  musicoletStore,
} from '../domain/musicoletStore';
import { LibraryViewTab, SUPPORT_EMAIL } from '../model/musicoletModels';

interface PrivacyPolicyViewProps {
  uiState: MusicoletUiState;
}

export function PrivacyPolicyView({ uiState }: PrivacyPolicyViewProps) {
  return (
    <section aria-labelledby="privacy-heading" className="space-y-6">
      <div className="rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 sm:p-8 space-y-4">
        <p className="text-xs font-semibold text-[var(--mornica-emerald)] flex items-center gap-1.5">
          <ShieldCheck className="w-4 h-4" aria-hidden="true" />
          <span>Privacy &amp; Account Transparency · Moronto Music</span>
        </p>
        <h1
          id="privacy-heading"
          className="text-2xl sm:text-4xl font-extrabold tracking-tight text-[#F4FAFE]"
        >
          Privacy Policy
        </h1>
        <p className="text-sm text-[#A5B6CA] leading-relaxed max-w-3xl">
          Moronto Music is engineered by ShadowSpark Technologies as a local-first
          offline audio studio with zero ads. Your personal music files stay on
          your device while your account credentials and support tickets are
          protected in Firebase.
        </p>

        <div className="flex flex-wrap items-center gap-3 pt-1">
          <button
            type="button"
            onClick={() => musicoletStore.selectTab(LibraryViewTab.BLOG)}
            className="min-h-[42px] px-4 py-2 rounded-xl bg-[var(--mornica-emerald)] text-[#04130E] text-xs font-extrabold inline-flex items-center gap-1.5"
          >
            <LifeBuoy className="w-4 h-4" aria-hidden="true" />
            <span>Contact Support ({SUPPORT_EMAIL})</span>
          </button>
          <button
            type="button"
            onClick={() => musicoletStore.selectTab(LibraryViewTab.UPGRADE)}
            className="min-h-[42px] px-4 py-2 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] hover:border-[#FFBE55] text-xs font-bold text-[#FFBE55]"
          >
            Pro Global Streaming · Coming Soon
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 space-y-2.5">
          <h2 className="text-base font-bold text-[#F4FAFE]">
            1. On-Device Audio &amp; Local Storage
          </h2>
          <p className="text-xs sm:text-sm text-[#A5B6CA] leading-relaxed">
            Imported audio files are stored strictly inside your device storage /
            browser IndexedDB. Your song files, queues, and playback history
            never leave your device.
          </p>
        </div>

        <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 space-y-2.5">
          <h2 className="text-base font-bold text-[#F4FAFE]">
            2. Account Authentication &amp; Profile Sync
          </h2>
          <p className="text-xs sm:text-sm text-[#A5B6CA] leading-relaxed">
            Signed in as{' '}
            <span className="text-[var(--mornica-emerald)] font-bold">
              {uiState.authUser?.email || 'Authenticated User'}
            </span>
            . Only your display name, email, preferred language, and console theme
            are synced privately to your Firestore profile.
          </p>
        </div>

        <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 space-y-2.5">
          <h2 className="text-base font-bold text-[#F4FAFE]">
            3. Zero Ads &amp; Upcoming Pro Global Streaming
          </h2>
          <p className="text-xs sm:text-sm text-[#A5B6CA] leading-relaxed">
            All offline audio features (10-band EQ, 0–12s crossfade, Sleep Timer,
            and all 4 themes) are unlocked free with zero ads. Pro tiers will only
            apply when Global Catalog Music Streaming launches.
          </p>
        </div>

        <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 space-y-2.5">
          <h2 className="text-base font-bold text-[#F4FAFE]">
            4. Support Tickets &amp; Waitlists
          </h2>
          <p className="text-xs sm:text-sm text-[#A5B6CA] leading-relaxed">
            Support tickets submitted for help moving offline music files are
            isolated strictly to your verified account so only you and Moronto
            Support can review them.
          </p>
        </div>
      </div>
    </section>
  );
}
