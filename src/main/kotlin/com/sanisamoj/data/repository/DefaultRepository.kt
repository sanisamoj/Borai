package com.sanisamoj.data.repository

import com.sanisamoj.config.GlobalContext.MIME_TYPE_ALLOWED
import com.sanisamoj.config.GlobalContext.PUBLIC_IMAGES_DIR
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.Followers
import com.sanisamoj.data.models.dataclass.MediaStorage
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.Infos
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.MongodbOperations
import com.sanisamoj.database.mongodb.MongodbOperationsWithQuery
import com.sanisamoj.database.mongodb.OperationField
import com.sanisamoj.utils.generators.CharactersGenerator
import io.ktor.http.content.*
import org.bson.Document
import org.bson.types.ObjectId
import java.io.File

class DefaultRepository: DatabaseRepository {

    override suspend fun createUser(user: User): User {
        val userId: String = MongodbOperations().register(CollectionsInDb.Users, user).toString()
        val followers = Followers(id = ObjectId(userId))
        MongodbOperations().register(CollectionsInDb.Followers, followers)
        return getUserById(userId)
    }

    override suspend fun getUsersByNick(nick: String): List<User> {
        // Build a regex query to find similar nicknames (case insensitive)
        val query = Document("nick", Document("\$regex", nick).append("\$options", "i"))

        return MongodbOperationsWithQuery().findAllByFilterWithQuery<User>(
            collectionName = CollectionsInDb.Users,
            query = query
        )
    }

    override suspend fun getUserById(userId: String): User {
        return MongodbOperations().findOne<User>(CollectionsInDb.Users, OperationField(Fields.Id, ObjectId(userId)))
            ?: throw CustomException(Errors.UserNotFound)
    }

    override suspend fun getUserByEmail(email: String): User? {
        return MongodbOperations().findOne<User>(CollectionsInDb.Users, OperationField(Fields.Email, email))
    }

    override suspend fun getUserByPhone(phone: String): User? {
        return MongodbOperations().findOne<User>(CollectionsInDb.Users, OperationField(Fields.Phone, phone))
    }

    override suspend fun updateUser(userId: String, update: OperationField): User {
        val user: User = MongodbOperations().updateAndReturnItem<User>(
            collectionName = CollectionsInDb.Users,
            filter = OperationField(Fields.Id, ObjectId(userId)),
            update = update
        ) ?: throw CustomException(Errors.UserNotFound)

        return user
    }

    override suspend fun deleteUser(userId: String) {
        MongodbOperations().deleteItem<User>(CollectionsInDb.Users, OperationField(Fields.Id, ObjectId(userId)))
    }

    override suspend fun getUsersWithPagination(pageSize: Int, pageNumber: Int): List<User> {
        return MongodbOperations().findAllWithPaging<User>(CollectionsInDb.Users, pageSize, pageNumber)
    }

    override suspend fun getUsersCount(): Int {
        return MongodbOperations().countDocumentsWithoutFilter<User>(CollectionsInDb.Users)
    }

    override suspend fun saveMedia(multipartData: MultiPartData, maxImagesAllowed: Int): List<MediaStorage> {
        val pathToPublicImages = PUBLIC_IMAGES_DIR
        val mediaStorageList: List<MediaStorage> = saveAndReturnMediaStorageList(multipartData, pathToPublicImages, maxImagesAllowed)
        return mediaStorageList
    }

    private suspend fun saveAndReturnMediaStorageList(multipartData: MultiPartData, path: File, maxImagesAllowed: Int): List<MediaStorage> {

        val mediaStorageList: MutableList<MediaStorage> = mutableListOf()
        val imagePathOfSavedImages: MutableList<File> = mutableListOf()

        var imageCount = 0

        multipartData.forEachPart { part ->
            when (part) {

                is PartData.FileItem -> {
                    if (imageCount >= maxImagesAllowed) {
                        imagePathOfSavedImages.forEach {
                            deleteMedia(it)
                        }
                        throw CustomException(
                            error = Errors.LimitOnTheNumberOfImageReached,
                            additionalInfo = "${Infos.LimitOnThePossibleQuantityForShippingIs.description} ${maxImagesAllowed}!"
                        )
                    }

                    val mimeType: String = getType(part.originalFileName!!)
                    if (!MIME_TYPE_ALLOWED.contains(mimeType)) {
                        imagePathOfSavedImages.forEach {
                            deleteMedia(it)
                        }
                        throw CustomException(Errors.UnsupportedMediaType)
                    }

                    val fileBytes: ByteArray = part.streamProvider().readBytes()
                    val filename = "${CharactersGenerator.generateWithNoSymbols()}-${part.originalFileName}"
                    val file = File(path, filename)
                    file.writeBytes(fileBytes)

                    mediaStorageList.add(
                        MediaStorage(
                            filename = filename,
                            filesize = fileBytes.size,
                            code = null
                        )
                    )

                    imagePathOfSavedImages.add(file)
                    imageCount++
                }

                else -> {}
            }

            part.dispose()
        }

        return mediaStorageList
    }

