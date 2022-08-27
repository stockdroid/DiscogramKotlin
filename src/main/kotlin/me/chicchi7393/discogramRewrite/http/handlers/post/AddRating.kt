package me.chicchi7393.discogramRewrite.http.handlers.post

import com.beust.klaxon.Klaxon
import io.javalin.http.Context
import it.tdlight.jni.TdApi
import it.tdlight.jni.TdApi.InputMessageText
import it.tdlight.jni.TdApi.SendMessage
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.http.handlers.HTTPHandlerClass
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.RatingDocument
import me.chicchi7393.discogramRewrite.telegram.TgApp

class AddRating: HTTPHandlerClass() {
    private val dbMan = DatabaseManager.instance
    private val tgClient = TgApp.instance
    private val settings = JsonReader().readJsonSettings("settings")!!
    val ratMess = JsonReader().readJsonMessageTable("messageTable")!!.ratings

    override var path = "/add"
    override fun handle(ctx: Context): Context {
        dbMan.Create().Ratings().createRatingDocument(
            RatingDocument(
                ctx.formParam("id")!!.toInt(),
                ctx.formParam("speedRating")!!.toFloat(),
                ctx.formParam("gentilezzaRating")!!.toFloat(),
                ctx.formParam("yesFixed")!!.toBoolean(),
                ctx.formParam("noFixed")!!.toBoolean(),
                ctx.formParam("generalRating")!!.toFloat(),
                ctx.formParam("comments")!!
            )
        )
        println("Added rating")

        var messageText = "${ratMess["prefix"]}\n" +
                "${ratMess["ticketIDDesc"]}: ${ctx.formParam("id")!!}\n" +
                "${ratMess["speedDesc"]}: ${ctx.formParam("speedRating")!!}\n" +
                "${ratMess["gentilezzaDesc"]}: ${ctx.formParam("gentilezzaRating")!!}\n" +
                "${ratMess["generalDesc"]}: ${ctx.formParam("generalRating")!!}\n" +
                if (ctx.formParam("yesFixed")!!.toBoolean() || ctx.formParam("noFixed")!!.toBoolean()) "${ratMess["isProblemFixedDesc"]}? ${if (ctx.formParam("yesFixed")!!.toBoolean()) ratMess["problemFixedYes"] else ratMess["problemFixedNo"]}\n" else ""
        if (ctx.formParam("comments")!! != "") {
            messageText += "${ratMess["commentsDesc"]}: ${ctx.formParam("comments")!!}"
        }
        tgClient.client.send(SendMessage((settings.telegram["moderatorGroup"] as Number).toLong(), 0, 0, null, null, InputMessageText(
            TdApi.FormattedText(messageText, null), false, false))) {}
        return ctx.result("Added")
    }
}