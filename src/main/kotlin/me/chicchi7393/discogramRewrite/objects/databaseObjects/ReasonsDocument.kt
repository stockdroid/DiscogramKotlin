package me.chicchi7393.discogramRewrite.objects.databaseObjects

data class ReasonsDocument(
    val ticketId: Int,
    val telegramId: Long,
    val reason: String,
    val reasonId: Int
)