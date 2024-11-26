package com.sanisamoj.data.models.dataclass

import com.sanisamoj.data.models.enums.EventStatus
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class Event(
    @BsonId val id: ObjectId = ObjectId(),
    val accountId: String,
    val name: String,
    val description: String,
    val image: String,
    val otherImages: List<String>? = null,
    val address: Address,
    val date: LocalDateTime,
    val presences: Int = 0,
    val type: List<String>,
    val status: String = EventStatus.SCHEDULED.name,
    val eventVotes: List<EventVote> = listOf(),
    val score: Double = 0.0,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {

    fun getAverageScore(): Double {
        return if (eventVotes.isNotEmpty()) {
            eventVotes.map { it.rating }.average()
        } else {
            0.0
        }
    }

}
