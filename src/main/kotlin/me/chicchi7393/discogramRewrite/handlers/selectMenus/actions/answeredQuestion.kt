package me.chicchi7393.discogramRewrite.handlers.selectMenus.actions

import me.chicchi7393.discogramRewrite.handlers.modalHandlers
import me.chicchi7393.discogramRewrite.handlers.selectMenus.ReasonAction
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.ReasonsDocument
import me.chicchi7393.discogramRewrite.objects.enums.ReasonEnum
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

class answeredQuestion : ReasonAction() {
    private val dbMan = DatabaseManager.instance
    override fun handle(event: SelectMenuInteractionEvent) {
        val ticket = dbMan.Search().Tickets().searchTicketDocumentByChannelId(
            event
                .values[0]
                .split("-")[1]
                .split(":")[0]
                .toLong()
        )!!
        dbMan.Create().Reasons().createReasonsDocument(
            ReasonsDocument(
                ticket.ticketId,
                ticket.telegramId,
                "Questione risolta.",
                ReasonEnum.QUESTION_ANSWERED.ordinal + 1
            )
        )
        event.reply(
            modalHandlers(event).closeTicketHandler(
                event
                    .values[0]
                    .split("-")[1]
                    .split(":")[0]
                    .toLong(), event.values[0].split(":")[1].toLong(),
                "Questione risolta.",
                true
            )
        ).setEphemeral(true).queue()
    }
}