package com.sanisamoj.services.followers

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.interfaces.DatabaseRepository

class FollowerService(
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository()
) {

    suspend fun addFollower(followerId: String, followingId: String) {
        repository.addFollower(followerId, followingId)
    }

    suspend fun removeFollower(followerId: String, followingId: String) {
        repository.removeFollower(followerId, followingId)
    }

}