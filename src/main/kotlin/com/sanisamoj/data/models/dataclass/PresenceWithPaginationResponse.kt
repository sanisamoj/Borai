package com.sanisamoj.data.models.dataclass

import com.sanisamoj.utils.pagination.PaginationResponse
import kotlinx.serialization.Serializable

@Serializable
data class PresenceWithPaginationResponse(
    val presences: List<MinimalUserResponse>,
    val paginationResponse: PaginationResponse
)
