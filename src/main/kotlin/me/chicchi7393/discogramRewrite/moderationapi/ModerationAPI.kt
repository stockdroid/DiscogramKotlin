package me.chicchi7393.discogramRewrite.moderationapi

import com.beust.klaxon.JsonObject
import me.chicchi7393.discogramRewrite.JsonReader
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response


object ModerationAPI {
    private val apiSettings = JsonReader().readJsonSettings()!!.moderationApi

    private fun triggerEndpoint(endpoint: String, arguments: Map<String, Any>): Response {
        val client = OkHttpClient().newBuilder().build()

        val body: RequestBody = ("""
            {
                "apikey": ${apiSettings["apiToken"]},
                "name": $endpoint,
                "arguments": ${JsonObject(arguments).toJsonString(true)}
            }
        """.trimIndent()).toRequestBody(
            "application/json".toMediaTypeOrNull()
        )

        val request: Request = Request.Builder()
            .url("https://crisatici.stockdroid.it:8443/api/v1")
            .method("POST", body)
            .addHeader("Content-Type", "application/json").build()

        return client.newCall(request).execute()
    }

    // funzione ban
    fun ban(user_id: Long, reason: String = ""): Int {
        val response = triggerEndpoint("ban", mapOf<String, Any>("userid" to user_id, "reason" to reason))
        return if (response.code == 200) 0 else 1
    }

    // funzione unmute (smuta automaticamente)
    fun unmute(user_id: Long, reason: String = ""): Int {
        val response = triggerEndpoint("unmute", mapOf<String, Any>("userid" to user_id, "reason" to reason))
        return if (response.code == 200) 0 else 1
    }

    // funzione captcha
    fun captcha(user_id: Long): Response {
        return triggerEndpoint("captcha", mapOf<String, Any>("userid" to user_id))
    }
}