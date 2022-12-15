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
        val tgSettings = TDLibSettings.create(
            APIToken(settings.telegram["api_id"] as Int, settings.telegram["api_hash"] as String)
        )

        Init.start()
        tgSettings.databaseDirectoryPath =
            Paths.get("session").resolve(if (VariableStorage.isProd) "database" else "database_dev")
        tgSettings.downloadedFilesDirectoryPath = Paths.get("session").resolve("downloads")

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
            FormattedText(message, null), false, false
        ),
        callback: (it.tdlight.client.Result<Message>) -> Unit
    ) {
        client.send(
            SendMessage(
                chatId, 0,
                replyId,
                null, null,
                inputMessageContent
            )
        ) { callback(it) }
    }

    fun downloadPic(pfp: ChatPhotoInfo?): FileInputStream {
        java.io.File("session/database/profile_photos").deleteRecursively()
        downloadFile(
            try {
                pfp!!.small.id
            } catch (_: NullPointerException) {
                69420
            }
        )
        Thread.sleep(500)
        return DsApp.getLastModified()
    }

    private fun downloadPlaceholder() = URL(settings.discord["no_pfp_placeholder"] as String).openStream().use { inp ->
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

    private fun downloadFile(fileId: Int) =
        if (fileId == 69420) downloadPlaceholder() else client.send(DownloadFile(fileId, 32, 0, 0, true)) {}

    fun alertTicket(chatTitle: String, message: String, threadLink: String) =
        sendMessage(
            (settings.telegram["moderatorGroup"] as Number).toLong(),
            "Nuovo ticket!\nDa: ${chatTitle}\nMessaggio: ${message}\nLink: $threadLink", 0
        ) {}
}
