package com.sanisamoj.data.models.dataclass

import java.time.LocalDateTime

data class SearchEventNearby(
    val longitude: Double,
    val latitude: Double,
    val maxDistanceMeters: Int,
    val nick: String? = null,
    val type: List<String>? = null,
    val date: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val page: Int = 1,
    val size: Int = 25
)
