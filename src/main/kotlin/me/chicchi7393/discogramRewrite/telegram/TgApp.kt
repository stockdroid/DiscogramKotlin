package me.chicchi7393.discogramRewrite.telegram

import it.tdlight.client.APIToken
import it.tdlight.client.AuthenticationData
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.client.TDLibSettings
import it.tdlight.common.Init
import me.chicchi7393.discogramRewrite.JsonReader
import java.nio.file.Paths

class TgApp private constructor() {
    private val settings = JsonReader().readJsonSettings("settings")!!

    init {
        println("TgApp Class Initialized")
    }

    private object GetInstance {
        val INSTANCE = TgApp()
    }

    companion object {
        val instance: TgApp by lazy { GetInstance.INSTANCE }
    }

    lateinit var client: SimpleTelegramClient

    fun createApp(): SimpleTelegramClient {
        val tg = settings.telegram
        val tgSettings = TDLibSettings.create(
            APIToken(tg["api_id"] as Int, tg["api_hash"] as String?)
        )
        val sessionPath = Paths.get("session")

        Init.start()
        tgSettings.databaseDirectoryPath = sessionPath.resolve("database")
        tgSettings.downloadedFilesDirectoryPath = sessionPath.resolve("downloads")

        tgSettings.applicationVersion = tg["app_version"] as String?
        tgSettings.deviceModel = tg["model"] as String?
        tgSettings.systemLanguageCode = tg["language_code"] as String?
        tgSettings.systemVersion = tg["system_version"] as String?
        client = SimpleTelegramClient(tgSettings)
        return client
    }

    fun generateAuth(): AuthenticationData {
        return AuthenticationData.user(settings.telegram["phone_number"] as Long)
    }
}
