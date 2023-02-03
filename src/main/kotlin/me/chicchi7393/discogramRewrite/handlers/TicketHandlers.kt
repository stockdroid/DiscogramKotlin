package me.chicchi7393.discogramRewrite.handlers

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.discord.utils.ReopenTicket
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.MessageLinkType
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketDocument
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.utils.FileUpload
import org.bson.BsonTimestamp

class TicketHandlers {
    private val settings = JsonReader().readJsonSettings()!!
    private val dbMan = DatabaseManager.instance
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val embedStrs = messTable.embed
    private val tgClient = TgApp.client

    fun startTicketWithFile(chat: Chat, file: DownloadFile?, text: String) {
        TgApp.sendMessage(chat.id, messTable.generalStrings["welcome"]!!, 0) {}

        val filePath = TgApp.downloadPic(chat.photo)

        tgClient.send(GetUser(chat.id)) { uname ->
            val embed = DsApp.generateTicketEmbed(
                chat.title,
                if (uname.get().usernames.activeUsernames[0] == null) embedStrs["tgRedirectPrefixLink"]!! + chat.id.toString() else "https://${(uname.get().usernames.activeUsernames[0])}.t.me",
                "Ticket iniziato con file",
                idOrUser = "${chat.id}/${if (uname.get().usernames.activeUsernames[0] == null) "Nessun username" else ("@" + uname.get().usernames.activeUsernames[0])}",
                footerStr = "${settings.discord["idPrefix"]}${dbMan.Utils().getLastUsedTicketId() + 1}"
            )
            DsApp.client
                .getChannelById(MessageChannel::class.java, settings.discord["channel_id"] as Long)!!
                .sendMessageEmbeds(
                    embed
                ).addFiles(FileUpload.fromData(filePath, "pic.png"))
                .queue {
                    Thread.sleep(350)
                    it.createThreadChannel(
                        "${settings.discord["idPrefix"]}${dbMan.Utils().getLastUsedTicketId() + 1}"
                    ).queue { threaad ->
                        println(threaad)
                        dbMan.Create().Tickets().createTicketDocument(
                            TicketDocument(
                                chat.id,
                                threaad.idLong,
                                dbMan.Utils().getLastUsedTicketId() + 1,
                                mapOf("open" to true, "suspended" to false, "closed" to false),
                                System.currentTimeMillis() / 1000
                            )
                        )
                        if (file == null) threaad.sendMessage(text).queue() else
                            tgClient.send(file) { itFile ->
                                threaad.sendMessage(text)
                                    .addFiles(FileUpload.fromData(java.io.File(itFile.get().local.path))).queue()
                            }
                        TgApp.alertTicket(
                            chat.title,
                            "Ticket avviato con file",
                            "https://discordapp.com/channels/${settings.discord["guild_id"].toString()}/${settings.discord["channel_id"].toString()}"
                        )
                    }
                    val rows = DsApp.generateRowsEmbedButtons(
                        embedStrs["tgRedirectPrefixLink"]!! + chat.id.toString(), it.idLong, it.id
                    )
                    it.editMessageComponents(rows[0], rows[1], rows[2]).queue()

                }

        }

    }

    fun startTicketWithText(chat: Chat, text: String) = DsApp.createTicket(chat, text)
    fun sendFileFollowMessage(
        id: Long,
        file: DownloadFile?,
        text: String,
        wasSuspended: Boolean,
        ticketId: Int,
        tgId: Long,
        replyId: Long
    ) {
        if (wasSuspended) {
            TgApp.sendMessage(id, messTable.modals["suspendTicket"]!!["reopenTgMessage"]!!, 0) {}
            ReopenTicket().reopenTicket(id)
        }
        if (file == null) {
            DsApp.sendTextMessageToChannel(
                dbMan.Utils().searchAlreadyOpen(id)!!.channelId,
                text,
                replyId,
                ticketId
            ).queue {
                dbMan.Update().MessageLinks().addMessageToMessageLinks(
                    ticketId,
                    MessageLinkType(tgId, it.idLong, BsonTimestamp(System.currentTimeMillis() / 1000))
                )
            }
        } else {
            tgClient.send(file) {
                DsApp.sendTextMessageToChannel(
                    dbMan.Utils().searchAlreadyOpen(id)!!.channelId,
                    text,
                    replyId,
                    ticketId
                )
                    .addFiles(FileUpload.fromData(java.io.File(it.get().local.path))).queue { itFile ->
                        dbMan.Update().MessageLinks().addMessageToMessageLinks(
                            ticketId,
                            MessageLinkType(tgId, itFile.idLong, BsonTimestamp(System.currentTimeMillis() / 1000))
                        )
                    }
            }
        }
    }

    fun sendTextFollowMessage(
        id: Long,
        text: String,
        wasSuspended: Boolean,
        ticketId: Int,
        tgId: Long,
        replyId: Long
    ) {
        if (wasSuspended) {
            TgApp.sendMessage(id, messTable.modals["suspendTicket"]!!["reopenTgMessage"]!!, 0) {}
            ReopenTicket().reopenTicket(id)
        }

        DsApp.sendTextMessageToChannel(dbMan.Utils().searchAlreadyOpen(id)!!.channelId, text, replyId, ticketId)
            .queue {
                dbMan.Update().MessageLinks().addMessageToMessageLinks(
                    ticketId,
                    MessageLinkType(tgId, it.idLong, BsonTimestamp(System.currentTimeMillis() / 1000))
                )
            }
    }


    fun closeTicket(ticket: TicketDocument, text: String, rating: Boolean) {
        val newrating = if (settings.discord["enable_ratings"]!! as Boolean) {
            rating
        } else {
            false
        }
        println("DEBUG: Rating = $newrating")
        dbMan.Update().Tickets().closeTicket(
            ticket
        )
        TgApp.sendMessage(
            ticket.telegramId,
            "${if (newrating) messTable.generalStrings["closedTicketTG"] else messTable.generalStrings["closedTicketTGWR"]} ${if (newrating) settings.discord["feedback_url"]!! as String + ticket.ticketId.toString() else ""} ${if (text != "") "\nMotivazione: $text" else ""}",
            0
        ) {}
    }

    fun suspendTicket(ticket: TicketDocument, text: String) {
        dbMan.Update().Tickets().suspendTicket(
            ticket
        )

        TgApp.sendMessage(
            ticket.telegramId,
            "${messTable.generalStrings["suspendedTicketTG"]} ${if (text != "") "\nMotivazione: $text" else ""}",
            0
        ) {}
    }

}
