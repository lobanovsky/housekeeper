package ru.housekeeper.model.dto

data class FileVO(
    val id: Long? = null,
    val name: String,
    val size: Long,
    val checksum: String,
    val type: FileType,
)

data class FileType(
    val name: String,
    val description: String
)