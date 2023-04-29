package me.chicchi7393.discogramRewrite.mongoDB

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.result.UpdateResult
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.objects.databaseObjects.*
import me.chicchi7393.discogramRewrite.objects.enums.ReasonEnum
import org.bson.BsonValue
import org.litote.kmongo.*


class DatabaseManager {
    private val settings = JsonReader().readJsonSettings()!!

    private object GetInstance {
        val INSTANCE = DatabaseManager()
    }

    companion object {
        val instance: DatabaseManager by lazy { GetInstance.INSTANCE }
        var newTick = -1
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
                createClient().getDatabase(settings.mongodb["database"]!!)
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

        fun getRatingsCollection(): MongoCollection<RatingDocument> {
            return getDB()
                .getCollection<RatingDocument>("ratings")
        }

        fun getReasonsCollection(): MongoCollection<ReasonsDocument> {
            return getDB()
                .getCollection<ReasonsDocument>("reasons")
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
                instance.Create().MessageLink().createMessageLinkDocument(
                    MessageLinksDocument(
                        ticketDocument.ticketId,
                        listOf()
                    )
                )
                newTick = ticketDocument.ticketId
                return instance.Get().getTicketsCollection()
                    .insertOne(ticketDocument)
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

        inner class Ratings {
            fun createRatingDocument(
                ratingDocument: RatingDocument
            ): BsonValue? {
                return instance.Get().getRatingsCollection()
                    .insertOne(ratingDocument)
                    .insertedId
            }
        }

        inner class Reasons {
            fun createReasonsDocument(
                reasonsDocument: ReasonsDocument
            ): BsonValue? {
                return instance.Get().getReasonsCollection()
                    .insertOne(reasonsDocument)
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

            fun searchTicketDocumentsByTelegramId(telegramId: Long): List<TicketDocument?> {
                return instance.Get().getTicketsCollection()
                    .find(TicketDocument::telegramId eq telegramId)
                    .descendingSort(TicketDocument::unixSeconds)
                    .toList()
            }
        }

        inner class Assignee {
            fun searchAssigneeDocumentById(ticketId: Int): AssigneeDocument? {
                return instance.Get().getAssigneesCollection()
                    .findOne(AssigneeDocument::ticketId eq ticketId)
            }
        }

        inner class MessageLinks {
            private fun looperThruMessages(ticketIdM: Int): List<MessageLinkType> {
                return instance.Get().getMessageLinkCollection()
                    .findOne(MessageLinksDocument::ticketId eq ticketIdM)!!.messages
            }

            fun searchMessageLinkById(ticketIdM: Int): MessageLinksDocument? {
                return instance.Get().getMessageLinkCollection()
                    .findOne(MessageLinksDocument::ticketId eq ticketIdM)
            }

            fun searchMessageByOtherMessage(
                ticketId: Int,
                dsMessageId: Long = 0,
                returnsDs: Boolean,
                tgMessageId: Long = 0
            ): Long {
                for (mess in looperThruMessages(ticketId)) {
                    if (mess.tgMessageId == tgMessageId && returnsDs) {
                        return mess.dsMessageId
                    } else if (mess.dsMessageId == dsMessageId && !returnsDs) {
                        return mess.tgMessageId
                    }
                }
                return 0L
            }
        }

        inner class Ratings {
            fun searchRatingById(ticketId: Int): RatingDocument? {
                return instance.Get().getRatingsCollection()
                    .findOne(RatingDocument::id eq ticketId)
            }
        }

        inner class Reasons {
            fun searchReasonDocumentsByTelegramId(telegramIdM: Long): List<ReasonsDocument?> {
                return instance.Get().getReasonsCollection()
                    .find(ReasonsDocument::telegramId eq telegramIdM)
                    .descendingSort(ReasonsDocument::ticketId)
                    .toList()
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
                )
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
        }

        inner class MessageLinks {
            fun addMessageToMessageLinks(ticketIdM: Int, messageLinkType: MessageLinkType) {
                val messages = Search().MessageLinks().searchMessageLinkById(ticketIdM)!!.messages.toMutableList()
                messages.add(messageLinkType)
                instance.Get().getMessageLinkCollection()
                    .updateOne(
                        MessageLinksDocument::ticketId eq ticketIdM,
                        setValue(
                            MessageLinksDocument::messages,
                            messages.toList()
                        )
                    )
            }

            fun updateMessageId(ticketId: Int, oldId: Long, newId: Long) {
                instance.Get().getMessageLinkCollection().updateOne(
                    "{ \"ticketId\": $ticketId, \"messages.tgMessageId\": $oldId }",
                    "{\$set: {\"messages.$.tgMessageId\": $newId}}"
                )
            }
        }
    }

    inner class Utils {
        fun getLastUsedTicketId(): Int {

            newTick = try {
                if (newTick == -1) {
                    FindLatest().findLatestTicket()!!
                        .ticketId
                } else {
                    newTick
                }
            } catch (e: java.lang.NullPointerException) {
                0
            }
            return newTick
        }

        fun searchAlreadyOpen(telegramId: Long): TicketDocument? {
            try {
                for (userTicket in this@DatabaseManager
                    .Get()
                    .getTicketsCollection()
                    .find(TicketDocument::telegramId eq telegramId)) {
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

        fun searchAlreadySuspended(telegramId: Long): TicketDocument? {
            try {
                for (userTicket in this@DatabaseManager
                    .Get()
                    .getTicketsCollection()
                    .find(TicketDocument::telegramId eq telegramId)) {
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

        fun isUserUnderage(telegramId: Long): Boolean {
            var foundOverage = false
            var isUnderage = false
            for (reason in Search().Reasons().searchReasonDocumentsByTelegramId(telegramId)) {
                if (reason!!.reasonId == ReasonEnum.OVERAGE.ordinal + 1) {
                    isUnderage = false
                    foundOverage = true
                }
                if (reason.reasonId == ReasonEnum.UNDERAGE.ordinal + 1) {
                    if (!foundOverage) {
                        isUnderage = true
                    }
                }
            }
            return isUnderage
        }
    }
}