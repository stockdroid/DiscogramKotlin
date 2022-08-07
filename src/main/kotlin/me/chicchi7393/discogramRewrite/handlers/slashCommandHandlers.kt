package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.handlers.commands.blockCommands
import me.chicchi7393.discogramRewrite.handlers.commands.messageHistoryCommand
import me.chicchi7393.discogramRewrite.handlers.commands.ticketListCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class slashCommandHandlers(val event: SlashCommandInteractionEvent) {
    fun onSlashCommand() {
        when (event.interaction.commandString.replace("/", "").split(" ")[0]) {
            "tickets" -> ticketListCommand(event).ticketList()
            "cronologia" -> messageHistoryCommand(event).ticketList()
            "block" -> blockCommands(event).block()
            "unblock" -> blockCommands(event).unblock()
            else -> TODO("Command not implemented")
        }
    }
}