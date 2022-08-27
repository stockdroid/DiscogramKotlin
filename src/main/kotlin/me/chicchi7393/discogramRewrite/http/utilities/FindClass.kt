package me.chicchi7393.discogramRewrite.http.utilities

import com.google.common.reflect.ClassPath


object FindClass {
    fun findClasses(pckgname: String): List<String> {
        val listResult = mutableListOf<String>()
        val cp = ClassPath.from(Thread.currentThread().contextClassLoader)
        for (info in cp.getTopLevelClassesRecursive(pckgname)) {
            listResult.add(info.name)
        }
        return listResult
    }
}