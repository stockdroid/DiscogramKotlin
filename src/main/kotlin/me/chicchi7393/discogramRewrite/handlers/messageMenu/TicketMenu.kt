package me.chicchi7393.discogramRewrite.handlers.messageMenu

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class TicketMenu(private val event: ButtonInteractionEvent) {
    private val dbMan = DatabaseManager.instance
    private val channelId =
        if (event.componentId.contains(":")) event.componentId.split(":")[1].split("/")[0].toLong() else 0
    private val messageId = if (event.componentId.contains("/")) event.componentId.split("/")[1].toLong() else 0
    private val settings = JsonReader().readJsonSettings()!!
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val embedStrs = messTable.embed
    private val menuStrs = messTable.menu["ticket_menu"]!!

    fun removeTicket() {
        dbMan.Update().Assignees().editAssignee(
            dbMan.Search().Tickets().searchTicketDocumentByChannelId(channelId)!!.ticketId,
            0
        )
        DsApp.client.getChannelById(TextChannel::class.java, settings.discord["channel_id"] as Long)!!
            .retrieveMessageById(messageId).queue {
                it.editMessageEmbeds(
                    DsApp.generateTicketEmbed(
                        it.embeds[0].author!!.name!!,
                        it.embeds[0].author!!.url!!,
                        it.embeds[0].description!!,
                        false,
                        it.embeds[0].fields[2].value!!,
                        it.embeds[0].footer!!.text!!,
                        it.embeds[0].fields[1].value!!
                    )
                ).queue()
            }

        event.editMessage(menuStrs["removed_ticket"]!!).queue()
    }

    fun marisaTicket() {
        dbMan.Update().Assignees().editAssignee(
            dbMan.Search().Tickets().searchTicketDocumentByChannelId(channelId)!!.ticketId,
            event.member!!.idLong
        )

        DsApp.client.getChannelById(TextChannel::class.java, settings.discord["channel_id"] as Long)!!
            .retrieveMessageById(messageId).queue {
                it.editMessageEmbeds(
                    DsApp.generateTicketEmbed(
                        it.embeds[0].author!!.name!!,
                        it.embeds[0].author!!.url!!,
                        it.embeds[0].description!!,
                        false,
                        it.embeds[0].fields[2].value!!,
                        if (event.member!!.nickname == null) event.member!!.effectiveName else event.member!!.nickname!!,
                        it.embeds[0].footer!!.text!!,
                        it.embeds[0].fields[1].value!!
                    )
                ).queue()
            }

        event.editMessage(menuStrs["stole_ticket"]!!).queue()
    }
}