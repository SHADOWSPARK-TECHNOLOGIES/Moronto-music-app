package com.shadowspark.morontomornica.domain

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * App-level record of an authorized catalogue download: the rights SNAPSHOT the track was
 * granted under at enqueue time. Offline playback re-checks this snapshot so a rights
 * expiry/revocation is still enforced AFTER the bytes are on disk (fail-closed).
 *
 * NOTE: implemented as a first-party JSON store, not Room (deferred contract deviation:
 * avoids adding a KSP/Room build subsystem until a release candidate needs it; Media3's own
 * DownloadIndex already persists download progress and resumes across process death).
 */
data class DownloadRecord(
  val trackId: String,
  val downloadId: String,
  val mediaUrl: String,
  val licenseReference: String,
  val rightsConfirmed: Boolean,
  val offlineAllowed: Boolean,
  val territoryGranted: String,
  val grantEpochMs: Long,
  val licenseExpiresAtEpochMs: Long?,
) {
  /** Fail-closed: offline playback only when the snapshot still entitles it NOW. */
  fun mayPlayOffline(territory: String, nowEpochMs: Long): Boolean =
    rightsConfirmed &&
      offlineAllowed &&
      territory.equals(territoryGranted, ignoreCase = true) &&
      (licenseExpiresAtEpochMs == null || nowEpochMs < licenseExpiresAtEpochMs)

  companion object {
    private const val K_ID = "trackId"
    private const val K_DL = "downloadId"
    private const val K_URL = "mediaUrl"
    private const val K_LIC = "licenseReference"
    private const val K_RC = "rightsConfirmed"
    private const val K_OFF = "offlineAllowed"
    private const val K_TERR = "territory"
    private const val K_GRANT = "grantMs"
    private const val K_EXP = "expiryMs"

    fun fromJson(obj: JSONObject): DownloadRecord =
      DownloadRecord(
        trackId = obj.getString(K_ID),
        downloadId = obj.getString(K_DL),
        mediaUrl = obj.getString(K_URL),
        licenseReference = obj.getString(K_LIC),
        rightsConfirmed = obj.getBoolean(K_RC),
        offlineAllowed = obj.getBoolean(K_OFF),
        territoryGranted = obj.getString(K_TERR),
        grantEpochMs = obj.getLong(K_GRANT),
        licenseExpiresAtEpochMs = if (obj.isNull(K_EXP)) null else obj.getLong(K_EXP),
      )

    fun fromTrack(track: LicensedCatalogTrack, territory: String, nowEpochMs: Long): DownloadRecord =
      DownloadRecord(
        trackId = track.id,
        downloadId = track.toMediaId(),
        mediaUrl = track.mediaUrl,
        licenseReference = track.licenseReference,
        rightsConfirmed = track.rightsConfirmed,
        offlineAllowed = track.offlineAllowed,
        territoryGranted = territory,
        grantEpochMs = nowEpochMs,
        licenseExpiresAtEpochMs = track.licenseExpiresAtEpochMs,
      )
  }

  fun toJson(): JSONObject =
    JSONObject()
      .put(K_ID, trackId)
      .put(K_DL, downloadId)
      .put(K_URL, mediaUrl)
      .put(K_LIC, licenseReference)
      .put(K_RC, rightsConfirmed)
      .put(K_OFF, offlineAllowed)
      .put(K_TERR, territoryGranted)
      .put(K_GRANT, grantEpochMs)
      .put(K_EXP, licenseExpiresAtEpochMs?.let { JSONObject.NULL } ?: JSONObject.NULL)
      .apply { if (licenseExpiresAtEpochMs != null) put(K_EXP, licenseExpiresAtEpochMs) }
}

/** Store interface so tests can use memory while the app uses a file-backed store. */
interface DownloadStore {
  fun save(record: DownloadRecord)
  fun remove(trackId: String)
  fun get(trackId: String): DownloadRecord?
  fun all(): List<DownloadRecord>
}

class InMemoryDownloadStore : DownloadStore {
  private val map = linkedMapOf<String, DownloadRecord>()
  override fun save(record: DownloadRecord) { map[record.trackId] = record }
  override fun remove(trackId: String) { map.remove(trackId) }
  override fun get(trackId: String): DownloadRecord? = map[trackId]
  override fun all(): List<DownloadRecord> = map.values.toList()
}

/** JSON-backed persistent store in the app's no-backup files dir. */
class FileDownloadStore(context: Context) : DownloadStore {
  private val file: File = File(context.noBackupFilesDir, "catalog_downloads.json")
  private val memory = InMemoryDownloadStore()

  init { load() }

  override fun save(record: DownloadRecord) { memory.save(record); persist() }
  override fun remove(trackId: String) { memory.remove(trackId); persist() }
  override fun get(trackId: String): DownloadRecord? = memory.get(trackId)
  override fun all(): List<DownloadRecord> = memory.all()

  private fun load() {
    if (!file.exists()) return
    try {
      val arr = JSONArray(file.readText())
      for (i in 0 until arr.length()) memory.save(DownloadRecord.fromJson(arr.getJSONObject(i)))
    } catch (_: Exception) {
      // Corrupt/unreadable store => start empty; never crash on bad cache metadata.
    }
  }

  private fun persist() {
    try {
      val arr = JSONArray()
      memory.all().forEach { arr.put(it.toJson()) }
      file.writeText(arr.toString())
    } catch (_: Exception) {
      // Best-effort persistence; do not crash playback on a storage error.
    }
  }
}

/** Critically: DOES NOT use `licenseExpiresAtEpochMs` from a piece of data we cannot prove. */
fun buildDownloadStore(context: Context): DownloadStore = FileDownloadStore(context)
