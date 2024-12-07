package com.sanisamoj.services.followers

import com.sanisamoj.config.GlobalContextTest
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.data.models.dataclass.UserResponse
import com.sanisamoj.data.models.enums.AccountStatus
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.OperationField
import com.sanisamoj.services.user.UserRequestFactory
import com.sanisamoj.utils.eraseAllDataInMongodb
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FollowerServiceTest {
    private val repository: DatabaseRepository = GlobalContextTest.getDatabaseRepository()

    @AfterTest
    fun eraseAllUserData() {
        runBlocking { eraseAllDataInMongodb<User>(CollectionsInDb.Users) }
    }

    private suspend fun activateUser(userId: String) {
        repository.updateUser(userId, OperationField(Fields.AccountStatus, AccountStatus.Active.name))
    }

    @Test
    fun sendFollowRequestTest() = testApplication {
        val user1: UserResponse = UserRequestFactory.createUser()
        activateUser(user1.id)
        val user2: UserResponse = UserRequestFactory.createUser()
        activateUser(user2.id)

        val followerService = FollowerService(repository)
        followerService.sendFollowRequest(user1.id, user2.id)
        val pendingRequestList: List<String> = repository.getPendingSentRequests(user1.id)
        assertEquals(1, pendingRequestList.size)
        assertEquals(user2.id, pendingRequestList[0])

        val pendingList: List<String> = repository.getPendingFollowRequests(user2.id)
        assertEquals(1, pendingList.size)
        assertEquals(user1.id, pendingList[0])
    }

    @Test
    fun acceptFollowRequestTest() = testApplication {
        val user1: UserResponse = UserRequestFactory.createUser()
        activateUser(user1.id)
        val user2: UserResponse = UserRequestFactory.createUser()
        activateUser(user2.id)

        val followerService = FollowerService(repository)
        followerService.sendFollowRequest(user1.id, user2.id)

        var pendingList: List<String> = repository.getPendingFollowRequests(user2.id)
        assertEquals(1, pendingList.size)
        assertEquals(user1.id, pendingList[0])

        followerService.acceptFollowRequest(user1.id, user2.id)
        val followersList: List<String> = repository.getFollowers(user2.id)
        pendingList = repository.getPendingFollowRequests(user2.id)
        assertEquals(0, pendingList.size)
        assertEquals(user1.id, followersList[0])

        val pendingRequestList: List<String> = repository.getPendingSentRequests(user1.id)
        assertEquals(0, pendingRequestList.size)
    }

    @Test
    fun rejectFollowRequestTest() = testApplication {
        val user1: UserResponse = UserRequestFactory.createUser()
        activateUser(user1.id)
        val user2: UserResponse = UserRequestFactory.createUser()
        activateUser(user2.id)

        val followerService = FollowerService(repository)
        followerService.sendFollowRequest(user1.id, user2.id)

        followerService.rejectFollowRequest(user2.id, user1.id)
        val followersList: List<String> = repository.getFollowers(user2.id)
        val pendingList: List<String> = repository.getPendingFollowRequests(user2.id)

        assertEquals(0, pendingList.size)
        assertEquals(0, followersList.size)
    }

    @Test
    fun cancelFollowRequestTest() = testApplication {
        val user1: UserResponse = UserRequestFactory.createUser()
        activateUser(user1.id)
        val user2: UserResponse = UserRequestFactory.createUser()
        activateUser(user2.id)

        val followerService = FollowerService(repository)
        followerService.sendFollowRequest(user1.id, user2.id)

        followerService.cancelFollowRequest(user1.id, user2.id)
        val pendingList: List<String> = repository.getPendingFollowRequests(user2.id)
        val pendingRequestList: List<String> = repository.getPendingSentRequests(user1.id)

        assertEquals(0, pendingRequestList.size)
        assertEquals(0, pendingList.size)
    }

    @Test
    fun removeFollowingRequest() = testApplication {
        val user1: UserResponse = UserRequestFactory.createUser()
        activateUser(user1.id)
        val user2: UserResponse = UserRequestFactory.createUser()
        activateUser(user2.id)

        val followerService = FollowerService(repository)
        followerService.sendFollowRequest(user1.id, user2.id)
        followerService.acceptFollowRequest(user1.id, user2.id)

        var followersList: List<String> = repository.getFollowers(user2.id)
        assertEquals(1, followersList.size)

        followerService.removeFollowing(user1.id, user2.id)
        followersList = repository.getFollowers(user2.id)
        assertEquals(0, followersList.size)
    }
}