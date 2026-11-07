package com.shadowspark.morontomornica

import com.shadowspark.morontomornica.domain.AudioAccessDecision
import com.shadowspark.morontomornica.domain.AuthorizedCatalogTrack
import com.shadowspark.morontomornica.domain.CatalogAudioAccessPolicy
import com.shadowspark.morontomornica.domain.CatalogAudioOrigin
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogAudioAccessPolicyTest {
  private val now = 1_800_000_000_000L
  private val policy = CatalogAudioAccessPolicy(
    approvedHttpsHosts = setOf("cdn.mornica.example"),
    territoryCode = "NG",
    nowEpochMs = { now },
  )

  private fun track() = AuthorizedCatalogTrack(
    id = "licensed-demo-001",
    origin = CatalogAudioOrigin.ARTIST_AUTHORIZED,
    streamUrl = "https://cdn.mornica.example/audio/demo.mp3",
    licenseReference = "demo-contract-001",
    rightsConfirmed = true,
    streamAllowed = true,
    offlineAllowed = true,
    allowedTerritories = setOf("NG"),
    validUntilEpochMs = now + 60_000L,
  )

  @Test fun authorizedNigerianLicensedTrackMayStreamAndSaveOffline() {
    assertEquals(AudioAccessDecision.ALLOW, policy.canStream(track()))
    assertEquals(AudioAccessDecision.ALLOW, policy.canDownload(track()))
  }

  @Test fun streamingPermissionAloneDoesNotPermitOfflineSaving() {
    val onlineOnly = track().copy(offlineAllowed = false)
    assertEquals(AudioAccessDecision.ALLOW, policy.canStream(onlineOnly))
    assertEquals(AudioAccessDecision.OFFLINE_NOT_AUTHORIZED, policy.canDownload(onlineOnly))
  }

  @Test fun missingRightsAndExpiredLicenseFailClosed() {
    assertEquals(AudioAccessDecision.UNVERIFIED_RIGHTS, policy.canStream(track().copy(rightsConfirmed = false)))
    assertEquals(AudioAccessDecision.LICENSE_EXPIRED, policy.canDownload(track().copy(validUntilEpochMs = now)))
    assertEquals(AudioAccessDecision.INVALID_METADATA, policy.canStream(track().copy(licenseReference = "")))
  }

  @Test fun territoryRestrictionIsEnforced() {
    assertEquals(AudioAccessDecision.TERRITORY_RESTRICTED, policy.canStream(track().copy(allowedTerritories = setOf("GH"))))
  }

  @Test fun nonStreamingEntitlementIsRejected() {
    assertEquals(AudioAccessDecision.STREAM_NOT_AUTHORIZED, policy.canStream(track().copy(streamAllowed = false)))
  }

  @Test fun youtubeAndUnapprovedInternetMediaCannotEnterNativePlayer() {
    val urls = listOf(
      "https://www.youtube.com/watch?v=12345678901",
      "https://r1---sn.googlevideo.com/audio",
      "https://youtu.be/12345678901",
      "https://cdn.mornica.example.evil.com/audio.mp3",
      "http://cdn.mornica.example/audio.mp3",
      "https://user:pass@cdn.mornica.example/audio.mp3",
      "https://cdn.mornica.example:8443/audio.mp3",
      "https://cdn.mornica.example/audio.mp3#secret",
      "not-a-uri",
    )
    urls.forEach { url ->
      assertEquals(url, AudioAccessDecision.SOURCE_NOT_APPROVED, policy.canStream(track().copy(streamUrl = url)))
      assertEquals(url, AudioAccessDecision.SOURCE_NOT_APPROVED, policy.canDownload(track().copy(streamUrl = url)))
    }
  }

  @Test fun noConfiguredMediaHostsMeansNoOnlinePlaybackOrDownloads() {
    val unconfigured = CatalogAudioAccessPolicy(emptySet(), "NG", nowEpochMs = { now })
    assertEquals(AudioAccessDecision.SOURCE_NOT_APPROVED, unconfigured.canStream(track()))
    assertEquals(AudioAccessDecision.SOURCE_NOT_APPROVED, unconfigured.canDownload(track()))
  }

  @Test fun doesNotAcceptExternalYouTubeAsAnAuthorizedHost() {
    val permissiveButUntrustedConfig = CatalogAudioAccessPolicy(setOf("youtube.com", "r1.googlevideo.com"), "NG", nowEpochMs = { now })
    assertEquals(AudioAccessDecision.SOURCE_NOT_APPROVED,
      permissiveButUntrustedConfig.canStream(track().copy(streamUrl = "https://youtube.com/watch?v=12345678901")))
    assertEquals(AudioAccessDecision.SOURCE_NOT_APPROVED,
      permissiveButUntrustedConfig.canStream(track().copy(streamUrl = "https://r1.googlevideo.com/media")))
  }
}
