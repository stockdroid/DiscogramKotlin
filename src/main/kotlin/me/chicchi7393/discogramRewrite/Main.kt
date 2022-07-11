package me.chicchi7393.discogramRewrite


import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.Message
import it.tdlight.jni.TdApi.UpdateAuthorizationState
import it.tdlight.jni.TdApi.UpdateNewMessage
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.databaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import me.chicchi7393.discogramRewrite.telegram.UpdateHandler
import reactor.core.publisher.Mono


class Main {
    private val tgAppClass = TgApp.instance
    private val tgClient = tgAppClass.createApp()
    private val updateHandlers = UpdateHandler(tgClient)
    private val dsAppClass = DsApp.instance

    fun main() {
        tgClient.addUpdateHandler(UpdateAuthorizationState::class.java, updateHandlers::authStateUpdate)
        tgClient.addUpdateHandler(UpdateNewMessage::class.java, updateHandlers::onUpdateNewMessage)
        val db = databaseManager().createClient()
        Thread {
            tgClient.start(tgAppClass.generateAuth())
            tgClient.waitForExit()
        }.start()

        Thread {
            dsAppClass.createApp().withGateway { client: GatewayDiscordClient ->
                client.on(
                    MessageCreateEvent::class.java
                ) { event: MessageCreateEvent ->
                    val message: Message = event.message
                    print(message)
                    Mono.empty<Any?>()
                }
            }.block()
        }.start()
    }
}

fun main() {
    Main().main()
}
