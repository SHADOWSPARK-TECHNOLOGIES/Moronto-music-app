package com.shadowspark.morontomornica.domain

/**
 * Trusted media host configuration and a fail-closed rights gate factory for the
 * licensed Mornica catalogue. The host allowlist is deliberately separate from any
 * single track record: a track can never grant itself a host.
 *
 * DEMO NOTE: `soundhelix.com/examples/mp3` is public, license-clear sample audio used
 * only to *prove the playback pipeline*. It is NOT a Mornica licensed track. A real
 * catalogue backend must supply verified rights, signed asset URLs and CDN ownership
 * before any production entitlement is claimed. Do not point production at this host.
 */
object LicensedCatalogConfig {
  /** Public sample-audio host used only for the demonstration catalogue (exact-host match, incl. www). */
  val DEMO_TRUSTED_AUDIO_HOSTS: Set<String> = setOf("soundhelix.com", "www.soundhelix.com")

  fun demoRights(): LicensedCatalogRights = LicensedCatalogRights(DEMO_TRUSTED_AUDIO_HOSTS)
}

/**
 * Source of the (demo) licensed catalogue. Each entry carries explicit rights metadata
 * that the gate rejects or accepts; nothing here is proof of a real content license.
 *
 * Kept as in-memory fixtures so Loop 2 can prove end-to-end gate -> Media3 wiring and
 * real network playback against a reachable audio source. Replace with an authenticated
 * backend-backed catalogue before shipping.
 */
class LicensedCatalogRepository {
  fun demoCatalog(): List<LicensedCatalogTrack> {
    val now = System.currentTimeMillis()
    val demoExpiry = now + 120L * 24L * 3600L * 1000L
    return listOf(
      LicensedCatalogTrack(
        id = "catalog-demo-afrobeat-01",
        title = "Demo Afrobeats Instrumental",
        artist = "SoundHelix (Sample)",
        mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        sourceLabel = "DEMO · Public sample audio (SoundHelix)",
        licenseReference = "DEMO-CATALOG-AFROBEAT-01",
        allowedTerritories = setOf("NG", "GH"),
        licenseExpiresAtEpochMs = demoExpiry,
        rightsConfirmed = true,
        streamAllowed = true,
        offlineAllowed = true,
      ),
      LicensedCatalogTrack(
        id = "catalog-demo-stream-only-02",
        title = "Demo Streaming-Only Track",
        artist = "SoundHelix (Sample)",
        mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        sourceLabel = "DEMO · Public sample audio (SoundHelix) · offline NOT authorized",
        licenseReference = "DEMO-CATALOG-STREAM-02",
        allowedTerritories = setOf("NG"),
        licenseExpiresAtEpochMs = demoExpiry,
        rightsConfirmed = true,
        streamAllowed = true,
        offlineAllowed = false,
      ),
    )
  }

  fun byId(id: String): LicensedCatalogTrack? = demoCatalog().firstOrNull { it.id == id }
}
