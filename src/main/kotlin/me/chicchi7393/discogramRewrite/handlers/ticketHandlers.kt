package me.chicchi7393.discogramRewrite.handlers

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.discord.utils.reopenTicket
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.MessageLinkType
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketDocument
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketState
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.FileUpload
import org.bson.BsonTimestamp

class ticketHandlers {
    private val settings = JsonReader().readJsonSettings()!!
    private val dbMan = DatabaseManager.instance
    private val dsClass = DsApp.instance
    private val tgClient = TgApp.instance
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val embedStrs = messTable.embed

    fun startTicketWithFile(chat: Chat, file: DownloadFile?, text: String) {
        tgClient.client.send(
            SendMessage(
                chat.id,
                0,
                0,
                null,
                null,
                InputMessageText(FormattedText(messTable.generalStrings["welcome"], null), false, false)
            )
        ) {}

        val filePath = tgClient.downloadPic(chat.photo)

        tgClient.client.send(GetUser(chat.id)) { uname ->
            val embed = dsClass.generateTicketEmbed(
                chat.title,
                embedStrs["tgRedirectPrefixLink"]!! + chat.id.toString(),
                "Ticket iniziato con file",
                isForced = false,
                isAssigned = false,
                "${chat.id}/${if (uname.get().username == null) "Nessun username" else ("@" + uname.get().username)}",
                footerStr = "${settings.discord["idPrefix"]}${dbMan.Utils().getLastUsedTicketId() + 1}",
                state = TicketState.OPEN
            )
            dsClass.dsClient
                .getChannelById(MessageChannel::class.java, settings.discord["channel_id"] as Long)!!
                .sendMessageEmbeds(
                    embed
                ).addFiles(FileUpload.fromData(filePath, "pic.png"))
                .queue {
                    it.editMessageEmbeds(
                        embed
                    ).setComponents(
                        dsClass.generateFirstEmbedButtons(
                            embedStrs["tgRedirectPrefixLink"]!! + chat.id.toString()
                        ),
                        dsClass.generateSecondEmbedButtons(it.idLong),
                        ActionRow.of(
                            Button.primary("menu-${it.id}", embedStrs["button_openMenu"]!!)
                        )
                    ).queue()
                    it.createThreadChannel(
                        "${settings.discord["idPrefix"]}${dbMan.Utils().getLastUsedTicketId() + 1}"
                    ).queue { threaad ->
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
                            tgClient.client.send(file) {
                                threaad.sendMessage(text)
                                    .addFiles(FileUpload.fromData(java.io.File(it.get().local.path))).queue()
                            }
                        tgClient.alertTicket(
                            chat.title,
                            "Ticket avviato con file",
                            "https://discordapp.com/channels/${settings.discord["guild_id"].toString()}/${settings.discord["channel_id"].toString()}"
                        )
                    }
                }

        }

    }

    fun startTicketWithText(chat: Chat, text: String) = dsClass.createTicket(chat, text)
    fun sendFileFollowMessage(
        id: Long,
        file: DownloadFile?,
        text: String,
        wasSuspended: Boolean,
        ticket_id: Int,
        tg_id: Long,
        reply_id: Long
    ) {
        if (wasSuspended) {
            tgClient.client.send(
                SendMessage(
                    id,
                    0,
                    0,
                    null,
                    null,
                    InputMessageText(
                        FormattedText(messTable.modals["suspendTicket"]!!["reopenTgMessage"], null),
                        false,
                        false
                    )
                )
            ) {}
            reopenTicket().reopenTicket(id)
        }
        if (file == null) {
            dsClass.sendTextMessageToChannel(
                dbMan.Utils().searchAlreadyOpen(id)!!.channelId,
                text,
                reply_id,
                ticket_id
            ).queue {
                dbMan.Update().MessageLinks().addMessageToMessageLinks(
                    ticket_id,
                    MessageLinkType(tg_id, it.idLong, BsonTimestamp(System.currentTimeMillis() / 1000))
                )
            }
        } else {
            tgClient.client.send(file) {
                dsClass.sendTextMessageToChannel(
                    dbMan.Utils().searchAlreadyOpen(id)!!.channelId,
                    text,
                    reply_id,
                    ticket_id
                )
                    .addFiles(FileUpload.fromData(java.io.File(it.get().local.path))).queue {
                        dbMan.Update().MessageLinks().addMessageToMessageLinks(
                            ticket_id,
                            MessageLinkType(tg_id, it.idLong, BsonTimestamp(System.currentTimeMillis() / 1000))
                        )
                    }
            }
        }
    }

    fun sendTextFollowMessage(
        id: Long,
        text: String,
        wasSuspended: Boolean,
        ticket_id: Int,
        tg_id: Long,
        reply_id: Long
    ) {
        if (wasSuspended) {
            tgClient.client.send(
                SendMessage(
                    id,
                    0,
                    0,
                    null,
                    null,
                    InputMessageText(
                        FormattedText(messTable.modals["suspendTicket"]!!["reopenTgMessage"], null),
                        false,
                        false
                    )
                )
            ) {}
            reopenTicket().reopenTicket(id)
        }

        dsClass.sendTextMessageToChannel(dbMan.Utils().searchAlreadyOpen(id)!!.channelId, text, reply_id, ticket_id)
            .queue {
                dbMan.Update().MessageLinks().addMessageToMessageLinks(
                    ticket_id,
                    MessageLinkType(tg_id, it.idLong, BsonTimestamp(System.currentTimeMillis() / 1000))
                )
            }
    }


    fun closeTicket(ticket: TicketDocument, text: String, rating: Boolean) {
        dbMan.Update().Tickets().closeTicket(
            ticket
        )
        tgClient.client.send(
            SendMessage(
                ticket.telegramId,
                0,
                0,
                null,
                null,
                InputMessageText(
                    FormattedText(
                        "${if (rating) messTable.generalStrings["closedTicketTG"] else messTable.generalStrings["closedTicketTGWR"]} ${if (rating) messTable.generalStrings["feedback_url"] + ticket.ticketId.toString() else ""} ${if (text != "") "\nMotivazione: $text" else ""}",
                        null
                    ), false, false
                )
            )
        ) {}
    }

    fun suspendTicket(ticket: TicketDocument, text: String) {
        dbMan.Update().Tickets().suspendTicket(
            ticket
        )
        tgClient.client.send(
            SendMessage(
                ticket.telegramId,
                0,
                0,
                null,
                null,
                InputMessageText(
                    FormattedText(
                        "${messTable.generalStrings["suspendedTicketTG"]} ${if (text != "") "\nMotivazione: $text" else ""}",
                        null
                    ), false, false
                )
            )
        ) {}
    }

    fun reOpenTicket(ticket: TicketDocument) {
        dbMan.Update().Tickets().reopenTicket(
            ticket
        )
        tgClient.client.send(
            SendMessage(
                ticket.telegramId,
                0,
                0,
                null,
                null,
                InputMessageText(FormattedText(messTable.generalStrings["reopenedTicketTG"], null), false, false)
            )
        ) {}
    }
}