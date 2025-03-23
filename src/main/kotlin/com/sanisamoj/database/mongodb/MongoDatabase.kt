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
    private lateinit var client: MongoClient

    // Obtenção de variáveis de ambiente do .env (ou variáveis definidas no Docker Compose)
    private val mongoUser: String = dotEnv("MONGODB_INITDB_ROOT_USERNAME")
    private val mongoPassword: String = dotEnv("MONGODB_INITDB_ROOT_PASSWORD")
    private val mongodb : String = dotEnv("MONGODB")
    private val mongoPort: String = dotEnv("MONGODB_PORT")
    private val connectionString: String = "mongodb://$mongoUser:$mongoPassword@${mongodb}:$mongoPort"  // Usando a senha e usuário do Docker
    private val nameDatabase: String = dotEnv("NAME_DATABASE")

    private suspend fun init() {
        try {
            // Tentar conexão com autenticação
            client = MongoClient.create(connectionString)
            val db: MongoDatabase = client.getDatabase(nameDatabase)

            // Verificar a conexão
            val command = Document("ping", BsonInt64(1))
            db.runCommand(command)
            println("You successfully connected to MongoDB with authentication!")
            database = db
            createGeospatialIndex(db)
        } catch (me: MongoException) {
            println("Error connecting with authentication: ${me.message}")
            println("Trying connection without authentication...")

            // Caso falhe, tente a conexão sem senha
            val connectionStringWithoutAuth = "mongodb://$mongodb:$mongoPort"  // Remover usuário e senha
            try {
                client = MongoClient.create(connectionStringWithoutAuth)
                val db: MongoDatabase = client.getDatabase(nameDatabase)

                // Verificar a conexão
                val command = Document("ping", BsonInt64(1))
                db.runCommand(command)
                println("You successfully connected to MongoDB without authentication!")
                database = db
                createGeospatialIndex(db)
            } catch (e: MongoException) {
                println("Error connecting without authentication: ${e.message}")
                println("A new attempt will be made to reconnect to mongodb in 30s.")
                delay(TimeUnit.SECONDS.toMillis(30))
                init() // Tentativa de reconectar
            }
        } catch (e: Exception) {
            println("Unexpected error: ${e.message}")
        }
    }


    suspend fun initialize() {
        if (database == null) init()
    }

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
