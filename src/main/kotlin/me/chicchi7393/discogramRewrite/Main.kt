package me.chicchi7393.discogramRewrite

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.http.HTTPManager
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import me.chicchi7393.discogramRewrite.telegram.UpdateHandler
import me.chicchi7393.discogramRewrite.utilities.VariableStorage

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        VariableStorage.init_timestamp = (System.currentTimeMillis() / 1000)

        val tgClient = TgApp.createApp()

        val updateHandlers = UpdateHandler(tgClient)
        tgClient.addUpdateHandler(UpdateNewMessage::class.java, updateHandlers::onUpdateNewMessage)
        tgClient.addUpdateHandler(UpdateMessageSendSucceeded::class.java, updateHandlers::onUpdateMessageSendSucceeded)
        tgClient.addUpdateHandler(UpdateChatAction::class.java, updateHandlers::onUpdateChatAction)

        DatabaseManager().createClient()
        HTTPManager.createApp(4763)
        Thread {
            tgClient.start(TgApp.generateAuth())
            tgClient.send(SetLogVerbosityLevel(2)) {}
            tgClient.waitForExit()
        }.start()

        Thread {
            DsApp.createApp()
            DsApp.createCommands()
        }.start()
    }
}