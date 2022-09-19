package me.chicchi7393.discogramRewrite.handlers.commands

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.utils.getId
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import kotlin.math.min

class ticketListCommand(val event: SlashCommandInteractionEvent) {
    private val dbMan = DatabaseManager.instance
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val commStrs = messTable.commands
    private val settings = JsonReader().readJsonSettings()!!


    fun ticketList() {
        val userId = getId(event)
        if (userId == 0L) {
            event.reply(messTable.errors["user0"]!!).queue()
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
                if (mess_parts.last() == part) {
                    event.reply(part).queue()
                } else {
                    event.channel.sendMessage(part).queue()
                }
            }
        }
    }
}