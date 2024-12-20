package com.sanisamoj.data.repository

import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.database.mongodb.*
import org.bson.Document
import org.bson.types.ObjectId

class DefaultEventRepository: EventRepository {

    override suspend fun createEvent(event: Event): Event {
        val eventId: String = MongodbOperations().register(CollectionsInDb.Events, event).toString()
        return getEventById(eventId)
    }

    override suspend fun deleteEvent(eventId: String) {
        MongodbOperations().deleteItem<Event>(CollectionsInDb.Events, OperationField(Fields.Id, ObjectId(eventId)))
    }

    override suspend fun getEventById(eventId: String): Event {
        return MongodbOperations().findOne<Event>(CollectionsInDb.Events, OperationField(Fields.Id, ObjectId(eventId)))
            ?: throw CustomException(Errors.EventNotFound)
    }

    override suspend fun getAllEventFromAccount(accountId: String): List<Event> {
        return MongodbOperations().findAllByFilter(CollectionsInDb.Events, OperationField(Fields.AccountId, accountId))
    }

    override suspend fun getAllEventFromAccountCount(accountId: String): Int {
        return MongodbOperations().countDocumentsWithFilter<Event>(
            collectionName = CollectionsInDb.Events,
            filter = OperationField(Fields.UserId, accountId)
        )
    }

    override suspend fun getAllEventFromAccountWithPagination(accountId: String, page: Int, size: Int): List<Event> {
        return MongodbOperations().findAllWithPagingAndFilter(
            collectionName = CollectionsInDb.Events,
            pageSize = size,
            pageNumber = page,
            filter = OperationField(Fields.AccountId, accountId),
            sort = Document(Fields.CreatedAt.title, -1)
        )
    }

    override suspend fun searchEvents(filters: SearchEventFilters): List<Event> {
        val query: Document = searchEventFiltersDocumentBuilder(filters)
        val eventList: List<Event> = MongodbOperationsWithQuery().findAllWithPagingAndFilterWithQuery<Event>(
            collectionName = CollectionsInDb.Events,
            pageNumber = filters.page,
            pageSize = filters.size,
            query = query
        )
        return eventList
    }

    override suspend fun findEventsNearby(filters: SearchEventNearby): List<Event> {
        val query = Document("address.geoCoordinates.coordinates", Document("\$near", Document()
            .append("\$geometry", Document()
                .append("type", "Point")
                .append("coordinates", listOf(filters.longitude, filters.latitude)))
            .append("\$maxDistance", filters.maxDistanceMeters)
        ))

        filters.nick?.let {
            val user: User = MongodbOperations().findOne<User>(CollectionsInDb.Users, OperationField(Fields.Nick, it))
                ?: throw CustomException(Errors.UserNotFound)

            query.append("accountId", user.id.toString())
        }

        filters.type?.let {
            query.append("type", Document("\$in", it))
        }

        filters.date?.let { startDate ->
            val endDate = filters.endDate ?: startDate.toLocalDate().atTime(23, 59, 59)
            query.append(
                "date",
                Document("\$gte", startDate).append("\$lte", endDate)
            )
        }

        return MongodbOperationsWithQuery().findAllWithPagingAndFilterWithQuery(
            collectionName = CollectionsInDb.Events,
            pageSize = filters.size,
            pageNumber = filters.page,
            query = query
        )
    }

    private suspend fun incrementPresence(eventId: String) {
        MongodbOperations().incrementField<Event>(
            collectionName = CollectionsInDb.Events,
            filter = OperationField(Fields.Id, ObjectId(eventId)),
            fieldName = Fields.Presences.title,
            incrementValue = 1
        )
    }

    private suspend fun decrementPresence(eventId: String) {
        MongodbOperations().decrementField<Event>(
            collectionName = CollectionsInDb.Events,
            filter = OperationField(Fields.Id, ObjectId(eventId)),
            fieldName = Fields.Presences.title,
            decrementValue = 1
        )
    }

    override suspend fun getEventsWithFilterCount(filters: SearchEventFilters): Int {
        val query: Document = searchEventFiltersDocumentBuilder(filters)
        return MongodbOperationsWithQuery().countDocumentsWithFilterWithQuery<Event>(
            collectionName = CollectionsInDb.Events,
            query = query
        )
    }

    override suspend fun getEventsWithFilterCount(filters: SearchEventNearby): Int {
        val query = Document("address.geoCoordinates.coordinates", Document("\$near", Document()
            .append("\$geometry", Document()
                .append("type", "Point")
                .append("coordinates", listOf(filters.longitude, filters.latitude)))
            .append("\$maxDistance", filters.maxDistanceMeters)
        ))

        return MongodbOperationsWithQuery().countDocumentsWithFilterWithQuery<Event>(
            collectionName = CollectionsInDb.Events,
            query = query
        )
    }

    override suspend fun updateEvent(eventId: String, update: OperationField): Event {
        return MongodbOperations().updateAndReturnItem<Event>(
            collectionName = CollectionsInDb.Events,
            filter = OperationField(Fields.Id, ObjectId(eventId)),
            update = update
        ) ?: throw CustomException(Errors.EventNotFound)
    }

