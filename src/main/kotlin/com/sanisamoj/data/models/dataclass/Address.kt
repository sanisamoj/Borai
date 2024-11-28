package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val geoCoordinates: GeoCoordinates? = null,
    val zipcode: String? = null,
    val street: String? = null,
    val houseNumber: String? = null,
    val complement: String? = null,
    val neighborhood: String,
    val city: String,
    val uf: String
)
