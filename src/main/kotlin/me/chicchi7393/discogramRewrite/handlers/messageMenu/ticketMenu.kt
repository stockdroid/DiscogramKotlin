package me.chicchi7393.discogramRewrite.handlers.messageMenu

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketState
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ticketMenu(private val event: ButtonInteractionEvent) {
    private val dbMan = DatabaseManager.instance
    private val channel_id =
        if (event.componentId.contains(":")) event.componentId.split(":")[1].split("/")[0].toLong() else 0
    private val message_id = if (event.componentId.contains("/")) event.componentId.split("/")[1].toLong() else 0
    private val discordClient = DsApp.instance
    private val settings = JsonReader().readJsonSettings()!!
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val embedStrs = messTable.embed
    private val menuStrs = messTable.menu["ticket_menu"]!!

    fun removeTicket() {
        dbMan.Update().Assignees().editAssignee(
            dbMan.Search().Tickets().searchTicketDocumentByChannelId(channel_id)!!.ticketId,
            0
        )
        discordClient.dsClient.getChannelById(TextChannel::class.java, settings.discord["channel_id"] as Long)!!
            .retrieveMessageById(message_id).queue {
                it.editMessageEmbeds(
                    discordClient.generateTicketEmbed(
                        it.embeds[0].author!!.name!!,
                        it.embeds[0].author!!.url!!,
                        it.embeds[0].description!!,
                        it.embeds[0].fields[0].value == embedStrs["embed_yes"],
                        false,
                        it.embeds[0].fields[2].value!!,
                        "",
                        it.embeds[0].footer!!.text!!,
                        it.embeds[0].fields[2].value!!
                    )
                ).queue()
            }

        event.editMessage(menuStrs["removed_ticket"]!!).queue()
    }

    fun marisaTicket() {
        dbMan.Update().Assignees().editAssignee(
            dbMan.Search().Tickets().searchTicketDocumentByChannelId(channel_id)!!.ticketId,
            event.member!!.idLong
        )

        discordClient.dsClient.getChannelById(TextChannel::class.java, settings.discord["channel_id"] as Long)!!
            .retrieveMessageById(message_id).queue {
                it.editMessageEmbeds(
                    discordClient.generateTicketEmbed(
                        it.embeds[0].author!!.name!!,
                        it.embeds[0].author!!.url!!,
                        it.embeds[0].description!!,
                        it.embeds[0].fields[0].value == "Sì",
                        true,
                        if (event.member!!.nickname == null) event.member!!.effectiveName else event.member!!.nickname!!,
                        it.embeds[0].footer!!.text!!,
                        it.embeds[0].fields[2].value!!,
                        TicketState.OPEN
                    )
                ).queue()
            }

        event.editMessage(menuStrs["stole_ticket"]!!).queue()
    }
}