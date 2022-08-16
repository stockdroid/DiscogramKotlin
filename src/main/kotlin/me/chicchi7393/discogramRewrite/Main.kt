package me.chicchi7393.discogramRewrite

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import me.chicchi7393.discogramRewrite.telegram.UpdateHandler

object Main {
    private val tgAppClass = TgApp.instance
    private val tgClient = tgAppClass.createApp()
    private val updateHandlers = UpdateHandler(tgClient)
    private val dsAppClass = DsApp.instance
    @JvmStatic
    fun main(args: Array<String>) {
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

