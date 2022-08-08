package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.handlers.commands.delete
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

class messageCommandHandler(val event: MessageContextInteractionEvent) {
    fun handle() {
        when (event.name) {
            "Elimina messaggio" -> delete(event).delete()
            else -> TODO()
        }
    }
}