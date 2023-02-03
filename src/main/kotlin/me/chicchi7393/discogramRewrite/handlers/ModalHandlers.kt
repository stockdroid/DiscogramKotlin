package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.ReasonsDocument
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketState
import me.chicchi7393.discogramRewrite.objects.enums.ReasonEnum
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import java.lang.Exception

class ModalHandlers(private val event: GenericInteractionCreateEvent) {
    private val ticketHandler = TicketHandlers()
    private val dbMan = DatabaseManager.instance
    private val discordClient = DsApp.client
    private val settings = JsonReader().readJsonSettings()!!
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    fun closeTicketModal(rating: Boolean) {
        val channelId = (event as ModalInteractionEvent).modalId.split("-")[1].split(":")[0].toLong()
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByChannelId(
            channelId
        )!!

        dbMan.Create().Reasons().createReasonsDocument(
            ReasonsDocument(
                ticket.ticketId,
                ticket.telegramId,
                event.values[0].asString,
                ReasonEnum.CUSTOM.ordinal + 1
            )
        )
        event.reply(
            closeTicketHandler(
                channelId,
                event.modalId.split(":")[1].toLong(),
                event.values[0].asString,
                rating
            )
        ).setEphemeral(true).queue()
    }

    fun closeTicketHandler(channelId: Long, messageId: Long, reason: String, rating: Boolean): String {
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByChannelId(
            channelId
        ).let {
            if (it == null) {
                return messTable.errors["notFoundTicket"]!!
            } else {
                return@let it
            }
        }
        ticketHandler.closeTicket(ticket, reason, rating)
        discordClient
            .getThreadChannelById(channelId).let {
                if (it == null) {
                    return messTable.errors["notFoundThread"]!!
                } else {
                    if (!it.isArchived) {
                        it.sendMessage(messTable.modals["closeTicket"]!!["reply"]!!).queue()
                    }
                    it.manager
                        .setName(it.name + messTable.generalStrings["suffix_closedTicket"])
                        .setArchived(true)
                        .queue()
                }
            }

        try {
            print(messageId)
            discordClient.getChannelById(TextChannel::class.java, settings.discord["channel_id"] as Long)!!
                .retrieveMessageById(messageId).queue { message ->
                    val rows = DsApp.generateRowsEmbedButtons(
                        messTable.embed["tgRedirectPrefixLink"] + ticket.telegramId,
                        channelId, "", true
                    )
                    message.editMessageComponents(
                        rows[0],
                        rows[1],
                        rows[2]
                    ).setEmbeds(
                        DsApp.generateTicketEmbed(
                            message.embeds[0].author!!.name!!,
                            message.embeds[0].author!!.url!!,
                            message.embeds[0].description!!,
                            message.embeds[0].fields[0].value != messTable.embed["embed_noOne"]!!,
                            message.embeds[0].fields[2].value!!,
                            message.embeds[0].fields[0].value!!,
                            message.embeds[0].footer!!.text!!,
                            TicketState.CLOSED
                        )
                    ).queue()
                }
        } catch(_: Exception) { }
        return messTable.modals["closeTicket"]!!["reply"]!!
    }

    fun suspendTicketHandler() {
        val channelId = (event as ModalInteractionEvent).modalId.split("-")[1].split(":")[0].toLong()
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByChannelId(
            channelId
        )!!
        ticketHandler.suspendTicket(ticket, event.values[0].asString)
        discordClient
            .getThreadChannelById(channelId).let {
                if (it == null) {
                    event.reply(messTable.errors["notFoundThread"]!!).setEphemeral(true).queue()
                } else {
                    if (!it.isArchived) {
                        it.sendMessage(messTable.modals["suspendTicket"]!!["reply"]!!).queue()
                    }
                    it.manager
                        .setName(it.name + messTable.generalStrings["suffix_suspendedTicket"])
                        .setLocked(true)
                        .queue()
                }
            }
        discordClient.getChannelById(TextChannel::class.java, settings.discord["channel_id"] as Long)!!
            .retrieveMessageById(event.modalId.split(":")[1].toLong()).queue { message ->
                message.editMessageEmbeds(
                    DsApp.generateTicketEmbed(
                        message.embeds[0].author!!.name!!,
                        message.embeds[0].author!!.url!!,
                        message.embeds[0].description!!,
                        message.embeds[0].fields[0].value != messTable.embed["embed_noOne"]!!,
                        message.embeds[0].fields[2].value!!,
                        message.embeds[0].fields[0].value!!,
                        message.embeds[0].footer!!.text!!,
                        TicketState.SUSPENDED
                    )
                ).queue()
            }
        event.reply(messTable.modals["suspendTicket"]!!["reply"]!!).setEphemeral(true).queue()
    }
}