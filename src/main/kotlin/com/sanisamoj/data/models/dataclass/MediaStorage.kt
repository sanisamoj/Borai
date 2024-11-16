package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class MediaStorage(
    val filename: String,
    val filesize: Int,
    val code: String? = null
)

