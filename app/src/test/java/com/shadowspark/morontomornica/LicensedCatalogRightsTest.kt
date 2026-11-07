package com.shadowspark.morontomornica

import com.shadowspark.morontomornica.domain.CatalogAccess
import com.shadowspark.morontomornica.domain.CatalogDenial
import com.shadowspark.morontomornica.domain.LicensedCatalogRights
import com.shadowspark.morontomornica.domain.LicensedCatalogTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LicensedCatalogRightsTest {
  private val now = 1_800_000_000_000L
  private val trustedHost = "media.mornica.example"
  private val gate = LicensedCatalogRights(setOf(trustedHost), clockMs = { now })

  // Fixture only: the .example domain is deliberately not a real music host or license.
  private fun fixture() = LicensedCatalogTrack(
    id = "demo-001",
    title = "Rights Gate Sample",
    artist = "Fixture Artist",
    mediaUrl = "https://$trustedHost/audio/demo-001.mp3?token=signed-placeholder",
    sourceLabel = "Internal fixture, not published",
    licenseReference = "TEST-ONLY-NOT-A-REAL-LICENSE",
    allowedTerritories = setOf("NG"),
    licenseExpiresAtEpochMs = now + 60_000,
    streamAllowed = true,
    offlineAllowed = true,
  )

  @Test fun permittedFixtureStreamsAndMayBeSavedForOffline() {
    val track = fixture()
    assertTrue(gate.evaluate(track, CatalogAccess.STREAM, "NG").allowed)
    assertTrue(gate.evaluate(track, CatalogAccess.SAVE_OFFLINE, "ng").allowed)
  }

  @Test fun completeDownloadedTrackPlaysOfflineButIncompleteTrackCannot() {
    val track = fixture()
    assertEquals(
      CatalogDenial.DOWNLOAD_INCOMPLETE,
      gate.evaluate(track, CatalogAccess.PLAY_DOWNLOADED, "NG").denial,
    )
    assertTrue(
      gate.evaluate(track, CatalogAccess.PLAY_DOWNLOADED, "NG", downloadCompleted = true).allowed
    )
  }

  @Test fun streamingPermissionNeverImpliesDownloadPermission() {
    val streamOnly = fixture().copy(offlineAllowed = false)
    assertTrue(gate.evaluate(streamOnly, CatalogAccess.STREAM, "NG").allowed)
    assertEquals(
      CatalogDenial.OFFLINE_NOT_AUTHORIZED,
      gate.evaluate(streamOnly, CatalogAccess.SAVE_OFFLINE, "NG").denial,
    )
    assertEquals(
      CatalogDenial.OFFLINE_NOT_AUTHORIZED,
      gate.evaluate(streamOnly, CatalogAccess.PLAY_DOWNLOADED, "NG", true).denial,
    )
  }

  @Test fun missingRightsExpiredRightsAndWrongTerritoryFailClosed() {
    assertEquals(
      CatalogDenial.MISSING_LICENSE,
      gate.evaluate(fixture().copy(licenseReference = ""), CatalogAccess.STREAM, "NG").denial,
    )
    assertEquals(
      CatalogDenial.LICENSE_EXPIRED,
      gate.evaluate(
        fixture().copy(licenseExpiresAtEpochMs = now),
        CatalogAccess.SAVE_OFFLINE,
        "NG"
      ).denial,
    )
    assertEquals(
      CatalogDenial.TERRITORY_UNAVAILABLE,
      gate.evaluate(fixture(), CatalogAccess.STREAM, "US").denial,
    )
    assertEquals(
      CatalogDenial.STREAM_NOT_AUTHORIZED,
      gate.evaluate(fixture().copy(streamAllowed = false), CatalogAccess.STREAM, "NG").denial,
    )
  }

  @Test fun youtubeAndUntrustedHostsNeverReachNativePlayerOrDownloader() {
    val banned = listOf(
      "https://www.youtube.com/watch?v=prqNqQUWXBE",
      "https://r1---sn-123.googlevideo.com/videoplayback",
      "https://youtu.be/prqNqQUWXBE",
      "https://media.mornica.example.attacker.invalid/audio.mp3",
      "http://media.mornica.example/audio.mp3",
      "https://user:pass@media.mornica.example/audio.mp3",
      "https://media.mornica.example:1234/audio.mp3",
      "javascript:alert(1)",
    )
    banned.forEach { url ->
      assertFalse("Unexpected trusted media URL: $url", gate.isTrustedHttpsMediaUrl(url))
      assertEquals(
        CatalogDenial.INVALID_SOURCE,
        gate.evaluate(fixture().copy(mediaUrl = url), CatalogAccess.SAVE_OFFLINE, "NG").denial,
      )
    }

    // Explicitly trusting a banned YouTube host still must not permit it.
    val hostileConfiguration = LicensedCatalogRights(
      setOf("www.youtube.com", "r1---sn-123.googlevideo.com")
    )
    assertFalse(hostileConfiguration.isTrustedHttpsMediaUrl(banned[0]))
    assertFalse(hostileConfiguration.isTrustedHttpsMediaUrl(banned[1]))
  }
}
