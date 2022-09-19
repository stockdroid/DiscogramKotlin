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

    @JvmStatic
    fun main(args: Array<String>) {
        VariableStorage.init_timestamp = (System.currentTimeMillis() / 1000)
        val tgClient = tgAppClass.createApp()
        val updateHandlers = UpdateHandler(tgClient)
        tgClient.addUpdateHandler(UpdateAuthorizationState::class.java, updateHandlers::authStateUpdate)
        tgClient.addUpdateHandler(UpdateNewMessage::class.java, updateHandlers::onUpdateNewMessage)
        tgClient.addUpdateHandler(UpdateMessageSendSucceeded::class.java, updateHandlers::onUpdateMessageSendSucceeded)
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
    }
}

