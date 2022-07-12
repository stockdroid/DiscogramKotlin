package me.chicchi7393.discogramRewrite.mongoDB
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.result.UpdateResult
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.objects.databaseObjects.ticketDocument
import org.bson.BsonValue
import org.litote.kmongo.*

class DatabaseManager {
    private val settings = JsonReader().readJsonSettings("settings")!!

    init {
        println("Mongo Class Initialized")
    }

    private object GetInstance {
        val INSTANCE = DatabaseManager()
    }

    companion object {
        val instance: DatabaseManager by lazy { GetInstance.INSTANCE }
    }

    lateinit var mongoClient: MongoClient

    fun createClient(): MongoClient {
        mongoClient = KMongo.createClient(settings.mongodb["connection_string"]!!)
        return mongoClient
    }

    inner class Get {
        fun getDB(): MongoDatabase {
            return if (this@DatabaseManager::mongoClient.isInitialized) {
                mongoClient.getDatabase(settings.mongodb["database"]!!)
            } else {
                createClient().getDatabase(settings.mongodb["database"])
            }
        }

        fun getTicketsCollection(): MongoCollection<ticketDocument> {
            return getDB()
                .getCollection<ticketDocument>("tickets")
        }
    }

    inner class Create {
        inner class Tickets {
            fun createTicketDocument(
                ticketDocument: ticketDocument
            ): BsonValue? {
                return DatabaseManager.instance.Get().getTicketsCollection()
                    .insertOne(ticketDocument)
                    .insertedId
            }
            fun createManyTicketDocuments(
                ticketDocuments: List<ticketDocument>
            ): MutableMap<Int, BsonValue> {
                return DatabaseManager.instance.Get().getTicketsCollection()
                    .insertMany(ticketDocuments)
                    .insertedIds
            }
        }
    }

    inner class Search {
        inner class Tickets {
            fun searchTicketDocumentById(ticketId: Int): ticketDocument? {
                return DatabaseManager.instance.Get().getTicketsCollection()
                    .findOne(ticketDocument::ticketId eq ticketId)
            }

            fun searchTicketDocumentByChannelId(channelId: Long): ticketDocument? {
                return DatabaseManager.instance.Get().getTicketsCollection()
                    .findOne(ticketDocument::channelId eq channelId)
            }

            fun searchTicketDocumentByTelegramId(telegramId: Long): ticketDocument? {
                return DatabaseManager.instance.Get().getTicketsCollection()
                    .findOne(ticketDocument::telegramId eq telegramId)
            }
        }
    }

    inner class FindLatest {
        fun findLatestTicket(): ticketDocument? {
            return DatabaseManager.instance.Get().getTicketsCollection()
                .find()
                .descendingSort(ticketDocument::ticketId)
                .limit(1)
                .first()
        }
    }

    inner class Update {
        inner class Tickets {
            private fun editState(channelId: Long, state: Map<String, Boolean>): UpdateResult {
                return instance.Get().getTicketsCollection()
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

    inner class Utils {
        fun getLastUsedTicketId(): Int {
            return try {
                FindLatest().findLatestTicket()!!
                    .ticketId
            } catch(e: java.lang.NullPointerException) {
                0
            }
        }
        fun searchAlreadyOpen(telegram_id: Long): ticketDocument? {
            try {
                for (userTicket in this@DatabaseManager
                    .Get()
                    .getTicketsCollection()
                    .find(ticketDocument::telegramId eq telegram_id)) {
                    if (userTicket.status["open"] == true) {
                        return userTicket
                    } else {
                        continue
                    }
                }
            } catch (e: java.lang.NullPointerException) {
                return null
            }
            return null
        }
    }
}