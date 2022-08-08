package me.chicchi7393.discogramRewrite.handlers.commands

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object idTransporter {
    var value = 0L
}

class blockCommands(val event: SlashCommandInteractionEvent) {
    private val tgClient = TgApp.instance
    private val dbMan = DatabaseManager.instance

    private fun getId(): Long {
        if (event.options.isEmpty()) {
            try {
                idTransporter.value =
                    dbMan.Search().Tickets().searchTicketDocumentById(
                        event.threadChannel.name.split(" ")[0].replace("TCK-", "").toInt()
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
            event.reply("Non hai fornito l'username e non sei in un thread").queue()
        } else {
            tgClient.client.send(GetChat(id)) {
                tgClient.client.send(ToggleMessageSenderIsBlocked(MessageSenderUser(it.get().id), true)) {}
            }
            event.reply("Utente bloccato").queue()
        }
    }

    fun unblock() {
        val id = getId()
        if (id == 0L) {
            event.reply("Non hai fornito l'username e non sei in un thread").queue()
        } else {
            tgClient.client.send(GetChat(id)) {
                tgClient.client.send(ToggleMessageSenderIsBlocked(MessageSenderUser(it.get().id), false)) {}
            }
            event.reply("Utente sbloccato").queue()
        }
    }
}