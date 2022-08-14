package me.chicchi7393.discogramRewrite.handlers.commands

import me.chicchi7393.discogramRewrite.JsonReader
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

class delete(val event: MessageContextInteractionEvent) {
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    fun delete() {
        event.target.delete().queue()
        event.reply(messTable.commands["delete_message"]!!["deletedMessage"]!!).queue()
    }
}