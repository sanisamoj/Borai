package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class MediaStorage(
    val filename: String,
    val filesize: Int,
    val code: String? = null,
    val createAt: String = LocalDateTime.now().toString()
)

