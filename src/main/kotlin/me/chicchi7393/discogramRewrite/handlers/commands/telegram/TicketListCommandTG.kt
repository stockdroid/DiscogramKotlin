package me.chicchi7393.discogramRewrite.handlers.commands.telegram

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.handlers.commands.telegram.utils.getIdTg
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import kotlin.math.min

class TicketListCommandTG {
    private val client = TgApp.client
    private val settings = JsonReader().readJsonSettings()!!
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val dbMan = DatabaseManager.instance
    private val commStrs = messTable.commands

    fun handle(commandContent: String) {
        val content = commandContent.replace("tickets ", "")
        val userId = getIdTg(content)
        if (userId == 0L) {
            TgApp.sendMessage(
                (settings.telegram["moderatorGroup"] as Number).toLong(),
                messTable.errors["user0"]!!,
                0
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
            val messParts = mutableListOf<String>()
            var index = 0
            while (index < message.length) {
                messParts.add(message.substring(index, min(index + 2000, message.length)))
                index += 2000
            }
            for (part in messParts.reversed()) {
                TgApp.sendMessage((settings.telegram["moderatorGroup"] as Number).toLong(), part, 0) {}
            }
        }
    }
}