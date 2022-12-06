package me.chicchi7393.discogramRewrite

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.http.HTTPManager
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import me.chicchi7393.discogramRewrite.telegram.UpdateHandler
import me.chicchi7393.discogramRewrite.utilities.VariableStorage

object Main {
    private val tgAppClass = TgApp.instance
    private val dsAppClass = DsApp.instance
    private val settings = JsonReader().readJsonSettings()!!

    @JvmStatic
    fun main(args: Array<String>) {
        VariableStorage.init_timestamp = (System.currentTimeMillis() / 1000)

        val tgClient = tgAppClass.createApp()
        val updateHandlers = UpdateHandler(tgClient)
        tgClient.addUpdateHandler(UpdateAuthorizationState::class.java, updateHandlers::authStateUpdate)
        tgClient.addUpdateHandler(UpdateNewMessage::class.java, updateHandlers::onUpdateNewMessage)
        tgClient.addUpdateHandler(UpdateMessageSendSucceeded::class.java, updateHandlers::onUpdateMessageSendSucceeded)
        tgClient.addUpdateHandler(UpdateChatAction::class.java, updateHandlers::onUpdateChatAction)
        DatabaseManager().createClient()
        HTTPManager.createApp(4763)
        Thread {
            tgClient.start(tgAppClass.generateAuth())
            tgClient.waitForExit()
        }.start()

        Thread {
            dsAppClass.createApp()
            dsAppClass.createCommands()
        }.start()
        Thread.setDefaultUncaughtExceptionHandler { _, paramThrowable ->
            tgClient.send(
                SendMessage(
                    (settings.telegram["moderatorGroup"] as Number).toLong(),
                    0L,
                    0L,
                    null,
                    null,
                    InputMessageText(
                        FormattedText(
                            "Errore avvenuto! @Chicchi7393\nMessaggio: ${paramThrowable.message}\nCausa: ${paramThrowable.cause}",
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
}

