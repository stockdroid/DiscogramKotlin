package me.chicchi7393.discogramRewrite.discord

import it.tdlight.jni.TdApi
import it.tdlight.jni.TdApi.Chat
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketDocument
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketState
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.utils.FileUpload
import java.awt.Color
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import kotlin.io.path.Path


object DsApp {
    private val settings = JsonReader().readJsonSettings()!!
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val embedStrs = messTable.embed
    private val commStrs = messTable.commands
    private val dbMan = DatabaseManager.instance

    lateinit var client: JDA

    fun createApp(): JDA {
        client = JDABuilder.createDefault(settings.discord["token"] as String)
            .setActivity(Activity.watching(messTable.generalStrings["bot_activity"]!!))
            .addEventListeners(EventHandler())
            .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
            .build()
        return client
    }

    fun generateTicketEmbed(
        authorName: String,
        authorUrl: String,
        message: String,
        isAssigned: Boolean = false,
        idOrUser: String,
        assignedTo: String = "",
        footerStr: String,
        state: Any = TicketState.OPEN
    ): MessageEmbed {
        return EmbedBuilder().setColor(Color.blue).setTitle(embedStrs["embed_newTicketTitle"]!!)
            .setAuthor(authorName, authorUrl, "attachment://pic.png")
            .setDescription(message)
            .addField(embedStrs["embed_assignedTo"]!!, if (isAssigned) assignedTo else embedStrs["embed_noOne"]!!, true)
            .addField(embedStrs["embed_state"]!!, state.toString(), false)
            .addField(embedStrs["embed_idOrUser"]!!, idOrUser, false)
            .setFooter(footerStr, null).build()
    }

    fun generateRowsEmbedButtons(
        tgProfile: String,
        channelId: Long,
        menuId: String,
        disableSecondButtons: Boolean = false
    ): List<ActionRow> {
        return listOf(
            ActionRow.of(
                listOf(
                    Button.link(tgProfile, embedStrs["button_openTg"]!!)
                )
            ), ActionRow.of(
                listOf(
                    Button.success("assign-$channelId", embedStrs["button_assign"]!!)
                        .withDisabled(disableSecondButtons),
                    Button.secondary("suspend-$channelId", embedStrs["button_suspend"]!!)
                        .withDisabled(disableSecondButtons),
                    Button.danger("close-$channelId", embedStrs["button_close"]!!).withDisabled(disableSecondButtons),
                )
            ), ActionRow.of(
                Button.primary("menu${if (menuId != "") "-$menuId" else ""}", "Apri menu")
            )
        )
    }

    fun getLastModified(): FileInputStream {
        val directory =
            File("session/${if (!Files.exists(Path("./json/dev"))) "database" else "database_dev"}/profile_photos")
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
        TgApp.downloadPlaceholder()
        return FileInputStream(chosenFile ?: File("./session/database/5900.jpg"))
    }

    fun sendTextMessageToChannel(channel: Long, text: String, replyId: Long, ticketId: Int): MessageCreateAction {
        return client.getThreadChannelById(
            channel
        )!!.sendMessage(text).setMessageReference(
            if (replyId != 0L) {
                dbMan.Search().MessageLinks().searchMessageByOtherMessage(ticketId, 0, true, replyId)
            } else 0L
        )
    }

    fun createTicket(chat: Chat, message: String) {
        TgApp.sendMessage(chat.id, messTable.generalStrings["welcome"] as String, 0) {}

        val filePath = TgApp.downloadPic(chat.photo)
        TgApp.client.send(TdApi.GetUser(chat.id)) { uname ->
            val usernames = uname.get().usernames
            val hasUsername = usernames != null

            client
                .getChannelById(MessageChannel::class.java, settings.discord["channel_id"] as Long)!!
                .sendMessageEmbeds(
                    generateTicketEmbed(
                        chat.title,
                        if (!hasUsername) embedStrs["tgRedirectPrefixLink"]!! + chat.id.toString() else "https://${(usernames.activeUsernames[0])}.t.me",
                        message,
                        idOrUser = "${chat.id}/${if (!hasUsername) "Nessun username" else ("@" + usernames.activeUsernames[0])}",
                        footerStr = "${settings.discord["idPrefix"]}${dbMan.Utils().getLastUsedTicketId() + 1}"
                    )

                ).addFiles(FileUpload.fromData(filePath, "pic.png")).queue {
                    Thread.sleep(350)
                    it.createThreadChannel(
                        "${settings.discord["idPrefix"]}${dbMan.Utils().getLastUsedTicketId() + 1}"
                    ).queue { itThread ->
                        dbMan.Create().Tickets().createTicketDocument(
                            TicketDocument(
                                chat.id,
                                itThread.idLong,
                                dbMan.Utils().getLastUsedTicketId() + 1,
                                mapOf("open" to true, "suspended" to false, "closed" to false),
                                System.currentTimeMillis() / 1000
                            )
                        )
                    }
                    val rows = generateRowsEmbedButtons(
                        embedStrs["tgRedirectPrefixLink"]!! + chat.id.toString(),
                        it.idLong, it.id
                    )
                    it.editMessageComponents(rows[0], rows[1], rows[2]).queue()

                    TgApp.alertTicket(
                        chat.title,
                        message,
                        "https://discordapp.com/channels/${settings.discord["guild_id"].toString()}/${settings.discord["channel_id"].toString()}"
                    )
                }
        }
    }

    fun isHigherRole(member: Member): Boolean {
        var isHigher = false
        for (role in member.roles) {
            if (role.idLong in settings.discord["higher_roles"] as List<*>) {
                isHigher = true
            }
        }
        return isHigher
    }

    fun createCommands() {
        client.updateCommands().addCommands(
            Commands.slash(commStrs["tickets"]!!["name"]!!, commStrs["tickets"]!!["description"]!!)
                .addOption(
                    OptionType.STRING,
                    commStrs["tickets"]!!["option_1_name"]!!,
                    commStrs["tickets"]!!["option_1_description"]!!,
                    true
                ),
            Commands.slash(commStrs["cronologia"]!!["name"]!!, commStrs["cronologia"]!!["description"]!!)
                .addOption(
                    OptionType.STRING,
                    commStrs["cronologia"]!!["option_1_name"]!!,
                    commStrs["cronologia"]!!["option_1_description"]!!,
                    true
                )
                .addOption(
                    OptionType.INTEGER,
                    commStrs["cronologia"]!!["option_2_name"]!!,
                    commStrs["cronologia"]!!["option_2_description"]!!,
                    false
                ),
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