package com.shadowspark.morontomornica.domain

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import com.shadowspark.morontomornica.model.CreatorProfile
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.model.ProvenanceStatus
import java.io.File

class LocalMediaRepository(
  private val packageName: String = "com.shadowspark.morontomornica",
) {
  fun defaultDemoAndCatalogTracks(): List<LocalMediaTrack> {
    val rawDemoUri = "android.resource://$packageName/raw/mornica_afrobeats_demo"
    return listOf(
      LocalMediaTrack(
        id = "demo-lagos-pulse",
        title = "Eko Neon Pulse (First-Party Synth Demo)",
        artist = "Teniola Blaze",
        album = "Mornica Creator Sessions",
        folderName = "Mornica Demos",
        durationMs = 30_000L,
        uri = rawDemoUri,
        provenance = ProvenanceStatus.DEMO,
        rightsConfirmed = true,
        releaseYear = 2026,
      ),
      LocalMediaTrack(
        id = "demo-lekki-logdrum",
        title = "Lekki Sunrise Log-Drum (First-Party Synth Demo)",
        artist = "Kofi & The LogDrums",
        album = "Amapiano Pilot Tapes",
        folderName = "Mornica Demos",
        durationMs = 45_000L,
        uri = rawDemoUri,
        provenance = ProvenanceStatus.DEMO,
        rightsConfirmed = true,
        releaseYear = 2025,
      ),
      LocalMediaTrack(
        id = "demo-yaba-nights",
        title = "Yaba Midnight Strings (First-Party Synth Demo)",
        artist = "Zainab Soul",
        album = "Afro-Soul Showcase",
        folderName = "Afro-Soul Previews",
        durationMs = 60_000L,
        uri = rawDemoUri,
        provenance = ProvenanceStatus.DEMO,
        rightsConfirmed = true,
        releaseYear = 2024,
      ),
      LocalMediaTrack(
        id = "unverified-archive-01",
        title = "Surulere Street Anthem (Awaiting Master Clearance)",
        artist = "Unsigned Collective",
        album = "Pending Vetting",
        folderName = "Pending Clearance",
        durationMs = 185_000L,
        uri = rawDemoUri,
        provenance = ProvenanceStatus.UNAVAILABLE,
        rightsConfirmed = false,
        releaseYear = 2023,
      ),
    )
  }

  fun queryDeviceMediaStore(contentResolver: ContentResolver): List<LocalMediaTrack> {
    val discovered = mutableListOf<LocalMediaTrack>()
    val projection =
      arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.YEAR,
      )
    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
    val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

    try {
      contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        null,
        sortOrder,
      )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
        val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)

        while (cursor.moveToNext()) {
          val id = cursor.getLong(idCol)
          val title = cursor.getString(titleCol)?.takeIf { it.isNotBlank() } ?: "Unknown Track"
          val artist = cursor.getString(artistCol)?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
          val album = cursor.getString(albumCol)?.takeIf { it.isNotBlank() } ?: "Local Audio"
          val duration = cursor.getLong(durationCol).coerceAtLeast(1_000L)
          val rawPath = if (dataCol >= 0) cursor.getString(dataCol) else null
          val rawYear = if (yearCol >= 0) cursor.getInt(yearCol) else 0
          val resolvedYear = if (rawYear in 1900..2100) rawYear else 2026
          val folder =
            rawPath?.let { File(it).parentFile?.name }?.takeIf { it.isNotBlank() }
              ?: "Device Music"
          val contentUri =
            ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()

          discovered.add(
            LocalMediaTrack(
              id = "media-$id",
              title = title,
              artist = artist,
              album = album,
              folderName = folder,
              durationMs = duration,
              uri = contentUri,
              provenance = ProvenanceStatus.LOCAL_DEVICE,
              rightsConfirmed = true,
              releaseYear = resolvedYear,
            ),
          )
        }
      }
    } catch (_: SecurityException) {
      // Permission not yet granted; caller shows honest permission prompt + bundled demos
    } catch (_: Exception) {
      // Defensive fallback on vendor MediaStore quirks
    }

    return discovered + defaultDemoAndCatalogTracks()
  }

  fun groupByFolder(tracks: List<LocalMediaTrack>): Map<String, List<LocalMediaTrack>> =
    tracks.groupBy { it.folderName }

  fun groupByArtist(tracks: List<LocalMediaTrack>): Map<String, List<LocalMediaTrack>> =
    tracks.groupBy { it.artist }

  fun groupByAlbum(tracks: List<LocalMediaTrack>): Map<String, List<LocalMediaTrack>> =
    tracks.groupBy { it.album }

  fun verifiedCreatorProfiles(): List<CreatorProfile> =
    listOf(
      CreatorProfile(
        id = "creator-teniola",
        artistName = "Teniola Blaze (Demo Profile)",
        city = "Lagos, NG",
        genre = "Afrobeats / Street-Pop",
        bio =
          "Illustrative independent creator profile using a first-party synthesized audio preview (mornica_afrobeats_demo.wav).",
        provenanceBadge = ProvenanceStatus.DEMO,
        rightsConfirmed = true,
        previewTrackIds = listOf("demo-lagos-pulse"),
        officialYouTubeChannelUrl = null,
      ),
      CreatorProfile(
        id = "creator-kofi",
        artistName = "Kofi & The LogDrums (Demo Profile)",
        city = "Accra · Johannesburg",
        genre = "Amapiano Crossover",
        bio =
          "Illustrative Amapiano showcase profile using a first-party synthesized audio preview; no third-party master is claimed.",
        provenanceBadge = ProvenanceStatus.DEMO,
        rightsConfirmed = true,
        previewTrackIds = listOf("demo-lekki-logdrum"),
        officialYouTubeChannelUrl = null,
      ),
      CreatorProfile(
        id = "creator-zainab",
        artistName = "Zainab Soul (Demo Profile)",
        city = "Abuja, NG",
        genre = "Afro-Soul / Alté",
        bio =
          "Illustrative Afro-Soul showcase profile using a first-party synthesized audio preview (mornica_afrobeats_demo.wav).",
        provenanceBadge = ProvenanceStatus.DEMO,
        rightsConfirmed = true,
        previewTrackIds = listOf("demo-yaba-nights"),
        officialYouTubeChannelUrl = null,
      ),
    )
}
