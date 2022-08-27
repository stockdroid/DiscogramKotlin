package me.chicchi7393.discogramRewrite.http.utilities

object ConvertQueryStringToMap {
    fun convert(query: String): Map<String, Int> {
        return query.split("&").associate {
            val (left, right) = it.split("=")
            left to right.toInt()
        }
    }
}