package me.chicchi7393.discogramRewrite.handlers

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketDocument
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketState
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.lang.NullPointerException
import java.net.URI

class ticketHandlers {
    private val settings = JsonReader().readJsonSettings("settings")!!
    private val dbMan = DatabaseManager.instance
    private val dsClass = DsApp.instance
    private val tgClient = TgApp.instance

    fun startTicketWithFile(id: Long, chat: Chat, file: DownloadFile?, text: String) {
        val pfpId = try {
            chat.photo.small.id
        } catch (_: NullPointerException) {
            69420
        }
        tgClient.downloadFile(pfpId)
        Thread.sleep(500)
        val filePath = dsClass.getLastModified("session/database/profile_photos")!!.absolutePath
        val embed = dsClass.generateTicketEmbed(
            chat.title,
            "https://chicchi7393.xyz/redirectTg.html?id=${chat.id}",
            "File",
            isForced = false,
            isAssigned = false,
            footerStr = "${settings.discord["IDPrefix"]}${dbMan.Utils().getLastUsedTicketId()}",
            state = TicketState.OPEN
        )
        dsClass.dsClient
            .getChannelById(MessageChannel::class.java, settings.discord["channel_id"] as Long)!!
            .sendMessageEmbeds(
                embed
            ).map {
                it.editMessageEmbeds(
                    embed
                ).setActionRows(
                    dsClass.generateFirstEmbedButtons(
                        "https://chicchi7393.xyz/redirectTg.html?id=${chat.id}"
                    ),
                    dsClass.generateSecondEmbedButtons(it.idLong),
                    ActionRow.of(
                        Button.primary("menu-${it.id}", "Apri menu")
                    )
                ).addFile(java.io.File(URI("file://${filePath}")), "pic.png").queue()
                it.createThreadChannel(
                    "${settings.discord["IDPrefix"]}${dbMan.Utils().getLastUsedTicketId() + 1}"
                ).map { tIt ->
                    dbMan.Create().Tickets().createTicketDocument(
                        TicketDocument(
                            chat.id,
                            tIt.idLong,
                            dbMan.Utils().getLastUsedTicketId()+1,
                            mapOf("open" to true, "suspended" to false, "closed" to false),
                            System.currentTimeMillis() / 1000
                        )
                    )
                    if (file == null) {
                        tIt.sendMessage(text).queue()
                    } else {
                        tgClient.client.send(file) {
                            tIt.sendMessage(text)
                                .addFile(java.io.File(it.get().local.path)).queue()
                        }
                }
            }
        }.queue()
    }

    fun startTicketWithText(chat: Chat, text: String) = dsClass.createTicket(chat, text)
    fun sendFileFollowMessage(id: Long, file: DownloadFile?, text: String) {
        if (file == null) {
            dsClass.sendTextMessageToChannel(
                dbMan.Utils().searchAlreadyOpen(id)!!.channelId,
                text
            ).queue()
        } else {
            tgClient.client.send(file) {
                dsClass.sendTextMessageToChannel(
                    dbMan.Utils().searchAlreadyOpen(id)!!.channelId,
                    text
                )
                    .addFile(java.io.File(it.get().local.path)).queue()
            }
        }
    }

    fun sendTextFollowMessage(id: Long, text: String) =
        dsClass.sendTextMessageToChannel(dbMan.Utils().searchAlreadyOpen(id)!!.channelId, text).queue()

    fun closeTicket(ticket: TicketDocument, text: String) {
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
                        "Ticket chiuso. ${if (text != "") "Motivazione: $text" else ""}",
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
                        "Ticket sospeso. ${if (text != "") "Motivazione: $text" else ""}",
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
                InputMessageText(FormattedText("Ticket riaperto.", null), false, false)
            )
        ) {}
    }
}