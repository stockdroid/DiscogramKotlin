package me.chicchi7393.discogramRewrite.handlers.commands

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.utils.dAndSendMess
import me.chicchi7393.discogramRewrite.discord.utils.getId
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent


class MessageHistoryCommand(val event: SlashCommandInteractionEvent) {
    private val settings = JsonReader().readJsonSettings()!!
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val commStrs = messTable.commands

    fun ticketList() {
        val userId = getId(event)
        if (userId == 0L) {
            event.reply(messTable.errors["user0"]!!).queue()
        } else {
            TgApp.client.send(
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
                    message += "${if ((mess.senderId as MessageSenderUser).userId == (settings.telegram["userbotID"] as Number).toLong()) commStrs["cronologia"]!!["assistance"] else commStrs["cronologia"]!!["user"]}: ${(mess.content as MessageText).text.text}\n"
                }
                dAndSendMess(message, event)
            }
        }
    }
}