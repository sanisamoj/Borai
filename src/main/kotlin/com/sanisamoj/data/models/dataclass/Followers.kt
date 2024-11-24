package com.sanisamoj.data.models.dataclass

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Followers(
    @BsonId val id: ObjectId = ObjectId(),
    val followerIds: MutableSet<String> = mutableSetOf(),
    val followingIds: MutableSet<String> = mutableSetOf(),
    val pendingFollowRequests: MutableSet<String> = mutableSetOf(),
    val pendingSentRequests: MutableSet<String> = mutableSetOf()
)
