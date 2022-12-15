package me.chicchi7393.discogramRewrite.http

import io.javalin.Javalin
import io.javalin.http.HandlerType
import me.chicchi7393.discogramRewrite.http.handlers.HTTPHandlerClass
import me.chicchi7393.discogramRewrite.http.utilities.FindClass

object HTTPManager {
    private lateinit var app: Javalin
    private val methods = mapOf("get" to HandlerType.GET, "post" to HandlerType.POST)
    fun createApp(port: Int) {
        app = Javalin.create {
            it.plugins.enableCors { cors ->
                cors.add { corsConf ->
                    corsConf.allowHost("crisatici.stockdroid.it", "stockdroid.it")
                }
            }
        }.start(port)
        for (method in methods) {
            for (className in FindClass.findClasses("me.chicchi7393.discogramRewrite.http.handlers.${method.key}")) {
                val temp = Class.forName(className).getDeclaredConstructor()
                temp.isAccessible = true
                val handlerClass = temp.newInstance()
                app.addHandler(method.value, (handlerClass as HTTPHandlerClass).path) { handlerClass.handle(it) }
            }
        }

    }
}