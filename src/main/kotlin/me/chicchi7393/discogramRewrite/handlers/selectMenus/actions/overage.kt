package me.chicchi7393.discogramRewrite.handlers.selectMenus.actions

import me.chicchi7393.discogramRewrite.handlers.modalHandlers
import me.chicchi7393.discogramRewrite.handlers.selectMenus.ReasonAction
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

class overage : ReasonAction() {
    override fun handle(event: SelectMenuInteractionEvent) {
        modalHandlers(event).closeTicketHandler(
            event
                .values[0]
                .split("-")[1]
                .split(":")[0]
                .toLong(), event.values[0].split(":")[1].toLong(),
            "Verifica completata, grazie per la collaborazione!",
            true
        )
    }
}