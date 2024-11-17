package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val geoCoordinates: GeoCoordinates? = null,
    val zipcode: String,
    val street: String,
    val houseNumber: String,
    val complement: String = "",
    val neighborhood: String,
    val city: String,
    val uf: String
)
