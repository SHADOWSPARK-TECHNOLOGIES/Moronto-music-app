import type { OfflineTrack } from '../model/musicoletModels';

const DB_NAME = 'mornica_musicolet_library_v1';
const STORE_NAME = 'device_tracks';
const DB_VERSION = 1;

interface StoredTrackRecord {
  id: string;
  title: string;
  artist: string;
  album: string;
  folderName: string;
  durationMs: number;
  releaseYear: number;
  blob: Blob;
  lyricsText?: string;
}

function openLibraryDb(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    if (typeof indexedDB === 'undefined') {
      reject(new Error('IndexedDB unavailable'));
      return;
    }
    const req = indexedDB.open(DB_NAME, DB_VERSION);
    req.onupgradeneeded = () => {
      const db = req.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: 'id' });
      }
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error ?? new Error('Failed to open IndexedDB'));
  });
}

export async function saveImportedTracksToDb(
  entries: Array<{ track: OfflineTrack; file: File }>,
): Promise<void> {
  try {
    const db = await openLibraryDb();
    await new Promise<void>((resolve, reject) => {
      const tx = db.transaction(STORE_NAME, 'readwrite');
      const store = tx.objectStore(STORE_NAME);
      for (const { track, file } of entries) {
        const record: StoredTrackRecord = {
          id: track.id,
          title: track.title,
          artist: track.artist,
          album: track.album,
          folderName: track.folderName,
          durationMs: track.durationMs,
          releaseYear: track.releaseYear,
          blob: file,
          lyricsText: track.lyricsText,
        };
        store.put(record);
      }
      tx.oncomplete = () => resolve();
      tx.onerror = () => reject(tx.error ?? new Error('IndexedDB write failed'));
    });
    db.close();
  } catch {
    // Graceful fallback: tracks remain active in current session via object URLs
  }
}

export async function loadImportedTracksFromDb(): Promise<OfflineTrack[]> {
  try {
    const db = await openLibraryDb();
    const records = await new Promise<StoredTrackRecord[]>((resolve, reject) => {
      const tx = db.transaction(STORE_NAME, 'readonly');
      const store = tx.objectStore(STORE_NAME);
      const req = store.getAll();
      req.onsuccess = () => resolve((req.result as StoredTrackRecord[]) ?? []);
      req.onerror = () => reject(req.error ?? new Error('IndexedDB read failed'));
    });
    db.close();
    return records.map((rec) => ({
      id: rec.id,
      title: rec.title,
      artist: rec.artist,
      album: rec.album,
      folderName: rec.folderName,
      durationMs: rec.durationMs,
      uri: URL.createObjectURL(rec.blob),
      releaseYear: rec.releaseYear,
      sourceType: 'DEVICE',
      lyricsText: rec.lyricsText,
    }));
  } catch {
    return [];
  }
}

export async function removeImportedTrackFromDb(trackId: string): Promise<void> {
  try {
    const db = await openLibraryDb();
    await new Promise<void>((resolve, reject) => {
      const tx = db.transaction(STORE_NAME, 'readwrite');
      tx.objectStore(STORE_NAME).delete(trackId);
      tx.oncomplete = () => resolve();
      tx.onerror = () => reject(tx.error ?? new Error('IndexedDB delete failed'));
    });
    db.close();
  } catch {
    // Ignore storage delete failures
  }
}

export function readAudioFileDurationMs(objectUrl: string): Promise<number> {
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
