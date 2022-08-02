package me.chicchi7393.discogramRewrite.discord

import it.tdlight.jni.TdApi.Chat
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketDocument
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketState
import me.chicchi7393.discogramRewrite.telegram.TgApp
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
    private val tgApp = TgApp.instance

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
        assignedTo: String = "",
        footerStr: String,
        state: Any
    ): MessageEmbed {
        return EmbedBuilder()
            .setColor(Color.blue)
            .setTitle("Nuovo ticket!")
            .setAuthor(authorName, authorUrl, "attachment://pic.png")
            .setDescription(message)
            .addField("Forzato?", if (isForced) "Sì" else "No", true)
            .addField("Assegnato a", if (isAssigned) assignedTo else "Nessuno", true)
            .addField("Stato", state.toString(), false)
            .setFooter(footerStr, null)
            .build()
    }

    fun generateFirstEmbedButtons(channel_link: String, tg_profile: String = "https://google.com"): ActionRow {
        return ActionRow.of(
            listOf(
                Button.link(channel_link, "Apri chat"),
                Button.link(tg_profile, "Apri profilo Telegram")
            )
        )
    }

    private fun generateSecondEmbedButtons(channel_id: Long): ActionRow {
        return ActionRow.of(
            listOf(
                Button.success("assign-$channel_id", "Assegna"),
                Button.secondary("suspend-$channel_id", "Sospendi"),
                Button.danger("close-$channel_id", "Chiudi"),
            )
        )
    }


    fun sendStartEmbed(chat: Chat, message: String, ticketId: Int, channel_id: Long, pathImage: String) {
        val embed = generateTicketEmbed(
            chat.title,
            "https://chicchi7393.xyz/redirectTg.html?id=${chat.id}",
            message,
            isForced = false,
            isAssigned = false,
            footerStr = "${settings.discord["IDPrefix"]}${ticketId - 1}",
            state = TicketState.OPEN
        )
        dsClient
            .getChannelById(MessageChannel::class.java, settings.discord["channel_id"] as Long)!!
            .sendMessageEmbeds(
                embed
            )
            .setActionRows(
                generateFirstEmbedButtons(
                    "https://discordapp.com/channels/${getGuild().idLong}/${channel_id}",
                    "https://chicchi7393.xyz/redirectTg.html?id=${chat.id}"
                ),
                generateSecondEmbedButtons(channel_id),
                ActionRow.of(
                    Button.primary("menu-$channel_id", "Apri menu")
                )
            )
            .addFile(File(URI("file://${pathImage}")), "pic.png")
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
            while (true) {
                val filePath = tgApp.downloadFile(chat.photo.small.id)
                if (filePath[0] == "") {
                    Thread.sleep(100)
                    continue
                } else {
                    sendStartEmbed(
                        chat,
                        message,
                        dbMan.Utils().getLastUsedTicketId() + 1,
                        it.idLong,
                        filePath[0]
                    )
                    break
                }
            }
        }.queue()
    }

    fun isHigherRole(member: Member): Boolean {
        var isHigher = false
        for (role in member.roles) {
            if (role.idLong in settings.discord["higher_roles"] as List<Long>) {
                isHigher = true
            }
        }
        return isHigher
    }

}