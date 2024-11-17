package com.sanisamoj.database.mongodb

import com.mongodb.client.model.Filters
import com.sanisamoj.data.models.dataclass.Event
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId
import java.time.LocalDateTime

class MongodbOperations {

    // Adds an item to the database
    suspend inline fun <reified T : Any> register(collectionName: CollectionsInDb, item: T): ObjectId {

        // Returns the database
        val database = MongoDatabase.getDatabase()

        // Returns the collection
        val collection = database.getCollection<T>(collectionName.name)

        val id = collection.insertOne(item).insertedId?.asObjectId()?.value
        return id!!

    }

    // Returns an item
    suspend inline fun <reified T : Any> findOne(collectionName: CollectionsInDb, filter: OperationField): T? {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        val result: T? = collection.find<T>(Document(filter.field.title, filter.value)).firstOrNull()

        return result
    }

    // Returns all items
    suspend inline fun <reified T : Any> findAll(collectionName: CollectionsInDb): List<T> {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        val result: List<T> = collection.find<T>()
            .toList()

        return result
    }

    // Returns all items by filter
    suspend inline fun <reified T : Any> findAllByFilter(collectionName: CollectionsInDb, filter: OperationField): List<T> {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        val result: List<T> = collection.find<T>(Document(filter.field.title, filter.value))
            .toList()

        return result
    }

    // Returns all items with paging and filtering
    suspend inline fun <reified T : Any> findAllWithPagingAndFilter(
        collectionName: CollectionsInDb,
        pageSize: Int,
        pageNumber: Int,
        filter: OperationField
    ): List<T> {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        val skip = (pageNumber - 1) * pageSize

        // Applying the filter and pagination
        val result: List<T> = collection.find(Document(filter.field.title, filter.value))
            .skip(skip)
            .limit(pageSize)
            .toList()

        return result
    }


    // Returns all items with paging
    suspend inline fun <reified T : Any> findAllWithPaging(
        collectionName: CollectionsInDb,
        pageSize: Int,
        pageNumber: Int
    ): List<T> {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        val skip = (pageNumber - 1) * pageSize

        val result: List<T> = collection.find<T>()
            .skip(skip)
            .limit(pageSize)
            .toList()

        return result
    }

    // Returns count of items with filter
    suspend inline fun <reified T : Any> countDocumentsWithFilter(collectionName: CollectionsInDb, filter: OperationField): Int {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        val result: Int = collection.find<T>(Document(filter.field.title, filter.value)).count()

        return result
    }

    // Returns count of items without filter
    suspend inline fun <reified T : Any> countDocumentsWithoutFilter(collectionName: CollectionsInDb): Int {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        val result: Int = collection.find<T>().count()

        return result
    }

