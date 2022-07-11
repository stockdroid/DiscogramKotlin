package me.chicchi7393.discogramRewrite.discord
import discord4j.core.DiscordClient
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Color
import me.chicchi7393.discogramRewrite.JsonReader
import java.time.Instant


class DsApp private constructor() {
    private val settings = JsonReader().readJsonSettings("settings")!!

    init {
        println("DsApp Class Initialized")
    }

    private object GetInstance {
        val INSTANCE = DsApp()
    }

    companion object {
        val instance: DsApp by lazy { GetInstance.INSTANCE }
    }

    lateinit var dsClient: DiscordClient

    fun createApp(): DiscordClient {
        dsClient = DiscordClient.create(settings.discord["token"] as String)
        return dsClient
    }
    fun generateTicketEmbed(
            authorName: String,
            authorUrl: String,
            message: String,
            isForced: Boolean,
            isAssigned: Boolean,
            assignedTo: String = "Nessuno",
            footerStr: String
    ): EmbedCreateSpec {
        return EmbedCreateSpec.builder()
            .color(Color.BLUE)
            .title("Nuovo ticket!")
            .author(authorName, authorUrl, "attachment://pic.png")
            .description(message)
            .addField("Forzato?", if (isForced) "Sì" else "No", true)
            .addField("Assegnato a", assignedTo, true)
            .footer(footerStr, null)
            .timestamp(Instant.now())
            .build()
    }
    fun generateFirstEmbedButtons(channel_link: String, tg_profile: String = "https://google.com"): ActionRow {
        return ActionRow.of(listOf(
            Button.link("https://google.com", "Apri chat"),
            Button.link(tg_profile, "Apri profilo Telegram")
        ))
    }
    fun generateSecondEmbedButtons(): ActionRow {
        return ActionRow.of(listOf(
            Button.success("assign", "Assegna"),
            Button.secondary("suspend", "Sospendi"),
            Button.danger("close", "Chiudi"),
        ))
    }

}