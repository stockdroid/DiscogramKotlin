package me.chicchi7393.discogramRewrite.handlers.selectMenus.actions

import me.chicchi7393.discogramRewrite.handlers.modalHandlers
import me.chicchi7393.discogramRewrite.handlers.selectMenus.ReasonAction
import me.chicchi7393.discogramRewrite.moderationapi.ModerationAPI
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.ReasonsDocument
import me.chicchi7393.discogramRewrite.objects.enums.ReasonEnum
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

class underage : ReasonAction() {
    private val dbman = DatabaseManager()
    override fun handle(event: SelectMenuInteractionEvent) {
        val response = modalHandlers(event).closeTicketHandler(
            event
                .values[0]
                .split("-")[1]
                .split(":")[0]
                .toLong(), event.values[0].split(":")[1].toLong(),
            "Underage, ricontattare ai 14 anni",
            true
        )

        event.reply(response).setEphemeral(true).queue()

        // cerca nel database per userID
        ModerationAPI.ban(
            dbman.Search().Tickets().searchTicketDocumentByChannelId(
                event
                    .values[0]
                    .split("-")[1]
                    .split(":")[0]
                    .toLong()
            )!!.telegramId, "underage"
        )

        val ticket = dbman.Search().Tickets().searchTicketDocumentByChannelId(
            event
                .values[0]
                .split("-")[1]
                .split(":")[0]
                .toLong()
        )!!

        dbman.Create().Reasons().createReasonsDocument(
            ReasonsDocument(
                ticket.ticketId,
                ticket.telegramId,
                "Underage, ricontattare ai 14 anni",
                ReasonEnum.UNDERAGE.ordinal + 1
            )
        )
    }
}