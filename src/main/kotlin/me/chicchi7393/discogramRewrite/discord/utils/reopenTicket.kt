package me.chicchi7393.discogramRewrite.discord.utils

import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketState
import me.chicchi7393.discogramRewrite.telegram.TgApp

class reopenTicket {
    private val discordClient = DsApp.instance
    private val telegramClient = TgApp.instance
    private val dbMan = DatabaseManager.instance

    fun reopenTicket(tg_id: Long) {
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByTelegramId(tg_id)!!
        dbMan.Update().Tickets().reopenTicket(ticket)
        val threadChannel = discordClient.dsClient.getThreadChannelById(ticket.channelId)!!
        threadChannel.manager
            .setName(threadChannel.name.replace(" (Sospeso)", ""))
            .setLocked(false).queue()
        threadChannel.retrieveParentMessage().queue { message ->
            message.editMessageEmbeds(
                discordClient.generateTicketEmbed(
                    message.embeds[0].author!!.name!!,
                    message.embeds[0].author!!.url!!,
                    message.embeds[0].description!!,
                    message.embeds[0].fields[0].value == "Sì",
                    message.embeds[0].fields[1].value != "Nessuno",
                    message.embeds[0].fields[1].value!!,
                    message.embeds[0].footer!!.text!!,
                    TicketState.OPEN
                )
            ).queue()
        }

    }
}