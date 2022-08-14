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
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    fun closeTicketHandler() {
        val channelId = event.modalId.split("-")[1].split(":")[0].toLong()
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByChannelId(
            channelId
        )!!
        ticketHandler.closeTicket(ticket, event.values[0].asString)

        val thread = discordClient.dsClient
            .getThreadChannelById(channelId)!!
        thread.manager
            .setName(thread.name + messTable.generalStrings["suffix_closedTicket"])
            .setArchived(true)
            .queue()
        Thread.sleep(1250)
        val message =
            discordClient.dsClient.getChannelById(TextChannel::class.java, settings.discord["channel_id"] as Long)!!
                .retrieveMessageById(event.modalId.split(":")[1].toLong()).queue { message ->
                    message.editMessageComponents(
                        discordClient.generateFirstEmbedButtons(
                            messTable.embed["tgRedirectPrefixLink"] + ticket.telegramId
                        ),
                        ActionRow.of(
                            Button.success("assign-$channelId", messTable.embed["button_assign"]!!).asDisabled(),
                            Button.secondary("suspend-$channelId", messTable.embed["button_suspend"]!!).asDisabled(),
                            Button.danger("close-$channelId", messTable.embed["button_close"]!!).asDisabled()
                        ),
                        ActionRow.of(
                            Button.primary("menu", messTable.embed["button_openMenu"]!!)
                        )
                    ).setEmbeds(
                        discordClient.generateTicketEmbed(
                            message.embeds[0].author!!.name!!,
                            message.embeds[0].author!!.url!!,
                            message.embeds[0].description!!,
                            message.embeds[0].fields[0].value == messTable.embed["embed_yes"]!!,
                            message.embeds[0].fields[1].value != messTable.embed["embed_noOne"]!!,
                            message.embeds[0].fields[1].value!!,
                            message.embeds[0].footer!!.text!!,
                            TicketState.CLOSED
                        )
                    ).queue()
                }
        Thread.sleep(1250)
        event.reply(messTable.modals["closeTicket"]!!["reply"]!!).queue()
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
            .setName(thread.name + messTable.generalStrings["suffix_suspendedTicket"])
            .setLocked(true)
            .queue()
        Thread.sleep(1250)
        discordClient.dsClient.getChannelById(TextChannel::class.java, settings.discord["channel_id"] as Long)!!
                .retrieveMessageById(event.modalId.split(":")[1].toLong()).queue {message ->
                    message.editMessageEmbeds(
                        discordClient.generateTicketEmbed(
                            message.embeds[0].author!!.name!!,
                            message.embeds[0].author!!.url!!,
                            message.embeds[0].description!!,
                            message.embeds[0].fields[0].value == messTable.embed["embed_yes"]!!,
                            message.embeds[0].fields[1].value != messTable.embed["embed_noOne"]!!,
                            message.embeds[0].fields[1].value!!,
                            message.embeds[0].footer!!.text!!,
                            TicketState.SUSPENDED
                        )
                    ).queue()
                }
        Thread.sleep(1250)
        event.reply(messTable.modals["suspendTicket"]!!["reply"]!!).queue()
    }
}