package me.chicchi7393.discogramRewrite.discord.utils

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketState

class reopenTicket {
    private val discordClient = DsApp.instance
    private val dbMan = DatabaseManager.instance
    private val messageTable = JsonReader().readJsonMessageTable("messageTable")!!

    fun reopenTicket(tg_id: Long) {
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByTelegramId(tg_id)!!
        dbMan.Update().Tickets().reopenTicket(ticket)
        val threadChannel = discordClient.dsClient.getThreadChannelById(ticket.channelId)!!
        threadChannel.manager
            .setName(threadChannel.name.replace(messageTable.generalStrings["suffix_suspendedTicket"]!!, ""))
            .setLocked(false).queue()
        threadChannel.retrieveParentMessage().queue { message ->
            message.editMessageEmbeds(
                discordClient.generateTicketEmbed(
                    message.embeds[0].author!!.name!!,
                    message.embeds[0].author!!.url!!,
                    message.embeds[0].description!!,
                    message.embeds[0].fields[0].value == messageTable.embed["embed_yes"],
                    message.embeds[0].fields[1].value != messageTable.embed["embed_noOne"],
                    message.embeds[0].fields[2].value!!,
                    message.embeds[0].fields[1].value!!,
                    message.embeds[0].footer!!.text!!,
                    TicketState.OPEN
                )
            ).queue()
        }

    }
}