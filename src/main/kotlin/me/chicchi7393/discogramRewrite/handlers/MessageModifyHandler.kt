package me.chicchi7393.discogramRewrite.handlers

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.MessageLinkType
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketDocument
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import org.bson.BsonTimestamp

class MessageModifyHandler(val event: GenericMessageEvent) {
    private val settings = JsonReader().readJsonSettings()!!
    private val genStrings = JsonReader().readJsonMessageTable("messageTable")!!.generalStrings
    private val dbMan = DatabaseManager.instance
    private val tgClient = TgApp.instance.client
    private fun sendContent(
        tgId: Long,
        dsId: Long,
        content: InputMessageContent,
        ticket_id: Int,
        reply_id: Long
    ) {
        var tg_reply = 0L
        if (reply_id != 0L) {
            tg_reply = dbMan.Search().MessageLinks().searchTgMessageByDiscordMessage(ticket_id, reply_id)
        }
        tgClient.send(
            SendMessage(tgId, 0, tg_reply, null, null, content)
        ) {
            dbMan.Update().MessageLinks().addMessageToMessageLinks(
                ticket_id,
                MessageLinkType(it.get().id, dsId, BsonTimestamp(System.currentTimeMillis() / 1000))
            )
        }
    }

    fun onMessageDelete(): Boolean {
        val newEvent = event as MessageDeleteEvent
        try {
            val ticket: TicketDocument =
                dbMan.Search().Tickets().searchTicketDocumentByChannelId(event.channel.idLong)
                    ?: return false
            val tgId =
                dbMan.Search().MessageLinks().searchTgMessageByDiscordMessage(ticket.ticketId, newEvent.messageIdLong)
            if (tgId == 0L) {
                return false
            }
            tgClient.send(GetMessage(ticket.telegramId, tgId)) { tgIt ->
                tgClient.send(DeleteMessages(ticket.telegramId, longArrayOf(tgId), true)) {}
                event.channel.sendMessage(genStrings["messageBeenDeleted"]!!).queue {
                    if ((tgIt.get().senderId as MessageSenderUser).userId != (settings.telegram["userbotID"] as Number).toLong()) {
                        sendContent(
                            ticket.telegramId,
                            it.idLong,
                            InputMessageText(FormattedText(genStrings["messageBeenDeleted"]!!, null), false, false),
                            ticket.ticketId,
                            0L
                        )
                    }
                }
            }


        } catch (_: Exception) {
            return false
        }
        return true
    }

    fun onMessageUpdate(): Boolean {
        val newEvent = event as MessageUpdateEvent
        try {
            val ticket: TicketDocument =
                dbMan.Search().Tickets().searchTicketDocumentByChannelId(event.channel.idLong) ?: return false
            val tgId =
                dbMan.Search().MessageLinks().searchTgMessageByDiscordMessage(ticket.ticketId, newEvent.messageIdLong)
            if (tgId == 0L) {
                return false
            }
            tgClient.send(GetMessage(ticket.telegramId, tgId)) {
                if (it.get().content is MessageText) {
                    tgClient.send(
                        EditMessageText(
                            ticket.telegramId,
                            tgId,
                            null,
                            InputMessageText(FormattedText(newEvent.message.contentRaw, null), false, false)
                        )
                    ) {}
                } else {
                    tgClient.send(
                        EditMessageCaption(
                            ticket.telegramId,
                            tgId,
                            null,
                            FormattedText(newEvent.message.contentRaw, null)
                        )
                    ) {}
                }

            }
        } catch (_: Exception) {
            return false
        }
        return true
    }
}