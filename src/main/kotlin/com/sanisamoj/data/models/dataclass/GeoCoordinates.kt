package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class GeoCoordinates(
    val type: String = "Point",
    val coordinates: List<Double> // [longitude, latitude]
)