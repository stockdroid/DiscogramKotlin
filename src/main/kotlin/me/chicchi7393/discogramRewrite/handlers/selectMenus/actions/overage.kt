package me.chicchi7393.discogramRewrite.handlers.selectMenus.actions

import me.chicchi7393.discogramRewrite.handlers.modalHandlers
import me.chicchi7393.discogramRewrite.handlers.selectMenus.ReasonAction
import me.chicchi7393.discogramRewrite.moderationapi.ModerationAPI
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.ReasonsDocument
import me.chicchi7393.discogramRewrite.objects.enums.ReasonEnum
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

class overage : ReasonAction() {
    private val dbman = DatabaseManager()

    override fun handle(event: SelectMenuInteractionEvent) {
        val ticket = dbman.Search().Tickets().searchTicketDocumentByChannelId(
            event
                .values[0]
                .split("-")[1]
                .split(":")[0]
                .toLong()
        )!!

        ModerationAPI.unmute(
            ticket.telegramId, "raggiunto 14 anni"
        )

        dbman.Create().Reasons().createReasonsDocument(
            ReasonsDocument(
                ticket.ticketId,
                ticket.telegramId,
                "Verifica completata, grazie per la collaborazione!",
                ReasonEnum.OVERAGE.ordinal + 1
            )
        )

        event.reply(
            modalHandlers(event).closeTicketHandler(
                event
                    .values[0]
                    .split("-")[1]
                    .split(":")[0]
                    .toLong(), event.values[0].split(":")[1].toLong(),
                "Verifica completata, grazie per la collaborazione!",
                true
            )
        ).setEphemeral(true).queue()
    }
}