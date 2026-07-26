package com.khwyld.downloader.domain.repository

import com.khwyld.downloader.domain.model.ResolvedLink

/**
 * عقد VideoResolver الموصوف في الوثيقة كوحدة مستقلة داخل LinkResolver.
 * التنفيذ (data layer) يقرر إذا كان الرابط ملفًا مباشرًا أو صفحة فيديو
 * تحتاج استخراج (MediaExtractor) قبل التنزيل الفعلي.
 */
interface VideoResolverRepository {
    suspend fun resolve(url: String): ResolvedLink
}
