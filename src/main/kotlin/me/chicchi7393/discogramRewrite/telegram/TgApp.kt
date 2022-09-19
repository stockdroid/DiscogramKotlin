package me.chicchi7393.discogramRewrite.telegram

import it.tdlight.client.APIToken
import it.tdlight.client.AuthenticationData
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.client.TDLibSettings
import it.tdlight.common.Init
import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.utilities.VariableStorage
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Paths


class TgApp private constructor() {
    private val settings = JsonReader().readJsonSettings()!!

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
        tgSettings.databaseDirectoryPath =
            sessionPath.resolve(if (VariableStorage.isProd) "database" else "database_dev")
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

    private fun remoteDownloadFile(url: URL) {
        url.openStream().use { inp ->
            BufferedInputStream(inp).use { bis ->
                FileOutputStream("./session/database/5900.jpg").use { fos ->
                    val data = ByteArray(1024)
                    var count: Int
                    while (bis.read(data, 0, 1024).also { count = it } != -1) {
                        fos.write(data, 0, count)
                    }
                }
            }
        }
    }

    fun downloadFile(file_id: Int) {
        if (file_id == 69420) {
            remoteDownloadFile(
                URL(settings.discord["no_pfp_placeholder"] as String)
            )
        } else {
            client.send(DownloadFile(file_id, 32, 0, 0, true)) {}
        }
        Thread.sleep(800)
    }

    fun alertTicket(chat_title: String, message: String, thread_link: String) {
        client.send(
            SendMessage(
                (settings.telegram["moderatorGroup"] as Number).toLong(),
                0L,
                0L,
                null,
                null,
                InputMessageText(
                    FormattedText(
                        "Nuovo ticket!\nDa: ${chat_title}\nMessaggio: ${message}\nLink: $thread_link",
                        null
                    ),
                    false,
                    false
                )
            )
        ) {
            it.get()
        }
    }
}
