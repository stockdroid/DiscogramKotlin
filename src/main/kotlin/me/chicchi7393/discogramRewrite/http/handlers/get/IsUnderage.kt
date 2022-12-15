package me.chicchi7393.discogramRewrite.http.handlers.get

import io.javalin.http.Context
import me.chicchi7393.discogramRewrite.http.handlers.HTTPHandlerClass
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager

class IsUnderage : HTTPHandlerClass() {
    override var path = "/api/isUnderage"
    override fun handle(ctx: Context): Context {
        return ctx.json(mapOf("result" to DatabaseManager.instance.Utils().isUserUnderage(ctx.body().toLong())))
    }
}