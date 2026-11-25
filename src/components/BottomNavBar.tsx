import { Award, Music, Tv } from 'lucide-react';
import {
  PRIMARY_SCREEN_LABELS,
  PrimaryScreen,
} from '../model/mornicaModels';

interface BottomNavBarProps {
  currentScreen: PrimaryScreen;
  isNowPlayingExpanded: boolean;
  onSelectScreen: (screen: PrimaryScreen) => void;
}

function ScreenIcon({ screen }: { screen: PrimaryScreen }) {
  switch (screen) {
    case PrimaryScreen.MY_MUSIC:
      return <Music className="w-5 h-5" aria-hidden="true" />;
    case PrimaryScreen.DISCOVER_WATCH:
      return <Tv className="w-5 h-5" aria-hidden="true" />;
    case PrimaryScreen.CREATOR_HUB:
      return <Award className="w-5 h-5" aria-hidden="true" />;
  }
}

export function BottomNavBar({
  currentScreen,
  isNowPlayingExpanded,
  onSelectScreen,
}: BottomNavBarProps) {
  return (
    <nav
      aria-label="Mobile bottom navigation"
      className="md:hidden bg-[#0B1727] border-t border-[#24344D]"
    >
      <div className="grid grid-cols-3 h-16">
        {(Object.values(PrimaryScreen) as PrimaryScreen[]).map((screen) => {
          const selected = currentScreen === screen && !isNowPlayingExpanded;
          return (
            <button
              key={screen}
              type="button"
              onClick={() => onSelectScreen(screen)}
              className={`flex flex-col items-center justify-center gap-1 text-[11px] transition-colors ${
                selected
                  ? 'text-[#12D9A1] font-bold'
                  : 'text-[#A5B6CA] font-medium hover:text-[#F4FAFE]'
              }`}
            >
              <div
                className={`px-4 py-1 rounded-full transition-colors ${
                  selected ? 'bg-[#062E22]' : ''
                }`}
              >
                <ScreenIcon screen={screen} />
              </div>
              <span className="whitespace-nowrap">
                {PRIMARY_SCREEN_LABELS[screen]}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
}
