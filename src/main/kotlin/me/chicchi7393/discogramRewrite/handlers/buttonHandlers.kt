package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.discord.utils.ReopenTicket
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder


class ButtonHandlers(private val event: ButtonInteractionEvent) {
    private val dbMan = DatabaseManager.instance
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val modalStrs = messTable.modals
    private val buttonsStrs = messTable.buttons
    private val menuStrs = messTable.menu["ticket_menu"]!!
    private val channelId = try {
        event.componentId.split("-")[1].toLong()
    } catch (e: Exception) {
        0
    }

    fun closeButtonTicketHandler() {
        event.reply(
            MessageCreateBuilder()
                .setContent(modalStrs["closeTicket"]!!["title"]!!)
                .addActionRow(
                    StringSelectMenu.create("closereason")
                        .addOptions(
                            SelectOption.of("Underage", "underage-$channelId:${event.message.id}")
                                .withDescription(modalStrs["closeTicket"]!!["underageDescription"]!!)
                                .withEmoji(Emoji.fromUnicode("\uD83D\uDD1E")),

                            SelectOption.of("Controllo età completato", "overage-$channelId:${event.message.id}")
                                .withDescription(modalStrs["closeTicket"]!!["overageDescription"]!!)
                                .withEmoji(Emoji.fromUnicode("✅")),

                            SelectOption.of("Questione risolta", "answeredQuestion-$channelId:${event.message.id}")
                                .withDescription(modalStrs["closeTicket"]!!["questioneRisoltaDescription"]!!)
                                .withEmoji(Emoji.fromUnicode("❔")),

                            SelectOption.of("Segnalazione ricevuta", "reported-$channelId:${event.message.id}")
                                .withDescription(modalStrs["closeTicket"]!!["segnalazioneEffettuataDescription"]!!)
                                .withEmoji(Emoji.fromUnicode("‼")),

                            SelectOption.of("Esegui captcha", "captcha-$channelId:${event.message.id}")
                                .withDescription(modalStrs["closeTicket"]!!["captchaDescription"]!!)
                                .withEmoji(Emoji.fromUnicode("\uD83E\uDD16")),

                            SelectOption.of("Custom", "custom-$channelId:${event.message.id}")
                                .withDescription(modalStrs["closeTicket"]!!["otherDescription"]!!)
                                .withEmoji(Emoji.fromUnicode("\uD83D\uDCDD")),

                            SelectOption.of("Custom senza rating", "custom_no_rating-$channelId:${event.message.id}")
                                .withDescription(modalStrs["closeTicket"]!!["otherDescription"]!!)
                                .withEmoji(Emoji.fromUnicode("\uD83D\uDCC4"))
                        )
                        .build()
                )
                .build()
        ).setEphemeral(true).queue()
    }

    fun suspendButtonTicketHandler() {
        if (event.message.embeds[0].fields[1].value == messTable.generalStrings["ticketState_open"]) {
            event.replyModal(
                Modal
                    .create("suspendModal-${channelId}:${event.message.id}", modalStrs["suspendTicket"]!!["title"]!!)
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
            ReopenTicket().reopenTicket(dbMan.Search().Tickets().getTgIdByChannelId(channelId))
            event.reply(modalStrs["suspendTicket"]!!["reopenTgMessage"]!!)
        }
    }

    fun assignButtonTicketHandler() {
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByChannelId(channelId)!!
        if (dbMan.Search().Assignee().searchAssigneeDocumentById(
                ticket.ticketId
            )!!.modId == 0L
        ) {
            dbMan.Update().Assignees().editAssignee(
                ticket.ticketId,
                event.member!!.idLong
            )
            event.message.editMessageEmbeds(
                DsApp.generateTicketEmbed(
                    event.message.embeds[0].author!!.name!!,
                    event.message.embeds[0].author!!.url!!,
                    event.message.embeds[0].description!!,
                    true,
                    event.message.embeds[0].fields[2].value!!,
                    if (event.member!!.nickname == null) event.member!!.effectiveName else event.member!!.nickname!!,
                    event.message.embeds[0].footer!!.text!!
                )
            ).queue()
            event.reply(buttonsStrs["assign"]!!["assignedReply"]!!).setEphemeral(true).queue()
        } else if (dbMan.Search().Assignee().searchAssigneeDocumentById(
                dbMan.Search().Tickets().searchTicketDocumentByChannelId(channelId)!!.ticketId
            )!!.modId == event.member!!.idLong
        ) {
            event.reply(buttonsStrs["assign"]!!["alreadyHave"]!!).setEphemeral(true).queue()
        } else {
            event.reply(buttonsStrs["assign"]!!["alreadyAssign"]!!).setEphemeral(true).queue()
        }
    }

    private fun menuChooser(optionA: Any, optionB: Any, optionC: Any): Any? {
        val optC = try {
            if (optionC == "kakone") null else optionC
        } catch (_: Exception) {
            optionC
        }
        return if (
            dbMan.Search().Assignee().searchAssigneeDocumentById(
                dbMan.Search().Tickets().searchTicketDocumentByChannelId(channelId)!!.ticketId
            )!!.modId == event.member!!.idLong && !DsApp.isHigherRole(event.member!!)
        ) optionA else if (
            DsApp.isHigherRole(event.member!!)
        ) optionB else optC
    }

    fun menuButtonHandler() {
        val assigneeMenu = menuStrs["assigneeMenu"]!!
        val capomodMenu = menuStrs["capomodMenu"]!!
        val noMenu = menuStrs["noMenu"]!!

        val assigneeRow = ActionRow.of(
            Button.secondary(
                "MenuButton-ticket-removeTicket:$channelId/${event.message.id}",
                menuStrs["freeYourselfTicketButton"]!!
            )
        )
        val capomodRow = ActionRow.of(
            Button.secondary(
                "MenuButton-ticket-removeTicket:$channelId/${event.message.id}",
                menuStrs["freeTicketButton"]!!
            ),
            Button.secondary(
                "MenuButton-ticket-marisaTicket:$channelId/${event.message.id}",
                menuStrs["stealTicket"]!!
            )
        )
        val noMenuRow = "kakone"

        val menu = menuChooser(assigneeMenu, capomodMenu, noMenu) as String
        val row = menuChooser(assigneeRow, capomodRow, noMenuRow) as ActionRow?

        if (row != null) event.reply(MessageCreateBuilder().setContent(menu).addComponents(row).build())
            .setEphemeral(true)
            .queue() else event.reply(MessageCreateBuilder().setContent(menu).build()).setEphemeral(true).queue()
    }
}