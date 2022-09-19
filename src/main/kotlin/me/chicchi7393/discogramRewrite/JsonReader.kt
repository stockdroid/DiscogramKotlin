package me.chicchi7393.discogramRewrite

import com.beust.klaxon.Klaxon
import me.chicchi7393.discogramRewrite.objects.MessageTableObject
import me.chicchi7393.discogramRewrite.objects.SettingsObject
import me.chicchi7393.discogramRewrite.utilities.VariableStorage
import java.io.FileInputStream
import java.io.InputStream

class JsonReader {
    private fun getResourceFile(fileName: String): InputStream {
        return FileInputStream("./json/$fileName.json")
    }

    fun readJsonSettings(): SettingsObject? {
        return Klaxon()
            .parse<SettingsObject>(getResourceFile(if (VariableStorage.isProd) "settings" else "settings_dev"))
    }

    fun readJsonMessageTable(fileName: String): MessageTableObject? {
        return Klaxon()
            .parse<MessageTableObject>(getResourceFile(fileName))
    }
}