package me.chicchi7393.discogramRewrite.discord

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter


class EventHandler : ListenerAdapter() {
    private val settings = JsonReader().readJsonSettings("settings")!!
    private val dbMan = DatabaseManager.instance
    private val tgClient = TgApp.instance.client
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            System.out.printf(
                "[PM] %s: %s\n", event.author.name,
                event.message.contentDisplay
            )
        } else {
            if (
                event.channel.name.startsWith(settings.discord["IDPrefix"] as String, true) &&
                !event.author.isBot()
            ) {
                val tgId = dbMan.Search().Tickets().searchTicketDocumentByChannelId(event.channel.idLong)!!.telegramId
                val content: InputMessageContent = InputMessageText(FormattedText(event.message.contentRaw, null), false, true)
                tgClient.send(SendMessage(tgId, 0, 0, null, null, content)) {}
            }
        }
    }
}