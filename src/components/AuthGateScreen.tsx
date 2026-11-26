import { useState } from 'react';
import {
  CheckCircle2,
  KeyRound,
  Lock,
  Mail,
  Music,
  ShieldCheck,
  SlidersHorizontal,
  UserPlus,
} from 'lucide-react';
import {
  createEmailAccount,
  mapFirebaseAuthError,
  sendAccountPasswordReset,
  signInWithEmailAccount,
  signInWithGoogleAccount,
} from '../firebase';

type AuthMode = 'signup' | 'signin' | 'reset';

export function AuthGateScreen() {
  const [mode, setMode] = useState<AuthMode>('signup');
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const handleGoogleSignIn = async () => {
    setError(null);
    setNotice(null);
    setBusy(true);
    try {
      await signInWithGoogleAccount();
    } catch (err) {
      setError(mapFirebaseAuthError(err));
    } finally {
      setBusy(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setNotice(null);

    const cleanEmail = email.trim();
    if (!cleanEmail || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleanEmail)) {
      setError('Enter a valid email address from any provider (Gmail, Yahoo, Outlook, iCloud, or custom domain).');
      return;
    }

    if (mode === 'reset') {
      setBusy(true);
      try {
        await sendAccountPasswordReset(cleanEmail);
        setNotice(`Password reset link sent to ${cleanEmail}. Check your inbox.`);
      } catch (err) {
        setError(mapFirebaseAuthError(err));
      } finally {
        setBusy(false);
      }
      return;
    }

    if (mode === 'signup' && displayName.trim().length < 2) {
      setError('Enter your name (at least 2 characters) to create your Moronto Music account.');
      return;
    }
    if (password.length < 6) {
      setError('Enter a password with at least 6 characters.');
      return;
    }

    setBusy(true);
    try {
      if (mode === 'signup') {
        await createEmailAccount({
          displayName,
          email: cleanEmail,
          password,
        });
      } else {
        await signInWithEmailAccount(cleanEmail, password);
      }
    } catch (err) {
      setError(mapFirebaseAuthError(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div
      className="min-h-screen bg-[var(--mornica-midnight-bg)] text-[#F4FAFE] flex items-center justify-center p-4 sm:p-6"
      style={{
        backgroundImage:
          'radial-gradient(circle at 20% 10%, rgba(18, 217, 161, 0.10), transparent 45%), radial-gradient(circle at 80% 85%, rgba(32, 182, 235, 0.08), transparent 40%)',
      }}
    >
      <div className="w-full max-w-4xl grid grid-cols-1 lg:grid-cols-12 rounded-3xl overflow-hidden bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)]">
        {/* Left Studio Value Panel */}
        <section className="lg:col-span-5 bg-[var(--mornica-slate-card)] border-b lg:border-b-0 lg:border-r border-[var(--mornica-slate-border)] p-6 sm:p-8 flex flex-col justify-between gap-6">
          <div className="space-y-4">
            <div className="flex items-center gap-2.5">
              <div className="w-10 h-10 rounded-xl bg-[var(--mornica-slate-header)] border border-[var(--mornica-slate-border)] flex items-center justify-center">
                <Music className="w-5 h-5 text-[var(--mornica-emerald)]" aria-hidden="true" />
              </div>
              <div>
                <div className="text-lg font-extrabold tracking-tight text-[#F4FAFE]">
                  Moronto Music
                </div>
                <div className="text-[11px] font-semibold text-[var(--mornica-emerald)]">
                  Offline Audio Studio · All Tools Unlocked
                </div>
              </div>
            </div>

            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-[#F4FAFE] leading-tight text-balance">
              Create your listener account to enter the studio.
            </h1>

            <p className="text-xs sm:text-sm text-[#A5B6CA] leading-relaxed">
              Authenticate once with Google or any email provider to unlock full
              access to every offline tool, sync your theme &amp; language, and
              reserve your spot for upcoming Pro Global Streaming.
            </p>
          </div>

          <ul className="space-y-2.5 text-xs text-[#F4FAFE]/90">
            <li className="flex items-start gap-2">
              <SlidersHorizontal
                className="w-4 h-4 text-[var(--mornica-emerald)] shrink-0 mt-0.5"
                aria-hidden="true"
              />
              <span>
                10-Band EQ (Afrobeats Bass, Amapiano Log Drum, Gospel, Highlife, Flat) &amp; 0–12s Crossfade unlocked for everyone
              </span>
            </li>
            <li className="flex items-start gap-2">
              <CheckCircle2
                className="w-4 h-4 text-[var(--mornica-cyan)] shrink-0 mt-0.5"
                aria-hidden="true"
              />
              <span>
                Zero ads, 20 resumable queues, Sleep Timer, &amp; all 4 Studio Themes
              </span>
            </li>
            <li className="flex items-start gap-2">
              <ShieldCheck
                className="w-4 h-4 text-[#FFBE55] shrink-0 mt-0.5"
                aria-hidden="true"
              />
              <span>
                Includes funny Naija Pidgin offline audio guide &amp; direct Support Desk
              </span>
            </li>
          </ul>
        </section>

        {/* Right Auth Controls */}
        <section
          aria-label="Account Authentication"
          className="lg:col-span-7 p-6 sm:p-8 space-y-5"
        >
          {/* Mode Switcher */}
          <div
            role="tablist"
            aria-label="Authentication mode"
            className="grid grid-cols-2 p-1 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)]"
          >
            <button
              type="button"
              role="tab"
              aria-selected={mode === 'signup'}
              onClick={() => {
                setMode('signup');
                setError(null);
                setNotice(null);
              }}
              className={`py-2.5 rounded-lg text-xs font-bold transition-colors ${
                mode === 'signup'
                  ? 'bg-[var(--mornica-emerald)] text-[#04130E]'
                  : 'text-[#A5B6CA] hover:text-[#F4FAFE]'
              }`}
            >
              Create Account
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={mode === 'signin'}
              onClick={() => {
                setMode('signin');
                setError(null);
                setNotice(null);
              }}
              className={`py-2.5 rounded-lg text-xs font-bold transition-colors ${
                mode === 'signin'
                  ? 'bg-[var(--mornica-cyan)] text-[#071321]'
                  : 'text-[#A5B6CA] hover:text-[#F4FAFE]'
              }`}
            >
              Sign In
            </button>
          </div>

          {/* Google Auth CTA */}
          <button
            type="button"
            disabled={busy}
            onClick={() => void handleGoogleSignIn()}
            data-testid="auth-google-btn"
            className="w-full min-h-[46px] px-4 py-2.5 rounded-xl bg-[#F4FAFE] hover:bg-white text-[#071321] text-xs sm:text-sm font-extrabold flex items-center justify-center gap-2.5 transition-colors disabled:opacity-50"
          >
            <svg className="w-4 h-4 shrink-0" viewBox="0 0 24 24" aria-hidden="true">
              <path
                fill="#4285F4"
                d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.82-2.4 3.68v3.05h3.88c2.27-2.09 3.665-5.17 3.665-9.17Z"
              />
              <path
                fill="#34A853"
                d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.11-6.72-4.96H1.29v3.14C3.26 21.3 7.31 24 12 24Z"
              />
              <path
                fill="#FBBC05"
                d="M5.28 14.24c-.24-.72-.38-1.49-.38-2.24s.14-1.52.38-2.24V6.62H1.29C.47 8.24 0 10.06 0 12s.47 3.76 1.29 5.38l3.99-3.14Z"
              />
              <path
                fill="#EA4335"
                d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.31 0 3.26 2.7 1.29 6.62l3.99 3.14c.95-2.85 3.6-4.96 6.72-4.96Z"
              />
            </svg>
            <span>Continue with Google</span>
          </button>

          <div className="flex items-center gap-3">
            <div className="h-px flex-1 bg-[var(--mornica-slate-border)]" />
            <span className="text-[11px] font-semibold text-[#A5B6CA]">
              Or use any email provider
            </span>
            <div className="h-px flex-1 bg-[var(--mornica-slate-border)]" />
          </div>

          <form onSubmit={handleSubmit} noValidate className="space-y-3.5">
            {mode === 'signup' && (
              <div className="space-y-1">
                <label
                  htmlFor="auth-display-name"
                  className="text-xs font-bold text-[#F4FAFE]"
                >
                  Your Name
                </label>
                <div className="relative">
                  <UserPlus
                    className="w-4 h-4 text-[#A5B6CA] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none"
                    aria-hidden="true"
                  />
                  <input
                    id="auth-display-name"
                    type="text"
                    value={displayName}
                    onChange={(e) => setDisplayName(e.target.value)}
                    placeholder="e.g. Chinedu Okeke"
                    className="w-full h-11 pl-10 pr-3.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE]"
                  />
                </div>
              </div>
            )}

            <div className="space-y-1">
              <label
                htmlFor="auth-email"
                className="text-xs font-bold text-[#F4FAFE]"
              >
                Email Address (Gmail, Yahoo, Outlook, Custom)
              </label>
              <div className="relative">
                <Mail
                  className="w-4 h-4 text-[#A5B6CA] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none"
                  aria-hidden="true"
                />
                <input
                  id="auth-email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="you@domain.com"
                  className="w-full h-11 pl-10 pr-3.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE]"
                />
              </div>
            </div>

            {mode !== 'reset' && (
              <div className="space-y-1">
                <div className="flex items-center justify-between">
                  <label
                    htmlFor="auth-password"
                    className="text-xs font-bold text-[#F4FAFE]"
                  >
                    Password
                  </label>
                  {mode === 'signin' && (
                    <button
                      type="button"
                      onClick={() => {
                        setMode('reset');
                        setError(null);
                        setNotice(null);
                      }}
                      className="text-[11px] font-semibold text-[var(--mornica-cyan)] hover:underline"
                    >
                      Forgot password?
                    </button>
                  )}
                </div>
                <div className="relative">
                  <Lock
                    className="w-4 h-4 text-[#A5B6CA] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none"
                    aria-hidden="true"
                  />
                  <input
                    id="auth-password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="At least 6 characters"
                    className="w-full h-11 pl-10 pr-3.5 rounded-xl bg-[var(--mornica-slate-card)] border border-[var(--mornica-slate-border)] focus:border-[var(--mornica-emerald)] focus:outline-none text-sm text-[#F4FAFE]"
                  />
                </div>
              </div>
            )}

            {error && (
              <p
                role="alert"
                data-testid="auth-error-banner"
                className="rounded-xl bg-[#FF6479]/15 border border-[#FF6479]/50 px-3.5 py-2.5 text-xs font-semibold text-[#FF8A9A] leading-relaxed"
              >
                {error}
              </p>
            )}

            {notice && (
              <p
                role="status"
                className="rounded-xl bg-[var(--mornica-emerald)]/15 border border-[var(--mornica-emerald)]/50 px-3.5 py-2.5 text-xs font-semibold text-[var(--mornica-emerald)] leading-relaxed"
              >
                {notice}
              </p>
            )}

            <button
              type="submit"
              disabled={busy}
              data-testid="auth-submit-btn"
              className="w-full min-h-[46px] px-5 py-2.5 rounded-xl mornica-amber-gradient text-[#0B101B] text-xs sm:text-sm font-extrabold inline-flex items-center justify-center gap-2 hover:brightness-105 disabled:opacity-50"
            >
              {mode === 'reset' && <KeyRound className="w-4 h-4" aria-hidden="true" />}
              <span>
                {busy
                  ? 'Please wait...'
                  : mode === 'signup'
                    ? 'Create Moronto Music Account'
                    : mode === 'signin'
                      ? 'Sign In to Moronto Music'
                      : 'Send Password Reset Email'}
              </span>
            </button>

            {mode === 'reset' && (
              <button
                type="button"
                onClick={() => {
                  setMode('signin');
                  setError(null);
                  setNotice(null);
                }}
                className="w-full text-center text-xs font-semibold text-[#A5B6CA] hover:text-[#F4FAFE]"
              >
                Back to Sign In
              </button>
            )}
          </form>
        </section>
      </div>
    </div>
  );
}
