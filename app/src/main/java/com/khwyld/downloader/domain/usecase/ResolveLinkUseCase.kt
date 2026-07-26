package com.khwyld.downloader.domain.usecase

import com.khwyld.downloader.domain.model.ResolvedLink
import com.khwyld.downloader.domain.repository.VideoResolverRepository

class ResolveLinkUseCase(
    private val repository: VideoResolverRepository
) {
    suspend operator fun invoke(url: String): Result<ResolvedLink> = try {
        Result.success(repository.resolve(url))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
