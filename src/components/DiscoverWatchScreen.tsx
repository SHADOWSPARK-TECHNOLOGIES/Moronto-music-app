import { useEffect, useState } from 'react';
import { ExternalLink, Search } from 'lucide-react';
import type { MornicaUiState, PlaybackRouter } from '../domain/playbackRouter';
import { buildOfficialWatchUrl } from '../model/mornicaModels';

interface DiscoverWatchScreenProps {
  uiState: MornicaUiState;
  router: PlaybackRouter;
}

export function DiscoverWatchScreen({
  uiState,
  router,
}: DiscoverWatchScreenProps) {
  const [searchText, setSearchText] = useState(uiState.searchQuery);

  useEffect(() => {
    setSearchText(uiState.searchQuery);
  }, [uiState.searchQuery]);

  const activeEstimate = uiState.researchEstimates.find(
    (e) =>
      e.query.toLowerCase() === uiState.searchQuery.toLowerCase() &&
      !uiState.risingCreatorsOnly,
  );

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    void router.updateSearchQuery(searchText, uiState.risingCreatorsOnly);
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-5 space-y-5">
      {/* Transparency Card */}
      <div className="rounded-2xl bg-[#14243A] border border-[#24344D] p-4">
        <h1 className="text-base font-bold text-[#12D9A1]">
          Explore Naija sounds · Videos open on YouTube
        </h1>
        <p className="text-xs sm:text-sm text-[#A5B6CA] mt-1 leading-relaxed">
          Explore official music videos from YouTube. Mornica never hides or
          downloads these videos. Your own music plays in My Music, even without
          data.
        </p>
      </div>

      {/* Search Form */}
      <form onSubmit={handleSubmit} className="flex items-center gap-2.5">
        <div className="relative flex-1">
          <Search
            className="w-4 h-4 text-[#A5B6CA] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none"
            aria-hidden="true"
          />
          <input
            type="search"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            placeholder="Find an artist or music video"
            aria-label="Find an artist or music video"
            className="w-full h-[50px] pl-10 pr-4 rounded-[14px] bg-[#14243A] border border-[#24344D] focus:border-[#12D9A1] focus:outline-none text-sm text-[#F4FAFE] placeholder:text-[#A5B6CA]"
          />
        </div>
        <button
          type="submit"
          className="h-[50px] px-5 rounded-[12px] bg-[#12D9A1] hover:bg-[#10c390] text-[#04130E] text-sm font-bold whitespace-nowrap shrink-0 transition-colors"
        >
          Search
        </button>
      </form>

      {/* Filter Controls */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        <button
          type="button"
          onClick={() => void router.updateSearchQuery('All', false)}
          className={`min-h-[38px] px-3.5 py-1.5 rounded-[10px] text-xs font-bold whitespace-nowrap shrink-0 transition-colors ${
            uiState.searchQuery.toLowerCase() === 'all' &&
            !uiState.risingCreatorsOnly
              ? 'bg-[#12D9A1] text-[#04130E]'
              : 'bg-[#14243A] border border-[#24344D] text-[#A5B6CA] hover:text-[#F4FAFE]'
          }`}
        >
          All videos
        </button>

        {uiState.researchEstimates.map((est) => {
          const selected =
            uiState.searchQuery.toLowerCase() === est.query.toLowerCase() &&
            !uiState.risingCreatorsOnly;
          return (
            <button
              key={est.query}
              type="button"
              onClick={() => void router.updateSearchQuery(est.query, false)}
              className={`min-h-[38px] px-3.5 py-1.5 rounded-[10px] text-xs font-bold whitespace-nowrap shrink-0 transition-colors ${
                selected
                  ? 'bg-[#12D9A1] text-[#04130E]'
                  : 'bg-[#14243A] border border-[#24344D] text-[#A5B6CA] hover:text-[#F4FAFE]'
              }`}
            >
              {est.query}
            </button>
          );
        })}

        <button
          type="button"
          onClick={() =>
            void router.updateSearchQuery('All', !uiState.risingCreatorsOnly)
          }
          className={`min-h-[38px] px-3.5 py-1.5 rounded-[10px] text-xs font-bold whitespace-nowrap shrink-0 transition-colors ${
            uiState.risingCreatorsOnly
              ? 'bg-[#12D9A1] text-[#04130E]'
              : 'bg-[#14243A] border border-[#24344D] text-[#A5B6CA] hover:text-[#F4FAFE]'
          }`}
        >
          Rising Creators
        </button>
      </div>

      {/* Directional Nigerian search context */}
      {activeEstimate && (
        <p className="text-xs text-[#A5B6CA] font-tabular">
          {activeEstimate.category} ·{' '}
          {activeEstimate.monthlyNigerianSearches.toLocaleString()} est. monthly
          Nigerian searches ({activeEstimate.framingNote})
        </p>
      )}

      {/* Results or Honest Empty State */}
      {uiState.youtubeVideos.length === 0 ? (
        <div className="rounded-2xl bg-[#14243A] border border-[#24344D] p-5">
          <h2 className="text-sm font-bold text-[#FFBE55]">No videos found</h2>
          <p className="text-xs text-[#A5B6CA] mt-1">
            Open YouTube to look for &lsquo;
            {uiState.searchQuery.trim() || 'Afrobeats'}&rsquo;. Videos play
            outside Mornica.
          </p>
          <a
            href={uiState.externalYouTubeSearchUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 mt-3 min-h-[40px] px-4 py-2 rounded-[10px] bg-[#12D9A1] hover:bg-[#10c390] text-[#04130E] text-sm font-bold transition-colors"
          >
            <span>Search on YouTube</span>
            <ExternalLink className="w-4 h-4" aria-hidden="true" />
          </a>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
          {uiState.youtubeVideos.map((video) => (
            <article
              key={video.videoId}
              className="rounded-2xl bg-[#14243A] border border-[#24344D] p-4 flex flex-col justify-between gap-3"
            >
              <div>
                <div className="text-[11px] font-semibold text-[#20B6EB]">
                  {video.sourceBadge}
                </div>
                <h2 className="text-base font-bold text-[#F4FAFE] mt-1 leading-snug">
                  {video.title}
                </h2>
                <p className="text-xs text-[#A5B6CA] mt-1">
                  {video.channelTitle} · {video.category} · Opens in YouTube
                </p>
              </div>

              <div>
                <a
                  href={buildOfficialWatchUrl(video.videoId)}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 min-h-[40px] px-4 py-2 rounded-[10px] border border-[#24344D] hover:border-[#20B6EB] text-sm font-semibold text-[#20B6EB] transition-colors"
                >
                  <span>Watch on YouTube</span>
                  <ExternalLink className="w-4 h-4" aria-hidden="true" />
                </a>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
