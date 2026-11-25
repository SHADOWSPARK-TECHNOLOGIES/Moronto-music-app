import { useState } from 'react';
import { ExternalLink, Play } from 'lucide-react';
import type { MornicaUiState, PlaybackRouter } from '../domain/playbackRouter';
import {
  PROVENANCE_META,
  PUBLIC_PRIVACY_POLICY_URL,
} from '../model/mornicaModels';

interface CreatorHubScreenProps {
  uiState: MornicaUiState;
  router: PlaybackRouter;
}

export function CreatorHubScreen({ uiState, router }: CreatorHubScreenProps) {
  const [artistName, setArtistName] = useState('');
  const [trackTitle, setTrackTitle] = useState('');
  const [genre, setGenre] = useState('Afrobeats');
  const [contact, setContact] = useState('');
  const [rightsAttested, setRightsAttested] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const ok = router.submitArtistLaunchRequest({
      artistName,
      trackTitle,
      genre,
      contactEmailOrWhatsApp: contact,
      rightsAttested,
    });
    if (ok) {
      setArtistName('');
      setTrackTitle('');
      setContact('');
      setRightsAttested(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-5 space-y-6">
      {/* Creator Demo Showcase */}
      <section aria-labelledby="creator-showcase-heading" className="space-y-3">
        <h1
          id="creator-showcase-heading"
          className="text-base font-bold text-[#20B6EB]"
        >
          Independent Creator Demo Showcase (First-Party Synth Previews)
        </h1>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3.5">
          {uiState.creatorProfiles.map((profile) => {
            const previewTrack = uiState.localTracks.find((t) =>
              profile.previewTrackIds.includes(t.id),
            );
            return (
              <div
                key={profile.id}
                className="rounded-2xl bg-[#14243A] border border-[#24344D] p-4 flex flex-col justify-between gap-3"
              >
                <div>
                  <div className="text-[11px] font-semibold text-[#FFBE55]">
                    {PROVENANCE_META[profile.provenanceBadge].label} ·{' '}
                    {profile.city}
                  </div>
                  <h2 className="text-base font-bold text-[#F4FAFE] mt-1">
                    {profile.artistName}
                  </h2>
                  <p className="text-xs text-[#A5B6CA] mt-1 leading-relaxed">
                    {profile.bio}
                  </p>
                </div>

                {previewTrack && (
                  <button
                    type="button"
                    onClick={() => router.playLocalTrack(previewTrack)}
                    className="inline-flex items-center justify-center gap-2 min-h-[42px] px-3.5 py-2 rounded-[10px] bg-[#12D9A1] hover:bg-[#10c390] text-[#04130E] text-xs font-bold transition-colors text-left"
                  >
                    <Play className="w-3.5 h-3.5 shrink-0 fill-current" aria-hidden="true" />
                    <span className="truncate">
                      Play First-Party Demo Preview: {previewTrack.title}
                    </span>
                  </button>
                )}
              </div>
            );
          })}
        </div>
      </section>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-5 items-start">
        {/* Commercial Pilot Inquiry Form */}
        <form
          onSubmit={handleSubmit}
          className="lg:col-span-3 rounded-2xl bg-[#0B1727] border border-[#24344D] p-5 space-y-3.5"
        >
          <div>
            <h2 className="text-base font-bold text-[#12D9A1]">
              Request Artist Launch Package (Commercial Pilot)
            </h2>
            <p className="text-xs text-[#A5B6CA] mt-1">
              Monetization applies strictly to independent creator promotion &amp;
              verified profile setup — never YouTube playback.
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label
                htmlFor="launch-artist-name"
                className="block text-xs font-medium text-[#A5B6CA] mb-1"
              >
                Artist or Label Name
              </label>
              <input
                id="launch-artist-name"
                type="text"
                value={artistName}
                onChange={(e) => setArtistName(e.target.value)}
                placeholder="e.g. Lagos Sound Collective"
                className="w-full h-11 px-3.5 rounded-xl bg-[#14243A] border border-[#24344D] focus:border-[#12D9A1] focus:outline-none text-sm text-[#F4FAFE]"
              />
            </div>

            <div>
              <label
                htmlFor="launch-track-title"
                className="block text-xs font-medium text-[#A5B6CA] mb-1"
              >
                Track / EP Title
              </label>
              <input
                id="launch-track-title"
                type="text"
                value={trackTitle}
                onChange={(e) => setTrackTitle(e.target.value)}
                placeholder="e.g. Victoria Island Nights"
                className="w-full h-11 px-3.5 rounded-xl bg-[#14243A] border border-[#24344D] focus:border-[#12D9A1] focus:outline-none text-sm text-[#F4FAFE]"
              />
            </div>

            <div>
              <label
                htmlFor="launch-genre"
                className="block text-xs font-medium text-[#A5B6CA] mb-1"
              >
                Genre (Afrobeats, Amapiano, Street-Pop, Afro-Soul)
              </label>
              <input
                id="launch-genre"
                type="text"
                value={genre}
                onChange={(e) => setGenre(e.target.value)}
                className="w-full h-11 px-3.5 rounded-xl bg-[#14243A] border border-[#24344D] focus:border-[#12D9A1] focus:outline-none text-sm text-[#F4FAFE]"
              />
            </div>

            <div>
              <label
                htmlFor="launch-contact"
                className="block text-xs font-medium text-[#A5B6CA] mb-1"
              >
                Contact Email or WhatsApp
              </label>
              <input
                id="launch-contact"
                type="text"
                value={contact}
                onChange={(e) => setContact(e.target.value)}
                placeholder="artist@example.com or +234..."
                className="w-full h-11 px-3.5 rounded-xl bg-[#14243A] border border-[#24344D] focus:border-[#12D9A1] focus:outline-none text-sm text-[#F4FAFE]"
              />
            </div>
          </div>

          <label className="flex items-start gap-3 cursor-pointer pt-1">
            <input
              type="checkbox"
              checked={rightsAttested}
              onChange={(e) => setRightsAttested(e.target.checked)}
              className="mt-0.5 w-4 h-4 accent-[#12D9A1] rounded shrink-0"
            />
            <span className="text-xs text-[#F4FAFE] leading-relaxed">
              I attest that I own or control 100% of the master &amp; publishing
              rights for this preview.
            </span>
          </label>

          <button
            type="submit"
            className="min-h-[44px] px-5 py-2.5 rounded-[10px] bg-[#12D9A1] hover:bg-[#10c390] text-[#04130E] text-sm font-bold transition-colors"
          >
            Submit Pilot Launch Inquiry
          </button>

          {uiState.submittedLaunchRequests.length > 0 && (
            <div className="pt-3 border-t border-[#24344D] space-y-2">
              <div className="text-xs font-bold text-[#20B6EB]">
                Recorded Inquiries ({uiState.submittedLaunchRequests.length})
              </div>
              {uiState.submittedLaunchRequests.map((req, idx) => (
                <div
                  key={`${req.artistName}-${idx}`}
                  className="rounded-xl bg-[#14243A] border border-[#24344D] px-3 py-2 text-xs text-[#F4FAFE]"
                >
                  <span className="font-bold">{req.artistName}</span> —{' '}
                  {req.trackTitle} ({req.genre}) · {req.contactEmailOrWhatsApp}
                </div>
              ))}
            </div>
          )}
        </form>

        {/* Privacy & Sources Card */}
        <div className="lg:col-span-2 rounded-2xl bg-[#14243A] border border-[#24344D] p-5 space-y-3">
          <h2 className="text-sm font-bold text-[#FFBE55]">
            Your Music, Privacy &amp; Sources
          </h2>
          <p className="text-xs text-[#A5B6CA] leading-relaxed">
            Music on your device stays on your device. Mornica shows clearly
            labeled YouTube links that open outside the app. Mornica never plays
            or downloads YouTube streams. Offline saves will only be available
            for tracks with confirmed download rights.
          </p>
          <a
            href={PUBLIC_PRIVACY_POLICY_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 min-h-[40px] px-4 py-2 rounded-[10px] border border-[#24344D] hover:border-[#20B6EB] text-xs font-semibold text-[#20B6EB] transition-colors"
          >
            <span>Open Public Privacy Policy</span>
            <ExternalLink className="w-3.5 h-3.5" aria-hidden="true" />
          </a>
        </div>
      </div>
    </div>
  );
}
