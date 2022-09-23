package me.chicchi7393.discogramRewrite.handlers.selectMenus.actions

import com.beust.klaxon.Parser
import me.chicchi7393.discogramRewrite.handlers.modalHandlers
import me.chicchi7393.discogramRewrite.handlers.selectMenus.ReasonAction
import me.chicchi7393.discogramRewrite.moderationapi.ModerationAPI
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

class captcha : ReasonAction() {
    private val dbman = DatabaseManager()
    override fun handle(event: SelectMenuInteractionEvent) {
        val response = ModerationAPI.captcha(
            dbman.Search().Tickets().searchTicketDocumentByChannelId(
                event
                    .values[0]
                    .split("-")[1]
                    .split(":")[0]
                    .toLong()
            )!!.telegramId
        )

        modalHandlers(event).closeTicketHandler(
            event
                .values[0]
                .split("-")[1]
                .split(":")[0]
                .toLong(), event.values[0].split(":")[1].toLong(),
            "Captcha richiesto di nuovo, link al messaggio: ${
                (Parser.default()
                    .parse(StringBuilder(response.body.toString())) as com.beust.klaxon.JsonObject).obj("response")!!["link"]
            }", // in
            true
        )
    }
}