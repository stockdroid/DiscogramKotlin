package me.chicchi7393.discogramRewrite.discord

import it.tdlight.jni.TdApi.Chat
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketDocument
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.restaction.MessageAction
import java.awt.Color
import java.io.File
import java.net.URI

class DsApp private constructor() {
    private val settings = JsonReader().readJsonSettings("settings")!!
    private val dbMan = DatabaseManager.instance

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

    private fun generateTicketEmbed(
        authorName: String,
        authorUrl: String,
        message: String,
        isForced: Boolean,
        isAssigned: Boolean,
        assignedTo: String = "",
        footerStr: String
    ): MessageEmbed {
        return EmbedBuilder()
            .setColor(Color.blue)
            .setTitle("Nuovo ticket!")
            .setAuthor(authorName, authorUrl, "attachment://pic.png")
            .setDescription(message)
            .addField("Forzato?", if (isForced) "Sì" else "No", true)
            .addField("Assegnato a", if (isAssigned) assignedTo else "Nessuno", true)
            .setFooter(footerStr, null)
            .build()
    }

    private fun generateFirstEmbedButtons(channel_link: String, tg_profile: String = "https://google.com"): ActionRow {
        return ActionRow.of(
            listOf(
                Button.link(channel_link, "Apri chat"),
                Button.link(tg_profile, "Apri profilo Telegram")
            )
        )
    }

    private fun generateSecondEmbedButtons(): ActionRow {
        return ActionRow.of(
            listOf(
                Button.success("assign", "Assegna"),
                Button.secondary("suspend", "Sospendi"),
                Button.danger("close", "Chiudi"),
            )
        )
    }

    fun sendStartEmbed(chat: Chat, message: String, ticketId: Int, channel_link: String) {
        val embed = generateTicketEmbed(
            chat.title,
            "https://chicchi7393.xyz/redirectTg.html?id=${chat.id}",
            message,
            isForced = false,
            isAssigned = false,
            footerStr = "${settings.discord["IDPrefix"]}${ticketId - 1}"
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

    fun sendTextMessageToChannel(channel: Long, text: String): MessageAction {
        return dsClient.getChannelById(
            TextChannel::class.java, channel
        )!!.sendMessage(text)
    }

    fun createTicket(chat: Chat, message: String) {
        dsClient.getCategoryById(
            settings.discord["category_id"] as Long
        )!!.createTextChannel(
            "${settings.discord["IDPrefix"]}${dbMan.Utils().getLastUsedTicketId() + 1}"
        ).map {
            dbMan.Create().Tickets().createTicketDocument(
                TicketDocument(
                    chat.id,
                    it.idLong,
                    dbMan.Utils().getLastUsedTicketId() + 1,
                    mapOf("open" to true, "suspended" to false, "closed" to false),
                    System.currentTimeMillis() / 1000
                )
            )
            sendStartEmbed(
                chat,
                message,
                dbMan.Utils().getLastUsedTicketId() + 1,
                "https://discordapp.com/channels/${getGuild().idLong}/${it.idLong}"
            )
        }.queue()
    }

}