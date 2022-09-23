package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.discord.utils.reopenTicket
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle


class buttonHandlers(private val event: ButtonInteractionEvent) {
    private val dbMan = DatabaseManager.instance
    private val discordClient = DsApp.instance
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val modalStrs = messTable.modals
    private val buttonsStrs = messTable.buttons
    private val menuStrs = messTable.menu["ticket_menu"]!!
    private val channel_id = try {
        event.componentId.split("-")[1].toLong()
    } catch (e: Exception) {
        0
    }

    fun closeButtonTicketHandler(rating: Boolean) {
        event.reply(
            MessageBuilder()
                .setContent(if (rating) modalStrs["closeTicket"]!!["title"]!! else modalStrs["closeTicket"]!!["titleWR"]!!)
                .setActionRows(
                    ActionRow.of(
                        SelectMenu.create("closereason")
                            .addOption(
                                "Underage",
                                "underage-$channel_id:${event.message.id}",
                                modalStrs["close_ticket"]!!["underageDescription"]!!
                            )
                            .addOption(
                                "Controllo età completato",
                                "overage-$channel_id:${event.message.id}",
                                modalStrs["close_ticket"]!!["underageDescription"]!!
                            )
                            .addOption(
                                "Questione risolta",
                                "answeredQuestion-$channel_id:${event.message.id}",
                                modalStrs["close_ticket"]!!["questioneRisoltaDescription"]!!
                            )
                            .addOption(
                                "Segnalazione ricevuta",
                                "reported-$channel_id:${event.message.id}",
                                modalStrs["close_ticket"]!!["segnalazioneEffettuataDescription"]!!
                            )
                            .addOption(
                                "Esegui captcha",
                                "captcha-$channel_id:${event.message.id}",
                                modalStrs["close_ticket"]!!["captchaDescription"]!!
                            )
                            .addOption(
                                "Custom",
                                "custom-$channel_id:${event.message.id}",
                                modalStrs["close_ticket"]!!["otherDescription"]!!
                            )
                            .addOption(
                                "Custom senza rating",
                                "custom_no_rating-$channel_id:${event.message.id}",
                                modalStrs["close_ticket"]!!["otherDescription"]!!
                            )
                            .build()
                    )
                )
                .build()
        ).setEphemeral(true).queue()
    }

    fun suspendButtonTicketHandler() {
        if (event.message.embeds[0].fields[2].value == messTable.generalStrings["ticketState_open"]) {
            event.replyModal(
                Modal
                    .create("suspendModal-${channel_id}:${event.message.id}", modalStrs["suspendTicket"]!!["title"]!!)
                    .addActionRow(
                        ActionRow.of(
                            TextInput.create(
                                "reason",
                                modalStrs["suspendTicket"]!!["reasonText"]!!,
                                TextInputStyle.PARAGRAPH
                            )
                                .setPlaceholder(modalStrs["suspendTicket"]!!["reasonPlaceholder"]!!)
                                .setRequiredRange(0, 1000)
                                .setRequired(false)
                                .build()
                        ).actionComponents
                    )
                    .build()
            ).queue()
        } else {
            reopenTicket().reopenTicket(dbMan.Search().Tickets().getTgIdByChannelId(channel_id))
            event.reply(modalStrs["suspendTicket"]!!["reopenTgMessage"]!!)
        }
    }

    fun assignButtonTicketHandler() {
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByChannelId(channel_id)!!
        if (dbMan.Search().Assignee().searchAssigneeDocumentById(
                ticket.ticketId
            )!!.modId == 0L || discordClient.isHigherRole(event.member!!)
        ) {
            dbMan.Update().Assignees().editAssignee(
                ticket.ticketId,
                event.member!!.idLong
            )
            event.message.editMessageEmbeds(
                discordClient.generateTicketEmbed(
                    event.message.embeds[0].author!!.name!!,
                    event.message.embeds[0].author!!.url!!,
                    event.message.embeds[0].description!!,
                    event.message.embeds[0].fields[0].value == messTable.embed["embed_yes"],
                    true,
                    ticket.telegramId.toString(),
                    if (event.member!!.nickname == null) event.member!!.effectiveName else event.member!!.nickname!!,
                    event.message.embeds[0].footer!!.text!!,
                    event.message.embeds[0].fields[2].value!!
                )
            ).queue()
            event.reply(buttonsStrs["assign"]!!["assignedReply"]!!).setEphemeral(true).queue()
        } else if (dbMan.Search().Assignee().searchAssigneeDocumentById(
                dbMan.Search().Tickets().searchTicketDocumentByChannelId(channel_id)!!.ticketId
            )!!.modId == event.member!!.idLong
        ) {
            event.reply(buttonsStrs["assign"]!!["alreadyHave"]!!).setEphemeral(true).queue()
        } else {
            event.reply(buttonsStrs["assign"]!!["alreadyAssign"]!!).setEphemeral(true).queue()
        }
    }

    private fun menuChooser(option_a: Any, option_b: Any, option_c: Any): Any? {
        val opt_c = try {
            if (option_c == "kakone") null else option_c
        } catch (_: Exception) {
            option_c
        }
        return if (
            dbMan.Search().Assignee().searchAssigneeDocumentById(
                dbMan.Search().Tickets().searchTicketDocumentByChannelId(channel_id)!!.ticketId
            )!!.modId == event.member!!.idLong && !discordClient.isHigherRole(event.member!!)
        ) option_a else if (
            discordClient.isHigherRole(event.member!!)
        ) option_b else opt_c
    }

    fun menuButtonHandler() {
        val ASSIGNEE_MENU = menuStrs["assigneeMenu"]!!
        val CAPOMOD_MENU = menuStrs["capomodMenu"]!!
        val NO_MENU = menuStrs["noMenu"]!!

        val ASSIGNEE_ROW = ActionRow.of(
            Button.secondary(
                "MenuButton-ticket-removeTicket:$channel_id/${event.message.id}",
                menuStrs["freeYourselfTicketButton"]!!
            )
        )
        val CAPOMOD_ROW = ActionRow.of(
            Button.secondary(
                "MenuButton-ticket-removeTicket:$channel_id/${event.message.id}",
                menuStrs["freeTicketButton"]!!
            ),
            Button.secondary(
                "MenuButton-ticket-marisaTicket:$channel_id/${event.message.id}",
                menuStrs["stealTicket"]!!
            )
        )
        val NO_MENU_ROW = "kakone"

        val menu = menuChooser(ASSIGNEE_MENU, CAPOMOD_MENU, NO_MENU) as String
        val row = menuChooser(ASSIGNEE_ROW, CAPOMOD_ROW, NO_MENU_ROW) as ActionRow?

        if (row != null) event.reply(MessageBuilder().setContent(menu).setActionRows(row).build()).setEphemeral(true)
            .queue() else event.reply(MessageBuilder().setContent(menu).build()).setEphemeral(true).queue()
    }
}