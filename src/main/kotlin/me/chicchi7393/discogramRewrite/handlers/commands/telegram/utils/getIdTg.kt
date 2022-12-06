package me.chicchi7393.discogramRewrite.handlers.commands.telegram.utils

import it.tdlight.jni.TdApi
import me.chicchi7393.discogramRewrite.discord.utils.idTransporter
import me.chicchi7393.discogramRewrite.telegram.TgApp

fun getIdTg(option: String): Long {
    if (option.isEmpty()) {
        idTransporter.value = 0L
    } else {
        try {
            idTransporter.value = option.toLong()
        } catch (_: Exception) {
            TgApp.instance.client.send(TdApi.SearchPublicChat(option)) {
                idTransporter.value = it.get().id
            }
        }
    }
    return idTransporter.value
}