    private fun getType(filename: String): String {
        val extension = filename.substringAfterLast('.', "")
        return extension
    }

    override fun getMedia(name: String): File {
        val file = File("$PUBLIC_IMAGES_DIR", name)
        if(!file.exists()) throw CustomException(Errors.MediaNotExist)
        else return file
    }

    override fun deleteMedia(file: File) {
        getMedia(file.name)
        if(file.exists()) file.delete()
    }

    override suspend fun sendFollowRequest(followerId: String, followingId: String) {
        val collection = CollectionsInDb.Followers
        val followerQuery = Document(Fields.Id.title, ObjectId(followerId))
        val followingQuery = Document(Fields.Id.title, ObjectId(followingId))

        val mongodbOperationsWithQuery = MongodbOperationsWithQuery()

        val userFollowers: Followers? = mongodbOperationsWithQuery.findOneWithQuery<Followers>(
            collectionName = collection,
            query = followerQuery
        )

        if (userFollowers?.pendingSentRequests?.contains(followingId) == true) {
            throw CustomException(Errors.FollowRequestAlreadyExists)
        }

        mongodbOperationsWithQuery.addToSetWithQuery<Followers>(
            collectionName = collection,
            query = followingQuery,
            update = Document(Fields.PendingFollowRequests.title, followerId)
        )

        mongodbOperationsWithQuery.addToSetWithQuery<Followers>(
            collectionName = collection,
            query = followerQuery,
            update = Document(Fields.PendingSentRequests.title, followingId)
        )
    }

    override suspend fun acceptFollowRequest(followerId: String, followingId: String) {
        val collection = CollectionsInDb.Followers
        val followerQuery = Document(Fields.Id.title, ObjectId(followerId))
        val followingQuery = Document(Fields.Id.title, ObjectId(followingId))

        val mongodbOperationsWithQuery = MongodbOperationsWithQuery()

        // Remove a pending request from the recipient
        mongodbOperationsWithQuery.pullItemWithQuery<Followers>(
            collectionName = collection,
            query = followingQuery,
            update = Document(Fields.PendingFollowRequests.title, followerId)
        )

        // Removes the sent request from the sender
        mongodbOperationsWithQuery.pullItemWithQuery<Followers>(
            collectionName = collection,
            query = followerQuery,
            update = Document(Fields.PendingSentRequests.title, followingId)
        )

        // Adds the follower to the recipient's follower list
        mongodbOperationsWithQuery.addToSetWithQuery<Followers>(
            collectionName = collection,
            query = followingQuery,
            update = Document(Fields.FollowerIds.title, followerId)
        )

        // Adds the recipient to the sender's "following" list
        mongodbOperationsWithQuery.addToSetWithQuery<Followers>(
            collectionName = collection,
            query = followerQuery,
            update = Document(Fields.FollowingIds.title, followingId)
        )
    }

    override suspend fun rejectFollowRequest(followerId: String, followingId: String) {
        val collection = CollectionsInDb.Followers
        val followerQuery = Document(Fields.Id.title, ObjectId(followerId))
        val followingQuery = Document(Fields.Id.title, ObjectId(followingId))

        val mongodbOperationsWithQuery = MongodbOperationsWithQuery()

        // Removes pending request from recipient
        mongodbOperationsWithQuery.pullItemWithQuery<Followers>(
            collectionName = collection,
            query = followingQuery,
            update = Document(Fields.PendingFollowRequests.title, followerId)
        )

        // Removes the sent request from the sender
        mongodbOperationsWithQuery.pullItemWithQuery<Followers>(
            collectionName = collection,
            query = followerQuery,
            update = Document(Fields.PendingSentRequests.title, followingId)
        )
    }

