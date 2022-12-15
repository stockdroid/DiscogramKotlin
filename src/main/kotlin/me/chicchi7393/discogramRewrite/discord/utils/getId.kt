package me.chicchi7393.discogramRewrite.discord.utils

import it.tdlight.jni.TdApi
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object IdTransporter {
    var value = 0L
}

private val dbMan = DatabaseManager.instance
private val settings = JsonReader().readJsonSettings()!!

fun getId(event: SlashCommandInteractionEvent): Long {
    if (event.options.isEmpty()) {
        try {
            IdTransporter.value =
                dbMan.Search().Tickets().searchTicketDocumentById(
                    event.channel.name.split(" ")[0].replace(settings.discord["idPrefix"] as String, "").toInt()
                )!!.telegramId
        } catch (_: Exception) {
            IdTransporter.value = 0L
        }
    } else {
        val username = event.options[0].asString
        try {
            IdTransporter.value = username.toLong()
        } catch (_: Exception) {
            TgApp.client.send(TdApi.SearchPublicChat(username)) {
                IdTransporter.value = it.get().id
            }
        }
    }
    return IdTransporter.value
}
