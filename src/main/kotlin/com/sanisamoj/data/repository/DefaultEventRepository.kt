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

    override suspend fun getEventById(eventId: String): Event {
        return MongodbOperations().findOne<Event>(CollectionsInDb.Events, OperationField(Fields.Id, ObjectId(eventId)))
            ?: throw CustomException(Errors.EventNotFound)
    }

    override suspend fun getAllEventFromAccount(accountId: String): List<Event> {
        return MongodbOperations().findAllByFilter(CollectionsInDb.Events, OperationField(Fields.AccountId, accountId))
    }

    override suspend fun getAllEventFromAccountWithPagination(accountId: String, page: Int, size: Int): List<Event> {
        return MongodbOperations().findAllWithPagingAndFilter(
            collectionName = CollectionsInDb.Events,
            pageSize = size,
            pageNumber = page,
            filter = OperationField(Fields.AccountId, accountId)
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

    override suspend fun getEventsWithFilterCount(searchEventFilters: SearchEventNearby): Int {
        val query = Document("address.geoCoordinates.coordinates", Document("\$near", Document()
            .append("\$geometry", Document()
                .append("type", "Point")
                .append("coordinates", listOf(searchEventFilters.longitude, searchEventFilters.latitude)))
            .append("\$maxDistance", searchEventFilters.maxDistanceMeters)
        ))

        return MongodbOperationsWithQuery().countDocumentsWithFilterWithQuery<Event>(
            collectionName = CollectionsInDb.Events,
            query = query
        )
    }

    private fun searchEventFiltersDocumentBuilder(filters: SearchEventFilters): Document {
        val query = Document()

        filters.name?.let {
            query.append("name", Document("\$regex", it).append("\$options", "i"))
        }

        filters.status?.let {
            query.append("status", it)
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

        val addressFilters = mutableListOf<Document>()
        filters.address?.let { address ->
            address.street?.let { street ->
                addressFilters.add(Document("address.street", Document("\$regex", street).append("\$options", "i")))
            }
            address.neighborhood?.let { neighborhood ->
                addressFilters.add(Document("address.neighborhood", Document("\$regex", neighborhood).append("\$options", "i")))
            }
            address.city?.let { city ->
                addressFilters.add(Document("address.city", Document("\$regex", city).append("\$options", "i")))
            }
            address.uf?.let { uf ->
                addressFilters.add(Document("address.uf", Document("\$regex", uf).append("\$options", "i")))
            }
        }

        if (addressFilters.isNotEmpty()) {
            query.append("\$and", addressFilters)
        }

        return query
    }

    override suspend fun getPresenceByEventAndUser(eventId: String, userId: String): Presence? {
        val query = Document(Fields.EventId.title, eventId).append(Fields.UserId.title, userId)
        return MongodbOperationsWithQuery().findOneWithQuery<Presence>(CollectionsInDb.Presences, query)
    }

    override suspend fun getPublicPresencesFromTheEvent(eventId: String, pageSize: Int,  pageNumber: Int): List<Presence> {
        val query = Document(Fields.EventId.title, eventId).append(Fields.AccountIsPublic.title, true)
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

    override suspend fun getAllPublicPresencesFromTheEvent(eventId: String): List<Presence> {
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
        return MongodbOperationsWithQuery().findAllWithPagingAndFilterWithQuery(
            collectionName = CollectionsInDb.Comments,
            pageSize = pageSize,
            pageNumber = pageNumber,
            query = Document(Fields.EventId.title, eventId).append(Fields.ParentId.title, null)
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

    override suspend fun getPresenceById(presenceId: String): Presence {
        return MongodbOperations().findOne<Presence>(CollectionsInDb.Presences, OperationField(Fields.Id, ObjectId(presenceId)))
            ?: throw CustomException(Errors.PresenceNotFound)
    }
}