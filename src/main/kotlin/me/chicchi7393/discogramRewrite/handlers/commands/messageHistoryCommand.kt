package me.chicchi7393.discogramRewrite.handlers.commands

import it.tdlight.jni.TdApi
import it.tdlight.jni.TdApi.GetChatHistory
import it.tdlight.jni.TdApi.MessageText
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent


class messageHistoryCommand(val event: SlashCommandInteractionEvent) {
    private val tgClient = TgApp.instance
    private val dbMan = DatabaseManager.instance
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val commStrs = messTable.commands
    private val settings = JsonReader().readJsonSettings("settings")!!

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
                tgClient.client.send(TdApi.SearchPublicChat(username)) {
                    idTransporter.value = it.get().id
                }
            }
        }
        return idTransporter.value
    }

    fun ticketList() {
        val userId = getId()
        if (userId == 0L) {
            event.reply(messTable.errors["user0"]!!).queue()
        } else {
            tgClient.client.send(
                GetChatHistory(
                    userId, 0, 0, try {
                        if (event.options[1].asInt > 100) 100 else event.options[1].asInt
                    } catch (_: Exception) {
                        10
                    }, false
                )
            ) {
                var message = commStrs["cronologia"]!!["template"]!!
                val messages = it.get().messages
                for (mess in messages) {
                    message += """${mess.authorSignature}: ${(mess.content as MessageText).text.text}"""
                }
                val mess_parts = mutableListOf<String>()
                var index = 0
                while (index < message.length) {
                    mess_parts.add(message.substring(index, Math.min(index + 2000, message.length)))
                    index += 2000
                }
                for (part in mess_parts) {
                    if (mess_parts.last() == part) {
                        event.reply(part)
                    } else {
                        event.channel.sendMessage(part).queue()
                    }
                }
            }
        }
    }
}