    private suspend fun searchEventFiltersDocumentBuilder(filters: SearchEventFilters): Document {
        val query = Document()
        var hasFilters = false

        filters.name?.let {
            hasFilters = true
            query.append("name", Document("\$regex", it).append("\$options", "i"))
        }

        filters.nick?.let {
            val user: User = MongodbOperations().findOne<User>(CollectionsInDb.Users, OperationField(Fields.Nick, filters.nick))
                ?: throw CustomException(Errors.UserNotFound)

            hasFilters = true
            query.append("accountId", user.id.toString())
        }

        filters.status?.let {
            hasFilters = true
            query.append("status", it)
        }

        filters.type?.let {
            hasFilters = true
            query.append("type", Document("\$in", it))
        }

        filters.date?.let { startDate ->
            val endDate = filters.endDate ?: startDate.toLocalDate().atTime(23, 59, 59)
            hasFilters = true
            query.append(
                "date",
                Document("\$gte", startDate).append("\$lte", endDate)
            )
        }

        val addressFilters = mutableListOf<Document>()
        filters.address?.let { address ->
            address.street?.let { street ->
                hasFilters = true
                addressFilters.add(Document("address.street", Document("\$regex", street).append("\$options", "i")))
            }
            address.neighborhood?.let { neighborhood ->
                hasFilters = true
                addressFilters.add(Document("address.neighborhood", Document("\$regex", neighborhood).append("\$options", "i")))
            }
            address.city?.let { city ->
                hasFilters = true
                addressFilters.add(Document("address.city", Document("\$regex", city).append("\$options", "i")))
            }
            address.uf?.let { uf ->
                hasFilters = true
                addressFilters.add(Document("address.uf", Document("\$regex", uf).append("\$options", "i")))
            }
        }

        if (addressFilters.isNotEmpty()) {
            query.append("\$and", addressFilters)
        }

        if (!hasFilters) {
            throw CustomException(Errors.InvalidParameters)
        }

        return query
    }

    override suspend fun getPresenceByEventAndUser(eventId: String, userId: String): Presence? {
        val query = Document(Fields.EventId.title, eventId).append(Fields.UserId.title, userId)
        return MongodbOperationsWithQuery().findOneWithQuery<Presence>(CollectionsInDb.Presences, query)
    }

    override suspend fun getPresencesFromTheEvent(eventId: String, pageSize: Int,  pageNumber: Int, public: Boolean): List<Presence> {
        val query = Document(Fields.EventId.title, eventId).append(Fields.AccountIsPublic.title, public)
        val sort = Document(Fields.CreatedAt.title, -1)

        return MongodbOperationsWithQuery().findAllWithPagingAndFilterWithQuery<Presence>(
            collectionName = CollectionsInDb.Presences,
            pageSize = pageSize,
            pageNumber = pageNumber,
            query = query,
            sort = sort
        )
    }

    override suspend fun getPublicPresencesFromTheEventCount(eventId: String): Int {
        return MongodbOperationsWithQuery().countDocumentsWithFilterWithQuery<Presence>(
            collectionName = CollectionsInDb.Presences,
            query = Document(Fields.EventId.title, eventId).append(Fields.AccountIsPublic.title, true)
        )
    }

    override suspend fun getAllPresencesFromTheEventCount(eventId: String): Int {
        return MongodbOperationsWithQuery().countDocumentsWithFilterWithQuery<Presence>(
            collectionName = CollectionsInDb.Presences,
            query = Document(Fields.EventId.title, eventId)
        )
    }

    override suspend fun getAllPresencesFromTheEvent(eventId: String): List<Presence> {
        val query = Document(Fields.EventId.title, eventId)
        val sort = Document(Fields.CreatedAt.title, -1)

        return MongodbOperationsWithQuery().findAllByFilterWithQuery(
            collectionName = CollectionsInDb.Presences,
            query = query,
            sort = sort
        )
    }

    override suspend fun getPresenceByUser(userId: String, pageSize: Int, pageNumber: Int): List<Presence> {
        val query = Document(Fields.UserId.title, userId)
        val sort = Document(Fields.CreatedAt.title, -1)

        return MongodbOperationsWithQuery().findAllWithPagingAndFilterWithQuery<Presence>(
            collectionName = CollectionsInDb.Presences,
            pageSize = pageSize,
            pageNumber = pageNumber,
            query = query,
            sort = sort
        )
    }

    override suspend fun getAllPresenceByUser(userId: String): List<Presence> {
        val query = Document(Fields.UserId.title, userId)
        val sort = Document(Fields.CreatedAt.title, -1)
        return MongodbOperationsWithQuery().findAllByFilterWithQuery(
            collectionName = CollectionsInDb.Presences,
            query = query,
            sort = sort
        )
    }

    override suspend fun getPresenceByUserCount(userId: String): Int {
        val query = Document(Fields.UserId.title, userId)
        return MongodbOperationsWithQuery().countDocumentsWithFilterWithQuery<Presence>(
            collectionName = CollectionsInDb.Presences,
            query = query
        )
    }

