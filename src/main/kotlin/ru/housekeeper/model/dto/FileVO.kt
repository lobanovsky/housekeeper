package ru.housekeeper.model.dto

import ru.housekeeper.enums.FileTypeEnum

data class FileVO(
    val id: Long? = null,
    val name: String,
    val size: Long,
    val checksum: String,
    val fileType: FileTypeEnum? = null,
)