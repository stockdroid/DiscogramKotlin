package me.chicchi7393.discogramRewrite.handlers.commands

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.utils.getId
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent


class BlockCommands(val event: SlashCommandInteractionEvent) {
    private val tgClient = TgApp.client
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val commStrs = messTable.commands

    fun block() {
        val id = getId(event)
        if (id == 0L) {
            event.reply(messTable.errors["noUserOrThread"]!!).queue()
        } else {
            tgClient.send(GetChat(id)) {
                tgClient.send(ToggleMessageSenderIsBlocked(MessageSenderUser(it.get().id), true)) {}
            }
            event.reply(commStrs["block"]!!["success"]!!).queue()
        }
    }

    fun unblock() {
        val id = getId(event)
        if (id == 0L) {
            event.reply(messTable.errors["noUserOrThread"]!!).queue()
        } else {
            tgClient.send(GetChat(id)) {
                tgClient.send(ToggleMessageSenderIsBlocked(MessageSenderUser(it.get().id), false)) {}
            }
            event.reply(commStrs["unblock"]!!["success"]!!).queue()
        }
    }
}