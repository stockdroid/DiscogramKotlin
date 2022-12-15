package me.chicchi7393.discogramRewrite.discord.utils

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager

class ReopenTicket {
    private val dbMan = DatabaseManager.instance
    private val messageTable = JsonReader().readJsonMessageTable("messageTable")!!

    fun reopenTicket(tgId: Long) {
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByTelegramId(tgId)!!
        dbMan.Update().Tickets().reopenTicket(ticket)
        val threadChannel = DsApp.client.getThreadChannelById(ticket.channelId)!!
        threadChannel.manager
            .setName(threadChannel.name.replace(messageTable.generalStrings["suffix_suspendedTicket"]!!, ""))
            .setLocked(false).queue()
        threadChannel.retrieveParentMessage().queue { message ->
            message.editMessageEmbeds(
                DsApp.generateTicketEmbed(
                    message.embeds[0].author!!.name!!,
                    message.embeds[0].author!!.url!!,
                    message.embeds[0].description!!,
                    message.embeds[0].fields[1].value != messageTable.embed["embed_noOne"],
                    message.embeds[0].fields[2].value!!,
                    message.embeds[0].fields[0].value!!,
                    message.embeds[0].footer!!.text!!
                )
            ).queue()
        }

    }
}