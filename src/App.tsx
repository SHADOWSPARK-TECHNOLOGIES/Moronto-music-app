import { useEffect } from 'react';
import { BottomNavBar } from './components/BottomNavBar';
import { CreatorHubScreen } from './components/CreatorHubScreen';
import { DiscoverWatchScreen } from './components/DiscoverWatchScreen';
import { MyMusicScreen } from './components/MyMusicScreen';
import {
  NowPlayingFullSheet,
  OfflineMiniPlayerBar,
} from './components/PlayerOverlays';
import { TopHeader } from './components/TopHeader';
import { mornicaRuntime, useMornicaState } from './domain/mornicaRuntime';
import { PrimaryScreen } from './model/mornicaModels';

export function App() {
  const uiState = useMornicaState();
  const { router, localRepo } = mornicaRuntime;

  const hasActiveTrack =
    uiState.activeLocalTrack !== null || uiState.activeCatalogTrack !== null;
  const showMiniPlayer = hasActiveTrack && !uiState.isNowPlayingExpanded;

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && uiState.isNowPlayingExpanded) {
        router.setNowPlayingExpanded(false);
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [router, uiState.isNowPlayingExpanded]);

  const handleSelectScreen = (screen: PrimaryScreen) => {
    router.setNowPlayingExpanded(false);
    router.selectScreen(screen);
  };

  const handleEqualizerOrPlayer = () => {
    if (hasActiveTrack) {
      router.setNowPlayingExpanded(true);
    } else {
      router.selectScreen(PrimaryScreen.MY_MUSIC);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#071321] text-[#F4FAFE]">
      <TopHeader
        uiState={uiState}
        onSelectScreen={handleSelectScreen}
        onAiDiscoveryClick={() =>
          handleSelectScreen(PrimaryScreen.DISCOVER_WATCH)
        }
        onEqualizerClick={handleEqualizerOrPlayer}
        onInfoClick={() => handleSelectScreen(PrimaryScreen.CREATOR_HUB)}
        onDismissBanner={() => router.dismissStatusBanner()}
      />

      <main
        className={`flex-1 ${
          showMiniPlayer ? 'pb-36 md:pb-24' : 'pb-20 md:pb-8'
        }`}
      >
        {uiState.currentScreen === PrimaryScreen.MY_MUSIC && (
          <MyMusicScreen
            uiState={uiState}
            router={router}
            localRepo={localRepo}
          />
        )}
        {uiState.currentScreen === PrimaryScreen.DISCOVER_WATCH && (
          <DiscoverWatchScreen uiState={uiState} router={router} />
        )}
        {uiState.currentScreen === PrimaryScreen.CREATOR_HUB && (
          <CreatorHubScreen uiState={uiState} router={router} />
        )}
      </main>

      {/* Fixed bottom chrome: Mini-Player + Mobile Navigation */}
      <div className="fixed bottom-0 left-0 right-0 z-40">
        {showMiniPlayer && (
          <OfflineMiniPlayerBar uiState={uiState} router={router} />
        )}
        <BottomNavBar
          currentScreen={uiState.currentScreen}
          isNowPlayingExpanded={uiState.isNowPlayingExpanded}
          onSelectScreen={handleSelectScreen}
        />
      </div>

      {/* Full Now Playing Sheet */}
      <NowPlayingFullSheet uiState={uiState} router={router} />
    </div>
  );
}

export default App;
