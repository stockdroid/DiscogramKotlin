package me.chicchi7393.discogramRewrite.handlers.commands

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object idTransporter {
    var value = 0L
}

class blockCommands(val event: SlashCommandInteractionEvent) {
    private val tgClient = TgApp.instance
    private val dbMan = DatabaseManager.instance
    private val settings = JsonReader().readJsonSettings("settings")!!
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val commStrs = messTable.commands

    private fun getId(): Long {
        if (event.options.isEmpty()) {
            try {
                idTransporter.value =
                    dbMan.Search().Tickets().searchTicketDocumentById(
                        event.threadChannel.name.split(" ")[0].replace(settings.discord["idPrefix"] as String, "").toInt()
                    )!!.telegramId
            } catch (_: Exception) {
                idTransporter.value = 0L
            }
        } else {
            val username = event.options[0].asString
            try {
                idTransporter.value = username.toLong()
            } catch (_: Exception) {
                tgClient.client.send(SearchPublicChat(username)) {
                    idTransporter.value = it.get().id
                }
            }
        }
        return idTransporter.value
    }

    fun block() {
        val id = getId()
        if (id == 0L) {
            event.reply(messTable.errors["noUserOrThread"]!!).queue()
        } else {
            tgClient.client.send(GetChat(id)) {
                tgClient.client.send(ToggleMessageSenderIsBlocked(MessageSenderUser(it.get().id), true)) {}
            }
            event.reply(commStrs["block"]!!["success"]!!).queue()
        }
    }

    fun unblock() {
        val id = getId()
        if (id == 0L) {
            event.reply(messTable.errors["noUserOrThread"]!!).queue()
        } else {
            tgClient.client.send(GetChat(id)) {
                tgClient.client.send(ToggleMessageSenderIsBlocked(MessageSenderUser(it.get().id), false)) {}
            }
            event.reply(commStrs["unblock"]!!["success"]!!).queue()
        }
    }
}