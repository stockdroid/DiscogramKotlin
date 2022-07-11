package me.chicchi7393.discogramRewrite.telegram
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.databaseManager

class UpdateHandler(private val tgClient: SimpleTelegramClient) {
	private val settings = JsonReader().readJsonSettings("settings")!!
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
    fun onUpdateNewMessage(update: UpdateNewMessage) {
		val messageContent = update.message.content
		val dbMan = databaseManager.instance
		val text: String = if (messageContent is MessageText) messageContent.text.text else String.format("(%s)", update.message)

		tgClient.send(GetChat(update.message.chatId)) { chatIdResult ->
			val dsClass = DsApp.instance
			val chat = chatIdResult.get()
			if (chat.type is ChatTypePrivate && chat.id !in settings.discord["ignoreTGAuthor"] as List<Long>) {
				dsClass.sendStartEmbed(chat, text, dbMan.utils().getLastUsedTicketId()+1)

			}
		}
	}
}