    override suspend fun cancelFollowRequest(followerId: String, followingId: String) {
        val collection = CollectionsInDb.Followers
        val followerQuery = Document(Fields.Id.title, ObjectId(followerId))
        val followingQuery = Document(Fields.Id.title, ObjectId(followingId))

        val mongodbOperationsWithQuery = MongodbOperationsWithQuery()

        // Removes pending request from recipient
        mongodbOperationsWithQuery.pullItemWithQuery<Followers>(
            collectionName = collection,
            query = followingQuery,
            update = Document(Fields.PendingFollowRequests.title, followerId)
        )

        // Removes the sent request from the sender
        mongodbOperationsWithQuery.pullItemWithQuery<Followers>(
            collectionName = collection,
            query = followerQuery,
            update = Document(Fields.PendingSentRequests.title, followingId)
        )
    }

    override suspend fun getPendingFollowRequests(userId: String): List<String> {
        val collection = CollectionsInDb.Followers
        val userQuery = Document(Fields.Id.title, ObjectId(userId))

        val mongodbOperationsWithQuery = MongodbOperationsWithQuery()

        val userFollowers: Followers? = mongodbOperationsWithQuery.findOneWithQuery<Followers>(
            collectionName = collection,
            query = userQuery
        )

        return userFollowers?.pendingFollowRequests?.map { it } ?: emptyList()
    }

    override suspend fun getPendingSentRequests(userId: String): List<String> {
        val collection = CollectionsInDb.Followers
        val userQuery = Document(Fields.Id.title, ObjectId(userId))

        val mongodbOperationsWithQuery = MongodbOperationsWithQuery()

        // Fetches the user and returns sent requests
        val userFollowers: Followers? = mongodbOperationsWithQuery.findOneWithQuery<Followers>(
            collectionName = collection,
            query = userQuery
        )

        return userFollowers?.pendingSentRequests?.map { it } ?: emptyList()
    }

    override suspend fun removeFollowing(followerId: String, followingId: String) {
        val collection = CollectionsInDb.Followers
        val followerQuery = Document(Fields.Id.title, ObjectId(followerId))
        val followingQuery = Document(Fields.Id.title, ObjectId(followingId))

        val mongodbOperationsWithQuery = MongodbOperationsWithQuery()

        // Remove who is being followed from the follower's "followingIds" list
        mongodbOperationsWithQuery.pullItemWithQuery<Followers>(
            collectionName = collection,
            query = followerQuery,
            update = Document("followingIds", followingId)
        )

        // Remove the follower from the followed user's "followerIds" list
        mongodbOperationsWithQuery.pullItemWithQuery<Followers>(
            collectionName = collection,
            query = followingQuery,
            update = Document("followerIds", followerId)
        )
    }

    override suspend fun getFollowers(userId: String): List<String> {
        val collection = CollectionsInDb.Followers
        val userQuery = Document(Fields.Id.title, ObjectId(userId))

        val mongodbOperationsWithQuery = MongodbOperationsWithQuery()

        val userFollowers: Followers? = mongodbOperationsWithQuery.findOneWithQuery<Followers>(
            collectionName = collection,
            query = userQuery
        )

        return userFollowers?.followerIds?.map { it } ?: emptyList()
    }

    override suspend fun getFollowing(userId: String): List<String> {
        val collection = CollectionsInDb.Followers
        val userQuery = Document(Fields.Id.title, ObjectId(userId))

        val mongodbOperationsWithQuery = MongodbOperationsWithQuery()

        val userFollowing: Followers? = mongodbOperationsWithQuery.findOneWithQuery<Followers>(
            collectionName = collection,
            query = userQuery
        )

        return userFollowing?.followingIds?.map { it } ?: emptyList()
    }

    override suspend fun getMutualFollowers(userId: String): List<String> {
        val collection = CollectionsInDb.Followers
        val userQuery = Document(Fields.Id.title, ObjectId(userId))

        val mongodbOperationsWithQuery = MongodbOperationsWithQuery()

        val userFollowers: Followers? = mongodbOperationsWithQuery.findOneWithQuery<Followers>(
            collectionName = collection,
            query = userQuery
        )

        if (userFollowers == null) return emptyList()

        val followerIds: MutableSet<String> = userFollowers.followerIds
        val followingIds: MutableSet<String> = userFollowers.followingIds

        // Finds intersection between followers and following
        return followerIds.intersect(followingIds).toList()
    }
}