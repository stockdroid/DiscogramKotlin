package me.chicchi7393.discogramRewrite.handlers.selectMenus

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.handlers.selectMenus.actions.answeredQuestion
import me.chicchi7393.discogramRewrite.handlers.selectMenus.actions.overage
import me.chicchi7393.discogramRewrite.handlers.selectMenus.actions.reported
import me.chicchi7393.discogramRewrite.handlers.selectMenus.actions.underage
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

class closeReason(val event: SelectMenuInteractionEvent) {
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val modalStrs = messTable.modals
    private val channel_id = try {
        event.values[0].split("-")[1].split(":")[0].toLong()
    } catch (e: Exception) {
        0
    }

    private fun customReason(rating: Boolean) {
        event.replyModal(
            Modal
                .create(
                    "close${if (rating) "" else "WR"}Modal-${channel_id}:${event.message.id}",
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
        )
            .queue()
    }

    fun handle() {
        when {
            event.values[0].startsWith("underage") -> underage().handle(event)
            event.values[0].startsWith("overage") -> overage().handle(event)
            event.values[0].startsWith("answeredQuestion") -> answeredQuestion().handle(event)
            event.values[0].startsWith("reported") -> reported().handle(event)
            event.values[0].startsWith("custom") -> customReason(true)
            event.values[0].startsWith("custom_no_rating") -> customReason(false)
            else -> {TODO("Option not implemented")}
        }
    }
}