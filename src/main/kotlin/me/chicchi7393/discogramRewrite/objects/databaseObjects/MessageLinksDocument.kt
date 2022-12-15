package me.chicchi7393.discogramRewrite.objects.databaseObjects

data class MessageLinksDocument(
    var ticketId: Int,
    var messages: List<MessageLinkType>
)