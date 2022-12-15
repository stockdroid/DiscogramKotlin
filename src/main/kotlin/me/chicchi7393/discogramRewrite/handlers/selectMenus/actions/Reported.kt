package me.chicchi7393.discogramRewrite.handlers.selectMenus.actions

import me.chicchi7393.discogramRewrite.handlers.ModalHandlers
import me.chicchi7393.discogramRewrite.handlers.selectMenus.ReasonAction
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.ReasonsDocument
import me.chicchi7393.discogramRewrite.objects.enums.ReasonEnum
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

class Reported : ReasonAction() {
    private val dbman = DatabaseManager()
    override fun handle(event: StringSelectInteractionEvent) {
        event.reply(
            ModalHandlers(event).closeTicketHandler(
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