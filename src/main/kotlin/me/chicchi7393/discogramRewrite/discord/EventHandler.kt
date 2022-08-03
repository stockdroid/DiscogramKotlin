package me.chicchi7393.discogramRewrite.discord

import it.tdlight.jni.TdApi.*
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.handlers.buttonHandlers
import me.chicchi7393.discogramRewrite.handlers.messageMenu.ticketMenu
import me.chicchi7393.discogramRewrite.handlers.modalHandlers
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.telegram.TgApp
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*


class EventHandler : ListenerAdapter() {
    private val settings = JsonReader().readJsonSettings("settings")!!
    private val dbMan = DatabaseManager.instance
    private val tgClient = TgApp.instance.client
    private fun sendContent(tgId: Long, content: InputMessageContent) = tgClient.send(
        SendMessage(tgId, 0, 0, null, null, content)
    ) {}

    private fun downloadFile(url: URL, filename: String): Path {
        val path = Files.createDirectory(
            Path.of(
                System.getProperty("java.io.tmpdir") + "/${
                    Random().nextInt(100000000, 999999999)
                }"
            )
        ).toString()
        url.openStream().use { Files.copy(it, Path.of("$path/$filename")) }
        return Path.of("$path/$filename")
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val buttonHandler = buttonHandlers(event)
        val ticketMenu = ticketMenu(event)
        when {
            event.componentId.startsWith("close") -> buttonHandler.closeButtonTicketHandler()
            event.componentId.startsWith("suspend") -> buttonHandler.suspendButtonTicketHandler()
            event.componentId.startsWith("assign") -> buttonHandler.assignButtonTicketHandler()
            event.componentId.startsWith("menu") -> buttonHandler.menuButtonHandler()
            event.componentId.startsWith("MenuButton-ticket-moveTicket") -> ticketMenu.moveTicket()
            event.componentId.startsWith("MenuButton-ticket-removeTicket") -> ticketMenu.removeTicket()
            event.componentId.startsWith("MenuButton-ticket-marisaTicket") -> ticketMenu.marisaTicket()
            else -> {}
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        val modalHandlers = modalHandlers(event)
        when {
            event.modalId.startsWith("closeModal") -> modalHandlers.closeTicketHandler()
            event.modalId.startsWith("suspendModal") -> modalHandlers.suspendTicketHandler()
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val tgId = dbMan.Search().Tickets().getTgIdByChannelId(event.channel.idLong)
        if (
            !event.isFromType(ChannelType.PRIVATE) &&
            event.channel.name.startsWith(settings.discord["IDPrefix"] as String, true) &&
            !event.author.isBot &&
            !event.message.contentRaw.startsWith("###")
        ) {
            if (event.author.idLong == dbMan.Search().Assignee()
                    .searchAssigneeDocumentById(event.channel.name.split("TCK-")[1].toInt())!!.modId
            ) {
                if (event.message.attachments.isEmpty()) {
                    sendContent(tgId, InputMessageText(FormattedText(event.message.contentRaw, null), false, true))
                } else if (event.message.attachments.isNotEmpty()) {
                    var i = 0
                    for (attach in event.message.attachments) {
                        val path = downloadFile(URL(attach.url), attach.fileName).toString()
                        i++
                        sendContent(
                            tgId,
                            InputMessageDocument(
                                InputFileLocal(path),
                                null,
                                false,
                                FormattedText(if (i == 1) event.message.contentRaw else "", null)
                            )
                        )
                    }
                }
            } else if (event.author.idLong != dbMan.Search().Assignee()
                    .searchAssigneeDocumentById(event.channel.name.split("TCK-")[1].toInt())!!.modId &&
                !event.message.contentRaw.startsWith("###")
            ) {
                event.message.delete().queue()
            }
        }
    }
}