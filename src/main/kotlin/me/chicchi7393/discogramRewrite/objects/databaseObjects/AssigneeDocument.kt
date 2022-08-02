package me.chicchi7393.discogramRewrite.objects.databaseObjects

data class AssigneeDocument(
    var ticketId: Int,
    var modId: Long,
    var previousAssignees: List<Long>
)
