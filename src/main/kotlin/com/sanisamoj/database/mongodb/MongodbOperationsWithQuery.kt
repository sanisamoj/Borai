package com.sanisamoj.database.mongodb

import com.mongodb.client.model.UpdateOptions
import com.sanisamoj.data.models.enums.Errors
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document

class MongodbOperationsWithQuery {

    // Returns an item using a query
    suspend inline fun <reified T : Any> findOneWithQuery(collectionName: CollectionsInDb, query: Document): T? {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        return collection.find<T>(query).firstOrNull()
    }

    // Searches for items by approximate name with pagination
    suspend inline fun <reified T : Any> findItemsWithPaging(
        collectionName: CollectionsInDb,
        pageSize: Int,
        pageNumber: Int,
        query: Document
    ): List<T> {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        // Search filter with approximate name (using regex)
        val regexQuery = query

        // Calculate skip for pagination
        val skip = (pageNumber - 1) * pageSize

        // Search with regex and pagination
        return collection.find(regexQuery)
            .skip(skip)
            .limit(pageSize)
            .toList()
    }

    // Returns all items with a query filter
    suspend inline fun <reified T : Any> findAllByFilterWithQuery(collectionName: CollectionsInDb, query: Document, sort: Document = Document()): List<T> {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        return collection.find<T>(query).sort(sort).toList()
    }

    // Returns items with pagination and query filter
    suspend inline fun <reified T : Any> findAllWithPagingAndFilterWithQuery(
        collectionName: CollectionsInDb,
        pageSize: Int,
        pageNumber: Int,
        query: Document,
        sort: Document = Document()
    ): List<T> {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        val skip = (pageNumber - 1) * pageSize
        return collection.find(query).skip(skip).limit(pageSize).sort(sort).toList()
    }

    // Counts documents with a query filter
    suspend inline fun <reified T : Any> countDocumentsWithFilterWithQuery(collectionName: CollectionsInDb, query: Document): Int {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        return collection.find<T>(query).count()
    }

    // Updates an item and returns the updated version using a query
    suspend inline fun <reified T : Any> updateAndReturnItemWithQuery(
        collectionName: CollectionsInDb,
        query: Document,
        update: Document
    ): T? {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        return try {
            collection.updateOne(query, Document("\$set", update))
            collection.find<T>(query).firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    // Replaces an item and returns the updated version using a query
    suspend inline fun <reified T : Any> replaceAndReturnItemWithQuery(
        collectionName: CollectionsInDb,
        query: Document,
        newItem: T
    ): T? {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        return try {
            collection.replaceOne(query, newItem)
            collection.find<T>(query).firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    // Removes an item using a query
    suspend inline fun <reified T : Any> deleteItemWithQuery(collectionName: CollectionsInDb, query: Document) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        val result = collection.deleteOne(query)
        if (result.deletedCount.toInt() == 0) throw Exception(Errors.NoItemsWereDeleted.description)
    }

    // Removes multiple items using a query
    suspend inline fun <reified T : Any> deleteAllItemsWithQuery(collectionName: CollectionsInDb, query: Document) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        collection.deleteMany(query)
    }

    // Adds a value to an array using a query
    suspend inline fun <reified T : Any> pushItemWithQuery(
        collectionName: CollectionsInDb,
        query: Document,
        update: Document
    ) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        collection.updateOne(query, Document("\$push", update))
    }

    // Updates an item inside an array using a query
    suspend inline fun <reified T : Any> updateItemInArrayWithQuery(
        collectionName: CollectionsInDb,
        query: Document,
        arrayField: String,
        arrayFilter: Document,
        updateField: Document
    ) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        // Combines array filters with the main query filter
        val combinedQuery = Document(query).append(
            "$arrayField.$[].${arrayFilter.keys.first()}", arrayFilter.values.first()
        )

        val update = Document(
            "\$set", Document("$arrayField.$[].${updateField.keys.first()}", updateField.values.first())
        )
        collection.updateOne(combinedQuery, update)
    }

    // Adds an item to a set in an array using a query
    suspend inline fun <reified T : Any> addToSetWithQuery(
        collectionName: CollectionsInDb,
        query: Document,
        update: Document
    ) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        collection.updateOne(query, Document("\$addToSet", update))
    }

    // Inserts or replaces an item and returns the updated version using a query
    suspend inline fun <reified T : Any> upsertAndReturnItemWithQuery(
        collectionName: CollectionsInDb,
        query: Document,
        newItem: T
    ): T? {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        return try {
            // Performs an upsert
            collection.updateOne(
                filter = query,
                update = Document("\$set", newItem), // Replaces the document fields with the new item's fields
                options = UpdateOptions().upsert(true) // Enables upsert
            )

            // Fetches the updated or newly created document
            collection.find(query).firstOrNull()
        } catch (exception: Exception) {
            println("Error in upsert: ${exception.message}")
            null
        }
    }

    // Removes a specific item from an array using a query
    suspend inline fun <reified T : Any> pullItemWithQuery(
        collectionName: CollectionsInDb,
        query: Document,
        update: Document
    ): Boolean {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        return try {
            val result = collection.updateOne(query, Document("\$pull", update))
            result.modifiedCount > 0
        } catch (e: Exception) {
            println("Error executing pullItemWithQuery: ${e.message}")
            false
        }
    }

    suspend inline fun <reified T : Any> incrementValueWithQuery(
        collectionName: CollectionsInDb,
        query: Document,
        field: String,
        incrementValue: Number
    ): Boolean {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        return try {
            val update = Document("\$inc", Document(field, incrementValue))
            val result = collection.updateOne(query, update)
            result.modifiedCount > 0
        } catch (e: Exception) {
            println("Error executing incrementValueWithQuery: ${e.message}")
            false
        }
    }

    suspend inline fun <reified T : Any> decrementValueWithQuery(
        collectionName: CollectionsInDb,
        query: Document,
        field: String,
        decrementValue: Number
    ): Boolean {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        return try {
            val update = Document("\$inc", Document(field, -decrementValue.toDouble()))
            val result = collection.updateOne(query, update)
            result.modifiedCount > 0
        } catch (e: Exception) {
            println("Error executing decrementValueWithQuery: ${e.message}")
            false
        }
    }

    suspend inline fun <reified T : Any> updateWithQuery(
        collectionName: CollectionsInDb,
        query: Document,
        update: Document,
        options: UpdateOptions
    ) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        collection.updateOne(query, update, options)
    }

}
