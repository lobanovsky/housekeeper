package ru.housekeeper.model.dto

import java.time.LocalDateTime

data class FileVO(
    val id: Long? = null,
    val name: String,
    val size: Long,
    val checksum: String,
    val type: FileType,
    val createDate: LocalDateTime,
)

data class FileType(
    val name: String,
    val description: String
)