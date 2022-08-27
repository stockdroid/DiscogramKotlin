package me.chicchi7393.discogramRewrite.http.utilities

import java.io.File
import java.net.URL

object FindClass {
    fun findClasses(pckgname: String): List<String> {
        val listResult = mutableListOf<String>()
        var name = pckgname
        if (!name.startsWith("/")) {
            name = "/$name"
        }
        name = name.replace('.', '/')
        val directory = File((FindClass::class.java.getResource(name) as URL).file)
        if (directory.exists()) {
            directory.walk()
                .filter { f -> f.isFile() && f.name.contains('$') == false && f.name.endsWith(".class") }
                .forEach {
                    val fullyQualifiedClassName = pckgname +
                            it.canonicalPath.removePrefix(directory.canonicalPath)
                                .dropLast(6) // remove .class
                                .replace('/', '.')
                    listResult.add(fullyQualifiedClassName)
                }
        }
        return listResult
    }
}