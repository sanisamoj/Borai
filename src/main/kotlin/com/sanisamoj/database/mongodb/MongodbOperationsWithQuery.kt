package com.sanisamoj.database.mongodb

import com.sanisamoj.data.models.enums.Errors
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document

class MongodbOperationsWithQuery {

    // Retorna um item usando Query
    suspend inline fun <reified T : Any> findOneWithQuery(collectionName: CollectionsInDb, query: Document): T? {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        return collection.find<T>(query).firstOrNull()
    }

    // Função para buscar eventos por nome aproximado com paginação
    suspend inline fun <reified T : Any> findItemsWithPaging(
        collectionName: CollectionsInDb,
        pageSize: Int,
        pageNumber: Int,
        query: Document
    ): List<T> {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        // Filtro de busca com nome aproximado (usando regex)
        val regexQuery = query

        // Cálculo de skip para paginação
        val skip = (pageNumber - 1) * pageSize

        // Busca com regex e paginação
        return collection.find(regexQuery)
            .skip(skip)
            .limit(pageSize)
            .toList()
    }

    // Retorna todos os itens com filtro Query
    suspend inline fun <reified T : Any> findAllByFilterWithQuery(collectionName: CollectionsInDb, query: Document): List<T> {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        return collection.find<T>(query).toList()
    }

    // Retorna itens com paginação e filtro Query
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

    // Conta documentos com filtro Query
    suspend inline fun <reified T : Any> countDocumentsWithFilterWithQuery(collectionName: CollectionsInDb, query: Document): Int {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        return collection.find<T>(query).count()
    }

    // Atualiza um item e retorna o atualizado usando Query
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

    // Substitui um item e retorna o atualizado usando Query
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

    // Remove um item usando Query
    suspend inline fun <reified T : Any> deleteItemWithQuery(collectionName: CollectionsInDb, query: Document) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        val result = collection.deleteOne(query)
        if (result.deletedCount.toInt() == 0) throw Exception(Errors.NoItemsWereDeleted.description)
    }

    // Remove vários itens usando Query
    suspend inline fun <reified T : Any> deleteAllItemsWithQuery(collectionName: CollectionsInDb, query: Document) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        collection.deleteMany(query)
    }

    // Adiciona um valor em um array usando Query
    suspend inline fun <reified T : Any> pushItemWithQuery(
        collectionName: CollectionsInDb,
        query: Document,
        update: Document
    ) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)
        collection.updateOne(query, Document("\$push", update))
    }

    // Atualiza um item dentro de um array usando Query
    suspend inline fun <reified T : Any> updateItemInArrayWithQuery(
        collectionName: CollectionsInDb,
        query: Document,
        arrayField: String,
        arrayFilter: Document,
        updateField: Document
    ) {
        val database = MongoDatabase.getDatabase()
        val collection = database.getCollection<T>(collectionName.name)

        // Combina filtros internos ao array com o filtro principal
        val combinedQuery = Document(query).append(
            "$arrayField.$[].${arrayFilter.keys.first()}", arrayFilter.values.first()
        )

        val update = Document(
            "\$set", Document("$arrayField.$[].${updateField.keys.first()}", updateField.values.first())
        )
        collection.updateOne(combinedQuery, update)
    }

}