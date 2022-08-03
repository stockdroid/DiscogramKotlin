package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketState
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button

class modalHandlers(private val event: ModalInteractionEvent) {
    private val ticketHandler = ticketHandlers()
    private val dbMan = DatabaseManager.instance
    private val discordClient = DsApp.instance
    private val settings = JsonReader().readJsonSettings("settings")!!
    fun closeTicketHandler() {
        val channelId = event.modalId.split("-")[1].split(":")[0].toLong()
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByChannelId(
            channelId
        )!!
        ticketHandler.closeTicket(ticket, event.values[0].asString)

        val thread = discordClient.dsClient
            .getThreadChannelById(channelId)!!
        thread.manager
            .setName(thread.name + " (Chiuso)")
            .setArchived(true)
            .queue()

        val message =
            discordClient.dsClient.getChannelById(TextChannel::class.java, settings.discord["channel_id"] as Long)!!
                .retrieveMessageById(event.modalId.split(":")[1].toLong()).complete(true)
        message.editMessageComponents(
            discordClient.generateFirstEmbedButtons(
                "https://chicchi7393.xyz/redirectTg.html?id=${ticket.telegramId}"
            ),
            ActionRow.of(
                Button.success("assign-$channelId", "Assegna").asDisabled(),
                Button.secondary("suspend-$channelId", "Sospendi").asDisabled(),
                Button.danger("close-$channelId", "Chiudi").asDisabled()
            ),
            ActionRow.of(
                Button.primary("menu", "Apri menu")
            )
        ).queue()
        message.editMessageEmbeds(
            discordClient.generateTicketEmbed(
                message.embeds[0].author!!.name!!,
                message.embeds[0].author!!.url!!,
                message.embeds[0].description!!,
                message.embeds[0].fields[0].value == "Sì",
                message.embeds[0].fields[1].value != "Nessuno",
                message.embeds[0].fields[1].value!!,
                message.embeds[0].footer!!.text!!,
                TicketState.CLOSED
            )
        ).queue()
        event.reply("Ticket chiuso.").queue()
    }

    fun suspendTicketHandler() {
        val channelId = event.modalId.split("-")[1].split(":")[0].toLong()
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByChannelId(
            channelId
        )!!
        ticketHandler.suspendTicket(ticket, event.values[0].asString)

        val thread = discordClient.dsClient
            .getThreadChannelById(channelId)!!
        thread.manager
            .setName(thread.name + " (Sospeso)")
            .setLocked(true)
            .queue()

        val message =
            discordClient.dsClient.getChannelById(TextChannel::class.java, settings.discord["channel_id"] as Long)!!
                .retrieveMessageById(event.modalId.split(":")[1].toLong()).complete(true)
        message.editMessageEmbeds(
            discordClient.generateTicketEmbed(
                message.embeds[0].author!!.name!!,
                message.embeds[0].author!!.url!!,
                message.embeds[0].description!!,
                message.embeds[0].fields[0].value == "Sì",
                message.embeds[0].fields[1].value != "Nessuno",
                message.embeds[0].fields[1].value!!,
                message.embeds[0].footer!!.text!!,
                TicketState.SUSPENDED
            )
        ).queue()
        event.reply("Ticket sospeso temporaneamente.").queue()
    }
}