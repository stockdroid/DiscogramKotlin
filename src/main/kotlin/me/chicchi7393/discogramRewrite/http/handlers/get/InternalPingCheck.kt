package me.chicchi7393.discogramRewrite.http.handlers.get

import io.javalin.http.Context
import me.chicchi7393.discogramRewrite.http.handlers.HTTPHandlerClass

class InternalPingCheck : HTTPHandlerClass() {
    override var path = "/api/internal/ping"
    override fun handle(ctx: Context): Context {
        return ctx.result("")
    }
}