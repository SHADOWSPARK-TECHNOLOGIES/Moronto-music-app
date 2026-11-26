import { useState } from 'react';
import { Check, Copy, HeartHandshake } from 'lucide-react';
import sponsorStageImg from '../assets/images/moronto_sponsor_stage_1795686000032.jpg';
import {
  type MusicoletUiState,
  musicoletStore,
} from '../domain/musicoletStore';
import {
  SPONSOR_TIERS,
  type SponsorBilling,
  type SponsorTierId,
} from '../model/musicoletModels';

interface SponsorViewProps {
  uiState: MusicoletUiState;
}

export function SponsorView({ uiState }: SponsorViewProps) {
  const [selectedTierId, setSelectedTierId] =
    useState<SponsorTierId>('patron');
  const [billing, setBilling] = useState<SponsorBilling>('monthly');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [note, setNote] = useState('');
  const [formError, setFormError] = useState<string | null>(null);
  const [copiedEmail, setCopiedEmail] = useState(false);

  const selectedTier =
    SPONSOR_TIERS.find((t) => t.id === selectedTierId) ?? SPONSOR_TIERS[1];
  const selectedAmount =
    billing === 'monthly' ? selectedTier.monthlyUsd : selectedTier.oneTimeUsd;

  const handleCopyEmail = async () => {
    try {
      await navigator.clipboard.writeText('sponsor@morontomusic.app');
      setCopiedEmail(true);
      window.setTimeout(() => setCopiedEmail(false), 2000);
    } catch {
      setCopiedEmail(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const res = musicoletStore.submitSponsorPledge({
      name,
      email,
      tierId: selectedTierId,
      billing,
      note,
    });
    if (!res.ok) {
      setFormError(res.error ?? 'Check your sponsorship details.');
      return;
    }
    setFormError(null);
    setName('');
    setEmail('');
    setNote('');
  };

  return (
    <section aria-labelledby="sponsor-heading" className="space-y-8">
      {/* Documentary Hero Banner */}
      <div className="rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] overflow-hidden grid grid-cols-1 lg:grid-cols-12 items-stretch">
        <div className="lg:col-span-7 p-6 sm:p-8 flex flex-col justify-between gap-5">
          <div className="space-y-3">
            <p className="text-xs font-semibold text-[#FFBE55]">
              Community Sponsorship · Moronto Music
            </p>
            <h1
              id="sponsor-heading"
              className="text-2xl sm:text-4xl font-extrabold tracking-tight text-[#F4FAFE] leading-tight text-balance"
            >
              Keep offline African audio tools independent and vibrant.
            </h1>
            <p className="text-sm sm:text-base text-[#A5B6CA] leading-relaxed max-w-2xl">
              Sponsorships directly fund lossless audio codec research, native
              Android Equalizer &amp; Crossfade development, and licensed indie
              artist showcases bundled inside Moronto Music.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button
              type="button"
              onClick={handleCopyEmail}
              className="min-h-[42px] px-4 py-2 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] hover:border-[var(--mornica-emerald)] text-xs font-bold text-[#F4FAFE] inline-flex items-center gap-2 whitespace-nowrap transition-colors"
            >
              {copiedEmail ? (
                <>
                  <Check className="w-4 h-4 text-[var(--mornica-emerald)]" aria-hidden="true" />
                  <span>Copied sponsor@morontomusic.app</span>
                </>
              ) : (
                <>
                  <Copy className="w-4 h-4 text-[var(--mornica-cyan)]" aria-hidden="true" />
                  <span>Copy sponsor@morontomusic.app</span>
                </>
              )}
            </button>
          </div>
        </div>

        <div className="lg:col-span-5 relative min-h-[220px] bg-[var(--mornica-slate-card)]">
          <img
            src={sponsorStageImg}
            alt="Live contemporary African brass and percussion recording session supported by community sponsors"
            referrerPolicy="no-referrer"
            className="w-full h-full object-cover"
          />
          <div
            aria-hidden="true"
            className="absolute inset-0 bg-gradient-to-t from-black/85 via-black/25 to-transparent"
          />
          <div className="absolute bottom-4 left-4 right-4 text-xs text-[#F4FAFE]">
            <p className="font-bold">100% Listener &amp; Studio Backed</p>
            <p className="text-[#A5B6CA]">
              Zero tracking scripts · Zero ad networks
            </p>
          </div>
        </div>
      </div>

      {/* Billing Cadence Switch & Tier Selector */}
      <div className="space-y-5">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-lg font-bold text-[#F4FAFE]">
              Choose a Sponsorship Tier
            </h2>
            <p className="text-xs text-[#A5B6CA] mt-0.5">
              Select a support level below and record your pledge or partner
              inquiry.
            </p>
          </div>

          <div
            role="group"
            aria-label="Sponsorship billing frequency"
            className="inline-flex p-1 rounded-xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] self-start"
          >
            {(['monthly', 'one-time'] as const).map((mode) => {
              const active = billing === mode;
              return (
                <button
                  key={mode}
                  type="button"
                  onClick={() => setBilling(mode)}
                  aria-pressed={active}
                  className={`px-4 py-2 rounded-lg text-xs font-bold whitespace-nowrap transition-colors ${
                    active
                      ? 'bg-[var(--mornica-emerald)] text-[#04130E]'
                      : 'text-[#A5B6CA] hover:text-[#F4FAFE]'
                  }`}
                >
                  {mode === 'monthly' ? 'Monthly' : 'One-Time'}
                </button>
              );
            })}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {SPONSOR_TIERS.map((tier) => {
            const active = tier.id === selectedTierId;
            const price =
              billing === 'monthly' ? tier.monthlyUsd : tier.oneTimeUsd;
            return (
              <button
                key={tier.id}
                type="button"
                onClick={() => setSelectedTierId(tier.id)}
                aria-pressed={active}
                className={`text-left rounded-2xl border p-5 flex flex-col justify-between gap-4 transition-colors ${
                  active
                    ? 'bg-[var(--mornica-slate-card-elevated)] border-[var(--mornica-emerald)]'
                    : 'bg-[var(--mornica-slate-card)] border-[var(--mornica-slate-border)] hover:border-[var(--mornica-emerald)]/60'
                }`}
              >
                <div className="space-y-2.5">
                  <div className="flex items-baseline justify-between gap-2">
                    <span className="text-base font-bold text-[#F4FAFE]">
                      {tier.name}
                    </span>
                    <span className="text-xl font-extrabold text-[var(--mornica-emerald)] font-tabular whitespace-nowrap">
                      ${price}
                      <span className="text-xs font-semibold text-[#A5B6CA]">
                        {billing === 'monthly' ? '/mo' : ''}
                      </span>
                    </span>
                  </div>
                  <p className="text-xs text-[#A5B6CA] leading-relaxed">
                    {tier.summary}
                  </p>
                  <ul className="space-y-1.5 pt-2 border-t border-[var(--mornica-slate-border)]">
                    {tier.perks.map((perk) => (
                      <li
                        key={perk}
                        className="text-xs text-[#F4FAFE]/90 leading-snug"
                      >
                        · {perk}
                      </li>
                    ))}
                  </ul>
                </div>

                <div className="text-xs font-bold text-[var(--mornica-cyan)]">
                  {active ? 'Selected Tier' : 'Select Tier'}
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Pledge Form + Recorded Pledges */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        <form
          onSubmit={handleSubmit}
          noValidate
          className="lg:col-span-7 rounded-2xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 space-y-4"
        >
          <div className="flex items-center justify-between gap-2">
            <div>
              <h2 className="text-base font-bold text-[#F4FAFE]">
                Record Sponsorship Pledge
              </h2>
              <p className="text-xs text-[#A5B6CA] mt-0.5 font-tabular">
                {selectedTier.name} · ${selectedAmount}{' '}
                {billing === 'monthly' ? 'per month' : 'one-time'}
              </p>
            </div>
            <HeartHandshake
              className="w-5 h-5 text-[#FFBE55] shrink-0"
              aria-hidden="true"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label
                htmlFor="sponsor-name"
                className="block text-xs font-semibold text-[#A5B6CA] mb-1.5"
              >
                Your Name or Organization
              </label>
              <input
                id="sponsor-name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g. Victoria Island Records"
                className="w-full h-11 px-3.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE]"
              />
            </div>

            <div>
              <label
                htmlFor="sponsor-email"
                className="block text-xs font-semibold text-[#A5B6CA] mb-1.5"
              >
                Email Address
              </label>
              <input
                id="sponsor-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@domain.com"
                className="w-full h-11 px-3.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE]"
              />
            </div>
          </div>

          <div>
            <label
              htmlFor="sponsor-note"
              className="block text-xs font-semibold text-[#A5B6CA] mb-1.5"
            >
              Note or Feature Priority (Optional)
            </label>
            <textarea
              id="sponsor-note"
              rows={3}
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Share what you would love to see next in Moronto Music..."
              className="w-full p-3.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE]"
            />
          </div>

          {formError && (
            <p role="alert" className="text-xs font-semibold text-[#FF6479]">
              {formError}
            </p>
          )}

          <button
            type="submit"
            className="min-h-[44px] px-5 py-2.5 rounded-xl mornica-amber-gradient text-[#0B101B] text-xs font-extrabold whitespace-nowrap hover:brightness-105 transition-all"
          >
            Confirm {selectedTier.name} Pledge (${selectedAmount})
          </button>
        </form>

        <div className="lg:col-span-5 rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-6 space-y-4">
          <div className="flex items-center justify-between gap-2">
            <h2 className="text-base font-bold text-[#F4FAFE]">
              Saved Sponsorship Ledger
            </h2>
            <span className="text-xs font-bold text-[var(--mornica-cyan)] font-tabular">
              {uiState.sponsorPledges.length} recorded
            </span>
          </div>

          {uiState.sponsorPledges.length === 0 ? (
            <p className="text-xs text-[#A5B6CA] leading-relaxed">
              No sponsorship pledges recorded in this browser yet. Submit the
              form to log a local supporter or partner commitment.
            </p>
          ) : (
            <ul className="divide-y divide-[var(--mornica-slate-border)]">
              {uiState.sponsorPledges.map((pledge) => (
                <li key={pledge.id} className="py-3 first:pt-0 last:pb-0 space-y-1">
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-sm font-bold text-[#F4FAFE] truncate">
                      {pledge.name}
                    </span>
                    <span className="text-xs font-extrabold text-[var(--mornica-emerald)] font-tabular whitespace-nowrap">
                      ${pledge.amountUsd}{' '}
                      {pledge.billing === 'monthly' ? '/mo' : 'once'}
                    </span>
                  </div>
                  <div className="text-xs text-[#A5B6CA] truncate">
                    {pledge.tierId} · {pledge.email}
                  </div>
                  {pledge.note && (
                    <p className="text-xs text-[#F4FAFE]/80">{pledge.note}</p>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </section>
  );
}
