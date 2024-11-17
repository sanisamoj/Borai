package com.sanisamoj.data.repository

import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.Event
import com.sanisamoj.data.models.dataclass.SearchEventFilters
import com.sanisamoj.data.models.dataclass.SearchEventNearby
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

    override suspend fun getAllEventFromAccountWithPagination(
        accountId: String,
        page: Int,
        size: Int
    ): List<Event> {
        return MongodbOperations().findAllWithPagingAndFilter(
            collectionName = CollectionsInDb.Events,
            pageSize = size,
            pageNumber = page,
            filter = OperationField(Fields.AccountId, accountId)
        )
    }

    override suspend fun searchEvents(filters: SearchEventFilters): List<Event> {
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

        filters.address?.let {
            it.street?.let { street ->
                Document("address.street", Document("\$regex", it).append("\$options", "i"))
            }
            it.neighborhood?.let { neighborhood ->
                Document("address.neighborhood", Document("\$regex", it).append("\$options", "i"))
            }
            it.city?.let { city ->
                Document("address.city", Document("\$regex", it).append("\$options", "i"))
            }
            it.uf?.let { uf ->
                Document("address.uf", Document("\$regex", it).append("\$options", "i"))
            }
        }

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
}