    override suspend fun markPresence(presence: Presence): Presence {
        incrementPresence(presence.eventId)
        val id: String = MongodbOperations().register(CollectionsInDb.Presences, presence).toString()
        return getPresenceById(id)
    }

    override suspend fun unmarkPresence(userId: String, eventId: String) {
        decrementPresence(eventId)

        val query = Document().apply {
            append(Fields.UserId.title, userId)
            append(Fields.EventId.title, eventId)
        }

        MongodbOperationsWithQuery().deleteItemWithQuery<Presence>(CollectionsInDb.Presences, query)
    }

    override suspend fun addComment(comment: Comment): Comment {
        val commentId: String = MongodbOperations().register(CollectionsInDb.Comments, comment).toString()
        comment.parentId?.let { incrementAnswer(it) }
        return getCommentById(commentId)
    }

    private suspend fun incrementAnswer(commentId: String) {
        MongodbOperations().incrementField<Comment>(
            collectionName = CollectionsInDb.Comments,
            filter = OperationField(Fields.Id, ObjectId(commentId)),
            fieldName = Fields.AnswersCount.title,
            incrementValue = 1
        )
    }

    private suspend fun decrementAnswer(commentId: String) {
        MongodbOperations().decrementField<Comment>(
            collectionName = CollectionsInDb.Comments,
            filter = OperationField(Fields.Id, ObjectId(commentId)),
            fieldName = Fields.AnswersCount.title,
            decrementValue = 1
        )
    }

    override suspend fun getCommentById(commentId: String): Comment {
        return MongodbOperations().findOne<Comment>(CollectionsInDb.Comments, OperationField(Fields.Id, ObjectId(commentId)))
            ?: throw CustomException(Errors.CommentNotFound)
    }

    override suspend fun getCommentsFromTheEvent(eventId: String, pageSize: Int, pageNumber: Int): List<Comment> {
        val sortQuery = Document()
            .append("answersCount", -1)
            .append("ups.size", -1)

        return MongodbOperationsWithQuery().findAllWithPagingAndFilterWithQuery(
            collectionName = CollectionsInDb.Comments,
            pageSize = pageSize,
            pageNumber = pageNumber,
            query = Document(Fields.EventId.title, eventId).append(Fields.ParentId.title, null),
            sort = sortQuery
        )
    }

    override suspend fun getParentComments(eventId: String, parentId: String, pageSize: Int, pageNumber: Int): List<Comment> {
        return MongodbOperationsWithQuery().findAllWithPagingAndFilterWithQuery(
            collectionName = CollectionsInDb.Comments,
            pageSize = pageSize,
            pageNumber = pageNumber,
            query = Document(Fields.EventId.title, eventId).append(Fields.ParentId.title, parentId)
        )
    }

    override suspend fun getCommentsFromTheEventCount(eventId: String): Int {
        return MongodbOperationsWithQuery().countDocumentsWithFilterWithQuery<Comment>(
            collectionName = CollectionsInDb.Comments,
            query = Document(Fields.EventId.title, eventId).append(Fields.ParentId.title, null)
        )
    }

    override suspend fun getParentCommentsCount(eventId: String, parentId: String): Int {
        return MongodbOperationsWithQuery().countDocumentsWithFilterWithQuery<Comment>(
            collectionName = CollectionsInDb.Comments,
            query = Document(Fields.EventId.title, eventId).append(Fields.ParentId.title, parentId)
        )
    }

    override suspend fun deleteComment(commentId: String) {
        val comment = getCommentById(commentId)
        comment.parentId?.let { decrementAnswer(it) }
        MongodbOperations().deleteItem<Comment>(CollectionsInDb.Comments, OperationField(Fields.Id, ObjectId(commentId)))
    }

    override suspend fun submitEventVote(eventVote: EventVote) {
        val query = Document(Fields.Id.title, ObjectId(eventVote.eventId))
        val update = Document(Fields.EventVotes.title, eventVote)
        MongodbOperationsWithQuery().pushItemWithQuery<Event>(CollectionsInDb.Events, query, update)
    }

    override suspend fun upComment(commentId: String, userId: String) {
        val query = Document(Fields.Id.title, ObjectId(commentId))
        val update = Document(Fields.Ups.title, userId)
        MongodbOperationsWithQuery().pushItemWithQuery<Comment>(CollectionsInDb.Comments, query, update)
    }

    override suspend fun downComment(commentId: String, userId: String) {
        val query = Document(Fields.Id.title, ObjectId(commentId))
        val update = Document(Fields.Ups.title, userId)
        MongodbOperationsWithQuery().pullItemWithQuery<Comment>(CollectionsInDb.Comments, query, update)
    }

    override suspend fun getPresenceById(presenceId: String): Presence {
        return MongodbOperations().findOne<Presence>(CollectionsInDb.Presences, OperationField(Fields.Id, ObjectId(presenceId)))
            ?: throw CustomException(Errors.PresenceNotFound)
    }
}