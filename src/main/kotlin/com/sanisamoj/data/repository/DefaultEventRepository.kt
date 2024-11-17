package com.sanisamoj.data.repository

import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.Event
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.MongodbOperations
import com.sanisamoj.database.mongodb.OperationField
import org.bson.types.ObjectId

class DefaultEventRepository: EventRepository {

    override suspend fun createEvent(event: Event): Event {
        val eventId: String = MongodbOperations().register(CollectionsInDb.Events, event).toString()
        return getEventById(eventId)!!
    }

    override suspend fun getEventById(eventId: String): Event? {
        return MongodbOperations().findOne<Event>(CollectionsInDb.Events, OperationField(Fields.Id, ObjectId(eventId)))
    }

    override suspend fun getEventByName(eventName: String): Event? {
        return MongodbOperations().findOne<Event>(CollectionsInDb.Events, OperationField(Fields.Name, eventName))
    }

    override suspend fun getAllEventFromAccount(accountId: String): List<Event> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllEventFromAccountWithPagination(
        accountId: String,
        page: Int,
        size: Int
    ): List<Event> {
        TODO("Not yet implemented")
    }

    override suspend fun findEventsNearby(
        longitude: Double,
        latitude: Double,
        maxDistanceMeters: Int
    ): List<Event> {
        TODO("Not yet implemented")
    }
}