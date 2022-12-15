package me.chicchi7393.discogramRewrite.handlers.selectMenus

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.handlers.selectMenus.actions.*
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal

class CloseReason(val event: StringSelectInteractionEvent) {
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val modalStrs = messTable.modals
    private val channelId = try {
        event.values[0].split("-")[1].split(":")[0].toLong()
    } catch (e: Exception) {
        0
    }

    private fun customReason(rating: Boolean) {
        event.replyModal(
            Modal
                .create(
                    "close${if (rating) "" else "WR"}Modal-${channelId}:${event.message.id}",
                    if (rating) modalStrs["closeTicket"]!!["title"]!! else modalStrs["closeTicket"]!!["titleWR"]!!
                )
                .addActionRow(
                    ActionRow.of(
                        TextInput.create("reason", modalStrs["closeTicket"]!!["reasonText"]!!, TextInputStyle.PARAGRAPH)
                            .setPlaceholder(modalStrs["closeTicket"]!!["reasonPlaceholder"]!!)
                            .setRequiredRange(0, 1000)
                            .setRequired(false)
                            .build()
                    ).actionComponents
                )
                .build()
        ).queue()
    }

    fun handle() {
        when {
            event.values[0].startsWith("underage") -> Underage().handle(event)
            event.values[0].startsWith("overage") -> Overage().handle(event)
            event.values[0].startsWith("answeredQuestion") -> AnsweredQuestion().handle(event)
            event.values[0].startsWith("reported") -> Reported().handle(event)
            event.values[0].startsWith("captcha") -> Captcha().handle(event)
            event.values[0].startsWith("custom_no_rating") -> customReason(false)
            event.values[0].startsWith("custom") -> customReason(true)
            else -> {
                TODO("Option not implemented")
            }
        }
    }
}