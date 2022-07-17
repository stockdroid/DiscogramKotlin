package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketState
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle


class buttonHandlers(private val event: ButtonInteractionEvent) {
    private val ticketHandler = ticketHandlers()
    private val dbMan = DatabaseManager.instance
    private val discordClient = DsApp.instance
    private val settings = JsonReader().readJsonSettings("settings")!!

    fun closeButtonTicketHandler() {
        event.replyModal(
            Modal
                .create("closeModal-${event.componentId.split("-")[1]}:${event.message.id}", "Chiudi ticket")
                .addActionRow(
                    ActionRow.of(
                        TextInput.create("reason", "Motivazione", TextInputStyle.PARAGRAPH)
                            .setPlaceholder("Hai rotto le palle")
                            .setRequiredRange(0, 1000)
                            .setRequired(false)
                            .build()
                    ).actionComponents
                )
                .build()
        )
            .queue()
    }
    fun suspendButtonTicketHandler() {
        if (event.message.embeds[0].fields[2].value == "Aperto") {
            event.replyModal(
                Modal
                    .create("suspendModal-${event.componentId.split("-")[1]}:${event.message.id}", "Sospendi ticket")
                    .addActionRow(
                        ActionRow.of(
                            TextInput.create("reason", "Motivazione", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("Hai rotto temporaneamente le palle")
                                .setRequiredRange(0, 1000)
                                .setRequired(false)
                                .build()
                        ).actionComponents
                    )
                    .build()
            ).queue()
        } else {
            ticketHandler.reOpenTicket(
                dbMan.Search().Tickets().searchTicketDocumentByChannelId(
                    event.componentId.split("-")[1].toLong()
                )!!
            )
            event.reply("Ticket riaperto.")
            discordClient.dsClient
                .getChannelById(TextChannel::class.java, event.componentId.split("-")[1].toLong())!!
                .manager
                .setParent(discordClient.dsClient.getCategoryById(settings.discord["category_id"] as Long))
                .queue()

        }
    }

}