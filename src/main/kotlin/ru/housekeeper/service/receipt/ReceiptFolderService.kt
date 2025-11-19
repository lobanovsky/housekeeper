package ru.housekeeper.service.receipt

import org.springframework.stereotype.Service
import java.io.File

@Service
class ReceiptFolderService {

    private val basePath = "/Users/evgeny/Projects/tsn/housekeeper/src/main/resources/receipt"

    fun getAvailableMonths(): List<String> {
        val base = File(basePath)
        if (!base.exists()) return emptyList()

        return base.listFiles()
            ?.filter { it.isDirectory && it.name.matches(Regex("""\d{4}-\d{2}""")) }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
    }

    fun folderExists(year: Int, month: Int): Boolean {
        val folder = "%04d-%02d".format(year, month)
        return File("$basePath/$folder").exists()
    }
}
