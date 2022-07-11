package me.chicchi7393.discogramRewrite
import com.beust.klaxon.Klaxon
import me.chicchi7393.discogramRewrite.objects.MessageTableObject
import me.chicchi7393.discogramRewrite.objects.SettingsObject
import java.io.File
import java.net.URI

class JsonReader {
    private fun getResourceUri(fileName: String): URI {return this.javaClass.classLoader.getResource("json/$fileName.json")!!.toURI()}

    fun readJsonSettings(fileName: String): SettingsObject? {
        return Klaxon()
            .parse<SettingsObject>(File(getResourceUri(fileName)))
    }

    fun readJsonMessageTable(fileName: String): MessageTableObject? {
        return Klaxon()
            .parse<MessageTableObject>(File(getResourceUri(fileName)))
    }
}