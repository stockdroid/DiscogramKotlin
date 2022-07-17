package me.chicchi7393.discogramRewrite.objects.databaseObjects

enum class TicketState {
    OPEN {
        override fun toString(): String {
            return "Aperto"
        }
    },
    SUSPENDED {
        override fun toString(): String {
            return "Sospeso"
        }
    },
    CLOSED {
        override fun toString(): String {
            return "Chiuso"
        }
    }
}