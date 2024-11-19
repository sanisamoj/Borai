package com.sanisamoj.data.models.interfaces

import com.sanisamoj.data.models.dataclass.MediaStorage
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.database.mongodb.OperationField
import io.ktor.http.content.*
import java.io.File

interface DatabaseRepository {
    suspend fun createUser(user: User): User
    suspend fun getUserByNick(nick: String): User?
    suspend fun getUserById(userId: String): User
    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserByPhone(phone: String): User?
    suspend fun updateUser(userId: String, update: OperationField): User
    suspend fun deleteUser(userId: String)

    suspend fun saveMedia(multipartData: MultiPartData, maxImagesAllowed: Int): List<MediaStorage>
    fun getMedia(name: String): File
    fun deleteMedia(file: File)

    suspend fun addFollower(followerId: String, followingId: String)
    suspend fun removeFollowing(followerId: String, followingId: String)
    suspend fun getFollowers(userId: String): List<String>
    suspend fun getFollowing(userId: String): List<String>
}