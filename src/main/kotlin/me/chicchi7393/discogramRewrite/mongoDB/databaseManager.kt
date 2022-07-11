package me.chicchi7393.discogramRewrite.mongoDB
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.result.UpdateResult
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.objects.databaseObjects.ticketDocument
import org.bson.BsonValue
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.*

class databaseManager {
    private val settings = JsonReader().readJsonSettings("settings")!!

    init {
        println("Mongo Class Initialized")
    }

    private object GetInstance {
        val INSTANCE = databaseManager()
    }

    companion object {
        val instance: databaseManager by lazy { GetInstance.INSTANCE }
    }

    lateinit var mongoClient: MongoClient

    fun createClient(): MongoClient {
        mongoClient = KMongo.createClient(settings.mongodb["connection_string"]!!)
        return mongoClient
    }

    inner class get {
        fun getDB(): MongoDatabase {
            return mongoClient.getDatabase(settings.mongodb["database"])
        }

        fun getTicketsCollection(): MongoCollection<ticketDocument> {
            return getDB()
                .getCollection<ticketDocument>("tickets")
        }
    }

    inner class create {
        inner class tickets {
            fun createTicketDocument(
                ticketDocument: ticketDocument
            ): BsonValue? {
                return databaseManager.instance.get().getTicketsCollection()
                    .insertOne(ticketDocument)
                    .insertedId
            }
            fun createManyTicketDocuments(
                ticketDocuments: List<ticketDocument>
            ): MutableMap<Int, BsonValue> {
                return databaseManager.instance.get().getTicketsCollection()
                    .insertMany(ticketDocuments)
                    .insertedIds
            }
        }
    }

    inner class search {
        inner class tickets {
            fun searchTicketDocumentById(ticketId: Int): ticketDocument? {
                return databaseManager.instance.get().getTicketsCollection()
                    .findOne(ticketDocument::ticketId eq ticketId)
            }

            fun searchTicketDocumentByChannelId(channelId: Long): ticketDocument? {
                return databaseManager.instance.get().getTicketsCollection()
                    .findOne(ticketDocument::channelId eq channelId)
            }

            fun searchTicketDocumentByTelegramId(telegramId: Long): ticketDocument? {
                return databaseManager.instance.get().getTicketsCollection()
                    .findOne(ticketDocument::telegramId eq telegramId)
            }
        }
    }

    inner class findLatest {
        fun findLatestTicket(): ticketDocument? {
            return databaseManager.instance.get().getTicketsCollection()
                .find()
                .descendingSort(ticketDocument::telegramId)
                .limit(1)
                .first()
        }
    }

    inner class update {
        inner class tickets {
            private fun editState(channelId: Long, state: Map<String, Boolean>): UpdateResult {
                return instance.get().getTicketsCollection()
                    .updateOne(
                        ticketDocument::channelId eq channelId,
                        setValue(ticketDocument::status, state)
                    )
            }
            fun closeTicket(ticket: ticketDocument): UpdateResult {
                return editState(
                    ticket.channelId,
                    mapOf(
                        "open" to false, "suspended" to false, "closed" to true
                    )
                )
            }
            fun suspendTicket(ticket: ticketDocument): UpdateResult {
                return editState(
                    ticket.channelId,
                    mapOf(
                        "open" to false, "suspended" to true, "closed" to false
                    )
                )
            }
            fun reopenTicket(ticket: ticketDocument): UpdateResult {
                return editState(
                    ticket.channelId,
                    mapOf(
                        "open" to true, "suspended" to false, "closed" to false
                    )
                )
            }
        }
    }
}