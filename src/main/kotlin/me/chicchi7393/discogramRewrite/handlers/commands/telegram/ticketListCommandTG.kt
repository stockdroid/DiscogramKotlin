package me.chicchi7393.discogramRewrite.handlers.commands.telegram

import it.tdlight.jni.TdApi
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.handlers.commands.telegram.utils.getIdTg
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import kotlin.math.min

class ticketListCommandTG {
    private val client = TgApp.instance.client
    private val settings = JsonReader().readJsonSettings()!!
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val dbMan = DatabaseManager.instance
    private val commStrs = messTable.commands

    fun handle(content: String) {
        val content = content.replace("tickets ", "")
        val userId = getIdTg(content)
        if (userId == 0L) {
            client.send(
                TdApi.SendMessage(
                    (settings.telegram["moderatorGroup"] as Number).toLong(),
                    0L,
                    0L,
                    null,
                    null,
                    TdApi.InputMessageText(
                        TdApi.FormattedText(
                            messTable.errors["user0"]!!,
                            null
                        ),
                        false,
                        false
                    )
                )
            ) {}
        } else {
            val tickets = dbMan.Search().Tickets().searchTicketDocumentsByTelegramId(userId)
            var message = commStrs["tickets"]!!["template"]!!
            for (ticket in tickets) {
                if (ticket != null) {
                    val messageLink =
                        "https://discordapp.com/channels/${settings.discord["guild_id"].toString()}/${ticket.channelId}"
                    message += "${settings.discord["idPrefix"] as String}${ticket.ticketId}: ${messageLink}\n"
                }
            }
            val mess_parts = mutableListOf<String>()
            var index = 0
            while (index < message.length) {
                mess_parts.add(message.substring(index, min(index + 2000, message.length)))
                index += 2000
            }
            for (part in mess_parts.reversed()) {
                client.send(
                    TdApi.SendMessage(
                        (settings.telegram["moderatorGroup"] as Number).toLong(),
                        0L,
                        0L,
                        null,
                        null,
                        TdApi.InputMessageText(
                            TdApi.FormattedText(
                                part,
                                null
                            ),
                            false,
                            false
                        )
                    )
                ) {}
            }
        }
    }
}