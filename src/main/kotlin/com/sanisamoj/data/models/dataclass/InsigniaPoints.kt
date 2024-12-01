package com.sanisamoj.data.models.dataclass

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class InsigniaPoints(
    @BsonId val userId: ObjectId,
    val comments: Double? = null, // Number of comments
    val upsComments: Double? = null, // Number of Ups in the comments
    val answerComments: Double? = null, // Number of responses in comments
    val events: Double? = null, // Number of events created
    val presencesEvents: Double? = null, // Number of attendances at created events
    val commentsEvents: Double? = null, // Number of comments on events,
    val ratingEvents: Double? = null, // Number of rate on events
    val presencesFromTheUser: Double? = null, // Number of user presences at events
    val invitationsReceived: Double? = null, // Number of invitations made
    val invitationsSent: Double? = null // Number of invitations received
)
