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
            fun searchMessageLinkById(ticketId: Int): MessageLinksDocument? {
                return instance.Get().getMessageLinkCollection()
                    .findOne(MessageLinksDocument::ticket_id eq ticketId)
            }

            fun searchTgMessageByDiscordMessage(ticketId: Int, dsMessageId: Long): Long {
                for (mess in instance.Get().getMessageLinkCollection()
                    .findOne(MessageLinksDocument::ticket_id eq ticketId)!!.messages) {
                    if (mess.ds_message_id == dsMessageId) {
                        return mess.tg_message_id
                    }
                }
                return 0L
            }

            fun searchDsMessageByTelegramMessage(ticketId: Int, tgMessageId: Long): Long {
                for (mess in instance.Get().getMessageLinkCollection()
                    .findOne(MessageLinksDocument::ticket_id eq ticketId)!!.messages) {
                    if (mess.tg_message_id == tgMessageId) {
                        return mess.ds_message_id
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

            fun searchRatingBySpeedRating(speed: Float): RatingDocument? {
                return instance.Get().getRatingsCollection()
                    .findOne(RatingDocument::speedRating eq speed)
            }

            fun searchRatingByGentilezzaRating(gentilezza: Float): RatingDocument? {
                return instance.Get().getRatingsCollection()
                    .findOne(RatingDocument::gentilezzaRating eq gentilezza)
            }

            fun searchRatingByGeneralRating(general: Float): RatingDocument? {
                return instance.Get().getRatingsCollection()
                    .findOne(RatingDocument::generalRating eq general)
            }
        }

        inner class Reasons {
            fun searchReasonDocumentById(ticketId: Int): ReasonsDocument? {
                return instance.Get().getReasonsCollection()
                    .findOne(ReasonsDocument::ticket_id eq ticketId)
            }

            fun searchReasonDocumentByTelegramId(telegramId: Long): ReasonsDocument? {
                return instance.Get().getReasonsCollection()
                    .find(ReasonsDocument::telegram_id eq telegramId)
                    .descendingSort(ReasonsDocument::ticket_id)
                    .first()
            }

            fun searchReasonDocumentsByTelegramId(telegramId: Long): List<ReasonsDocument?> {
                return instance.Get().getReasonsCollection()
                    .find(ReasonsDocument::telegram_id eq telegramId)
                    .descendingSort(ReasonsDocument::ticket_id)
                    .toList()
            }

            fun searchReasonDocumentByReasonId(reasonId: Int): ReasonsDocument? {
                return instance.Get().getReasonsCollection()
                    .find(ReasonsDocument::reason_id eq reasonId)
                    .descendingSort(ReasonsDocument::ticket_id)
                    .first()
            }

            fun searchReasonDocumentsByReasonId(reasonId: Int): List<ReasonsDocument?> {
                return instance.Get().getReasonsCollection()
                    .find(ReasonsDocument::reason_id eq reasonId)
                    .descendingSort(ReasonsDocument::ticket_id)
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

            /*private fun editAssigneewithoutTrack(ticketId: Int, assigneeId: Long): UpdateResult {
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
            }*/
        }

        inner class MessageLinks {
            fun addMessageToMessageLinks(ticketId: Int, messageLinkType: MessageLinkType) {
                val messages = Search().MessageLinks().searchMessageLinkById(ticketId)!!.messages.toMutableList()
                messages.add(messageLinkType)
                instance.Get().getMessageLinkCollection()
                    .updateOne(
                        MessageLinksDocument::ticket_id eq ticketId,
                        setValue(
                            MessageLinksDocument::messages,
                            messages.toList()
                        )
                    )
            }

            fun updateMessageId(ticketId: Int, old_id: Long, new_id: Long) {
                instance.Get().getMessageLinkCollection()
                    .updateOne(
                        "{ticket_id: $ticketId, \"messages.tg_message_id\": $old_id}",
                        "{\$set: {\"messages.\$.tg_message_id\": $new_id}}"
                    )

            }
        }
    }

    inner class Utils {
        fun getLastUsedTicketId(): Int {
            val ticketId = try {
                FindLatest().findLatestTicket()!!
                    .ticketId
            } catch (e: java.lang.NullPointerException) {
                0
            }
            return ticketId
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

        fun isUserUnderage(telegram_id: Long): Boolean {
            for (reason in Search().Reasons().searchReasonDocumentsByTelegramId(telegram_id)) {
                if (reason!!.reason_id == ReasonEnum.OVERAGE.ordinal + 1) {
                    return false
                }
            }
            return true
        }
    }
}