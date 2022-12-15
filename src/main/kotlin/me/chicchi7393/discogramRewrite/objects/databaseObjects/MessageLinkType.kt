package me.chicchi7393.discogramRewrite.objects.databaseObjects

import org.bson.BsonTimestamp

data class MessageLinkType(
    var tgMessageId: Long,
    var dsMessageId: Long,
    var unixTimestamp: BsonTimestamp
)