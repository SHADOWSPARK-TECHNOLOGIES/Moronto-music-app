package com.shadowspark.morontomornica.domain

/**
 * Trusted media host configuration and a fail-closed rights gate factory for the
 * licensed Mornica catalogue. The host allowlist is deliberately separate from any
 * single track record: a track can never grant itself a host.
 *
 * DEMO NOTE: `soundhelix.com/examples/mp3` is public, license-clear sample audio used only
 * to *prove the playback pipeline*. It is NOT a Mornica licensed track. A real catalogue
 * backend must supply verified rights, signed asset URLs and CDN ownership before any
 * production entitlement is claimed. Do not point production at this host.
 */
object LicensedCatalogConfig {
  /** Public sample-audio host used only for the demonstration catalogue (exact-host match, incl. www). */
  val DEMO_TRUSTED_AUDIO_HOSTS: Set<String> = setOf("soundhelix.com", "www.soundhelix.com")

  fun demoRights(): LicensedCatalogRights = LicensedCatalogRights(DEMO_TRUSTED_AUDIO_HOSTS)
}

/**
 * Data-adapter seam for catalogue content. A source is either a clearly-labeled demo
 * fixture set or a live/backend source. Live sources MUST be read-only and MUST NOT claim
 * rights the app cannot prove; `LicensedCatalogBackendSource` stays empty (failed-closed)
 * until an authenticated backend actually issues `rightsConfirmed` + signed asset URLs.
 */
interface CatalogSource {
  val label: String
  val isDemo: Boolean
  fun tracks(): List<LicensedCatalogTrack>
  fun byId(id: String): LicensedCatalogTrack? = tracks().firstOrNull { it.id == id }
}

/**
 * Demo fixtures: reachable, license-clear public sample audio used to prove the pipeline.
 * Every entry is visibly labeled DEMO and uses a `DEMO-*` licenseReference.
 */
class DemoCatalogSource : CatalogSource {
  override val label: String =
    "DEMO · Public sample audio (SoundHelix) — NOT a Mornica licensed track"
  override val isDemo: Boolean = true

  override fun tracks(): List<LicensedCatalogTrack> {
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
}

/**
 * Future backend-backed licensed catalogue. It intentionally returns no tracks until an
 * authenticated backend issues real agreements/signed asset URLs; `rightsConfirmed` is
 * NEVER set here. The gate therefore stays default-deny for anything this source emits.
 */
class LicensedCatalogBackendSource : CatalogSource {
  override val label: String =
    "LICENSED · Requires an authenticated backend agreement (not yet connected)"
  override val isDemo: Boolean = false
  override fun tracks(): List<LicensedCatalogTrack> = emptyList()
}

/**
 * Default catalogue surface used by the router. Defaults to demo fixtures so Loop 1-2
 * playback is provable without a real backend; swap the source when one exists.
 */
class LicensedCatalogRepository(
  private val source: CatalogSource = DemoCatalogSource(),
) {
  fun demoCatalog(): List<LicensedCatalogTrack> = source.tracks()
  fun byId(id: String): LicensedCatalogTrack? = source.byId(id)
}
