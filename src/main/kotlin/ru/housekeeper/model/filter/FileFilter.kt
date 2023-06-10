package ru.housekeeper.model.filter

import ru.housekeeper.enums.FileTypeEnum

data class FileFilter(
    val name: String? = null,
    val fileType: FileTypeEnum? = null,
)
