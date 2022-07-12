package me.chicchi7393.discogramRewrite.telegram
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.ticketDocument
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import java.util.*

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
		val dbMan = DatabaseManager.instance
		val text: String =
			if (messageContent is MessageText) messageContent.text.text else String.format("(%s)", update.message)

		tgClient.send(GetChat(update.message.chatId)) { chatIdResult ->
			val dsClass = DsApp.instance
			val message = update.message
			val chat = chatIdResult.get()
			if (chat.type is ChatTypePrivate && chat.id !in settings.discord["ignoreTGAuthor"] as List<Long> && (message.senderId as MessageSenderUser).userId != settings.telegram["userbotID"] as Long) {
				if (dbMan.Utils().searchAlreadyOpen(chat.id) != null) {
					dsClass.dsClient
						.getChannelById(
							TextChannel::class.java,
							dbMan.Utils().searchAlreadyOpen(chat.id)!!.channelId
						)!!
						.sendMessage(text)
						.queue()
				} else {
					dsClass.dsClient
						.getCategoryById(
							settings.discord["category_id"] as Long
						)!!
						.createTextChannel(
							"${settings.discord["IDPrefix"]}${dbMan.Utils().getLastUsedTicketId() + 1}"
						)
						.map {
							dbMan.Create().Tickets().createTicketDocument(
								ticketDocument(
									chat.id,
									it.idLong,
									dbMan.Utils().getLastUsedTicketId() + 1,
									mapOf("open" to true, "suspended" to false, "closed" to false),
									System.currentTimeMillis() / 1000
								)
							)
							dsClass.sendStartEmbed(
								chat,
								text,
								dbMan.Utils().getLastUsedTicketId() + 1,
								"https://discordapp.com/channels/${dsClass.getGuild().idLong}/${it.idLong}"
							)
						}
						.queue()
				}
			}
		}
	}
}