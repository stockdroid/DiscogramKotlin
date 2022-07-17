package me.chicchi7393.discogramRewrite.telegram

import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.utils.FindContent
import me.chicchi7393.discogramRewrite.ticketHandlers.ticketHandler

class UpdateHandler(private val tgClient: SimpleTelegramClient) {
    private val settings = JsonReader().readJsonSettings("settings")!!
    private val ticketHandler = ticketHandler()
    val dbMan = DatabaseManager.instance
    fun authStateUpdate(update: UpdateAuthorizationState) {
        println(
            when (update.authorizationState) {
                is AuthorizationStateReady -> "Logged in"
                is AuthorizationStateLoggingOut -> "Logging out..."
                is AuthorizationStateClosing -> "Closing..."
                is AuthorizationStateClosed -> "Closed"
                else -> ""
            }
        )
    }

    private fun getChat(id: Long): Chat {
        val value = arrayOf<Chat>()
        tgClient.send(GetChat(id)) { value[0] = it.get() }
        return value[0]
    }

    fun ticketIfList(chat: Chat, message: Message): Boolean {
        return (chat.type is ChatTypePrivate
                && chat.id !in settings.discord["ignoreTGAuthor"] as List<Long>
                && (message.senderId as MessageSenderUser).userId != settings.telegram["userbotID"] as Long)
    }

    fun onUpdateNewMessage(update: UpdateNewMessage) {
        val findContentClass = FindContent(update.message)
        val text = findContentClass.findText()
        val document = findContentClass.findData()
        val chat = getChat(update.message.chatId)

        if (update.message.content is MessageText) {
            if (ticketIfList(chat, update.message)) {
                if (dbMan.Utils().searchAlreadyOpen(chat.id) != null)
                    ticketHandler.sendTextFollowMessage(chat.id, text)
                else
                    ticketHandler.startTicketWithText(chat, text)
            }
        } else {
            val file: DownloadFile? = if (document != 0) {
                DownloadFile(document, 1, 0, 0, true)
            } else {
                null
            }
            if (ticketIfList(chat, update.message)) {
                if (dbMan.Utils().searchAlreadyOpen(chat.id) == null)
                    ticketHandler.startTicketWithFile(
                        chat.id,
                        chat,
                        file,
                        text
                    )
                else
                    ticketHandler.sendFileFollowMessage(chat.id, file, text)
            }
        }
    }
}