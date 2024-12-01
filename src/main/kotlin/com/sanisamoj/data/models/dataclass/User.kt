package com.sanisamoj.data.models.dataclass

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class User(
    @BsonId val id: ObjectId = ObjectId(),
    val nick: String,
    val bio: String? = null,
    val username: String,
    val imageProfile: String,
    val email: String,
    val phone: String,
    val password: String,
    val type: String,
    val address: Address? = null,
    val accountStatus: String,
    val public: Boolean = true,
    val mediaStorage: List<MediaStorage> = listOf(),
    val validationCode: Int? = null,
    val insignias: List<Insignia>? = null,
    val visibleInsignias: List<Insignia>? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)