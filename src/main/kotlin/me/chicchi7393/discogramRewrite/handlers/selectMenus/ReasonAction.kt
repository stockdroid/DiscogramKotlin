package me.chicchi7393.discogramRewrite.handlers.selectMenus

import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

open class ReasonAction {
    open fun handle(event: SelectMenuInteractionEvent) {
        event.reply("todo")
    }
}