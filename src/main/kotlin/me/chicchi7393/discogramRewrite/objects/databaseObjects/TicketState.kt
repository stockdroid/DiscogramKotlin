package me.chicchi7393.discogramRewrite.objects.databaseObjects

import me.chicchi7393.discogramRewrite.JsonReader

private val messageTable = JsonReader().readJsonMessageTable("messageTable")!!.generalStrings
enum class TicketState {

    OPEN {
        override fun toString(): String {
            return messageTable["ticketState_open"] as String
        }
    },
    SUSPENDED {
        override fun toString(): String {
            return messageTable["ticketState_suspended"] as String
        }
    },
    CLOSED {
        override fun toString(): String {
            return messageTable["ticketState_closed"] as String
        }
    }



}