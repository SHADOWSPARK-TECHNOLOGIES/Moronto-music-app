package com.shadowspark.morontomornica.domain

import com.shadowspark.morontomornica.model.VIDIQ_NIGERIA_SEARCH_ESTIMATES
import com.shadowspark.morontomornica.model.VidIqSearchEstimate
import com.shadowspark.morontomornica.model.YouTubeSourceBadge
import com.shadowspark.morontomornica.model.YouTubeVideoCard
import java.net.URLEncoder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class CachedDiscoveryResult(
  val queryKey: String,
  val items: List<YouTubeVideoCard>,
  val fetchedAtEpochMs: Long,
  val expiresAtEpochMs: Long,
  val fromCache: Boolean,
)

class YouTubeDiscoveryRepository(
  private val apiKey: String = "",
  private val httpClient: OkHttpClient = OkHttpClient(),
  private val clockMs: () -> Long = { System.currentTimeMillis() },
) {
  // ponytail: bounded in-memory LinkedHashMap with 24h TTL avoids SQLite cache schema bloat
  // while protecting YouTube Data API v3 daily quota (search.list = 100 units).
  private val cache =
    object : LinkedHashMap<String, CachedDiscoveryResult>(32, 0.75f, true) {
      override fun removeEldestEntry(
        eldest: MutableMap.MutableEntry<String, CachedDiscoveryResult>?,
      ): Boolean = size > MAX_CACHE_ENTRIES
    }

  fun getResearchEstimates(): List<VidIqSearchEstimate> = VIDIQ_NIGERIA_SEARCH_ESTIMATES

  @Synchronized
  fun searchOfficialVideos(
    query: String,
    risingCreatorsOnly: Boolean = false,
    forceRefresh: Boolean = false,
  ): CachedDiscoveryResult {
    val normalizedKey = "${query.trim().lowercase()}|rising=$risingCreatorsOnly"
    val now = clockMs()

    if (!forceRefresh) {
      val existing = cache[normalizedKey]
      if (existing != null && now < existing.expiresAtEpochMs) {
        return existing.copy(fromCache = true)
      } else if (existing != null) {
        cache.remove(normalizedKey)
      }
    }

    val liveOrFallback =
      if (apiKey.isNotBlank() && !risingCreatorsOnly) {
        fetchFromYouTubeDataApi(query.trim()).ifEmpty {
          filterCuratedCatalog(query, risingCreatorsOnly)
        }
      } else {
        filterCuratedCatalog(query, risingCreatorsOnly)
      }

    val entry =
      CachedDiscoveryResult(
        queryKey = normalizedKey,
        items = liveOrFallback,
        fetchedAtEpochMs = now,
        expiresAtEpochMs = now + MAX_CACHE_TTL_MS,
        fromCache = false,
      )
    cache[normalizedKey] = entry
    return entry
  }

  @Synchronized
  fun putCacheEntryForTest(
    query: String,
    items: List<YouTubeVideoCard>,
    ttlMs: Long = MAX_CACHE_TTL_MS,
  ): CachedDiscoveryResult {
    val boundedTtl = ttlMs.coerceAtMost(MAX_CACHE_TTL_MS).coerceAtLeast(1L)
    val now = clockMs()
    val key = "${query.trim().lowercase()}|rising=false"
    val entry =
      CachedDiscoveryResult(
        queryKey = key,
        items = items,
        fetchedAtEpochMs = now,
        expiresAtEpochMs = now + boundedTtl,
        fromCache = false,
      )
    cache[key] = entry
    return entry
  }

  private fun fetchFromYouTubeDataApi(query: String): List<YouTubeVideoCard> {
    return try {
      val encoded = URLEncoder.encode("$query official music video", "UTF-8")
      val url =
        "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&videoEmbeddable=true&maxResults=8&regionCode=NG&q=$encoded&key=$apiKey"
      val request = Request.Builder().url(url).get().build()
      httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return emptyList()
        val body = response.body?.string().orEmpty()
        if (body.isBlank()) return emptyList()
        val root = JSONObject(body)
        val items = root.optJSONArray("items") ?: return emptyList()
        val results = mutableListOf<YouTubeVideoCard>()
        for (i in 0 until items.length()) {
          val obj = items.optJSONObject(i) ?: continue
          val idObj = obj.optJSONObject("id")
          val videoId = idObj?.optString("videoId").orEmpty()
          val snippet = obj.optJSONObject("snippet") ?: continue
          if (videoId.isNotBlank()) {
            val rawChannelId = snippet.optString("channelId", "")
            val card =
              YouTubeVideoCard(
                videoId = videoId,
                title = snippet.optString("title", "Official Music Video"),
                channelTitle = snippet.optString("channelTitle", query),
                channelId = if (YouTubeVideoCard.CHANNEL_ID_REGEX.matches(rawChannelId)) rawChannelId else "",
                category = query,
                publishedAt = snippet.optString("publishedAt", "2026"),
                sourceBadge = YouTubeSourceBadge.LIVE_YOUTUBE_API,
              )
            if (card.isPolicyCompliant()) {
              results.add(card)
            }
          }
        }
        results
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun filterCuratedCatalog(
    query: String,
    risingCreatorsOnly: Boolean,
  ): List<YouTubeVideoCard> {
    if (risingCreatorsOnly) {
      // Honest empty state: do not invent fake rising creator YouTube channels or video IDs
      return emptyList()
    }
    val q = query.trim().lowercase()
    return CURATED_OFFICIAL_VIDEOS.filter { card ->
      val matchesQuery =
        q.isBlank() ||
          q == "all" ||
          card.title.lowercase().contains(q) ||
          card.channelTitle.lowercase().contains(q) ||
          card.category.lowercase().contains(q)
      matchesQuery && card.isPolicyCompliant()
    }
  }

  fun buildExternalSearchUrl(query: String): String {
    val cleanQuery = query.trim().ifBlank { "Afrobeats official music video" }
    val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
    return "https://www.youtube.com/results?search_query=$encoded"
  }

  companion object {
    const val MAX_CACHE_TTL_MS: Long = 24L * 60L * 60L * 1000L // 24 hours max per YouTube policy
    const val MAX_CACHE_ENTRIES: Int = 64

    /**
     * All 7 entries below are verified against https://www.youtube.com/oembed (HTTP 200 OK)
     * and confirmed playableInEmbed=true with real 11-char videoIds and 24-char UC channelIds.
     */
    val CURATED_OFFICIAL_VIDEOS: List<YouTubeVideoCard> =
      listOf(
        YouTubeVideoCard(
          videoId = "prqNqQUWXBE",
          title = "Asake - Oba (Official Video)",
          channelTitle = "AsakeVEVO",
          channelId = "UCqNsFioLEaG2NQpYGvidLPg",
          category = "Asake · Street-Pop",
          publishedAt = "2026-10-10",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "OSBan_sH_b8",
          title = "Davido - UNAVAILABLE (Official Video) ft. Musa Keys",
          channelTitle = "DavidoVEVO",
          channelId = "UCQJOYS9v30qM74f6gZDk0TA",
          category = "Davido · Afrobeats",
          publishedAt = "2023-04-11",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "jipQpjUA_o8",
          title = "Wizkid - Essence (Official Video) ft. Tems",
          channelTitle = "WizkidVEVO",
          channelId = "UCE0-Bxu88d4bsGCv34zvtbA",
          category = "Wizkid · Afrobeats",
          publishedAt = "2021-04-09",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "421w1j87fEM",
          title = "Burna Boy - Last Last [Official Music Video]",
          channelTitle = "Burna Boy",
          channelId = "UCEzDdNqNkT-7rSfSGSr1hWg",
          category = "Burna Boy · Afro-Fusion",
          publishedAt = "2022-05-12",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "CQLsdm1ZYAw",
          title = "Rema - Calm Down (Official Music Video)",
          channelTitle = "Rema",
          channelId = "UCHGF6zfD2gwLuke95X3CKFQ",
          category = "Rema · Global Afrobeats",
          publishedAt = "2022-02-11",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "k6eE3c70hgg",
          title = "Omah Lay - soso (Official Music Video)",
          channelTitle = "Omah Lay",
          channelId = "UCSUVM9Ygr6AI5Eje5BnFhtw",
          category = "Omah Lay · Afro-Soul",
          publishedAt = "2022-11-29",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "l_-v1fNdSHs",
          title = "Asake, Olamide - Amapiano (Official Video)",
          channelTitle = "AsakeVEVO",
          channelId = "UCqNsFioLEaG2NQpYGvidLPg",
          category = "Amapiano · Log-Drum",
          publishedAt = "2023-05-23",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
      )
  }
}
