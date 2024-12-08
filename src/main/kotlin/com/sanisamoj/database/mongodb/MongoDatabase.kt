package com.sanisamoj.database.mongodb

import com.mongodb.MongoException
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.sanisamoj.data.models.dataclass.Event
import com.sanisamoj.utils.analyzers.dotEnv
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import org.bson.BsonInt64
import org.bson.Document
import java.util.concurrent.TimeUnit

object MongoDatabase {
    private var database: MongoDatabase? = null
    private lateinit var client : MongoClient
    private val connectionString: String = dotEnv("MONGODB_SERVER_URL")
    private val nameDatabase : String = dotEnv("NAME_DATABASE")

    private suspend fun init() {
        client = MongoClient.create(connectionString)
        val db: MongoDatabase = client.getDatabase(nameDatabase)

        try {
            val command = Document("ping", BsonInt64(1))
            db.runCommand(command)
            println("You successfully connected to MongoDB!")
            database = db
            createGeospatialIndex(db)
        } catch (me: MongoException) {
            System.err.println(me)
            println("A new attempt will be made to reconnect to mongodb in 30s.")
            delay(TimeUnit.SECONDS.toMillis(30))
            init()
        }
    }

    suspend fun initialize() { if (database == null) init() }

    suspend fun getDatabase(): MongoDatabase {
        if (database == null) init()
        return database!!
    }

    private suspend fun createGeospatialIndex(db: MongoDatabase) {
        val collection = db.getCollection<Event>("Events")

        try {
            val indexes = collection.listIndexes().toList()
            val indexExists = indexes.any { it["name"] == "address.geoCoordinates.coordinates_2dsphere" }

            if (!indexExists) {
                collection.createIndex(Indexes.geo2dsphere("address.geoCoordinates.coordinates"))
            } else {
                return
            }
        } catch (e: Exception) {
            println("Error creating geospatial index: ${e.message}")
        }
    }

}