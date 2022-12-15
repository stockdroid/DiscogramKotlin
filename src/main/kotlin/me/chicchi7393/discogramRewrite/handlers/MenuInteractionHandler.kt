package me.chicchi7393.discogramRewrite.handlers

import me.chicchi7393.discogramRewrite.handlers.selectMenus.CloseReason
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

class MenuInteractionHandler(private val event: StringSelectInteractionEvent) {
    fun onSelectMenuInteraction() {
        when (event.componentId) {
            "closereason" -> CloseReason(event).handle()
            else -> {
                TODO("Select Menu not implemented")
            }
        }
    }
}