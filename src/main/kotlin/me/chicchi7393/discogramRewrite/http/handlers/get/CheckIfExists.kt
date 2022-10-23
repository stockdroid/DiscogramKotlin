package me.chicchi7393.discogramRewrite.http.handlers.get

import io.javalin.http.Context
import me.chicchi7393.discogramRewrite.http.handlers.HTTPHandlerClass
import me.chicchi7393.discogramRewrite.http.utilities.ConvertQueryStringToMap
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager

class CheckIfExists : HTTPHandlerClass() {
    private val dbMan = DatabaseManager.instance

    override var path = "/check"
    override fun handle(ctx: Context): Context {
        try {
            val args = ConvertQueryStringToMap.convert(ctx.req.queryString)
            if (!args.containsKey("id")) return ctx.status(404).result("ID not in args")
            val ticket = dbMan.Search().Tickets().searchTicketDocumentById(args["id"] as Int)
                ?: return ctx.status(404).result("Ticket not found")
            if (ticket.status["open"] == true) return ctx.status(403).result("Ticket still open")
            if (dbMan.Search().Ratings().searchRatingById(args["id"] as Int) != null) return ctx.status(403)
                .result("Ticket already rated")
            return ctx.result("Ticket found")
        } catch (_: NullPointerException) {
            return ctx.status(404).result("No args specified")
        }
    }
}