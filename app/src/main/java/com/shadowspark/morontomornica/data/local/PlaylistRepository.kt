package com.shadowspark.morontomornica.data.local

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Playlist persistence seam. The JSON SharedPreferences implementation mirrors the existing
 * FileDownloadStore pattern; a Room implementation may later replace [SharedPrefsPlaylistStore]
 * behind this same interface without touching UI code.
 *
 * Playlists track track IDs only; resolution to LocalMediaTrack stays in the UI/repository layer.
 */
interface PlaylistStore {
  fun names(): List<String>
  fun create(name: String): Boolean
  fun tracks(playlistName: String): List<String>
  fun add(playlistName: String, trackId: String): Boolean
  fun removeTrack(playlistName: String, trackId: String): Boolean
  fun rename(oldName: String, newName: String): Boolean
  fun delete(playlistName: String): Boolean
  fun move(playlistName: String, fromIndex: Int, toIndex: Int): Boolean
}

class InMemoryPlaylistStore : PlaylistStore {
  private val playlists = LinkedHashMap<String, MutableList<String>>()

  override fun names(): List<String> = playlists.keys.toList()

  override fun create(name: String): Boolean {
    val trimmed = name.trim()
    if (trimmed.isBlank() || playlists.containsKey(trimmed)) return false
    playlists[trimmed] = mutableListOf()
    return true
  }

  override fun tracks(playlistName: String): List<String> =
    playlists[playlistName].orEmpty().toList()

  override fun add(playlistName: String, trackId: String): Boolean {
    if (trackId.isBlank()) return false
    val list = playlists.getOrPut(playlistName) { mutableListOf() }
    if (list.contains(trackId)) return false
    list.add(trackId)
    return true
  }

  override fun removeTrack(playlistName: String, trackId: String): Boolean {
    val list = playlists[playlistName] ?: return false
    val removed = list.remove(trackId)
    if (list.isEmpty()) playlists.remove(playlistName)
    return removed
  }

  override fun rename(oldName: String, newName: String): Boolean {
    val trimmed = newName.trim()
    if (oldName == newName || trimmed.isBlank() || playlists.containsKey(trimmed)) return false
    val list = playlists.remove(oldName) ?: return false
    playlists[trimmed] = list
    return true
  }

  override fun delete(playlistName: String): Boolean = playlists.remove(playlistName) != null

  override fun move(playlistName: String, fromIndex: Int, toIndex: Int): Boolean {
    val list = playlists[playlistName] ?: return false
    if (fromIndex !in list.indices || toIndex !in list.indices) return false
    if (fromIndex == toIndex) return false
    val item = list.removeAt(fromIndex)
    list.add(toIndex, item)
    return true
  }
}

class SharedPrefsPlaylistStore(context: Context) : PlaylistStore {
  private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  private val memory = InMemoryPlaylistStore()

  init {
    load()
  }

  override fun names(): List<String> = memory.names()

  override fun create(name: String): Boolean = memory.create(name).also { if (it) persist() }

  override fun tracks(playlistName: String): List<String> = memory.tracks(playlistName)

  override fun add(playlistName: String, trackId: String): Boolean =
    memory.add(playlistName, trackId).also { if (it) persist() }

  override fun removeTrack(playlistName: String, trackId: String): Boolean =
    memory.removeTrack(playlistName, trackId).also { if (it) persist() }

  override fun rename(oldName: String, newName: String): Boolean =
    memory.rename(oldName, newName).also { if (it) persist() }

  override fun delete(playlistName: String): Boolean =
    memory.delete(playlistName).also { if (it) persist() }

  override fun move(playlistName: String, fromIndex: Int, toIndex: Int): Boolean =
    memory.move(playlistName, fromIndex, toIndex).also { if (it) persist() }

  private fun load() {
    val raw = prefs.getString(KEY_PLAYLISTS_JSON, null) ?: return
    runCatching {
      val root = JSONObject(raw)
      val list = root.getJSONArray(KEY_PLAYLISTS)
      for (i in 0 until list.length()) {
        val entry = list.getJSONObject(i)
        val name = entry.getString(KEY_NAME)
        // Recreate the playlist itself, including empty ones, before restoring track IDs.
        // Without this, a playlist with zero tracks silently disappears on relaunch.
        if (!memory.create(name)) continue
        val ids = entry.getJSONArray(KEY_TRACK_IDS)
        for (j in 0 until ids.length()) {
          memory.add(name, ids.getString(j))
        }
      }
    }
  }

  private fun persist() {
    val root = JSONObject()
    val arr = JSONArray()
    memory.names().forEach { name ->
      val entry = JSONObject()
      entry.put(KEY_NAME, name)
      val ids = JSONArray()
      memory.tracks(name).forEach(ids::put)
      entry.put(KEY_TRACK_IDS, ids)
      arr.put(entry)
    }
    root.put(KEY_PLAYLISTS, arr)
    prefs.edit().putString(KEY_PLAYLISTS_JSON, root.toString()).apply()
  }

  private companion object {
    const val PREFS_NAME = "mornica_playlists"
    const val KEY_PLAYLISTS_JSON = "playlists_json"
    const val KEY_PLAYLISTS = "playlists"
    const val KEY_NAME = "name"
    const val KEY_TRACK_IDS = "trackIds"
  }
}
