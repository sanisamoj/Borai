package com.sanisamoj.services.followers

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository

class FollowerService(
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository()
) {

    suspend fun addFollower(followerId: String, followingId: String) {
        val followingList: List<String> = repository.getFollowing(followerId)
        if(followingList.contains(followingId)) throw CustomException(Errors.UserIsAlreadyOnTheFollowersList)

        repository.addFollower(followerId, followingId)
    }

    suspend fun removeFollowing(followerId: String, followingId: String) {
        val followingList: List<String> = repository.getFollowing(followerId)
        if(!followingList.contains(followingId)) throw CustomException(Errors.UserIsNotPresentInTheListOfFollowing)

        repository.removeFollowing(followerId, followingId)
    }

}