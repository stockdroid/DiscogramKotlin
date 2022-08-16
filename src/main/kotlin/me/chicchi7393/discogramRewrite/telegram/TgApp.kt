package me.chicchi7393.discogramRewrite.telegram

import it.tdlight.client.APIToken
import it.tdlight.client.AuthenticationData
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.client.TDLibSettings
import it.tdlight.common.Init
import it.tdlight.jni.TdApi.DownloadFile
import me.chicchi7393.discogramRewrite.JsonReader
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Paths
import java.util.*


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

    private fun remoteDownloadFile(url: URL) {
        url.openStream().use { inp ->
            BufferedInputStream(inp).use { bis ->
                FileOutputStream("./session/database/profile_photos/5900.jpg").use { fos ->
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
        Thread.sleep(1000)
    }
}
