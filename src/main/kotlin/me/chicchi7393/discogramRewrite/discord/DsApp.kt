package me.chicchi7393.discogramRewrite.discord

import it.tdlight.jni.TdApi
import it.tdlight.jni.TdApi.Chat
import it.tdlight.jni.TdApi.InputMessageText
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketDocument
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketState
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.restaction.MessageAction
import java.awt.Color
import java.io.File
import java.io.FileInputStream


class DsApp private constructor() {
    private val settings = JsonReader().readJsonSettings("settings")!!
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val embedStrs = messTable.embed
    private val commStrs = messTable.commands
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
            .setActivity(Activity.watching(messTable.generalStrings["bot_activity"]!!))
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
            .setTitle(embedStrs["embed_newTicketTitle"]!!)
            .setAuthor(authorName, authorUrl, "attachment://pic.png")
            .setDescription(message)
            .addField(embedStrs["embed_forced"]!!, if (isForced) embedStrs["embed_yes"]!! else embedStrs["embed_no"]!!, true)
            .addField(embedStrs["embed_assignedTo"]!!, if (isAssigned) assignedTo else embedStrs["embed_noOne"]!!, true)
            .addField(embedStrs["embed_state"]!!, state.toString(), false)
            .setFooter(footerStr, null)
            .build()
    }

    fun generateFirstEmbedButtons(tg_profile: String = "https://google.com"): ActionRow {
        return ActionRow.of(
            listOf(
                Button.link(tg_profile, embedStrs["button_openTg"]!!)
            )
        )
    }

    fun generateSecondEmbedButtons(channel_id: Long): ActionRow {
        return ActionRow.of(
            listOf(
                Button.success("assign-$channel_id", embedStrs["button_assign"]!!),
                Button.secondary("suspend-$channel_id", embedStrs["button_suspend"]!!),
                Button.danger("close-$channel_id", embedStrs["button_close"]!!),
            )
        )
    }

    private fun getLastModified(directoryFilePath: String): FileInputStream {
        val directory = File(directoryFilePath)
        val files = directory.listFiles { obj: File -> obj.isFile }
        var lastModifiedTime = Long.MIN_VALUE
        var chosenFile: File? = null
        if (files != null) {
            for (file in files) {
                if (file.lastModified() > lastModifiedTime) {
                    chosenFile = file
                    lastModifiedTime = file.lastModified()
                }
            }
        }
        return FileInputStream(chosenFile)
    }

    fun sendTextMessageToChannel(channel: Long, text: String, reply_id: Long, ticket_id: Int): MessageAction {
        var ds_reply = 0L
        if (reply_id != 0L) {
            ds_reply = dbMan.Search().MessageLinks().searchDsMessageByTelegramMessage(ticket_id, reply_id)
        }
        return dsClient.getThreadChannelById(
            channel
        )!!.sendMessage(text).referenceById(ds_reply)
    }

    fun createTicket(chat: Chat, message: String) {
        TgApp.instance.client.send(
            TdApi.SendMessage(chat.id, 0, 0, null, null, InputMessageText(TdApi.FormattedText(messTable.generalStrings["welcome"] as String, null), false, false))
        ) {}
        File("session/database/profile_photos").deleteRecursively()
        val pfpId = try {
            chat.photo.small.id
        } catch (_: NullPointerException) {
            69420
        }
        tgApp.downloadFile(pfpId)

        val filePath =
            if (pfpId != 69420) getLastModified("session/database/profile_photos") else FileInputStream("./session/database/profile_photos/5900.jpg")

        val embed = generateTicketEmbed(
            chat.title,
            embedStrs["tgRedirectPrefixLink"]!!+chat.id.toString(),
            message,
            isForced = false,
            isAssigned = false,
            footerStr = "${settings.discord["idPrefix"]}${dbMan.Utils().getLastUsedTicketId() + 1}",
            state = TicketState.OPEN
        )
        dsClient
            .getChannelById(MessageChannel::class.java, settings.discord["channel_id"] as Long)!!
            .sendMessageEmbeds(
                embed
            ).addFile(filePath, "pic.png").map {
                it.editMessageEmbeds(
                    embed
                ).setActionRows(
                    generateFirstEmbedButtons(
                        embedStrs["tgRedirectPrefixLink"]!!+chat.id.toString()
                    ),
                    generateSecondEmbedButtons(it.idLong),
                    ActionRow.of(
                        Button.primary("menu-${it.id}", "Apri menu")
                    )
                ).queue()
                Thread.sleep(500)
                val tIt = it.createThreadChannel(
                    "${settings.discord["idPrefix"]}${dbMan.Utils().getLastUsedTicketId() + 1}"
                ).complete(true)
                Thread.sleep(500)
                dbMan.Create().Tickets().createTicketDocument(
                        TicketDocument(
                            chat.id,
                            tIt.idLong,
                            dbMan.Utils().getLastUsedTicketId() + 1,
                            mapOf("open" to true, "suspended" to false, "closed" to false),
                            System.currentTimeMillis() / 1000
                        )
                    )
            }
            .queue()
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

    fun createCommands() {
        dsClient.updateCommands().addCommands(
            Commands.slash(commStrs["tickets"]!!["name"]!!, commStrs["tickets"]!!["description"]!!)
                .addOption(OptionType.STRING, commStrs["tickets"]!!["option_1_name"]!!, commStrs["tickets"]!!["option_1_description"]!!, true),
            Commands.slash(commStrs["cronologia"]!!["name"]!!, commStrs["cronologia"]!!["description"]!!)
                .addOption(OptionType.STRING, commStrs["cronologia"]!!["option_1_name"]!!, commStrs["cronologia"]!!["option_1_description"]!!, true)
                .addOption(OptionType.INTEGER, commStrs["cronologia"]!!["option_2_name"]!!, commStrs["cronologia"]!!["option_2_description"]!!, false),
            Commands.slash(commStrs["block"]!!["name"]!!, commStrs["block"]!!["description"]!!)
                .addOption(
                    OptionType.STRING,
                    commStrs["block"]!!["option_1_name"]!!,
                    commStrs["block"]!!["option_1_description"]!!,
                    false
                ),
            Commands.slash(commStrs["unblock"]!!["name"]!!, commStrs["unblock"]!!["description"]!!)
                .addOption(
                    OptionType.STRING,
                    commStrs["unblock"]!!["option_1_name"]!!,
                    commStrs["unblock"]!!["option_1_description"]!!,
                    false
                ),
            Commands.message(commStrs["delete_message"]!!["name"]!!)
        ).queue()
    }

}