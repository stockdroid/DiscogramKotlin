package me.chicchi7393.discogramRewrite.handlers

import it.tdlight.jni.TdApi
import it.tdlight.jni.TdApi.DeleteMessages
import it.tdlight.jni.TdApi.EditMessageCaption
import it.tdlight.jni.TdApi.EditMessageText
import it.tdlight.jni.TdApi.GetMessage
import it.tdlight.jni.TdApi.InputMessageContent
import it.tdlight.jni.TdApi.InputMessageText
import it.tdlight.jni.TdApi.MessageText
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketDocument
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent

class messageModifyHandler(val event: GenericMessageEvent) {
    private val dbMan = DatabaseManager.instance
    private val tgClient = TgApp.instance.client
    fun onMessageDelete(): Boolean {
        val newEvent = event as MessageDeleteEvent
        try {
            val ticket: TicketDocument? = dbMan.Search().Tickets().searchTicketDocumentByChannelId(event.threadChannel.idLong)
            if (ticket == null) {return false}
            val tgId = dbMan.Search().MessageLinks().searchTgMessageByDiscordMessage(ticket.ticketId, newEvent.messageIdLong)
            if (tgId == null) {return false}
            tgClient.send(DeleteMessages(ticket.telegramId, longArrayOf(tgId), true)) {}
        } catch (_: Exception) {return false}
        return true
    }
    fun onMessageUpdate(): Boolean {
        val newEvent = event as MessageUpdateEvent
        try {
            val ticket: TicketDocument? =
                dbMan.Search().Tickets().searchTicketDocumentByChannelId(event.threadChannel.idLong)
            if (ticket == null) {
                return false
            }
            val tgId =
                dbMan.Search().MessageLinks().searchTgMessageByDiscordMessage(ticket.ticketId, newEvent.messageIdLong)
            if (tgId == null) {
                return false
            }
            tgClient.send(GetMessage(ticket.telegramId, tgId)) {
                if (it.get().content is MessageText) {
                    tgClient.send(
                        EditMessageText(
                            ticket.telegramId,
                            tgId,
                            null,
                            InputMessageText(TdApi.FormattedText(newEvent.message.contentRaw, null), false, false)
                        )
                    ) {}
                } else {
                    tgClient.send(
                        EditMessageCaption(
                            ticket.telegramId,
                            tgId,
                            null,
                            TdApi.FormattedText(newEvent.message.contentRaw, null)
                        )
                    ) {}
                }

            }
        } catch (_: Exception) {return false}
        return true
    }
}