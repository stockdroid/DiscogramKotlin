package me.chicchi7393.discogramRewrite.handlers.commands

import it.tdlight.jni.TdApi
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.utils.getId
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ticketListCommand(val event: SlashCommandInteractionEvent) {
    private val tgClient = TgApp.instance
    private val dbMan = DatabaseManager.instance
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val commStrs = messTable.commands
    private val settings = JsonReader().readJsonSettings("settings")!!


    fun ticketList() {
        val userId = getId(event)
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