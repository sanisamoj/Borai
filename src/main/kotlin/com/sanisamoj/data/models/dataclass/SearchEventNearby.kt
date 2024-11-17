package com.sanisamoj.data.models.dataclass

data class SearchEventNearby(
    val longitude: Double,
    val latitude: Double,
    val maxDistanceMeters: Int,
    val page: Int = 1,
    val size: Int = 25
)
