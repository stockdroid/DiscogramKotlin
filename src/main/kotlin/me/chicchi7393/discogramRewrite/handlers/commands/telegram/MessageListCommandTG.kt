package me.chicchi7393.discogramRewrite.handlers.commands.telegram

import it.tdlight.jni.TdApi
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.handlers.commands.telegram.utils.getIdTg
import me.chicchi7393.discogramRewrite.telegram.TgApp
import kotlin.math.min

class MessageListCommandTG {
    private val client = TgApp.client
    private val settings = JsonReader().readJsonSettings()!!
    private val messTable = JsonReader().readJsonMessageTable("messageTable")!!
    private val commStrs = messTable.commands
    fun handle(commandContent: String) {
        val content = commandContent.replace("cronologia ", "")
        val userId = getIdTg(content.split(" ")[0])
        if (userId == 0L) {
            TgApp.sendMessage(
                (settings.telegram["moderatorGroup"] as Number).toLong(),
                messTable.errors["user0"]!!,
                0
            ) {}
        } else {
            client.send(
                TdApi.GetChatHistory(
                    userId, 0, 0, try {
                        if (content.split(" ")[1].toInt() > 100) 100 else content.split(" ")[0].toInt()
                    } catch (_: Exception) {
                        10
                    }, false
                )
            ) {
                var message = commStrs["cronologia"]!!["template"]!!
                val messages = it.get().messages
                for (mess in messages) {
                    message += "${if ((mess.senderId as TdApi.MessageSenderUser).userId == (settings.telegram["userbotID"] as Number).toLong()) commStrs["cronologia"]!!["assistance"] else commStrs["cronologia"]!!["user"]}: ${(mess.content as TdApi.MessageText).text.text}\n"
                }
                val messParts = mutableListOf<String>()
                var index = 0
                while (index < message.length) {
                    messParts.add(message.substring(index, min(index + 2000, message.length)))
                    index += 2000
                }
                for (part in messParts.reversed()) TgApp.sendMessage(
                    (settings.telegram["moderatorGroup"] as Number).toLong(),
                    part,
                    0
                ) {}
            }
        }
    }
}