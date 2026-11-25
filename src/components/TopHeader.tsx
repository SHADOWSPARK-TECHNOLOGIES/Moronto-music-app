import { useEffect, useRef, useState } from 'react';
import {
  Info,
  MoreVertical,
  Music,
  SlidersHorizontal,
  Sparkles,
  Timer,
  X,
} from 'lucide-react';
import type { MornicaUiState } from '../domain/playbackRouter';
import {
  PRIMARY_SCREEN_LABELS,
  PrimaryScreen,
} from '../model/mornicaModels';

interface TopHeaderProps {
  uiState: MornicaUiState;
  onSelectScreen: (screen: PrimaryScreen) => void;
  onAiDiscoveryClick: () => void;
  onEqualizerClick: () => void;
  onInfoClick: () => void;
  onDismissBanner: () => void;
}

export function TopHeader({
  uiState,
  onSelectScreen,
  onAiDiscoveryClick,
  onEqualizerClick,
  onInfoClick,
  onDismissBanner,
}: TopHeaderProps) {
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!menuOpen) return;
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [menuOpen]);

  return (
    <header className="sticky top-0 z-30 w-full bg-[#0B1727]/95 backdrop-blur-md border-b border-[#24344D]">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-4 sm:gap-8">
        {/* Brand Zone */}
        <button
          type="button"
          onClick={() => onSelectScreen(PrimaryScreen.MY_MUSIC)}
          className="flex items-center gap-3 text-left whitespace-nowrap shrink-0 focus-visible:outline-2 focus-visible:outline-[#12D9A1] rounded-xl"
        >
          <div className="w-10 h-10 rounded-xl bg-[#14243A] border border-[#24344D] flex items-center justify-center shrink-0">
            <Music className="w-5 h-5 text-[#12D9A1]" aria-hidden="true" />
          </div>
          <div>
            <div className="text-lg font-extrabold tracking-tight text-[#F4FAFE] leading-tight">
              Mornica
            </div>
            <div className="text-xs font-medium text-[#A5B6CA] leading-tight">
              Your music, your way
            </div>
          </div>
        </button>

        {/* Desktop Primary Navigation */}
        <nav
          aria-label="Primary navigation"
          className="hidden md:flex items-center gap-6"
        >
          {(Object.values(PrimaryScreen) as PrimaryScreen[]).map((screen) => {
            const active =
              uiState.currentScreen === screen && !uiState.isNowPlayingExpanded;
            return (
              <button
                key={screen}
                type="button"
                onClick={() => onSelectScreen(screen)}
                className={`relative py-2 text-sm font-semibold whitespace-nowrap shrink-0 transition-colors duration-150 focus-visible:outline-2 focus-visible:outline-[#12D9A1] rounded-md px-1 ${
                  active
                    ? 'text-[#12D9A1]'
                    : 'text-[#A5B6CA] hover:text-[#F4FAFE]'
                }`}
              >
                {PRIMARY_SCREEN_LABELS[screen]}
                {active && (
                  <span
                    aria-hidden="true"
                    className="absolute left-0 right-0 -bottom-[13px] h-[2px] bg-[#12D9A1]"
                  />
                )}
              </button>
            );
          })}
        </nav>

        {/* Actions Zone */}
        <div className="flex items-center gap-1 shrink-0">
          <button
            type="button"
            onClick={onAiDiscoveryClick}
            aria-label="Explore official music videos"
            title="Explore official music videos"
            className="min-w-[44px] min-h-[44px] flex items-center justify-center rounded-xl text-[#FFBE55] hover:bg-[#14243A] transition-colors focus-visible:outline-2 focus-visible:outline-[#12D9A1]"
          >
            <Sparkles className="w-5 h-5" aria-hidden="true" />
          </button>

          <div className="relative" ref={menuRef}>
            <button
              type="button"
              onClick={() => setMenuOpen((prev) => !prev)}
              aria-label="More music controls and information"
              aria-expanded={menuOpen}
              className="min-w-[44px] min-h-[44px] flex items-center justify-center rounded-xl text-[#A5B6CA] hover:text-[#F4FAFE] hover:bg-[#14243A] transition-colors focus-visible:outline-2 focus-visible:outline-[#12D9A1]"
            >
              <MoreVertical className="w-5 h-5" aria-hidden="true" />
            </button>

            {menuOpen && (
              <div
                role="menu"
                className="absolute right-0 mt-2 w-64 rounded-2xl bg-[#14243A] border border-[#24344D] shadow-xl py-1.5 z-50"
              >
                <button
                  type="button"
                  role="menuitem"
                  onClick={() => {
                    setMenuOpen(false);
                    onEqualizerClick();
                  }}
                  className="w-full px-4 py-2.5 text-left text-sm text-[#F4FAFE] hover:bg-[#1B2F4D] flex items-center gap-3 transition-colors"
                >
                  <Timer className="w-4 h-4 text-[#A5B6CA]" aria-hidden="true" />
                  <span>Playback Timer &amp; Controls</span>
                </button>
                <button
                  type="button"
                  role="menuitem"
                  onClick={() => {
                    setMenuOpen(false);
                    onEqualizerClick();
                  }}
                  className="w-full px-4 py-2.5 text-left text-sm text-[#F4FAFE] hover:bg-[#1B2F4D] flex items-center gap-3 transition-colors"
                >
                  <SlidersHorizontal
                    className="w-4 h-4 text-[#A5B6CA]"
                    aria-hidden="true"
                  />
                  <span>Audio Equalizer &amp; Now Playing</span>
                </button>
                <button
                  type="button"
                  role="menuitem"
                  onClick={() => {
                    setMenuOpen(false);
                    onInfoClick();
                  }}
                  className="w-full px-4 py-2.5 text-left text-sm text-[#F4FAFE] hover:bg-[#1B2F4D] flex items-center gap-3 transition-colors"
                >
                  <Info className="w-4 h-4 text-[#A5B6CA]" aria-hidden="true" />
                  <span>About &amp; Rights Info</span>
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Status Banner */}
      {uiState.statusBannerMessage && (
        <div className="bg-[#0B1727] border-t border-[#24344D]/60">
          <div className="max-w-6xl mx-auto px-4 sm:px-6 py-2 flex items-center justify-between gap-3">
            <p className="text-xs font-medium text-[#FFBE55] leading-snug">
              {uiState.statusBannerMessage}
            </p>
            <button
              type="button"
              onClick={onDismissBanner}
              aria-label="Dismiss status message"
              className="p-1 text-[#A5B6CA] hover:text-[#F4FAFE] rounded-md shrink-0"
            >
              <X className="w-3.5 h-3.5" aria-hidden="true" />
            </button>
          </div>
        </div>
      )}
    </header>
  );
}
