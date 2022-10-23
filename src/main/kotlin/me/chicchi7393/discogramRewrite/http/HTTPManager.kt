package me.chicchi7393.discogramRewrite.http

import io.javalin.Javalin
import io.javalin.http.HandlerType
import me.chicchi7393.discogramRewrite.http.handlers.HTTPHandlerClass
import me.chicchi7393.discogramRewrite.http.utilities.FindClass

object HTTPManager {
    private lateinit var app: Javalin
    private val methods = mapOf("get" to HandlerType.GET, "post" to HandlerType.POST)
    fun createApp(port: Int) {
        app = Javalin.create().start(port)
        for (method in methods) {
            val classes = FindClass.findClasses("me.chicchi7393.discogramRewrite.http.handlers.${method.key}")
            for (className in classes) {
                val temp = Class.forName(className).getDeclaredConstructor()
                temp.isAccessible = true
                val handlerClass = temp.newInstance()
                app.addHandler(method.value, (handlerClass as HTTPHandlerClass).path) { ctx ->
                    handlerClass.handle(
                        ctx
                    )
                }
            }
        }

    }
}