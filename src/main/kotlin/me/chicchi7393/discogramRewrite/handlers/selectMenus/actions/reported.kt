package me.chicchi7393.discogramRewrite.handlers.selectMenus.actions

import me.chicchi7393.discogramRewrite.handlers.modalHandlers
import me.chicchi7393.discogramRewrite.handlers.selectMenus.ReasonAction
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.ReasonsDocument
import me.chicchi7393.discogramRewrite.objects.enums.ReasonEnum
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

class reported : ReasonAction() {
    private val dbman = DatabaseManager()
    override fun handle(event: SelectMenuInteractionEvent) {
        event.reply(
            modalHandlers(event).closeTicketHandler(
                event
                    .values[0]
                    .split("-")[1]
                    .split(":")[0]
                    .toLong(), event.values[0].split(":")[1].toLong(),
                "Segnalazione effettuata",
                true
            )
        ).setEphemeral(true).queue()


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
                "Segnalazione effettuata",
                ReasonEnum.REPORTED.ordinal + 1
            )
        )
    }
}