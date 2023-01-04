package me.chicchi7393.discogramRewrite.objects

class SettingsObject(
    val telegram: Map<String, Any>,
    val discord: Map<String, Any>,
    val mongodb: Map<String, String>,
    val moderationApi: Map<String, String>? = null
)