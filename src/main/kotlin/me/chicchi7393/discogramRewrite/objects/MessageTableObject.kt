package me.chicchi7393.discogramRewrite.objects

class MessageTableObject(
    val generalStrings: Map<String, String>,
    val menu: Map<String, Map<String, String>>,
    val buttons: Map<String, Map<String, String>>,
    val commands: Map<String, Map<String, String>>,
    val modals: Map<String, Map<String, String>>,
    val embed: Map<String, String>,
    val errors: Map<String, String>,
    val ratings: Map<String, String>
)
