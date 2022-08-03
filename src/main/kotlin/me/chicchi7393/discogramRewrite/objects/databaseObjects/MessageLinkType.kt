package me.chicchi7393.discogramRewrite.objects.databaseObjects

import org.bson.BsonTimestamp

data class MessageLinkType(
    var tg_message_id: Long,
    var ds_message_id: Long,
    var unixTimestamp: BsonTimestamp
)