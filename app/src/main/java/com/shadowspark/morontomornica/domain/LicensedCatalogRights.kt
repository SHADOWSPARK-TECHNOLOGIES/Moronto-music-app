package com.shadowspark.morontomornica.domain

import java.net.URI
import java.util.Locale

/**
 * Explicit rights metadata for tracks delivered by a licensed Mornica catalogue.
 *
 * Client-side flags are a defense-in-depth UI gate, NOT proof of a content license.
 * The catalogue backend must validate rights, territory, expiry, signed asset URLs and
 * offline entitlement on every stream/download request. Do not create entries until
 * rights documentation and trusted CDN ownership have been reviewed.
 */
data class LicensedCatalogTrack(
  val id: String,
  val title: String,
  val artist: String,
  val mediaUrl: String,
  val sourceLabel: String,
  val licenseReference: String,
  val allowedTerritories: Set<String>,
  val licenseExpiresAtEpochMs: Long?,
  val streamAllowed: Boolean,
  val offlineAllowed: Boolean,
)

enum class CatalogAccess {
  STREAM,
  SAVE_OFFLINE,
  PLAY_DOWNLOADED,
}

enum class CatalogDenial {
  NONE,
  INVALID_SOURCE,
  MISSING_LICENSE,
  TERRITORY_UNAVAILABLE,
  LICENSE_EXPIRED,
  STREAM_NOT_AUTHORIZED,
  OFFLINE_NOT_AUTHORIZED,
  DOWNLOAD_INCOMPLETE,
}

data class CatalogAccessDecision(
  val allowed: Boolean,
  val denial: CatalogDenial,
) {
  companion object {
    fun allow(): CatalogAccessDecision = CatalogAccessDecision(true, CatalogDenial.NONE)
    fun deny(reason: CatalogDenial): CatalogAccessDecision = CatalogAccessDecision(false, reason)
  }
}

/**
 * Evaluates access before giving a remote track to a native Media3 player or
 * queueing a licensed asset in a future DownloadManager implementation.
 *
 * This gate never downloads media, assumes licenses, or accepts arbitrary URLs.
 */
class LicensedCatalogRights(
  trustedMediaHosts: Set<String>,
  private val clockMs: () -> Long = { System.currentTimeMillis() },
) {
  private val hosts = trustedMediaHosts.map { it.lowercase(Locale.ROOT) }.toSet()

  fun evaluate(
    track: LicensedCatalogTrack,
    action: CatalogAccess,
    territory: String,
    downloadCompleted: Boolean = false,
  ): CatalogAccessDecision {
    if (!isTrustedHttpsMediaUrl(track.mediaUrl)) {
      return CatalogAccessDecision.deny(CatalogDenial.INVALID_SOURCE)
    }
    if (
      track.id.isBlank() ||
        track.licenseReference.isBlank() ||
        track.sourceLabel.isBlank() ||
        track.title.isBlank() ||
        track.artist.isBlank()
    ) {
      return CatalogAccessDecision.deny(CatalogDenial.MISSING_LICENSE)
    }

    val normalizedTerritory = territory.uppercase(Locale.ROOT)
    val permittedTerritories = track.allowedTerritories.map { it.uppercase(Locale.ROOT) }
    if (normalizedTerritory.length != 2 || normalizedTerritory !in permittedTerritories) {
      return CatalogAccessDecision.deny(CatalogDenial.TERRITORY_UNAVAILABLE)
    }

    val expiry = track.licenseExpiresAtEpochMs
    if (expiry != null && clockMs() >= expiry) {
      return CatalogAccessDecision.deny(CatalogDenial.LICENSE_EXPIRED)
    }
    if (!track.streamAllowed) {
      return CatalogAccessDecision.deny(CatalogDenial.STREAM_NOT_AUTHORIZED)
    }
    if (action != CatalogAccess.STREAM && !track.offlineAllowed) {
      return CatalogAccessDecision.deny(CatalogDenial.OFFLINE_NOT_AUTHORIZED)
    }
    if (action == CatalogAccess.PLAY_DOWNLOADED && !downloadCompleted) {
      return CatalogAccessDecision.deny(CatalogDenial.DOWNLOAD_INCOMPLETE)
    }
    return CatalogAccessDecision.allow()
  }

  fun isTrustedHttpsMediaUrl(rawUrl: String): Boolean {
    if (rawUrl.isBlank()) return false
    return try {
      val uri = URI(rawUrl)
      val scheme = uri.scheme?.lowercase(Locale.ROOT)
      val host = uri.host?.lowercase(Locale.ROOT)
      if (scheme != "https" || host == null || uri.rawUserInfo != null ||
          uri.rawFragment != null || (uri.port != -1 && uri.port != 443)) {
        false
      } else {
        val forbidden =
          host == "youtube.com" || host.endsWith(".youtube.com") ||
            host == "youtu.be" || host.endsWith(".youtu.be") ||
            host == "googlevideo.com" || host.endsWith(".googlevideo.com")
        !forbidden && host in hosts
      }
    } catch (_: Exception) {
      false
    }
  }
}
