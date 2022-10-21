package me.chicchi7393.discogramRewrite.objects.databaseObjects

data class ReasonsDocument(
    val ticket_id: Int,
    val telegram_id: Long,
    val reason: String,
    val reason_id: Int
)