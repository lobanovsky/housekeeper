package ru.housekeeper.experiment

import java.io.File

fun findDuplicateFilenames(directory: File) {
    if (!directory.exists() || !directory.isDirectory) {
        println("Указанный путь не является директорией: ${directory.absolutePath}")
        return
    }

    val filenameMap = mutableMapOf<String, MutableList<String>>()

    directory.walkTopDown().forEach { file ->
        if (file.isFile) {
            val filename = file.name
            val fullPath = file.absolutePath
            filenameMap.getOrPut(filename) { mutableListOf() }.add(fullPath)
        }
    }

    println("Дубликаты имён файлов:")
    filenameMap.filter { it.value.size > 1 }.forEach { (name, paths) ->
        println("Файл: $name")
        paths.forEach { path ->
            println("  $path")
        }
    }
}


fun main3()  {
//    val dir = "/Users/evgeny/Yandex.Disk.localized/Домовладелец/from sber/VTB-реестры" // Укажите путь к директории
    val dir = "/Users/evgeny/Yandex.Disk.localized/Домовладелец/from sber/Sber-реестры" // Укажите путь к директории
    findDuplicateFilenames(File(dir))
}