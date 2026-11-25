import {
  type CreatorProfile,
  type LocalMediaTrack,
  ProvenanceStatus,
} from '../model/mornicaModels';

const DEMO_AUDIO_PATH = '/audio/mornica_afrobeats_demo.wav';

export class LocalMediaRepository {
  defaultDemoAndCatalogTracks(): LocalMediaTrack[] {
    return [
      {
        id: 'demo-lagos-pulse',
        title: 'Eko Neon Pulse (First-Party Synth Demo)',
        artist: 'Teniola Blaze',
        album: 'Mornica Creator Sessions',
        folderName: 'Mornica Demos',
        durationMs: 30_000,
        uri: DEMO_AUDIO_PATH,
        provenance: ProvenanceStatus.DEMO,
        rightsConfirmed: true,
        releaseYear: 2026,
      },
      {
        id: 'demo-lekki-logdrum',
        title: 'Lekki Sunrise Log-Drum (First-Party Synth Demo)',
        artist: 'Kofi & The LogDrums',
        album: 'Amapiano Pilot Tapes',
        folderName: 'Mornica Demos',
        durationMs: 45_000,
        uri: DEMO_AUDIO_PATH,
        provenance: ProvenanceStatus.DEMO,
        rightsConfirmed: true,
        releaseYear: 2025,
      },
      {
        id: 'demo-yaba-nights',
        title: 'Yaba Midnight Strings (First-Party Synth Demo)',
        artist: 'Zainab Soul',
        album: 'Afro-Soul Showcase',
        folderName: 'Afro-Soul Previews',
        durationMs: 60_000,
        uri: DEMO_AUDIO_PATH,
        provenance: ProvenanceStatus.DEMO,
        rightsConfirmed: true,
        releaseYear: 2024,
      },
      {
        id: 'unverified-archive-01',
        title: 'Surulere Street Anthem (Awaiting Master Clearance)',
        artist: 'Unsigned Collective',
        album: 'Pending Vetting',
        folderName: 'Pending Clearance',
        durationMs: 185_000,
        uri: DEMO_AUDIO_PATH,
        provenance: ProvenanceStatus.UNAVAILABLE,
        rightsConfirmed: false,
        releaseYear: 2023,
      },
    ];
  }

  /**
   * Scans user-selected local audio files strictly in-browser via File/Blob URLs.
   * Zero files are ever uploaded or transmitted off-device.
   */
  async scanSelectedAudioFiles(files: FileList | File[]): Promise<LocalMediaTrack[]> {
    const fileArray = Array.from(files).filter(
      (f) =>
        f.type.startsWith('audio/') ||
        /\.(mp3|wav|flac|m4a|ogg|aac|opus)$/i.test(f.name),
    );

    const parsed = await Promise.all(
      fileArray.map(async (file, index): Promise<LocalMediaTrack> => {
        const objectUrl = URL.createObjectURL(file);
        const durationMs = await readAudioDurationMs(objectUrl);
        const cleanBase = file.name.replace(/\.[^.]+$/, '').trim() || 'Unknown Track';
        const parts = cleanBase.split(' - ');
        const artist = parts.length >= 2 ? parts[0].trim() : 'Device Library';
        const title = parts.length >= 2 ? parts.slice(1).join(' - ').trim() : cleanBase;
        const relPath = (file as File & { webkitRelativePath?: string }).webkitRelativePath ?? '';
        const folderSegments = relPath.split('/').filter(Boolean);
        const folderName =
          folderSegments.length >= 2
            ? folderSegments[folderSegments.length - 2]
            : 'Device Music';

        return {
          id: `media-${file.lastModified}-${index}-${cleanBase.toLowerCase().replace(/[^a-z0-9]/g, '')}`,
          title,
          artist,
          album: 'Local Audio',
          folderName,
          durationMs: durationMs > 0 ? durationMs : 180_000,
          uri: objectUrl,
          provenance: ProvenanceStatus.LOCAL_DEVICE,
          rightsConfirmed: true,
          releaseYear: new Date(file.lastModified || Date.now()).getFullYear() || 2026,
        };
      }),
    );

    return [...parsed, ...this.defaultDemoAndCatalogTracks()];
  }

  groupByFolder(tracks: LocalMediaTrack[]): Array<[string, LocalMediaTrack[]]> {
    return groupTracksBy(tracks, (t) => t.folderName);
  }

  groupByArtist(tracks: LocalMediaTrack[]): Array<[string, LocalMediaTrack[]]> {
    return groupTracksBy(tracks, (t) => t.artist);
  }

  groupByAlbum(tracks: LocalMediaTrack[]): Array<[string, LocalMediaTrack[]]> {
    return groupTracksBy(tracks, (t) => t.album);
  }

  verifiedCreatorProfiles(): CreatorProfile[] {
    return [
      {
        id: 'creator-teniola',
        artistName: 'Teniola Blaze (Demo Profile)',
        city: 'Lagos, NG',
        genre: 'Afrobeats / Street-Pop',
        bio: 'Illustrative independent creator profile using a first-party synthesized audio preview (mornica_afrobeats_demo.wav).',
        provenanceBadge: ProvenanceStatus.DEMO,
        rightsConfirmed: true,
        previewTrackIds: ['demo-lagos-pulse'],
        officialYouTubeChannelUrl: null,
      },
      {
        id: 'creator-kofi',
        artistName: 'Kofi & The LogDrums (Demo Profile)',
        city: 'Accra · Johannesburg',
        genre: 'Amapiano Crossover',
        bio: 'Illustrative Amapiano showcase profile using a first-party synthesized audio preview; no third-party master is claimed.',
        provenanceBadge: ProvenanceStatus.DEMO,
        rightsConfirmed: true,
        previewTrackIds: ['demo-lekki-logdrum'],
        officialYouTubeChannelUrl: null,
      },
      {
        id: 'creator-zainab',
        artistName: 'Zainab Soul (Demo Profile)',
        city: 'Abuja, NG',
        genre: 'Afro-Soul / Alté',
        bio: 'Illustrative Afro-Soul showcase profile using a first-party synthesized audio preview (mornica_afrobeats_demo.wav).',
        provenanceBadge: ProvenanceStatus.DEMO,
        rightsConfirmed: true,
        previewTrackIds: ['demo-yaba-nights'],
        officialYouTubeChannelUrl: null,
      },
    ];
  }
}

function groupTracksBy(
  tracks: LocalMediaTrack[],
  keyFn: (t: LocalMediaTrack) => string,
): Array<[string, LocalMediaTrack[]]> {
  const map = new Map<string, LocalMediaTrack[]>();
  for (const track of tracks) {
    const key = keyFn(track);
    const list = map.get(key) ?? [];
    list.push(track);
    map.set(key, list);
  }
  return Array.from(map.entries());
}

function readAudioDurationMs(objectUrl: string): Promise<number> {
  return new Promise((resolve) => {
    const audio = new Audio();
    const cleanup = () => {
      audio.removeEventListener('loadedmetadata', onMeta);
      audio.removeEventListener('error', onErr);
      audio.src = '';
    };
    const onMeta = () => {
      const ms = Number.isFinite(audio.duration)
        ? Math.round(audio.duration * 1000)
        : 0;
      cleanup();
      resolve(ms);
    };
    const onErr = () => {
      cleanup();
      resolve(0);
    };
    audio.addEventListener('loadedmetadata', onMeta);
    audio.addEventListener('error', onErr);
    audio.src = objectUrl;
  });
}
