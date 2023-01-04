package me.chicchi7393.discogramRewrite.handlers.selectMenus.actions

import com.beust.klaxon.Parser
import me.chicchi7393.discogramRewrite.handlers.ModalHandlers
import me.chicchi7393.discogramRewrite.handlers.selectMenus.ReasonAction
import me.chicchi7393.discogramRewrite.moderationapi.ModerationAPI
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.ReasonsDocument
import me.chicchi7393.discogramRewrite.objects.enums.ReasonEnum
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

class Captcha : ReasonAction() {
    private val dbMan = DatabaseManager()
    override fun handle(event: StringSelectInteractionEvent) {
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByChannelId(
            event
                .values[0]
                .split("-")[1]
                .split(":")[0]
                .toLong()
        )!!
        /*
            This file is for the IVDC's Moderation api, it's not relevant, it's just for automating some tasks
        */
        val response = ModerationAPI.captcha(
            ticket.telegramId
        )

        event.reply(
            ModalHandlers(event).closeTicketHandler(
                event
                    .values[0]
                    .split("-")[1]
                    .split(":")[0]
                    .toLong(), event.values[0].split(":")[1].toLong(),
                "Captcha richiesto di nuovo${if (response.code != 420) ", link al messaggio: ${
                    (Parser.default()
                        .parse(StringBuilder(response.body!!.string())) as com.beust.klaxon.JsonObject).obj("response")!!["link"]
                }" else ""}",
                true
            )
        ).setEphemeral(true).queue()

        dbMan.Create().Reasons().createReasonsDocument(
            ReasonsDocument(
                ticket.ticketId,
                ticket.telegramId,
                "Captcha richiesto di nuovo.",
                ReasonEnum.CAPTCHA.ordinal + 1
            )
        )
    }
}