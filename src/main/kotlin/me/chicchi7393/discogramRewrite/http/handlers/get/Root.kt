package me.chicchi7393.discogramRewrite.http.handlers.get

import io.javalin.http.Context
import me.chicchi7393.discogramRewrite.http.handlers.HTTPHandlerClass

class Root: HTTPHandlerClass() {
    override var path = "/"
    override fun handle(ctx: Context): Context {
        return ctx.result("Cosa cazzo stai cercando?")
    }
}