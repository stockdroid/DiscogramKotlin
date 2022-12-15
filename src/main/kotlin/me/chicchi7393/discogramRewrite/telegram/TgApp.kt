package me.chicchi7393.discogramRewrite.telegram

import it.tdlight.client.APIToken
import it.tdlight.client.AuthenticationData
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.client.TDLibSettings
import it.tdlight.common.Init
import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.utilities.VariableStorage
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Paths


object TgApp {
    private val settings = JsonReader().readJsonSettings()!!

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

    fun sendMessage(
        chatId: Long,
        message: String,
        replyId: Long,
        inputMessageContent: InputMessageContent = InputMessageText(
            FormattedText(message, null),
            false,
            false
        ),
        callback: (it.tdlight.client.Result<Message>) -> Unit
    ) {
        client.send(
            SendMessage(
                chatId,
                0,
                replyId,
                null,
                null,
                inputMessageContent
            )
        ) {
            callback(it)
        }
    }

    fun downloadPic(pfp: ChatPhotoInfo?): FileInputStream {
        java.io.File("session/database/profile_photos").deleteRecursively()
        val pfpId = try {
            pfp!!.small.id
        } catch (_: NullPointerException) {
            69420
        }
        downloadFile(pfpId)
        Thread.sleep(600)
        return DsApp.getLastModified()
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

    private fun downloadFile(fileId: Int) {
        if (fileId == 69420) {
            remoteDownloadFile(
                URL(settings.discord["no_pfp_placeholder"] as String)
            )
        } else {
            client.send(DownloadFile(fileId, 32, 0, 0, true)) {}
        }
    }

    fun alertTicket(chatTitle: String, message: String, threadLink: String) {
        sendMessage(
            (settings.telegram["moderatorGroup"] as Number).toLong(),
            "Nuovo ticket!\nDa: ${chatTitle}\nMessaggio: ${message}\nLink: $threadLink",
            0
        ) {}
    }
}
