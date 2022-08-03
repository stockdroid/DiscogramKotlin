package me.chicchi7393.discogramRewrite.objects.databaseObjects

data class MessageLinksDocument(
    var ticket_id: Int,
    var messages: List<MessageLinkType>
)