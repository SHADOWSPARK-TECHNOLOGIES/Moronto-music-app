import { CatalogDownloadState } from '../domain/catalogDownloadManager';
import type { MornicaUiState, PlaybackRouter } from '../domain/playbackRouter';

interface LicensedCatalogSectionProps {
  uiState: MornicaUiState;
  router: PlaybackRouter;
}

export function LicensedCatalogSection({
  uiState,
  router,
}: LicensedCatalogSectionProps) {
  const downloadedCount = uiState.catalogTracks.filter((track) =>
    router.offlinePlaybackAllowed(track),
  ).length;

  return (
    <section aria-labelledby="ready-to-play-heading" className="mb-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
        <div>
          <h2
            id="ready-to-play-heading"
            className="text-base font-bold text-[#12D9A1]"
          >
            Ready to play
          </h2>
          <p className="text-xs text-[#A5B6CA] mt-0.5">
            Stream now or save offline · DEMO tracks clearly labelled
          </p>
        </div>

        {/* Wi-Fi-only download preference */}
        <label className="inline-flex items-center gap-3 cursor-pointer select-none min-h-[44px]">
          <button
            type="button"
            role="switch"
            aria-checked={uiState.preferWifiOnlyDownloads}
            onClick={() =>
              router.setWifiOnlyDownloads(!uiState.preferWifiOnlyDownloads)
            }
            className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors duration-150 focus-visible:outline-2 focus-visible:outline-[#12D9A1] ${
              uiState.preferWifiOnlyDownloads ? 'bg-[#12D9A1]' : 'bg-[#24344D]'
            }`}
          >
            <span
              className={`inline-block h-4 w-4 transform rounded-full bg-[#071321] transition-transform duration-150 ${
                uiState.preferWifiOnlyDownloads
                  ? 'translate-x-6'
                  : 'translate-x-1'
              }`}
            />
          </button>
          <span className="flex flex-col">
            <span className="text-sm font-semibold text-[#F4FAFE] leading-tight">
              Wi-Fi downloads
            </span>
            <span className="text-xs text-[#A5B6CA] leading-tight">
              {uiState.preferWifiOnlyDownloads
                ? 'Saves your mobile data'
                : 'May use mobile data'}
            </span>
          </span>
        </label>
      </div>

      {downloadedCount > 0 && (
        <p className="text-xs font-semibold text-[#20B6EB] mb-3 font-tabular">
          {downloadedCount === 1
            ? '1 track saved · ready offline'
            : `${downloadedCount} tracks saved · ready offline`}
        </p>
      )}

      {uiState.catalogTracks.length === 0 ? (
        <p className="text-sm text-[#A5B6CA]">
          No tracks available yet — check back soon
        </p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {uiState.catalogTracks.map((track) => {
            const downloadState =
              uiState.catalogDownloadStates[track.id] ??
              CatalogDownloadState.NONE;
            const isOfflineReady = router.offlinePlaybackAllowed(track);
            const isDemo = track.sourceLabel.toUpperCase().includes('DEMO');
            const isActive = uiState.activeCatalogTrack?.id === track.id;

            return (
              <div
                key={track.id}
                className={`rounded-2xl p-4 border transition-colors ${
                  isActive
                    ? 'bg-[#1B2F4D] border-[#12D9A1]'
                    : 'bg-[#14243A] border-[#24344D]'
                }`}
              >
                <h3 className="text-base font-bold text-[#F4FAFE] leading-snug truncate">
                  {track.title}
                </h3>
                <p
                  className={`text-xs mt-1 leading-relaxed line-clamp-2 ${
                    isDemo ? 'text-[#FFBE55]' : 'text-[#A5B6CA]'
                  }`}
                >
                  {track.artist} · {track.sourceLabel}
                </p>

                <div className="flex flex-wrap items-center gap-2 mt-3">
                  <button
                    type="button"
                    onClick={() =>
                      isOfflineReady
                        ? void router.playDownloadedCatalogTrack(track)
                        : router.playCatalogTrack(track)
                    }
                    className="min-h-[40px] px-4 py-2 rounded-[10px] bg-[#12D9A1] hover:bg-[#10c390] text-[#071321] text-sm font-bold whitespace-nowrap shrink-0 transition-colors focus-visible:outline-2 focus-visible:outline-[#F4FAFE]"
                  >
                    {isOfflineReady ? 'Play' : 'Stream'}
                  </button>

                  {!track.offlineAllowed ? (
                    <span className="text-xs font-semibold text-[#A5B6CA] px-2 py-1 whitespace-nowrap">
                      Streaming only
                    </span>
                  ) : isOfflineReady ? (
                    <button
                      type="button"
                      onClick={() => void router.playDownloadedCatalogTrack(track)}
                      className="min-h-[40px] px-3 py-2 rounded-[10px] text-xs font-bold text-[#20B6EB] hover:bg-[#1B2F4D] whitespace-nowrap shrink-0 transition-colors"
                    >
                      Ready offline
                    </button>
                  ) : downloadState === CatalogDownloadState.QUEUED ||
                    downloadState === CatalogDownloadState.DOWNLOADING ? (
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-semibold text-[#FFBE55] whitespace-nowrap">
                        Downloading…
                      </span>
                      <button
                        type="button"
                        onClick={() => router.cancelCatalogDownload(track.id)}
                        className="min-h-[40px] px-3 py-2 rounded-[10px] text-xs font-semibold text-[#A5B6CA] hover:text-[#F4FAFE] hover:bg-[#1B2F4D] whitespace-nowrap shrink-0 transition-colors"
                      >
                        Cancel
                      </button>
                    </div>
                  ) : downloadState === CatalogDownloadState.FAILED ? (
                    <button
                      type="button"
                      onClick={() => router.requestCatalogDownload(track)}
                      className="min-h-[40px] px-3 py-2 rounded-[10px] text-xs font-bold text-[#FFBE55] hover:bg-[#1B2F4D] whitespace-nowrap shrink-0 transition-colors"
                    >
                      Retry
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={() => router.requestCatalogDownload(track)}
                      className="min-h-[40px] px-3 py-2 rounded-[10px] text-xs font-bold text-[#12D9A1] hover:bg-[#1B2F4D] whitespace-nowrap shrink-0 transition-colors"
                    >
                      Save offline
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
}
