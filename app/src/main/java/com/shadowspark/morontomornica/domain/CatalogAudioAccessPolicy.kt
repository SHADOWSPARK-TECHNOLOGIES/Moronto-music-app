package com.shadowspark.morontomornica.domain

import java.net.URI

/**
 * A fail-closed, side-effect-free authorization boundary for future catalog playback.
 * This model does NOT fetch audio, issue licenses, authorize real rights, or download files.
 * A trusted catalog service must supply verified rights and allowed media hostnames.
 */
enum class CatalogAudioOrigin {
  LICENSED_CATALOG,
  ARTIST_AUTHORIZED,
}

data class AuthorizedCatalogTrack(
  val id: String,
  val origin: CatalogAudioOrigin,
  val streamUrl: String,
  val licenseReference: String,
  val rightsConfirmed: Boolean,
  val streamAllowed: Boolean,
  val offlineAllowed: Boolean,
  val allowedTerritories: Set<String>,
  val validUntilEpochMs: Long,
)

enum class AudioAccessDecision {
  ALLOW,
  INVALID_METADATA,
  UNVERIFIED_RIGHTS,
  LICENSE_EXPIRED,
  TERRITORY_RESTRICTED,
  STREAM_NOT_AUTHORIZED,
  OFFLINE_NOT_AUTHORIZED,
  SOURCE_NOT_APPROVED,
}

/**
 * Host allowlist MUST be configured from trusted application/provider configuration,
 * not supplied by an untrusted track record. Do not accept wildcard/suffix matches.
 */
class CatalogAudioAccessPolicy(
  private val approvedHttpsHosts: Set<String>,
  private val territoryCode: String,
  private val nowEpochMs: () -> Long = System::currentTimeMillis,
) {
  fun canStream(track: AuthorizedCatalogTrack): AudioAccessDecision = commonCheck(track)

  fun canDownload(track: AuthorizedCatalogTrack): AudioAccessDecision {
    val result = commonCheck(track)
    if (result != AudioAccessDecision.ALLOW) return result
    return if (track.offlineAllowed) AudioAccessDecision.ALLOW
    else AudioAccessDecision.OFFLINE_NOT_AUTHORIZED
  }

  private fun commonCheck(track: AuthorizedCatalogTrack): AudioAccessDecision {
    if (track.id.isBlank() || track.licenseReference.isBlank() ||
      territoryCode.isBlank() || track.allowedTerritories.isEmpty()
    ) return AudioAccessDecision.INVALID_METADATA
    if (!track.rightsConfirmed) return AudioAccessDecision.UNVERIFIED_RIGHTS
    if (track.validUntilEpochMs <= nowEpochMs()) return AudioAccessDecision.LICENSE_EXPIRED
    if (territoryCode.uppercase() !in track.allowedTerritories.map { it.uppercase() }) {
      return AudioAccessDecision.TERRITORY_RESTRICTED
    }
    if (!track.streamAllowed) return AudioAccessDecision.STREAM_NOT_AUTHORIZED
    if (!isApprovedStreamUrl(track.streamUrl)) return AudioAccessDecision.SOURCE_NOT_APPROVED
    return AudioAccessDecision.ALLOW
  }

  private fun isApprovedStreamUrl(url: String): Boolean {
    val uri = try {
      URI(url)
    } catch (_: Exception) {
      return false
    }
    val host = uri.host?.lowercase() ?: return false
    if (uri.scheme?.lowercase() != "https" || uri.rawUserInfo != null ||
      uri.rawFragment != null || uri.port !in listOf(-1, 443)
    ) return false

    // No provider fallback to YouTube, its CDN, or user-supplied arbitrary URLs.
    if (host in setOf("youtube.com", "www.youtube.com", "m.youtube.com",
        "youtu.be", "googlevideo.com", "www.googlevideo.com")
    ) return false
    if (host.endsWith(".youtube.com") || host.endsWith(".googlevideo.com")) return false
    return host in approvedHttpsHosts.map { it.lowercase() }
  }
}
