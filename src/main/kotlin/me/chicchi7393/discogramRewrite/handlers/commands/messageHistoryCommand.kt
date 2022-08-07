package me.chicchi7393.discogramRewrite.handlers.commands

import it.tdlight.jni.TdApi
import it.tdlight.jni.TdApi.GetChatHistory
import it.tdlight.jni.TdApi.MessageText
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent


class messageHistoryCommand(val event: SlashCommandInteractionEvent) {
    private val tgClient = TgApp.instance
    private val dbMan = DatabaseManager.instance

    private fun getId(): Long {
        var res = mutableListOf<Long>()
        if (event!!.options.isEmpty()) {
            try {
                res.add(dbMan.Search().Tickets().searchTicketDocumentById(event.threadChannel.name.split(" ")[0].replace("TCK-", "").toInt())!!.telegramId )
            } catch (_: Exception) {
                res.add(0L)
            }
        } else {
            val username = event!!.options[0].asString
            try {
                res.add(username.toLong())
            } catch (_: Exception) {
                tgClient.client.send(TdApi.SearchPublicChat(username)) {
                    res.add(it.get().id)
                }
            }
        }
        return res[0]
    }

    fun ticketList() {
        val userId = getId()
        if (userId == 0L) {
            event.reply("Non dovrebbe accadere, ma l'userid è risultato 0").queue()
        } else {
            tgClient.client.send(GetChatHistory(userId, 1L, 0, try {event!!.options[1].asInt} catch(_:Exception) {999}, false)) {
                var message = """Cronologia messaggi:
                |
                """.trimMargin()
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