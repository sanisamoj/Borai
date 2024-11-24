package com.sanisamoj.services.followers

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.MinimalUserResponse
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.services.user.UserFactory

class FollowerService(
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository()
) {

    suspend fun sendFollowRequest(followerId: String, followingId: String) {
        val followingList: List<String> = repository.getFollowing(followerId)
        if(followingList.contains(followingId)) throw CustomException(Errors.UserIsAlreadyOnTheFollowersList)

        repository.sendFollowRequest(followerId, followingId)
    }

    suspend fun acceptFollowRequest(followerId: String, followingId: String) {
        repository.acceptFollowRequest(followerId, followingId)
    }

    suspend fun rejectFollowRequest(followerId: String, followingId: String) {
        repository.rejectFollowRequest(followingId, followerId)
    }

    suspend fun cancelFollowRequest(followerId: String, followingId: String) {
        repository.cancelFollowRequest(followerId, followingId)
    }

    suspend fun getPendingFollowRequests(userId: String): List<MinimalUserResponse> {
        val pendingFollowRequestIds: List<String> = repository.getPendingFollowRequests(userId)
        val minimalUserResponseList: List<MinimalUserResponse> = pendingFollowRequestIds.map { it ->
            val user: User = repository.getUserById(it)
            UserFactory.minimalUserResponseWithUser(user)
        }
        return minimalUserResponseList
    }

    suspend fun getPendingSentRequests(userId: String): List<MinimalUserResponse> {
        val pendingSentRequestIds: List<String> = repository.getPendingSentRequests(userId)
        val minimalUserResponseList: List<MinimalUserResponse> = pendingSentRequestIds.map { it ->
            val user: User = repository.getUserById(it)
            UserFactory.minimalUserResponseWithUser(user)
        }
        return minimalUserResponseList
    }

    suspend fun removeFollowing(followerId: String, followingId: String) {
        val followingList: List<String> = repository.getFollowing(followerId)
        if(!followingList.contains(followingId)) throw CustomException(Errors.UserIsNotPresentInTheListOfFollowing)

        repository.removeFollowing(followerId, followingId)
    }

}