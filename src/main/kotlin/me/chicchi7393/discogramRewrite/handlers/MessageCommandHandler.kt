package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.handlers.commands.Delete
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

class MessageCommandHandler(val event: MessageContextInteractionEvent) {
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    fun handle() {
        when (event.name) {
            messTable.commands["delete_message"]!!["name"]!! -> Delete(event).delete()
            else -> TODO()
        }
    }
}