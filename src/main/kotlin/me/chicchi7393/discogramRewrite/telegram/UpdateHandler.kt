package me.chicchi7393.discogramRewrite.telegram
import discord4j.common.util.Snowflake
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import discord4j.core.spec.MessageCreateSpec
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi
import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import java.io.FileInputStream
import java.net.URI

class UpdateHandler(val tgClient: SimpleTelegramClient) {
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

		val text: String
		if (messageContent is MessageText) {
			text = messageContent.text.text
		} else {
			text = String.format("(%s)", update.message)
		}

		tgClient.send(GetChat(update.message.chatId)) { chatIdResult ->
			// Get the chat response
			val dsClass = DsApp.instance
			val chat = chatIdResult.get()
			if (chat.type is ChatTypePrivate && chat.id !in settings.discord["ignoreTGAuthor"] as List<Long>) {
				val embed = dsClass.generateTicketEmbed(
					chat.title,
					"https://chicchi7393.xyz/redirectTg.html?id=${chat.id}",
					text,
					false,
					false,
					footerStr = "TCK-1"
				)


				dsClass.dsClient
					.getChannelById(Snowflake.of(settings.discord["channel_id"] as Long))
					.createMessage(
						MessageCreateSpec.builder()
							.addEmbed(embed)
							.addComponent(
								dsClass.generateFirstEmbedButtons("", "https://chicchi7393.xyz/redirectTg.html?id=${chat.id}")
							)
							.addComponent(
								dsClass.generateSecondEmbedButtons()
							)
							.addComponent(
								ActionRow.of(
									Button.primary("menu", "Apri menu")
								)
							)
							.addFile("pic.png", FileInputStream(java.io.File(URI("file://${chat.photo.big.local.path}"))))
							.build()
							.asRequest()
					)
					.subscribe()
			}
		}
	}
}