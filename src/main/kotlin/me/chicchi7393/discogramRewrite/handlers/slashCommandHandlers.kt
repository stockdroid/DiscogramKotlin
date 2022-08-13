package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.handlers.commands.blockCommands
import me.chicchi7393.discogramRewrite.handlers.commands.messageHistoryCommand
import me.chicchi7393.discogramRewrite.handlers.commands.ticketListCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class slashCommandHandlers(val event: SlashCommandInteractionEvent) {
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val commands = messTable.commands
    fun onSlashCommand() {
        when (event.interaction.commandString.replace("/", "").split(" ")[0]) {
            commands["tickets"]!!["name"] -> ticketListCommand(event).ticketList()
            commands["cronologia"]!!["name"] -> messageHistoryCommand(event).ticketList()
            commands["block"]!!["name"] -> blockCommands(event).block()
            commands["unblock"]!!["name"] -> blockCommands(event).unblock()
            else -> TODO("Command not implemented")
        }
    }
}