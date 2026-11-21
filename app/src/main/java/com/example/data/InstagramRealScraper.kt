package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object InstagramRealScraper {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    /**
     * Честно загружает публичные посты Instagram-профиля через Picuki без скрытой подмены фейковыми фото.
     * В случае ошибки сети или блокировки возвращает Result.failure с реальной причиной.
     */
    suspend fun fetchPostsResult(username: String): Result<List<InstagramPost>> {
        val targetUsername = username.trim().lowercase().removePrefix("@")
        if (targetUsername.isEmpty()) {
            return Result.failure(IllegalArgumentException("Укажите имя профиля Instagram (например, @studio_name)."))
        }

        return withContext(Dispatchers.IO) {
            val url = "https://www.picuki.com/profile/$targetUsername"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9,ru;q=0.8")
                .header("Cache-Control", "no-cache")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            IllegalStateException("Сервер вернул HTTP ${response.code} для @$targetUsername. Используйте импорт из галереи устройства.")
                        )
                    }
                    val html = response.body?.string().orEmpty()
                    val posts = parseHtml(html, targetUsername)
                    if (posts.isEmpty()) {
                        return@withContext Result.failure(
                            IllegalStateException("Профиль @$targetUsername недоступен, закрыт или изменил HTML-разметку. Импортируйте реальные фото работ из галереи телефона.")
                        )
                    }
                    Result.success(posts.sortedByDescending { it.likesCount }.take(20))
                }
            } catch (e: Exception) {
                android.util.Log.e("InstagramRealScraper", "Network error while fetching @$targetUsername", e)
                Result.failure(
                    Exception("Ошибка сети при загрузке @$targetUsername: ${e.localizedMessage ?: "нет ответа"}. Попробуйте импорт фото из галереи.")
                )
            }
        }
    }

    private fun parseHtml(html: String, username: String): List<InstagramPost> {
        val list = mutableListOf<InstagramPost>()
        try {
            val postBoxRegex = """<div[^>]*class="[^"]*post-box[^"]*"[^>]*>([\s\S]*?)</div>\s*</div>\s*</div>""".toRegex()
            val postMatches = postBoxRegex.findAll(html).toList()

            var idCounter = 3000
            for (postMatch in postMatches) {
                val block = postMatch.value
                val imgRegex = """<img[^>]*class="[^"]*post-image[^"]*"[^>]*src="([^"]+)"[^>]*alt="([^"]*)"""".toRegex()
                val imgMatch = imgRegex.find(block)
                val imageUrl = imgMatch?.groupValues?.get(1) ?: ""
                val rawCaption = imgMatch?.groupValues?.get(2) ?: ""
                val caption = rawCaption.trim()

                val linkRegex = """href="[^"]*/media/([0-9_]+)"""".toRegex()
                val linkMatch = linkRegex.find(block)
                val mediaId = linkMatch?.groupValues?.get(1) ?: ""

                val likesRegex = """<i[^>]*class="[^"]*fa-heart[^"]*"[^>]*></i>\s*([0-9,]+)""".toRegex()
                val likesMatch = likesRegex.find(block)
                val likesStr = likesMatch?.groupValues?.get(1)?.replace(",", "") ?: ""
                val likesCount = likesStr.toIntOrNull() ?: 0

                val originalUrl = if (mediaId.isNotEmpty()) {
                    "https://www.instagram.com/p/${getShortcodeFromMediaId(mediaId)}/"
                } else {
                    null
                }

                if (imageUrl.isNotEmpty()) {
                    list.add(
                        InstagramPost(
                            id = idCounter++,
                            imageUrl = imageUrl,
                            caption = caption,
                            originalUrl = originalUrl,
                            dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()),
                            likesCount = likesCount,
                            username = username
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("InstagramRealScraper", "HTML parse error", e)
        }
        return list
    }

    private fun getShortcodeFromMediaId(mediaId: String): String {
        return try {
            val cleanId = mediaId.substringBefore("_")
            var id = cleanId.toLong()
            val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
            var shortcode = ""
            while (id > 0) {
                val remainder = (id % 64).toInt()
                shortcode = alphabet[remainder] + shortcode
                id /= 64
            }
            shortcode
        } catch (_: Exception) {
            ""
        }
    }
}
