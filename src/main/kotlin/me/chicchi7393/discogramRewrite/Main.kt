package me.chicchi7393.discogramRewrite

import it.tdlight.jni.TdApi.UpdateAuthorizationState
import it.tdlight.jni.TdApi.UpdateMessageSendSucceeded
import it.tdlight.jni.TdApi.UpdateNewMessage
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import me.chicchi7393.discogramRewrite.telegram.UpdateHandler


class Main {
    private val tgAppClass = TgApp.instance
    private val tgClient = tgAppClass.createApp()
    private val updateHandlers = UpdateHandler(tgClient)
    private val dsAppClass = DsApp.instance

    fun main() {
        tgClient.addUpdateHandler(UpdateAuthorizationState::class.java, updateHandlers::authStateUpdate)
        tgClient.addUpdateHandler(UpdateNewMessage::class.java, updateHandlers::onUpdateNewMessage)
        tgClient.addUpdateHandler(UpdateMessageSendSucceeded::class.java, updateHandlers::onUpdateMessageSendSucceeded)
        DatabaseManager().createClient()
        Thread {
            tgClient.start(tgAppClass.generateAuth())
            tgClient.waitForExit()
        }.start()

        Thread {
            dsAppClass.createApp()
            dsAppClass.createCommands()
        }.start()
    }
}

fun main() {
    Main().main()
}
