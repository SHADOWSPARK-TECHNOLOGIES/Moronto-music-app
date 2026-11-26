import { useState } from 'react';
import {
  Bell,
  BookOpen,
  CheckCircle2,
  Copy,
  Headphones,
  LifeBuoy,
  Send,
} from 'lucide-react';
import {
  type MusicoletUiState,
  musicoletStore,
} from '../domain/musicoletStore';
import type { SupportDeviceType } from '../firebase';
import {
  BLOG_PREVIEW_POSTS,
  PIDGIN_OFFLINE_ARTICLE,
  SUPPORT_EMAIL,
} from '../model/musicoletModels';

interface BlogViewProps {
  uiState: MusicoletUiState;
}

export function BlogView({ uiState }: BlogViewProps) {
  const [emailInput, setEmailInput] = useState('');
  const [blogError, setBlogError] = useState<string | null>(null);

  const [deviceType, setDeviceType] = useState<SupportDeviceType>('android');
  const [topic, setTopic] = useState('');
  const [message, setMessage] = useState('');
  const [supportError, setSupportError] = useState<string | null>(null);
  const [supportSuccess, setSupportSuccess] = useState<string | null>(null);
  const [copiedEmail, setCopiedEmail] = useState(false);

  const handleWaitlistSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const res = musicoletStore.submitBlogNotifyEmail(emailInput);
    if (!res.ok) {
      setBlogError(res.error ?? 'Enter a valid email address.');
      return;
    }
    setBlogError(null);
    setEmailInput('');
  };

  const handleSupportSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSupportError(null);
    setSupportSuccess(null);
    const res = await musicoletStore.submitSupportRequest({
      topic,
      message,
      deviceType,
    });
    if (!res.ok) {
      setSupportError(res.error ?? 'Could not submit your support request.');
      return;
    }
    setTopic('');
    setMessage('');
    setSupportSuccess('Ticket saved! Our Support team will reply to your account email.');
  };

  const handleCopySupportEmail = async () => {
    try {
      await navigator.clipboard.writeText(SUPPORT_EMAIL);
      setCopiedEmail(true);
      window.setTimeout(() => setCopiedEmail(false), 2500);
    } catch {
      // Fallback ignored inside restricted clipboard context
    }
  };

  return (
    <section aria-labelledby="blog-heading" className="space-y-8">
      {/* Featured Funny Nigerian Pidgin English Article */}
      <article className="rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 sm:p-8 space-y-6">
        <div className="space-y-3 max-w-4xl">
          <div className="flex flex-wrap items-center gap-2 text-xs font-bold text-[#FFBE55]">
            <Headphones className="w-4 h-4" aria-hidden="true" />
            <span>{PIDGIN_OFFLINE_ARTICLE.eyebrow}</span>
            <span aria-hidden="true">·</span>
            <span className="text-[var(--mornica-emerald)]">
              {PIDGIN_OFFLINE_ARTICLE.readTime}
            </span>
          </div>
          <h1
            id="blog-heading"
            className="text-2xl sm:text-4xl font-extrabold tracking-tight text-[#F4FAFE] leading-tight text-balance"
          >
            {PIDGIN_OFFLINE_ARTICLE.title}
          </h1>
          <p className="text-sm sm:text-base text-[#A5B6CA] leading-relaxed">
            {PIDGIN_OFFLINE_ARTICLE.subtitle}
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {PIDGIN_OFFLINE_ARTICLE.sections.map((section) => (
            <div
              key={section.heading}
              className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 space-y-2"
            >
              <h2 className="text-sm sm:text-base font-extrabold text-[var(--mornica-emerald)]">
                {section.heading}
              </h2>
              <p className="text-xs sm:text-sm text-[#F4FAFE]/90 leading-relaxed">
                {section.body}
              </p>
            </div>
          ))}
        </div>
      </article>

      {/* Contact Moronto Support Desk for Offline Audio File Help */}
      <section
        aria-labelledby="support-desk-heading"
        className="rounded-3xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] p-6 sm:p-8 grid grid-cols-1 lg:grid-cols-12 gap-6"
      >
        <div className="lg:col-span-5 space-y-4">
          <p className="text-xs font-semibold text-[var(--mornica-cyan)] flex items-center gap-1.5">
            <LifeBuoy className="w-4 h-4" aria-hidden="true" />
            <span>Moronto Music File Support Desk</span>
          </p>
          <h2
            id="support-desk-heading"
            className="text-xl sm:text-2xl font-extrabold text-[#F4FAFE] leading-snug"
          >
            Need help getting or moving music files into your device?
          </h2>
          <p className="text-xs sm:text-sm text-[#A5B6CA] leading-relaxed">
            Whether your MP3/WAV tracks are sitting inside WhatsApp Documents,
            Telegram, an SD card, or an old laptop, tell us what device you use
            and our team will guide you cleanly.
          </p>

          <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-4 space-y-2.5">
            <div className="text-xs font-bold text-[#F4FAFE]">
              Direct Support Email
            </div>
            <div className="flex items-center justify-between gap-2">
              <a
                href={`mailto:${SUPPORT_EMAIL}?subject=Moronto%20Music%20Offline%20File%20Help`}
                className="text-xs sm:text-sm font-mono font-bold text-[var(--mornica-emerald)] hover:underline truncate"
              >
                {SUPPORT_EMAIL}
              </a>
              <button
                type="button"
                onClick={() => void handleCopySupportEmail()}
                className="px-3 py-1.5 rounded-lg bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] text-[11px] font-bold text-[#F4FAFE] inline-flex items-center gap-1 shrink-0"
              >
                <Copy className="w-3.5 h-3.5" aria-hidden="true" />
                <span>{copiedEmail ? 'Copied!' : 'Copy'}</span>
              </button>
            </div>
          </div>

          {uiState.supportRequests.length > 0 && (
            <div className="space-y-2 pt-2">
              <div className="text-xs font-bold text-[#FFBE55]">
                Your Submitted Support Tickets ({uiState.supportRequests.length})
              </div>
              <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
                {uiState.supportRequests.map((ticket) => (
                  <div
                    key={ticket.id}
                    className="rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-3 space-y-1"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-xs font-bold text-[#F4FAFE] truncate">
                        {ticket.topic}
                      </span>
                      <span className="text-[10px] font-bold uppercase text-[var(--mornica-emerald)] shrink-0">
                        {ticket.status}
                      </span>
                    </div>
                    <p className="text-[11px] text-[#A5B6CA] line-clamp-2">
                      {ticket.message}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        <form
          onSubmit={handleSupportSubmit}
          noValidate
          className="lg:col-span-7 rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 space-y-3.5"
        >
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div className="space-y-1">
              <label
                htmlFor="support-device"
                className="text-xs font-bold text-[#F4FAFE]"
              >
                Your Device
              </label>
              <select
                id="support-device"
                value={deviceType}
                onChange={(e) => setDeviceType(e.target.value as SupportDeviceType)}
                className="w-full h-11 px-3 rounded-xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] text-sm text-[#F4FAFE] focus:border-[var(--mornica-emerald)] focus:outline-none"
              >
                <option value="android">Android Phone / Tablet</option>
                <option value="iphone">iPhone / iPad</option>
                <option value="windows-mac">Windows / Mac Computer</option>
                <option value="other">Other Device</option>
              </select>
            </div>

            <div className="space-y-1">
              <label
                htmlFor="support-topic"
                className="text-xs font-bold text-[#F4FAFE]"
              >
                Topic
              </label>
              <input
                id="support-topic"
                type="text"
                value={topic}
                onChange={(e) => setTopic(e.target.value)}
                placeholder="e.g. Moving MP3 files from Telegram"
                className="w-full h-11 px-3.5 rounded-xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] text-sm text-[#F4FAFE] focus:border-[var(--mornica-emerald)] focus:outline-none"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label
              htmlFor="support-message"
              className="text-xs font-bold text-[#F4FAFE]"
            >
              How can Support help you get your audio files into Moronto Music?
            </label>
            <textarea
              id="support-message"
              rows={4}
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="Explain where your songs are saved (WhatsApp, SD card, laptop, or downloads folder) and what happens when you try to import them..."
              className="w-full p-3.5 rounded-xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] text-sm text-[#F4FAFE] focus:border-[var(--mornica-emerald)] focus:outline-none"
            />
          </div>

          {!uiState.authUser?.emailVerified && (
            <p className="text-[11px] font-semibold text-[#FFBE55]">
              Verify your email from the top banner first so your support ticket can be saved to your account.
            </p>
          )}

          {supportError && (
            <p role="alert" className="text-xs font-semibold text-[#FF6479]">
              {supportError}
            </p>
          )}

          {supportSuccess && (
            <p role="status" className="text-xs font-semibold text-[var(--mornica-emerald)]">
              {supportSuccess}
            </p>
          )}

          <button
            type="submit"
            disabled={uiState.supportSubmitting}
            className="min-h-[44px] px-5 py-2.5 rounded-xl mornica-amber-gradient text-[#0B101B] text-xs font-extrabold inline-flex items-center gap-2 disabled:opacity-50"
          >
            <Send className="w-4 h-4" aria-hidden="true" />
            <span>
              {uiState.supportSubmitting ? 'Sending...' : 'Send Message to Support'}
            </span>
          </button>
        </form>
      </section>

      {/* Upcoming Issues Preview (Coming Soon) + Waitlist */}
      <div className="space-y-4">
        <div className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="flex items-center gap-2 text-sm font-bold text-[#F4FAFE]">
              <Bell className="w-4 h-4 text-[var(--mornica-emerald)]" aria-hidden="true" />
              <span>More Moronto Journal Issues — Coming Soon</span>
            </div>
            <p className="text-xs text-[#A5B6CA]">
              Deep dives on dual-deck crossfading, Afrobeats EQ curves, and 20-queue workflows.
            </p>
          </div>

          {uiState.blogNotifyEmail ? (
            <div className="flex items-center gap-3 text-xs text-[var(--mornica-emerald)] font-semibold">
              <CheckCircle2 className="w-4 h-4 shrink-0" aria-hidden="true" />
              <span>Registered {uiState.blogNotifyEmail}</span>
              <button
                type="button"
                onClick={() => musicoletStore.clearBlogNotifyEmail()}
                className="text-[#A5B6CA] hover:text-[#F4FAFE] underline"
              >
                Remove
              </button>
            </div>
          ) : (
            <form onSubmit={handleWaitlistSubmit} noValidate className="flex flex-col sm:flex-row gap-2 w-full lg:w-auto">
              <input
                type="email"
                value={emailInput}
                onChange={(e) => setEmailInput(e.target.value)}
                placeholder="you@domain.com"
                aria-label="Email address for Blog launch updates"
                className="h-10 px-3 rounded-xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] text-xs text-[#F4FAFE] focus:border-[var(--mornica-emerald)] focus:outline-none"
              />
              <button
                type="submit"
                className="h-10 px-4 rounded-xl bg-[var(--mornica-emerald)] text-[#04130E] text-xs font-bold whitespace-nowrap"
              >
                Notify Me
              </button>
              {blogError && (
                <p role="alert" className="text-xs font-semibold text-[#FF6479]">
                  {blogError}
                </p>
              )}
            </form>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {BLOG_PREVIEW_POSTS.map((post) => (
            <article
              key={post.id}
              className="rounded-2xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] p-5 flex flex-col justify-between gap-4"
            >
              <div className="space-y-2.5">
                <div className="text-xs text-[#A5B6CA] flex items-center gap-1.5">
                  <span className="text-[var(--mornica-cyan)] font-semibold">
                    {post.category}
                  </span>
                  <span aria-hidden="true">·</span>
                  <span>{post.readTime}</span>
                  <span aria-hidden="true">·</span>
                  <span className="text-[#FFBE55] font-semibold">Coming Soon</span>
                </div>
                <h3 className="text-base font-bold text-[#F4FAFE] leading-snug">
                  {post.title}
                </h3>
                <p className="text-xs text-[#A5B6CA] leading-relaxed">
                  {post.summary}
                </p>
              </div>

              <div className="pt-3 border-t border-[var(--mornica-slate-border)] flex items-center gap-2 text-xs font-semibold text-[#A5B6CA]">
                <BookOpen className="w-4 h-4 text-[var(--mornica-emerald)]" aria-hidden="true" />
                <span>In Editorial Production · Coming Soon</span>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
