package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.handlers.commands.telegram.MessageListCommandTG
import me.chicchi7393.discogramRewrite.handlers.commands.telegram.ticketListCommandTG

class TelegramCommandsHandler {
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val commands = messTable.commands
    fun onSlashCommand(message: String) {
        if (message.startsWith("/")) {
            when (message.replace("/", "").split(" ")[0]) {
                commands["tickets"]!!["name"] -> ticketListCommandTG().handle(message.replace("/", "").split(" ")[0])
                commands["cronologia"]!!["name"] -> MessageListCommandTG().handle(
                    message.replace("/", "").split(" ")[0]
                )
                else -> TODO("Command not implemented")
            }
        }
    }
}