package me.chicchi7393.discogramRewrite

import com.beust.klaxon.Klaxon
import me.chicchi7393.discogramRewrite.objects.MessageTableObject
import me.chicchi7393.discogramRewrite.objects.SettingsObject
import java.io.File

class JsonReader {
    private fun getResourceFile(fileName: String): File {
        return File(this.javaClass.classLoader.getResource("json/$fileName.json")!!.toURI())
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