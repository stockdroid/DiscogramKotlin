package me.chicchi7393.discogramRewrite.telegram.utils

import it.tdlight.jni.TdApi.*

class FindContent(private val input: Message) {
    fun findText(): String {
        val text = when (input.content) {
            is MessageAnimatedEmoji -> (input.content as MessageAnimatedEmoji).emoji
            is MessageVoiceNote -> (input.content as MessageVoiceNote).caption.text
            is MessageDocument -> (input.content as MessageDocument).caption.text
            is MessageAudio -> (input.content as MessageAudio).caption.text
            is MessageVideo -> (input.content as MessageVideo).caption.text
            is MessagePhoto -> (input.content as MessagePhoto).caption.text
            is MessageDice -> (input.content as MessageDice).emoji
            is MessageText -> (input.content as MessageText).text.text
            else -> {
                "ㅤ"
            }
        }
        return if (text != "") text else "ㅤ"
    }

    fun findData(): Int {
        return when (input.content) {
            is MessageVideoNote -> (input.content as MessageVideoNote).videoNote.video.id
            is MessageVoiceNote -> (input.content as MessageVoiceNote).voiceNote.voice.id
            is MessageDocument -> (input.content as MessageDocument).document.document.id
            is MessageSticker -> (input.content as MessageSticker).sticker.sticker.id
            is MessageAudio -> (input.content as MessageAudio).audio.audio.id
            is MessageVideo -> (input.content as MessageVideo).video.video.id
            is MessagePhoto -> (input.content as MessagePhoto).photo.sizes.last().photo.id
            else -> {
                0
            }
        }
    }
}