    // Deletes an item from the database
    suspend inline fun <reified T : Any> deleteItem(collectionName: CollectionsInDb, filter: OperationField) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        val result = collection.deleteOne(Document(filter.field.title, filter.value))
        if (result.deletedCount.toInt() == 0) throw Exception("No items were deleted")
        return
    }

    // Deletes a many items from the database
    suspend inline fun <reified T : Any> deleteAllItems(collectionName: CollectionsInDb, filter: OperationField) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        collection.deleteMany(Document(filter.field.title, filter.value))
        return
    }

    // Drop collections
    suspend inline fun <reified T : Any> dropCollection(collectionName: CollectionsInDb) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        collection.drop()
    }

    // Updates a value in the item and return
    suspend inline fun <reified T : Any> updateAndReturnItem(
        collectionName: CollectionsInDb,
        filter: OperationField,
        update: OperationField
    ): T? {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        return try {
            collection.updateOne(
                Document(filter.field.title, filter.value),
                Document("\$set", Document(update.field.title, update.value))
            )
            val result: T = collection.find<T>(Document(filter.field.title, filter.value)).first()
            result
        } catch (_: Exception) {
            null
        }

    }

    // Updates an entire item and returns it
    suspend inline fun <reified T : Any> replaceAndReturnItem(
        collectionName: CollectionsInDb,
        filter: OperationField,
        newItem: T
    ): T? {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        return try {
            // Replaces the entire document with newItem
            collection.replaceOne(
                Document(filter.field.title, filter.value),
                newItem
            )
            // Finds and returns the updated document
            collection.find(Document(filter.field.title, filter.value)).first()
        } catch (_: Exception) {
            null
        }
    }


    // Push a value in the array of items
    suspend inline fun <reified T : Any> pushItem(
        collectionName: CollectionsInDb,
        filter: OperationField,
        update: OperationField
    ) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        collection.updateOne(
            Document(filter.field.title, filter.value),
            Document("\$push", Document(update.field.title, update.value))
        )
    }

    suspend inline fun <reified T : Any> updateItemInArray(
        collectionName: CollectionsInDb,
        filter: OperationField,
        arrayField: String,
        arrayFilter: OperationField,
        updateField: OperationField
    ) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        // Filter to find the document through the external filter and the item inside the array
        val query = Document(filter.field.title, filter.value).append(
            "$arrayField.${arrayFilter.field.title}", arrayFilter.value
        )

        // Update to change item property within array
        val update = Document(
            "\$set", Document("$arrayField.$[].${updateField.field.title}", updateField.value)
        )

        collection.updateOne(query, update)
    }


    suspend inline fun <reified T : Any> findItemsExpiringBeforeOrOn(
        collectionName: CollectionsInDb,
        dateFieldName: String,
        dateTime: LocalDateTime,
        filter: OperationField? = null
    ): List<T> {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        // Current data and time in ISO format
        val currentDateTime = dateTime.toString()

        // Filter for documents with 'dateFieldName' less than or equal to the specified date-time
        val dateFilter = Filters.lte(dateFieldName, currentDateTime)

        // If additional filter is not null, combine filters
        val combinedFilter = if (filter != null) {
            val additionalFilter = Filters.eq(filter.field.title, filter.value)
            Filters.and(dateFilter, additionalFilter)
        } else {
            dateFilter
        }

        // Find documents that match the filter
        return collection.find(combinedFilter).toList()
    }

    suspend inline fun <reified T : Any> findItemsExpiringAfterOrOn(
        collectionName: CollectionsInDb,
        dateFieldName: String,
        dateTime: LocalDateTime,
        filter: OperationField? = null
    ): List<T> {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        // Current date and time in ISO format
        val currentDateTime = dateTime.toString()

        // Filter for documents with 'dateFieldName' greater than or equal to the specified date-time
        val dateFilter = Filters.gte(dateFieldName, currentDateTime)

        // If additional filter is not null, combine filters
        val combinedFilter = if (filter != null) {
            val additionalFilter = Filters.eq(filter.field.title, filter.value)
            Filters.and(dateFilter, additionalFilter)
        } else {
            dateFilter
        }

        // Find documents that match the filter
        return collection.find(combinedFilter).toList()
    }

    suspend inline fun <reified T : Any> findItemsInPeriod(
        collectionName: CollectionsInDb,
        dateFieldName: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        filter: OperationField? = null
    ): List<T> {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        // Dates and times in ISO format
        val startDate = startDateTime.toString()
        val endDate = endDateTime.toString()

        // Filter for documents with 'dateFieldName' between the specified dates
        val dateFilter = Filters.and(
            Filters.gte(dateFieldName, startDate),
            Filters.lte(dateFieldName, endDate)
        )

        // If additional filter is not null, combine filters
        val combinedFilter = if (filter != null) {
            val additionalFilter = Filters.eq(filter.field.title, filter.value)
            Filters.and(dateFilter, additionalFilter)
        } else {
            dateFilter
        }

        // Find documents that match the filter
        return collection.find(combinedFilter).toList()
    }

    suspend fun findEventsNearby(longitude: Double, latitude: Double, maxDistanceMeters: Int): Event? {
        val query = Document("address.coordinates", Document("\$near", Document()
            .append("\$geometry", Document()
                .append("type", "Point")
                .append("coordinates", listOf(longitude, latitude)))
            .append("\$maxDistance", maxDistanceMeters)
        ))
        return MongodbOperationsWithQuery().findOneWithQuery<Event>(CollectionsInDb.Events, query)
    }


}