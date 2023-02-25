package me.chicchi7393.discogramRewrite.discord.utils

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import kotlin.math.min

fun dAndSendMess(message: String, event: SlashCommandInteractionEvent) {
    val messParts = mutableListOf<String>()
    var index = 0
    while (index < message.length) {
        messParts.add(message.substring(index, min(index + 2000, message.length)))
        index += 2000
    }
    for (part in messParts.reversed()) {
        if (messParts.last() == part) {
            event.reply(part).queue()
        } else {
            event.channel.sendMessage(part).queue()
        }
    }
}