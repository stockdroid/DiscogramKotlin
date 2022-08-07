package me.chicchi7393.discogramRewrite.handlers.commands

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.util.concurrent.atomic.AtomicReference


class blockCommands(val event: SlashCommandInteractionEvent) {
    private val tgClient = TgApp.instance
    private val dbMan = DatabaseManager.instance

    private fun getId(): Long {
        val value = AtomicReference(0L)
        if (event.options.isEmpty()) {
            try {
                value.set(
                    dbMan.Search().Tickets().searchTicketDocumentById(
                        event.threadChannel.name.split(" ")[0].replace("TCK-", "").toInt()
                    )!!.telegramId
                )
            } catch (_: Exception) {
                value.set(0L)
            }
        } else {
            val username = event.options[0].asString
            try {
                value.set(username.toLong())
            } catch (_: Exception) {
                tgClient.client.send(SearchPublicChat(username)) {
                    value.set(it.get().id)
                }
            }
        }
        return value.get()
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