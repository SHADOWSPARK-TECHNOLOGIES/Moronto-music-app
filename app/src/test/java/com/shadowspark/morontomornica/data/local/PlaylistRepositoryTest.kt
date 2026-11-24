package com.shadowspark.morontomornica.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistRepositoryTest {

  private val store = InMemoryPlaylistStore()

  @Test
  fun add_storesTrackIdsWithoutDuplicates() {
    assertTrue(store.add("Lagos Commute", "t1"))
    assertFalse(store.add("Lagos Commute", "t1"))
    assertEquals(listOf("t1"), store.tracks("Lagos Commute"))
  }

  @Test
  fun removeTrack_deletesEmptyPlaylist() {
    store.add("Mix", "t1")
    assertTrue(store.removeTrack("Mix", "t1"))
    assertTrue(store.names().none { it == "Mix" })
  }

  @Test
  fun rename_updatesName() {
    store.add("Old", "t1")
    assertTrue(store.rename("Old", "New"))
    assertTrue(store.names().contains("New"))
    assertFalse(store.names().contains("Old"))
  }

  @Test
  fun delete_removesPlaylist() {
    store.add("Temp", "t1")
    assertTrue(store.delete("Temp"))
    assertFalse(store.names().contains("Temp"))
  }

  @Test
  fun move_reordersWithinBounds() {
    val name = "Queue"
    listOf("t1", "t2", "t3").forEach { store.add(name, it) }

    assertTrue(store.move(name, 0, 2))
    assertEquals(listOf("t2", "t3", "t1"), store.tracks(name))

    assertFalse(store.move(name, 0, 99))
  }
}
