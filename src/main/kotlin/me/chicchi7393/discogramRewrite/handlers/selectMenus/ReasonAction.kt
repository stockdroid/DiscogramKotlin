package me.chicchi7393.discogramRewrite.handlers.selectMenus

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

open class ReasonAction {
    open fun handle(event: StringSelectInteractionEvent) {
        event.reply("todo")
    }
}