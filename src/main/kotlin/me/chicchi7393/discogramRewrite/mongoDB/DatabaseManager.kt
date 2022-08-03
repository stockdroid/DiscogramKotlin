package me.chicchi7393.discogramRewrite.mongoDB

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.result.UpdateResult
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.objects.databaseObjects.AssigneeDocument
import me.chicchi7393.discogramRewrite.objects.databaseObjects.MessageLinksDocument
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketDocument
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
        private fun getDB(): MongoDatabase {
            return if (this@DatabaseManager::mongoClient.isInitialized) {
                mongoClient.getDatabase(settings.mongodb["database"]!!)
            } else {
                createClient().getDatabase(settings.mongodb["database"])
            }
        }

        fun getTicketsCollection(): MongoCollection<TicketDocument> {
            return getDB()
                .getCollection<TicketDocument>("tickets")
        }

        fun getAssigneesCollection(): MongoCollection<AssigneeDocument> {
            return getDB()
                .getCollection<AssigneeDocument>("assignees")
        }

        fun getMessageLinkCollection(): MongoCollection<MessageLinksDocument> {
            return getDB()
                .getCollection<MessageLinksDocument>("messageLinks")
        }
    }

    inner class Create {
        inner class Tickets {
            fun createTicketDocument(
                ticketDocument: TicketDocument
            ): BsonValue? {
                instance.Get().getAssigneesCollection()
                    .insertOne(
                        AssigneeDocument(
                            ticketDocument.ticketId,
                            0,
                            arrayListOf()
                        )
                    )
                return instance.Get().getTicketsCollection()
                    .insertOne(ticketDocument)
                    .insertedId
            }
        }

        inner class Assignee {
            fun createAssigneeDocument(
                assigneeDocument: AssigneeDocument
            ): BsonValue? {
                return instance.Get().getAssigneesCollection()
                    .insertOne(assigneeDocument)
                    .insertedId
            }
        }

        inner class MessageLink {
            fun createMessageLinkDocument(
                messageLinkDocument: MessageLinksDocument
            ): BsonValue? {
                return instance.Get().getMessageLinkCollection()
                    .insertOne(messageLinkDocument)
                    .insertedId
            }
        }
    }

    inner class Search {
        inner class Tickets {
            fun searchTicketDocumentById(ticketId: Int): TicketDocument? {
                return instance.Get().getTicketsCollection()
                    .findOne(TicketDocument::ticketId eq ticketId)
            }

            fun searchTicketDocumentByChannelId(channelId: Long): TicketDocument? {
                return instance.Get().getTicketsCollection()
                    .findOne(TicketDocument::channelId eq channelId)
            }

            fun getTgIdByChannelId(channelId: Long): Long {
                return try {
                    searchTicketDocumentByChannelId(channelId)!!.telegramId
                } catch (e: NullPointerException) {
                    0
                }
            }

            fun searchTicketDocumentByTelegramId(telegramId: Long): TicketDocument? {
                return instance.Get().getTicketsCollection()
                    .find(TicketDocument::telegramId eq telegramId)
                    .descendingSort(TicketDocument::unixSeconds)
                    .first()
            }
        }

        inner class Assignee {
            fun searchAssigneeDocumentById(ticketId: Int): AssigneeDocument? {
                return instance.Get().getAssigneesCollection()
                    .findOne(AssigneeDocument::ticketId eq ticketId)
            }

            fun searchAssigneeDocumentByAssigneeId(assigneeId: Long): AssigneeDocument? {
                return instance.Get().getAssigneesCollection()
                    .findOne(AssigneeDocument::modId eq assigneeId)
            }

        }

        inner class MessageLinks {
            fun searchMessageLinkById(ticketId: Int): MessageLinksDocument? {
                return instance.Get().getMessageLinkCollection()
                    .findOne(MessageLinksDocument::ticket_id eq ticketId)
            }

            fun searchTgMessageByDiscordMessage(tgMessageId: Long): Long {
                TODO()
            }

            fun searchDsMessageByTelegramMessage(tgMessageId: Long): Long {
                TODO()
            }
        }
    }

    inner class FindLatest {
        fun findLatestTicket(): TicketDocument? {
            return instance.Get().getTicketsCollection()
                .find()
                .descendingSort(TicketDocument::ticketId)
                .limit(1)
                .first()
        }
    }

    inner class Update {
        inner class Tickets {
            private fun editState(channelId: Long, state: Map<String, Boolean>): UpdateResult {
                return instance.Get().getTicketsCollection()
                    .updateOne(
                        TicketDocument::channelId eq channelId,
                        setValue(TicketDocument::status, state)
                    )
            }

            fun closeTicket(ticket: TicketDocument): UpdateResult {
                return editState(
                    ticket.channelId,
                    mapOf(
                        "open" to false, "suspended" to false, "closed" to true
                    )
                )
            }

            fun suspendTicket(ticket: TicketDocument): UpdateResult {
                return editState(
                    ticket.channelId,
                    mapOf(
                        "open" to false, "suspended" to true, "closed" to false
                    )
                )
            }

            fun reopenTicket(ticket: TicketDocument): UpdateResult {
                return editState(
                    ticket.channelId,
                    mapOf(
                        "open" to true, "suspended" to false, "closed" to false
                    )
                )
            }
        }

        inner class Assignees {
            fun editAssignee(ticketId: Int, assigneeId: Long): UpdateResult {
                val assigneeDocument = instance.Get().getAssigneesCollection()
                    .findOne(AssigneeDocument::ticketId eq ticketId)!!
                val previousAssignees = assigneeDocument.previousAssignees.toMutableList()
                if (assigneeDocument.previousAssignees.isNotEmpty()) previousAssignees.add(
                    assigneeDocument.modId
                ) else null
                instance.Get().getAssigneesCollection()
                    .updateOne(
                        AssigneeDocument::ticketId eq ticketId,
                        setValue(
                            AssigneeDocument::previousAssignees,
                            previousAssignees.toList()
                        )
                    )
                return instance.Get().getAssigneesCollection()
                    .updateOne(
                        AssigneeDocument::ticketId eq ticketId,
                        setValue(AssigneeDocument::modId, assigneeId)
                    )
            }

            private fun editAssigneewithoutTrack(ticketId: Int, assigneeId: Long): UpdateResult {
                return instance.Get().getAssigneesCollection()
                    .updateOne(
                        AssigneeDocument::ticketId eq ticketId,
                        setValue(AssigneeDocument::modId, assigneeId)
                    )
            }

            fun cleanAssignee(ticket: AssigneeDocument): UpdateResult {
                return editAssigneewithoutTrack(
                    ticket.ticketId,
                    0
                )
            }
        }
    }

    inner class Utils {
        fun getLastUsedTicketId(): Int {
            return try {
                FindLatest().findLatestTicket()!!
                    .ticketId
            } catch (e: java.lang.NullPointerException) {
                0
            }
        }

        fun searchAlreadyOpen(telegram_id: Long): TicketDocument? {
            try {
                for (userTicket in this@DatabaseManager
                    .Get()
                    .getTicketsCollection()
                    .find(TicketDocument::telegramId eq telegram_id)) {
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

        fun searchAlreadySuspended(telegram_id: Long): TicketDocument? {
            try {
                for (userTicket in this@DatabaseManager
                    .Get()
                    .getTicketsCollection()
                    .find(TicketDocument::telegramId eq telegram_id)) {
                    if (userTicket.status["suspended"] == true) {
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