package me.chicchi7393.discogramRewrite.handlers.commands

import it.tdlight.jni.TdApi
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ticketListCommand(val event: SlashCommandInteractionEvent) {
    private val tgClient = TgApp.instance
    private val dbMan = DatabaseManager.instance
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val commStrs = messTable.commands
    private val settings = JsonReader().readJsonSettings("settings")!!

    private fun getId(): Long {
        if (event.options.isEmpty()) {
            try {
                idTransporter.value =
                    dbMan.Search().Tickets().searchTicketDocumentById(
                        event.threadChannel.name.split(" ")[0].replace(settings.discord["idPrefix"] as String, "").toInt()
                    )!!.telegramId
            } catch (_: Exception) {
                idTransporter.value = 0L
            }
        } else {
            val username = event.options[0].asString
            try {
                idTransporter.value = username.toLong()
            } catch (_: Exception) {
                tgClient.client.send(TdApi.SearchPublicChat(username)) {
                    idTransporter.value = it.get().id
                }
            }
        }
        return idTransporter.value
    }

    fun ticketList() {
        val userId = getId()
        if (userId == 0L) {
            event.reply(messTable.errors["user0"]!!).queue()
        } else {
            val tickets = dbMan.Search().Tickets().searchTicketDocumentsByTelegramId(userId)
            var message = commStrs["tickets"]!!["template"]!!
            for (ticket in tickets) {
                if (ticket != null) {
                    message += settings.discord["idPrefix"] as String + ticket.ticketId + "\n"
                }
            }
            event.reply(message).queue()
        }
    }
}