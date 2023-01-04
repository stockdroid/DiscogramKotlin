package me.chicchi7393.discogramRewrite.moderationapi

import com.beust.klaxon.JsonObject
import me.chicchi7393.discogramRewrite.JsonReader
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody


/*
    This file is for the IVDC's Moderation api, it's not relevant, it's just for automating some tasks
 */
object ModerationAPI {
    private var canModApi: Boolean = true
    private val apiSettings = try {
        JsonReader().readJsonSettings()!!.moderationApi!!
    } catch (_: Exception) {
        canModApi = false
        println("Moderation API not present in the settings, must not be the main bot.")
        mapOf()
    }

    private fun triggerEndpoint(endpoint: String, arguments: Map<String, Any>): Response {
        val client = OkHttpClient().newBuilder().build()
        val apiToken = if (canModApi) apiSettings["apiToken"] else ""

        val body: RequestBody = ("""
            {
                "apikey": $apiToken,
                "name": "$endpoint",
                "arguments": ${JsonObject(arguments).toJsonString(true)}
            }
        """.trimIndent()).toRequestBody(
            "application/json".toMediaTypeOrNull()
        )

        val request: Request = Request.Builder()
            .url(if (canModApi) apiSettings["url"]!! else "https://example.org")
            .method("POST", body)
            .addHeader("Content-Type", "application/json").build()

        return if (canModApi) client.newCall(request).execute() else Response.Builder()
            .request(Request.Builder().url("https://some-url.com").build())
            .protocol(Protocol.HTTP_2).code(420).message("")
            .body("{}".toResponseBody("application/json; charset=utf-8".toMediaType()))
            .build()

    }

    // funzione ban
    fun ban(userId: Long, reason: String = ""): Int {
        val response = triggerEndpoint("ban", mapOf<String, Any>("userid" to userId, "reason" to reason))
        response.body?.close()
        return if (response.code == 200) 0 else 1
    }

    // funzione unmute (smuta automaticamente)
    fun unmute(userId: Long, reason: String = ""): Int {
        val response = triggerEndpoint("unmute", mapOf<String, Any>("userid" to userId, "reason" to reason))
        response.body?.close()
        return if (response.code == 200) 0 else 1
    }

    // funzione captcha
    fun captcha(userId: Long): Response {
        return triggerEndpoint("captcha", mapOf<String, Any>("userid" to userId))
    }
}