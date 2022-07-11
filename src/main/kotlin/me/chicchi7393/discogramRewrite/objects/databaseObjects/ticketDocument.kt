package me.chicchi7393.discogramRewrite.objects.databaseObjects

data class ticketDocument(
    var telegramId: Long,
    var channelId: Long,
    var ticketId: Int,
    var status: Map<String, Boolean>,
    var unixSeconds: Long
)