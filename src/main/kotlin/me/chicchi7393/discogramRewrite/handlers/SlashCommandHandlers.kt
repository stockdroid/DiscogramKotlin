package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.handlers.commands.BlockCommands
import me.chicchi7393.discogramRewrite.handlers.commands.MessageHistoryCommand
import me.chicchi7393.discogramRewrite.handlers.commands.TicketListCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class SlashCommandHandlers(val event: SlashCommandInteractionEvent) {
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val commands = messTable.commands
    fun onSlashCommand() {
        when (event.interaction.commandString.replace("/", "").split(" ")[0]) {
            commands["tickets"]!!["name"] -> TicketListCommand(event).ticketList()
            commands["cronologia"]!!["name"] -> MessageHistoryCommand(event).ticketList()
            commands["block"]!!["name"] -> BlockCommands(event).block()
            commands["unblock"]!!["name"] -> BlockCommands(event).unblock()
            else -> TODO("Command not implemented")
        }
    }
}