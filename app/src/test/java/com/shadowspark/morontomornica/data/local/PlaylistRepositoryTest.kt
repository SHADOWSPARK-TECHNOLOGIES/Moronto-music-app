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

  @Test
  fun create_rejectsBlankAndDuplicateNames() {
    assertFalse(store.create("   "))
    assertTrue(store.create("Morning Drive"))
    assertFalse(store.create("Morning Drive"))
    assertTrue(store.names().contains("Morning Drive"))
    assertTrue(store.tracks("Morning Drive").isEmpty())
  }

  @Test
  fun rename_preservesTrackIds() {
    assertTrue(store.create("Working Title"))
    assertTrue(store.add("Working Title", "t1"))
    assertTrue(store.rename("Working Title", "Lagos Drive"))
    assertEquals(listOf("t1"), store.tracks("Lagos Drive"))
    assertFalse(store.names().contains("Working Title"))
  }

  @Test
  fun rename_rejectsExistingDestination() {
    assertTrue(store.create("A"))
    assertTrue(store.create("B"))
    assertTrue(store.add("A", "t1"))
    assertTrue(store.add("B", "t2"))

    assertFalse(store.rename("A", "B"))

    assertEquals(listOf("t1"), store.tracks("A"))
    assertEquals(listOf("t2"), store.tracks("B"))
  }
}
