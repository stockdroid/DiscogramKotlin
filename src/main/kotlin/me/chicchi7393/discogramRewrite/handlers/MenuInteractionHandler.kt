package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.handlers.selectMenus.closeReason
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

class MenuInteractionHandler(private val event: SelectMenuInteractionEvent) {
    fun onSelectMenuInteraction() {
        when (event.componentId) {
            "closereason" -> closeReason(event).handle()
            else -> {TODO("Select Menu not implemented")}
        }
    }
}