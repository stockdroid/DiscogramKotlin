package me.chicchi7393.discogramRewrite.telegram

import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.handlers.TelegramCommandsHandler
import me.chicchi7393.discogramRewrite.handlers.TicketHandlers
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.utils.FindContent
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel


class UpdateHandler(private val tgClient: SimpleTelegramClient) {
    private val settings = JsonReader().readJsonSettings()!!
    private val ticketHandlers = TicketHandlers()
    private val dbMan = DatabaseManager.instance

    private fun ticketIfList(chat: Chat, message: Message): Boolean {
        return (chat.type is ChatTypePrivate
                && chat.id !in (settings.discord["ignoreTGAuthor"] as List<*>)
                && (message.senderId as MessageSenderUser).userId != (settings.telegram["userbotID"] as Number).toLong())
    }

    fun onUpdateChatAction(update: UpdateChatAction) {
        if (update.action is ChatActionTyping) {
            val ticket = dbMan.Search().Tickets().searchTicketDocumentByTelegramId(update.chatId)
            if (ticket != null) {
                val channel = DsApp.client.getChannelById(ThreadChannel::class.java, ticket.channelId)
                channel!!.sendTyping().queue()
            }
        }
    }

    fun onUpdateNewMessage(update: UpdateNewMessage) {
        val findContentClass = FindContent(update.message)
        val text = findContentClass.findText()
        val document = findContentClass.findData()
        if (((System.currentTimeMillis() / 1000) - update.message.date) > 30) {
            return
        }
        tgClient.send(GetChat(update.message.chatId)) {
            val chat = it.get()
            if (chat.id == (settings.telegram["moderatorGroup"] as Number).toLong() && update.message.content is MessageText) {
                // se viene da moderatori
                TelegramCommandsHandler().onSlashCommand(update.message.content.toString())
            }
            if (ticketIfList(chat, update.message)) {
                // se è di testo
                if (update.message.content is MessageText) {
                    // se è privato, non in blacklist e non è se stesso
                    if (dbMan.Utils().searchAlreadyOpen(chat.id) != null || dbMan.Utils()
                            .searchAlreadySuspended(chat.id) != null
                    ) {
                        // se c'è già un ticket all'utente
                        ticketHandlers.sendTextFollowMessage(
                            chat.id,
                            text,
                            dbMan.Utils().searchAlreadySuspended(chat.id) != null,
                            dbMan.Search().Tickets()
                                .searchTicketDocumentByTelegramId((update.message.senderId as MessageSenderUser).userId)!!.ticketId,
                            update.message.id,
                            update.message.replyToMessageId
                        )
                    } else {
                        // avvia nuovo ticket
                        ticketHandlers.startTicketWithText(chat, text)
                    }
                } else {
                    // se è documento
                    val file: DownloadFile? = if (document != 0) DownloadFile(document, 1, 0, 0, true) else null
                    if (dbMan.Utils().searchAlreadyOpen(chat.id) != null || dbMan.Utils()
                            .searchAlreadySuspended(chat.id) != null
                    ) {
                        // se c'è già un ticket all'utente
                        ticketHandlers.sendFileFollowMessage(
                            chat.id,
                            file,
                            text,
                            dbMan.Utils().searchAlreadySuspended(chat.id) != null,
                            dbMan.Search().Tickets()
                                .searchTicketDocumentByTelegramId((update.message.senderId as MessageSenderUser).userId)!!.ticketId,
                            update.message.id,
                            update.message.replyToMessageId
                        )
                    } else {
                        // avvia nuovo ticket
                        ticketHandlers.startTicketWithFile(chat, file, text)
                    }
                }
            }
        }
    }

    fun onUpdateMessageSendSucceeded(update: UpdateMessageSendSucceeded) {
        try {
            dbMan.Update().MessageLinks().updateMessageId(
                dbMan.Search().Tickets().searchTicketDocumentByTelegramId(update.message.chatId)!!.ticketId,
                update.oldMessageId,
                update.message.id
            )
        } catch (_: NullPointerException) {
        }
    }
}