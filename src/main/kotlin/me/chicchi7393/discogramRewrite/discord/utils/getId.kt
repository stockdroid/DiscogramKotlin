package me.chicchi7393.discogramRewrite.discord.utils

import it.tdlight.jni.TdApi
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object idTransporter {
    var value = 0L
}

private val dbMan = DatabaseManager.instance
private val settings = JsonReader().readJsonSettings("settings")!!
private val tgClient = TgApp.instance

fun getId(event: SlashCommandInteractionEvent): Long {
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
