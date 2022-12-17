package me.chicchi7393.discogramRewrite

import com.beust.klaxon.Klaxon
import me.chicchi7393.discogramRewrite.objects.MessageTableObject
import me.chicchi7393.discogramRewrite.objects.SettingsObject
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import kotlin.io.path.Path

class JsonReader {
    private fun getResourceFile(fileName: String): InputStream {
        return FileInputStream("./json/$fileName.json")
    }

    fun readJsonSettings(): SettingsObject? {
        return Klaxon().parse<SettingsObject>(getResourceFile(if (!Files.exists(Path("./json/dev"))) "settings" else "settings_dev"))
    }

    fun readJsonMessageTable(fileName: String): MessageTableObject? {
        return Klaxon().parse<MessageTableObject>(getResourceFile(fileName))
    }
}