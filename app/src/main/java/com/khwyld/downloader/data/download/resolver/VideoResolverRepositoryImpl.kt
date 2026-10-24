package com.khwyld.downloader.data.download.resolver

import com.khwyld.downloader.domain.model.ResolvedLink
import com.khwyld.downloader.domain.model.VideoStreamOption
import com.khwyld.downloader.domain.repository.VideoResolverRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import java.net.URI
import java.util.Locale

/**
 * Resolves supported YouTube page URLs into downloadable streams.
 *
 * This adapter deliberately handles YouTube only. Other sites must be added
 * as explicit platform adapters rather than assuming every web page is a file.
 * The current model exposes progressive streams (audio + video in one file);
 * separate audio/video streams require a muxing pipeline and are not offered yet.
 */
class VideoResolverRepositoryImpl : VideoResolverRepository {

    override suspend fun resolve(url: String): ResolvedLink = withContext(Dispatchers.IO) {
        if (!isYouTubeVideoPage(url)) {
            return@withContext ResolvedLink.DirectFile(url)
        }

        try {
            val extractor = ServiceList.YouTube.getStreamExtractor(url)
            extractor.fetchPage()

            val title = extractor.name?.trim()?.takeIf { it.isNotEmpty() } ?: "video"

            // DownloadManager can download a progressive stream as a single file.
            // Separate video-only/audio-only streams must not be presented as complete videos.
            val streams = extractor.videoStreams
                .mapNotNull { stream ->
                    val streamUrl = stream.content?.takeIf { it.startsWith("https://") }
                        ?: return@mapNotNull null
                    val suffix = stream.format?.suffix
                        ?.lowercase(Locale.ROOT)
                        ?.takeIf { it.matches(Regex("[a-z0-9]{2,5}")) }
                        ?: "mp4"
                    val resolution = stream.resolution
                    VideoStreamOption(
                        label = "${resolution ?: "جودة غير معروفة"} · $suffix",
                        url = streamUrl,
                        format = suffix,
                        resolution = resolution
                    )
                }
                .distinctBy { it.url }
                .sortedByDescending { resolutionHeight(it.resolution) }

            if (streams.isEmpty()) {
                throw IllegalStateException(
                    "لم يعثر المستخرج على صيغة فيديو مدمجة بالصوت. قد يحتاج هذا الفيديو إلى دمج مساري الصوت والصورة، وهي ميزة لم تُفعّل بعد."
                )
            }

            ResolvedLink.VideoPage(
                title = title,
                thumbnailUrl = null,
                streams = streams
            )
        } catch (e: Exception) {
            throw IllegalStateException(
                "تعذّر استخراج فيديو YouTube. قد يكون الرابط غير متاح أو يتطلب تحديثًا آخر للمستخرج. التفاصيل: ${e.message ?: e.javaClass.simpleName}",
                e
            )
        }
    }

    private fun isYouTubeVideoPage(rawUrl: String): Boolean {
        val uri = try {
            URI(rawUrl)
        } catch (_: Exception) {
            return false
        }

        val host = uri.host?.lowercase(Locale.ROOT)?.removePrefix("www.") ?: return false
        val path = uri.path.orEmpty()
        val isYouTubeHost = host == "youtu.be" ||
            host == "youtube.com" ||
            host == "m.youtube.com" ||
            host == "music.youtube.com" ||
            host == "youtube-nocookie.com"

        if (!isYouTubeHost) return false

        return when {
            host == "youtu.be" -> path.trim('/').isNotEmpty()
            path == "/watch" -> uri.rawQuery.orEmpty().split("&")
                .any { it.substringBefore("=") == "v" && it.substringAfter("=", "").isNotBlank() }
            path.startsWith("/shorts/") -> path.removePrefix("/shorts/").isNotBlank()
            path.startsWith("/live/") -> path.removePrefix("/live/").isNotBlank()
            path.startsWith("/embed/") -> path.removePrefix("/embed/").isNotBlank()
            path.startsWith("/v/") -> path.removePrefix("/v/").isNotBlank()
            else -> false
        }
    }

    private fun resolutionHeight(resolution: String?): Int =
        Regex("(\\d+)").find(resolution.orEmpty())?.groupValues?.get(1)?.toIntOrNull() ?: 0
}
