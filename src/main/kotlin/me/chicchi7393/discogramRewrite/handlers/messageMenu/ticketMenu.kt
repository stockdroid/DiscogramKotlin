package me.chicchi7393.discogramRewrite.handlers.messageMenu

import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ticketMenu(val event: ButtonInteractionEvent) {
    private val dbMan = DatabaseManager.instance
    private val channel_id = if (event.componentId.contains(":")) event.componentId.split(":")[1].toLong() else 0
    private val discordClient = DsApp.instance

    fun moveTicket() {
        ////dopo
    }

    fun removeTicket() {
        dbMan.Update().Assignees().editAssignee(
            dbMan.Search().Tickets().searchTicketDocumentByChannelId(channel_id)!!.ticketId,
            0
        )
        event.message.editMessageEmbeds(
            discordClient.generateTicketEmbed(
                event.message.embeds[0].author!!.name!!,
                event.message.embeds[0].author!!.url!!,
                event.message.embeds[0].description!!,
                event.message.embeds[0].fields[0].value == "Sì",
                false,
                "",
                event.message.embeds[0].footer!!.text!!,
                event.message.embeds[0].fields[2].value!!
            )
        ).queue()
        event.reply("Tolto il ticket!").setEphemeral(true).queue()
    }

    fun marisaTicket() {
        event.message.editMessageEmbeds(
            discordClient.generateTicketEmbed(
                event.message.embeds[0].author!!.name!!,
                event.message.embeds[0].author!!.url!!,
                event.message.embeds[0].description!!,
                event.message.embeds[0].fields[0].value == "Sì",
                true,
                if (event.member!!.nickname == null) event.member!!.effectiveName else event.member!!.nickname!!,
                event.message.embeds[0].footer!!.text!!,
                event.message.embeds[0].fields[2].value!!
            )
        ).queue()
        dbMan.Update().Assignees().editAssignee(
            dbMan.Search().Tickets().searchTicketDocumentByChannelId(channel_id)!!.ticketId,
            event.member!!.idLong
        )
        event.reply("Ticket preso!").setEphemeral(true).queue()
    }
}