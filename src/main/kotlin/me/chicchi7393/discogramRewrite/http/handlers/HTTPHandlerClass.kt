package me.chicchi7393.discogramRewrite.http.handlers

import io.javalin.http.Context

open class HTTPHandlerClass {
    open var path = "/"
    open fun handle(ctx: Context): Context {
        return ctx.result("No code to handle this request has been specified.")
    }
}