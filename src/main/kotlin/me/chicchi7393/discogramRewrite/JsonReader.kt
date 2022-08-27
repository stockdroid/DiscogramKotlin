package me.chicchi7393.discogramRewrite

import com.beust.klaxon.Klaxon
import me.chicchi7393.discogramRewrite.objects.MessageTableObject
import me.chicchi7393.discogramRewrite.objects.SettingsObject
import java.io.FileInputStream
import java.io.InputStream

class JsonReader {
    private fun getResourceFile(fileName: String): InputStream {
        return FileInputStream("./json/$fileName.json")!!
    }

    fun readJsonSettings(fileName: String): SettingsObject? {
        return Klaxon()
            .parse<SettingsObject>(getResourceFile(fileName))
    }

    fun readJsonMessageTable(fileName: String): MessageTableObject? {
        return Klaxon()
            .parse<MessageTableObject>(getResourceFile(fileName))
    }
}