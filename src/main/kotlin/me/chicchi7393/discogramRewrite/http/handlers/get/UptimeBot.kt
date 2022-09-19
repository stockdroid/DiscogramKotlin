package me.chicchi7393.discogramRewrite.http.handlers.get

import io.javalin.http.Context
import me.chicchi7393.discogramRewrite.http.handlers.HTTPHandlerClass
import me.chicchi7393.discogramRewrite.utilities.VariableStorage

class UptimeBot : HTTPHandlerClass() {
    override var path = "/api/uptime"
    override fun handle(ctx: Context): Context {
        return ctx.result(VariableStorage.init_timestamp.toString())
    }
}