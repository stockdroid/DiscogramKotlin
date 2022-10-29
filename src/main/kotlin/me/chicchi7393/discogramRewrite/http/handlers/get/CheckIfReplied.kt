package me.chicchi7393.discogramRewrite.http.handlers.get

import io.javalin.http.Context
import me.chicchi7393.discogramRewrite.http.handlers.HTTPHandlerClass
import me.chicchi7393.discogramRewrite.http.utilities.ConvertQueryStringToMap
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager

class CheckIfReplied : HTTPHandlerClass() {
    private val dbMan = DatabaseManager.instance
    override var path = "/api/checkIfReplied"
    override fun handle(ctx: Context): Context {
        val time: Int = try {
            val args = ConvertQueryStringToMap.convert(ctx.req().queryString)
            args["time"]!!
        } catch (_: NullPointerException) {
            86400
        }
        if (ctx.body().isEmpty() or !ctx.body().all { char -> char.isDigit() }) return ctx.status(406)
        val ticket = dbMan.Search().Tickets().searchTicketDocumentsByTelegramId(ctx.body().toLong())
        if (ticket.isEmpty()) return return ctx.json(mapOf("result" to false))
        if (ticket[0]!!.unixSeconds >= (System.currentTimeMillis() / 1000) - time) {
            return ctx.json(mapOf("result" to true))
        } else {
            return ctx.json(mapOf("result" to false))
        }
    }
}
