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
  private val _unused = Unit

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
            results.add(
              YouTubeVideoCard(
                videoId = videoId,
                title = snippet.optString("title", "Official Music Video"),
                channelTitle = snippet.optString("channelTitle", query),
                channelId = snippet.optString("channelId", "yt-channel"),
                category = query,
                publishedAt = snippet.optString("publishedAt", "2026"),
                sourceBadge = YouTubeSourceBadge.LIVE_YOUTUBE_API,
              ),
            )
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
    val q = query.trim().lowercase()
    return CURATED_OFFICIAL_VIDEOS.filter { card ->
      val matchesRising = !risingCreatorsOnly || card.isRisingCreator
      val matchesQuery =
        q.isBlank() ||
          q == "all" ||
          card.title.lowercase().contains(q) ||
          card.channelTitle.lowercase().contains(q) ||
          card.category.lowercase().contains(q)
      matchesRising && matchesQuery
    }
  }

  companion object {
    const val MAX_CACHE_TTL_MS: Long = 24L * 60L * 60L * 1000L // 24 hours max per YouTube policy
    const val MAX_CACHE_ENTRIES: Int = 64

    val CURATED_OFFICIAL_VIDEOS: List<YouTubeVideoCard> =
      listOf(
        YouTubeVideoCard(
          videoId = "7f8d9Asake01",
          title = "Asake — Lonely At The Top (Official Visualizer)",
          channelTitle = "Asake Official",
          channelId = "UC_AsakeOfficial",
          category = "Asake · Street-Pop",
          publishedAt = "2025-08-14",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "4b2cDavido02",
          title = "Davido — Unavailable (Official Music Video)",
          channelTitle = "DavidoOfficial",
          channelId = "UC_DavidoOfficial",
          category = "Davido · Afrobeats",
          publishedAt = "2025-05-11",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "9k1mWizkid03",
          title = "Wizkid — Kese (Dance) Official Video",
          channelTitle = "WizkidVEVO",
          channelId = "UC_WizkidVEVO",
          category = "Wizkid · Afrobeats",
          publishedAt = "2025-11-20",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "3p7qBurna04",
          title = "Burna Boy — City Boys (Official Music Video)",
          channelTitle = "Burna Boy",
          channelId = "UC_BurnaBoy",
          category = "Burna Boy · Afro-Fusion",
          publishedAt = "2025-09-22",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "6n5vRema05",
          title = "Rema — Calm Down / Ozeba Live Session",
          channelTitle = "Rema Official",
          channelId = "UC_RemaOfficial",
          category = "Rema · Global Afrobeats",
          publishedAt = "2025-10-03",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "2w8xOmahLay06",
          title = "Omah Lay — Soso / Holy Ghost (Official Video)",
          channelTitle = "Omah Lay",
          channelId = "UC_OmahLay",
          category = "Omah Lay · Afro-Soul",
          publishedAt = "2025-07-18",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "8t4zAmapiano07",
          title = "Lagos to Pretoria Amapiano Log-Drum Sessions Vol. 4",
          channelTitle = "Amapiano Africa Official",
          channelId = "UC_AmapianoAfrica",
          category = "Amapiano · Log-Drum",
          publishedAt = "2026-01-12",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
        ),
        YouTubeVideoCard(
          videoId = "5r9yRising08",
          title = "Teniola Blaze — Eko Midnight Groove (Official Creator Showcase)",
          channelTitle = "Teniola Blaze Indie",
          channelId = "UC_TeniolaBlaze",
          category = "Rising Creator · Afrobeats",
          publishedAt = "2026-03-01",
          sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
          isRisingCreator = true,
        ),
      )
  }
}
