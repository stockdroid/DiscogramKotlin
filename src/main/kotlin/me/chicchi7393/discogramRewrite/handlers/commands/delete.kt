package me.chicchi7393.discogramRewrite.handlers.commands

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

class delete(val event: MessageContextInteractionEvent) {
    fun delete() {
        event.target.delete().queue()
    }
}