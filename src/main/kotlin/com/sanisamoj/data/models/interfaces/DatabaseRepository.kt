package com.sanisamoj.data.models.interfaces

import com.sanisamoj.data.models.dataclass.MediaStorage
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.database.mongodb.OperationField
import io.ktor.http.content.*
import java.io.File

interface DatabaseRepository {
    suspend fun createUser(user: User): User
    suspend fun getUsersByNick(nick: String): List<User>
    suspend fun getUserById(userId: String): User
    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserByPhone(phone: String): User?
    suspend fun updateUser(userId: String, update: OperationField): User
    suspend fun deleteUser(userId: String)
    suspend fun getUsersWithPagination(pageSize: Int = 10, pageNumber: Int = 1): List<User>
    suspend fun getUsersCount(): Int

    suspend fun saveMedia(multipartData: MultiPartData, maxImagesAllowed: Int): List<MediaStorage>
    fun getMedia(name: String): File
    fun deleteMedia(file: File)

    suspend fun sendFollowRequest(followerId: String, followingId: String)
    suspend fun acceptFollowRequest(followerId: String, followingId: String)
    suspend fun rejectFollowRequest(followerId: String, followingId: String)
    suspend fun cancelFollowRequest(followerId: String, followingId: String)
    suspend fun getPendingFollowRequests(userId: String): List<String>
    suspend fun getPendingSentRequests(userId: String): List<String>
    suspend fun removeFollowing(followerId: String, followingId: String)
    suspend fun getFollowers(userId: String): List<String>
    suspend fun getFollowing(userId: String): List<String>
    suspend fun getMutualFollowers(userId: String): List<String>
}