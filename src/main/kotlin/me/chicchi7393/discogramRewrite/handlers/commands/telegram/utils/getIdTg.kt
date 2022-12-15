package me.chicchi7393.discogramRewrite.handlers.commands.telegram.utils

import it.tdlight.jni.TdApi
import me.chicchi7393.discogramRewrite.discord.utils.IdTransporter
import me.chicchi7393.discogramRewrite.telegram.TgApp

fun getIdTg(option: String): Long {
    if (option.isEmpty()) {
        IdTransporter.value = 0L
    } else {
        try {
            IdTransporter.value = option.toLong()
        } catch (_: Exception) {
            TgApp.client.send(TdApi.SearchPublicChat(option)) {
                IdTransporter.value = it.get().id
            }
        }
    }
    return IdTransporter.value
}
