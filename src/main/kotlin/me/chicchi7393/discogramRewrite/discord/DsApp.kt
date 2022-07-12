package me.chicchi7393.discogramRewrite.discord

import it.tdlight.jni.TdApi.Chat
import me.chicchi7393.discogramRewrite.JsonReader
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import java.awt.Color
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.time.Instant
import java.time.temporal.TemporalAccessor

class DsApp private constructor() {
    private val settings = JsonReader().readJsonSettings("settings")!!

    init {
        println("DsApp Class Initialized")
    }

    private object GetInstance {
        val INSTANCE = DsApp()
    }

    companion object {
        val instance: DsApp by lazy { GetInstance.INSTANCE }
    }

    lateinit var dsClient: JDA

    fun createApp(): JDA {
        dsClient = JDABuilder.createDefault(settings.discord["token"] as String)
            .setActivity(Activity.watching("i ban degli underage"))
            .addEventListeners(EventHandler())
            .build()
        return dsClient
    }
    fun generateTicketEmbed(
            authorName: String,
            authorUrl: String,
            message: String,
            isForced: Boolean,
            isAssigned: Boolean,
            assignedTo: String = "Nessuno",
            footerStr: String
    ): MessageEmbed {
        return EmbedBuilder()
            .setColor(Color.blue)
            .setTitle("Nuovo ticket!")
            .setAuthor(authorName, authorUrl, "attachment://pic.png")
            .setDescription(message)
            .addField("Forzato?", if (isForced) "Sì" else "No", true)
            .addField("Assegnato a", assignedTo, true)
            .setFooter(footerStr, null)
            .build()
    }
    fun generateFirstEmbedButtons(channel_link: String, tg_profile: String = "https://google.com"): ActionRow {
        return ActionRow.of(listOf(
            Button.link(channel_link, "Apri chat"),
            Button.link(tg_profile, "Apri profilo Telegram")
        ))
    }
    fun generateSecondEmbedButtons(): ActionRow {
        return ActionRow.of(listOf(
            Button.success("assign", "Assegna"),
            Button.secondary("suspend", "Sospendi"),
            Button.danger("close", "Chiudi"),
        ))
    }

    fun sendStartEmbed(chat: Chat, message: String, ticketId: Int, channel_link: String) {
        val embed = generateTicketEmbed(
            chat.title,
            "https://chicchi7393.xyz/redirectTg.html?id=${chat.id}",
            message,
            false,
            false,
            footerStr = "${settings.discord["IDPrefix"]}${ticketId-1}"
        )
        dsClient
            .getChannelById(MessageChannel::class.java, settings.discord["channel_id"] as Long)!!
            .sendMessageEmbeds(
                embed
            )
            .setActionRows(
                generateFirstEmbedButtons(channel_link, "https://chicchi7393.xyz/redirectTg.html?id=${chat.id}"),
                generateSecondEmbedButtons(),
                ActionRow.of(
                        Button.primary("menu", "Apri menu")
                )
            )
            .addFile(File(URI("file://${chat.photo.big.local.path}")), "pic.png")
            .queue()
    }
    fun getGuild(): Guild {
        return dsClient.getGuildById(settings.discord["guild_id"] as Long)!!
    }

}