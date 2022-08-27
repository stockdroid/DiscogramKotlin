package me.chicchi7393.discogramRewrite.objects.databaseObjects

data class RatingDocument(
    val id: Int,
    val speedRating: Float,
    val gentilezzaRating: Float,
    val yesFixed: Boolean,
    val noFixed: Boolean,
    val generalRating: Float,
    val comments: String
)